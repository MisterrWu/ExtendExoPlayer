package com.wh.extendexoplayer.widget;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.MotionEvent;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public interface RendererView {

    int EDGE_NONE = -1;
    int EDGE_LEFT = 0;
    int EDGE_RIGHT = 1;
    int EDGE_BOTH = 2;

    Context getContext();

    void setRenderer(Renderer renderer);

    void queueEvent(Runnable r);

    void setSingleTapListener(OnSingleTapListener listener);

    void onResume();

    void onPause();

    interface Renderer {
        /**
         * Called when the surface is created or recreated.
         * <p>
         * Called when the rendering thread
         * starts and whenever the EGL context is lost. The EGL context will typically
         * be lost when the Android device awakes after going to sleep.
         * <p>
         * Since this method is called at the beginning of rendering, as well as
         * every time the EGL context is lost, this method is a convenient place to put
         * code to create resources that need to be created when the rendering
         * starts, and that need to be recreated when the EGL context is lost.
         * Textures are an example of a resource that you might want to create
         * here.
         * <p>
         * Note that when the EGL context is lost, all OpenGL resources associated
         * with that context will be automatically deleted. You do not need to call
         * the corresponding "glDelete" methods such as glDeleteTextures to
         * manually delete these lost resources.
         * <p>
         *
         * @param gl     the GL interface. Use <code>instanceof</code> to
         *               test if the interface supports GL11 or higher interfaces.
         * @param config the EGLConfig of the created surface. Can be used
         *               to create matching pbuffers.
         */
        void onSurfaceCreated(GL10 gl, EGLConfig config);

        /**
         * Called when the surface changed size.
         * <p>
         * Called after the surface is created and whenever
         * the OpenGL ES surface size changes.
         * <p>
         * Typically you will set your viewport here. If your camera
         * is fixed then you could also set your projection matrix here:
         * <pre class="prettyprint">
         * void onSurfaceChanged(GL10 gl, int width, int height) {
         *     gl.glViewport(0, 0, width, height);
         *     // for a fixed camera, set the projection too
         *     float ratio = (float) width / height;
         *     gl.glMatrixMode(GL10.GL_PROJECTION);
         *     gl.glLoadIdentity();
         *     gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
         * }
         * </pre>
         *
         * @param gl     the GL interface. Use <code>instanceof</code> to
         *               test if the interface supports GL11 or higher interfaces.
         * @param width
         * @param height
         */
        void onSurfaceChanged(GL10 gl, int width, int height);

        /**
         * Called to draw the current frame.
         * <p>
         * This method is responsible for drawing the current frame.
         * <p>
         * The implementation of this method typically looks like this:
         * <pre class="prettyprint">
         * void onDrawFrame(GL10 gl) {
         *     gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
         *     //... other gl calls to render the scene ...
         * }
         * </pre>
         *
         * @param gl the GL interface. Use <code>instanceof</code> to
         *           test if the interface supports GL11 or higher interfaces.
         */
        void onDrawFrame(GL10 gl);

        void onDetachedFromWindow();

        void setVideoComponent(ExoPlayer newVideoComponent);

        void addOnVideoSizeChangedListener(MediaPlayer.OnVideoSizeChangedListener listener);
    }

    interface OnSingleTapListener {

        boolean onSingleTapUp(MotionEvent e);
    }

    interface OnMotionDownUpListener {

        void onMotionDown();

        boolean onMotionUp();
    }

    interface OnOrientationListener {

        void onOrientationChange(float[] deviceOrientationMatrix, float deviceRoll);
    }

    interface OnScaleDragListener {

        /**
         * 返回是否到边界
         *
         * @param dx
         * @param dy
         * @return edge
         */
        int onDrag(float dx, float dy);

        void onFling(float startX, float startY, float velocityX, float velocityY);

        void onScale(float scaleFactor, float focusX, float focusY);
    }
}
