package com.wh.extendexoplayer.renderers.types;

import android.opengl.Matrix;

import java.util.Arrays;
import java.util.Stack;

final class MatrixTools {

    private float[] mMatrixProjection = new float[16];    //投影矩阵
    private float[] mMatrixTransform =     //原始矩阵
            {1, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0, 1, 0,
                    0, 0, 0, 1};

    private Stack<float[]> mStack;      //变换矩阵堆栈

    MatrixTools() {
        mStack = new Stack<>();
    }

    //保护现场
    void pushMatrix() {
        mStack.push(Arrays.copyOf(mMatrixTransform, 16));
    }

    //恢复现场
    public void popMatrix() {
        mMatrixTransform = mStack.pop();
    }

    void peekMatrix() {
        float[] values = mStack.peek();
        mMatrixTransform = Arrays.copyOf(values, values.length);
    }

    public void clearStack() {
        mStack.clear();
    }

    //平移变换
    public void translate(float x, float y, float z, boolean showLog) {
        Matrix.translateM(mMatrixTransform, 0, x, y, z);
    }

    //旋转变换
    public void rotate(float angle, float x, float y, float z) {
        Matrix.rotateM(mMatrixTransform, 0, angle, x, y, z);
    }

    //缩放变换
    void scale(float x, float y, float z, boolean shoLog) {
        Matrix.scaleM(mMatrixTransform, 0, x, y, z);
    }

    public void frustum(float left, float right, float bottom, float top, float near, float far) {
        Matrix.frustumM(mMatrixProjection, 0, left, right, bottom, top, near, far);
    }

    void ortho(float left, float right, float bottom, float top, float near, float far) {
        Matrix.orthoM(mMatrixProjection, 0, left, right, bottom, top, near, far);
    }

    void perspective(int offset, float fovy, float aspect, float zNear, float zFar) {
        Matrix.perspectiveM(mMatrixProjection, offset, fovy, aspect, zNear, zFar);
    }

    float[] getFinalMatrix() {
        float[] ans = new float[16];
        Matrix.multiplyMM(ans, 0, mMatrixProjection, 0, mMatrixTransform, 0);
        return ans;
    }
}
