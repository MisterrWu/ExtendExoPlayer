package com.wh.extendexoplayer.renderers.types;

import static com.google.android.exoplayer2.util.GlUtil.checkGlError;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.video.VideoSize;
import com.wh.extendexoplayer.GlUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class RectRenderer implements Player.Listener {

    private final String TAG = "RectRenderer";
    private final AtomicBoolean frameAvailable;

    // Program related GL items. These are only valid if program != 0.
    private int program;
    private int positionHandle;
    private int uMatrixHandle;
    private int uSTMatrixHandle;
    private int texCoordsHandle;
    private int textureHandle;

    // Used by GL thread only
    private int textureId;
    private SurfaceTexture surfaceTexture;
    private int surfaceWidth, surfaceHeight;
    private int videoWidth, videoHeight;

    // Arbitrary vertical field of view.
    private static final int FIELD_OF_VIEW_DEGREES = 90;
    private static final float Z_NEAR = .1f;
    private static final float Z_FAR = 100;

    private List<MediaPlayer.OnVideoSizeChangedListener> videoSizeChangedListeners = new ArrayList<>();

    // 顶点着色器
    private static final String[] VERTEX_SHADER_CODE =
            new String[]{
                    "attribute vec4 aPosition;", //顶点位置
                    "attribute vec4 aTexCoord;", //S T 纹理坐标
                    "varying vec2 vTexCoord;",   //全局变量
                    "uniform mat4 uMatrix;",
                    "uniform mat4 uSTMatrix;",

                    // 标准转换。
                    "void main() {",
                    "  vTexCoord = (uSTMatrix * aTexCoord).xy;",
                    "  gl_Position = uMatrix*aPosition;",
                    "}"
            };
    // 片段着色器
    private static final String[] FRAGMENT_SHADER_CODE =
            new String[]{
                    // 必须设置GL_OES_EGL_image_external
                    "#extension GL_OES_EGL_image_external : require",
                    "precision mediump float;",

                    // 标准纹理渲染
                    "varying vec2 vTexCoord;",
                    "uniform samplerExternalOES sTexture;",
                    "void main() {",
                    "  gl_FragColor=texture2D(sTexture, vTexCoord);",
                    "}"
            };

    // 顶点坐标
    private final FloatBuffer vertexBuffer;
    private final float[] vertexData = {
            1f, -1f, 0f,
            -1f, -1f, 0f,
            1f, 1f, 0f,
            -1f, 1f, 0f
    };

    // 纹理坐标，必须与顶点坐标顺序相同
    private final FloatBuffer textureVertexBuffer;
    private final float[] textureVertexData = {
            1f, 0f,
            0f, 0f,
            1f, 1f,
            0f, 1f
    };

    // 矩阵
    private final MatrixTools matrixTools;
    private final float[] mSTMatrix = new float[16];

    private boolean isFitXY = false;

    public RectRenderer(boolean fitXY) {
        isFitXY = fitXY;
        frameAvailable = new AtomicBoolean();
        matrixTools = new MatrixTools();
        matrixTools.pushMatrix();
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        textureVertexBuffer = ByteBuffer.allocateDirect(textureVertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureVertexData);
        textureVertexBuffer.position(0);
    }

    public boolean frameAvailable() {
        return frameAvailable.get();
    }

    public SurfaceTexture init() {
        // Set the background frame color. This is only visible if the display mesh isn't a full sphere.
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1f);
        checkGlError();

        program = GlUtil.compileProgram(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE);
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        uMatrixHandle = GLES20.glGetUniformLocation(program, "uMatrix");
        uSTMatrixHandle = GLES20.glGetUniformLocation(program, "uSTMatrix");
        textureHandle = GLES20.glGetUniformLocation(program, "sTexture");
        texCoordsHandle = GLES20.glGetAttribLocation(program, "aTexCoord");
        checkGlError();

        textureId = GlUtil.createExternalTexture();
        surfaceTexture = new SurfaceTexture(textureId);
        surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                frameAvailable.set(true);
            }
        });
        return surfaceTexture;
    }

    public void surfaceChanged(int width, int height) {
        this.surfaceWidth = width;
        this.surfaceHeight = height;
        GLES20.glViewport(0, 0, width, height);
        float aspect = (float) width / height;
        float fovY = calculateFieldOfViewInYDirection(aspect);
        matrixTools.perspective(0, fovY, aspect, Z_NEAR, Z_FAR);
        updateProjection();
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

    public void drawFrame() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        checkGlError();

        if (frameAvailable.compareAndSet(true, false)) {
            if (surfaceTexture != null) {
                surfaceTexture.updateTexImage();
                surfaceTexture.getTransformMatrix(mSTMatrix);//让新的纹理和纹理坐标系能够正确的对应,mSTMatrix的定义是和projectionMatrix完全一样的。
                checkGlError();
            }
        }

        GLES20.glUseProgram(program);
        GLES20.glUniformMatrix4fv(uMatrixHandle, 1, false, matrixTools.getFinalMatrix(), 0);
        GLES20.glUniformMatrix4fv(uSTMatrixHandle, 1, false, mSTMatrix, 0);

        vertexBuffer.position(0);
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false,
                12, vertexBuffer);

        textureVertexBuffer.position(0);
        GLES20.glEnableVertexAttribArray(texCoordsHandle);
        GLES20.glVertexAttribPointer(texCoordsHandle, 2, GLES20.GL_FLOAT, false, 8, textureVertexBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        GLES20.glUniform1i(textureHandle, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    @Override
    public void onVideoSizeChanged(VideoSize videoSize) {
        int width = videoSize.width;
        int height = videoSize.height;
        Log.e(TAG, "onVideoSizeChanged: width " + width + ",height " + height);
        this.videoWidth = width;
        this.videoHeight = height;
        updateVideoSize(width, height);
        updateProjection();
    }

    private void updateVideoSize(int width, int height) {
        for (MediaPlayer.OnVideoSizeChangedListener listener : videoSizeChangedListeners) {
            if (listener != null) {
                listener.onVideoSizeChanged(null, width, height);
            }
        }
    }

    private synchronized void updateProjection() {
        if (isFitXY) {
            matrixTools.ortho(-1f, 1f, -1, 1, -1f, 1);
        } else {
            float screenRatio = (float) surfaceWidth / surfaceHeight;
            float videoRatio = (float) videoWidth / videoHeight;
            if (videoRatio > screenRatio) {
                matrixTools.ortho(-1f, 1f, -videoRatio / screenRatio, videoRatio / screenRatio, -1f, 10);
            } else {
                matrixTools.ortho(-screenRatio / videoRatio, screenRatio / videoRatio, -1f, 1f, -1f, 10);
            }
        }
    }

    public float getVideoWidth() {
        return videoWidth;
    }

    public float getVideoHeight() {
        return videoHeight;
    }

    public int getSurfaceWidth() {
        return surfaceWidth;
    }

    public int getSurfaceHeight() {
        return surfaceHeight;
    }

    public void setVideoMatrix(android.graphics.Matrix matrix) {
        float[] values = new float[9];
        matrix.getValues(values);
        //logMatrix("setVideoMatrix", values);
        float scale = (float) Math.sqrt((float) Math.pow(values[android.graphics.Matrix.MSCALE_X], 2) + (float) Math.pow(values[android.graphics.Matrix.MSKEW_Y], 2));
        matrixTools.peekMatrix();
        matrixTools.scale(scale, scale, 0, false);
        float transX = values[android.graphics.Matrix.MTRANS_X];
        float transY = values[android.graphics.Matrix.MTRANS_Y];
        float x = (transX + (surfaceWidth * scale - surfaceWidth) / 2) / surfaceWidth;
        float y = (transY + (surfaceHeight * scale - surfaceHeight)/2) / surfaceHeight;
        Log.e(TAG, "setVideoMatrix: x " + x +",y "+y);
        // x 为负数向左，y 为负数向下
        matrixTools.translate(x / 2,-y / 2,0,true);
    }

    private void logMatrix(String name, float[] values) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < values.length; i++) {
            builder.append(values[i]).append(i < (values.length - 1) ? "," : "");
        }
        builder.append("]");
        Log.e(TAG, name + ": " + builder.toString());
    }

    public void addOnVideoSizeChangedListener(MediaPlayer.OnVideoSizeChangedListener listener) {
        videoSizeChangedListeners.add(listener);
    }

    public void setScale(float scale, boolean shoLog) {
        matrixTools.scale(scale, scale, 0, shoLog);
    }

    public void setTranslate(float transX, float transY, boolean shoLog){
        matrixTools.translate(transX,transY,0,shoLog);
    }
}
