package com.wh.extendexoplayer.renderers;

import android.graphics.PointF;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Player;
import com.wh.extendexoplayer.renderers.types.SceneRenderer;
import com.wh.extendexoplayer.widget.RendererView;

import javax.microedition.khronos.opengles.GL10;


public class SphericalRenderer extends BaseRenderer implements RendererView.Renderer
        , GestureDetector.OnGestureListener, RendererView.OnOrientationListener {

    private final String TAG = "SphericalRenderer";
    // Arbitrary vertical field of view.
    private static final int FIELD_OF_VIEW_DEGREES = 90;
    private static final float Z_NEAR = .1f;
    private static final float Z_FAR = 100;

    // TODO Calculate this depending on surface size and field of view.
    private static final float PX_PER_DEGREES = 25;
    /* package */ static final float UPRIGHT_ROLL = (float) Math.PI;

    private final SceneRenderer scene;
    private final float[] projectionMatrix = new float[16];

    // There is no model matrix for this scene so viewProjectionMatrix is used for the mvpMatrix.
    private final float[] viewProjectionMatrix = new float[16];

    // Device orientation is derived from sensor data. This is accessed in the sensor's thread and
    // the GL thread.
    private final float[] deviceOrientationMatrix = new float[16];

    // Optional pitch and yaw rotations are applied to the sensor orientation. These are accessed on
    // the UI, sensor and GL Threads.
    private final float[] touchPitchMatrix = new float[16];
    private final float[] touchYawMatrix = new float[16];
    private float touchPitch;
    private float deviceRoll;

    // viewMatrix = touchPitch * deviceOrientation * touchYaw.
    private final float[] viewMatrix = new float[16];
    private final float[] tempMatrix = new float[16];

    private final TouchTracker touchTracker;

    public SphericalRenderer(){
        scene = new SceneRenderer();
        touchTracker = new TouchTracker(PX_PER_DEGREES);
        Matrix.setIdentityM(deviceOrientationMatrix, 0);
        Matrix.setIdentityM(touchPitchMatrix, 0);
        Matrix.setIdentityM(touchYawMatrix, 0);
        deviceRoll = UPRIGHT_ROLL;
    }

    /**
     * Sets the default stereo mode. If the played video doesn't contain a stereo mode the default one
     * is used.
     *
     * @param stereoMode A {@link C.StereoMode} value.
     */
    public void setDefaultStereoMode(@C.StereoMode int stereoMode) {
        scene.setDefaultStereoMode(stereoMode);
    }

    @Override
    protected SurfaceTexture onSurfaceTextureCreate() {
        return scene.init();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float aspect = (float) width / height;
        float fovY = calculateFieldOfViewInYDirection(aspect);
        Matrix.perspectiveM(projectionMatrix, 0, fovY, aspect, Z_NEAR, Z_FAR);
    }

    private float calculateFieldOfViewInYDirection(float aspect) {
        boolean landscapeMode = aspect > 1;
        if (landscapeMode) {
            double halfFovX = FIELD_OF_VIEW_DEGREES / 2;
            double tanY = Math.tan(Math.toRadians(halfFovX)) / aspect;
            double halfFovY = Math.toDegrees(Math.atan(tanY));
            return (float) (halfFovY * 2);
        } else {
            return FIELD_OF_VIEW_DEGREES;
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Combine touch & sensor data.
        // Orientation = pitch * sensor * yaw since that is closest to what most users expect the
        // behavior to be.
        synchronized (this) {
            Matrix.multiplyMM(tempMatrix, 0, deviceOrientationMatrix, 0, touchYawMatrix, 0);
            Matrix.multiplyMM(viewMatrix, 0, touchPitchMatrix, 0, tempMatrix, 0);
        }

        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        scene.drawFrame(viewProjectionMatrix, /* rightEye= */ false);
    }

    @Override
    protected void setupOldVideoComponent(Player.VideoComponent oldVideoComponent) {
        if (oldVideoComponent != null) {
            oldVideoComponent.clearVideoFrameMetadataListener(scene);
            oldVideoComponent.clearCameraMotionListener(scene);
        }
    }

    @Override
    protected void setupNewVideoComponent(Player.VideoComponent newVideoComponent) {
        if (newVideoComponent != null) {
            newVideoComponent.setVideoFrameMetadataListener(scene);
            newVideoComponent.setCameraMotionListener(scene);
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return touchTracker.onDown(e);
    }

    @Override
    public void onShowPress(MotionEvent e) {}

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return touchTracker.onScroll(e1,e2,distanceX,distanceY);
    }

    @Override
    public void onLongPress(MotionEvent e) {}

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public void onOrientationChange(float[] matrix, float deviceRoll) {
        touchTracker.onOrientationChange(matrix,deviceRoll);
        System.arraycopy(matrix, 0, deviceOrientationMatrix, 0, deviceOrientationMatrix.length);
        this.deviceRoll = -deviceRoll;
        updatePitchMatrix();
    }

    private void onScrollChange(PointF scrollOffsetDegrees) {
        touchPitch = scrollOffsetDegrees.y;
        updatePitchMatrix();
        Matrix.setRotateM(touchYawMatrix, 0, -scrollOffsetDegrees.x, 0, 1, 0);
    }

    /**
     * Updates the pitch matrix after a physical rotation or touch input. The pitch matrix rotation
     * is applied on an axis that is dependent on device rotation so this must be called after
     * either touch or sensor update.
     */
    private void updatePitchMatrix() {
        // The camera's pitch needs to be rotated along an axis that is parallel to the real world's
        // horizon. This is the <1, 0, 0> axis after compensating for the device's roll.
        Matrix.setRotateM(
                touchPitchMatrix,
                0,
                -touchPitch,
                (float) Math.cos(deviceRoll),
                (float) Math.sin(deviceRoll),
                0);
    }

    private class TouchTracker {

        // Touch input won't change the pitch beyond +/- 45 degrees. This reduces awkward situations
        // where the touch-based pitch and gyro-based pitch interact badly near the poles.
        /* package */ static final float MAX_PITCH_DEGREES = 45;

        // With every touch event, update the accumulated degrees offset by the new pixel amount.
        private final PointF previousTouchPointPx = new PointF();
        private final PointF accumulatedTouchOffsetDegrees = new PointF();

        private final float pxPerDegrees;
        // The conversion from touch to yaw & pitch requires compensating for device roll. This is set
        // on the sensor thread and read on the UI thread.
        private volatile float roll;

        TouchTracker(float pxPerDegrees) {
            this.pxPerDegrees = pxPerDegrees;
            roll = UPRIGHT_ROLL;
        }

        boolean onDown(MotionEvent e) {
            // Initialize drag gesture.
            previousTouchPointPx.set(e.getX(), e.getY());
            return true;
        }

        boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Calculate the touch delta in screen space.
            float touchX = (e2.getX() - previousTouchPointPx.x) / pxPerDegrees;
            float touchY = (e2.getY() - previousTouchPointPx.y) / pxPerDegrees;
            previousTouchPointPx.set(e2.getX(), e2.getY());

            float r = roll; // Copy volatile state.
            float cr = (float) Math.cos(r);
            float sr = (float) Math.sin(r);
            // To convert from screen space to the 3D space, we need to adjust the drag vector based
            // on the roll of the phone. This is standard rotationMatrix(roll) * vector math but has
            // an inverted y-axis due to the screen-space coordinates vs GL coordinates.
            // Handle yaw.
            accumulatedTouchOffsetDegrees.x -= cr * touchX - sr * touchY;
            // Handle pitch and limit it to 45 degrees.
            accumulatedTouchOffsetDegrees.y += sr * touchX + cr * touchY;
            accumulatedTouchOffsetDegrees.y =
                    Math.max(-MAX_PITCH_DEGREES, Math.min(MAX_PITCH_DEGREES, accumulatedTouchOffsetDegrees.y));

            onScrollChange(accumulatedTouchOffsetDegrees);
            return true;
        }

        void onOrientationChange(float[] matrix, float roll) {
            // We compensate for roll by rotating in the opposite direction.
            this.roll = -roll;
        }
    }
}
