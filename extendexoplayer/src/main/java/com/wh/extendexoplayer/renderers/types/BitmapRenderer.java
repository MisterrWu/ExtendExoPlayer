package com.wh.extendexoplayer.renderers.types;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.google.android.exoplayer2.util.GlUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static com.google.android.exoplayer2.util.GlUtil.checkGlError;

public class BitmapRenderer {

    // 顶点着色器
    private static final String[] VERTEX_SHADER_CODE =
            new String[]{
                    "attribute vec4 vPosition;",
                    "attribute vec2 vCoordinate;",
                    "uniform mat4 vMatrix;",
                    "varying vec2 aCoordinate;",

                    "void main(){",
                    "  gl_Position=vMatrix*vPosition;",
                    "  aCoordinate=vCoordinate;",
                    "}"
            };

    // 片段着色器
    private static final String[] FRAGMENT_SHADER_CODE =
            new String[]{
                    "precision mediump float;",
                    // 标准纹理渲染
                    "uniform sampler2D vTexture;",
                    "varying vec2 aCoordinate;",
                    "void main() {",
                    "  gl_FragColor=texture2D(vTexture,aCoordinate);",
                    "}"
            };

    // 顶点坐标
    private final FloatBuffer vertexBuffer;
    private final float[] vertexData = {
            -1.0f,1.0f,    //左上角
            -1.0f,-1.0f,   //左下角
            1.0f,1.0f,     //右上角
            1.0f,-1.0f     //右下角
    };

    // 纹理坐标，必须与顶点坐标顺序相同
    private final FloatBuffer textureVertexBuffer;
    private final float[] textureVertexData = {
            0.0f,0.0f,
            0.0f,1.0f,
            1.0f,0.0f,
            1.0f,1.0f,
    };
    private static final int FIELD_OF_VIEW_DEGREES = 90;
    private static final float Z_NEAR = .1f;
    private static final float Z_FAR = 100;

    private final MatrixTools matrixTools;
    private boolean isFitXY;
    private int program;
    private int vPositionHandle;
    private int vMatrixHandle;
    private int vTextureHandle;
    private int vCoordinateHandle;
    private int textureId;
    private int surfaceWidth;
    private int surfaceHeight;
    private int bitmapWidth;
    private int bitmapHeight;
    private Bitmap mBitmap;

    public BitmapRenderer(boolean fiXY){
        isFitXY = fiXY;
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

    public void init() {
        // Set the background frame color. This is only visible if the display mesh isn't a full sphere.
        GLES20.glClearColor(1, 1, 1, 1f);
        checkGlError();

        program = GlUtil.compileProgram(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE);
        vPositionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        vMatrixHandle = GLES20.glGetUniformLocation(program, "vMatrix");
        vTextureHandle = GLES20.glGetUniformLocation(program, "vTexture");
        vCoordinateHandle = GLES20.glGetAttribLocation(program, "vCoordinate");
        checkGlError();
    }

    private synchronized int createTexture(Bitmap bitmap){
        int[] texture=new int[1];
        if(bitmap!=null&&!bitmap.isRecycled()){
            //生成纹理
            GLES20.glGenTextures(1,IntBuffer.wrap(texture));
            //生成纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texture[0]);
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
            //根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            checkGlError();
            return texture[0];
        }
        return 0;
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

        if(textureId <= 0 && mBitmap!=null && !mBitmap.isRecycled()){
            textureId = createTexture(mBitmap);
        }
        GLES20.glUseProgram(program);
        GLES20.glUniformMatrix4fv(vMatrixHandle, 1, false, matrixTools.getFinalMatrix(), 0);

        vertexBuffer.position(0);
        GLES20.glEnableVertexAttribArray(vPositionHandle);
        GLES20.glVertexAttribPointer(vPositionHandle, 2, GLES20.GL_FLOAT, false,
                0, vertexBuffer);

        textureVertexBuffer.position(0);
        GLES20.glEnableVertexAttribArray(vCoordinateHandle);
        GLES20.glVertexAttribPointer(vCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, textureVertexBuffer);

        GLES20.glUniform1i(vTextureHandle, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(vPositionHandle);
        GLES20.glDisableVertexAttribArray(vCoordinateHandle);
    }

    public void setBitmap(Bitmap bitmap){
        this.mBitmap = bitmap;
        bitmapWidth = bitmap.getWidth();
        bitmapHeight = bitmap.getHeight();
        updateProjection();
    }

    private synchronized void updateProjection() {
        if (isFitXY) {
            matrixTools.ortho(-1f, 1f, -1, 1, -1f, 1);
        } else {
            float screenRatio = (float) surfaceWidth / surfaceHeight;
            float videoRatio = (float) bitmapWidth / bitmapHeight;
            if (videoRatio > screenRatio) {
                matrixTools.ortho(-1f, 1f, -videoRatio / screenRatio, videoRatio / screenRatio, -1f, 10);
            } else {
                matrixTools.ortho(-screenRatio / videoRatio, screenRatio / videoRatio, -1f, 1f, -1f, 10);
            }
        }
    }
}
