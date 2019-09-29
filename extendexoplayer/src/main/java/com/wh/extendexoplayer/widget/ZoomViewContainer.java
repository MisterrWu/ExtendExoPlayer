package com.wh.extendexoplayer.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class ZoomViewContainer extends FrameLayout {

    private final String TAG = "ViewContainer";
    private ZoomViewContainerAttacher attacher;
    private Matrix mMatrix = new Matrix();
    private Matrix mDrawMatrix;

    public ZoomViewContainer(Context context) {
        super(context);
        init();
    }

    public ZoomViewContainer(Context context, AttributeSet attr) {
        super(context, attr);
        init();
    }

    private void init() {
        setClipChildren(false);
        attacher = new ZoomViewContainerAttacher(this);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean isDraw;
        final int saveCount = canvas.getSaveCount();
        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());
        if (mDrawMatrix != null) {
            canvas.concat(mDrawMatrix);
        }
        isDraw = super.drawChild(canvas, child, drawingTime);
        Log.e(TAG, "drawChild: isDraw " + isDraw );
        canvas.restoreToCount(saveCount);
        return isDraw;
    }

    public void setChildMatrix(Matrix matrix) {
        // collapse null and identity to just null
        if (matrix != null && matrix.isIdentity()) {
            matrix = null;
        }

        // don't invalidate unless we're actually changing our matrix
        if (matrix == null && !mMatrix.isIdentity() ||
                matrix != null && !mMatrix.equals(matrix)) {
            //ogMatrix("setChildMatrix",matrix);
            mMatrix.set(matrix);
            configureBounds();
            invalidate();
        }
    }

    private void configureBounds() {
        View child = getChildAt(0);
        if (child == null) {
            return;
        }

        final int dwidth = child.getWidth();
        final int dheight = child.getHeight();

        final int vwidth = getWidth() - getPaddingLeft() - getPaddingRight();
        final int vheight = getHeight() - getPaddingTop() - getPaddingBottom();

        Log.e(TAG, "configureBounds: dwidth "+dwidth+",dheight "+dheight+",vwidth "+vwidth+",vheight "+vheight);

        mDrawMatrix = mMatrix;
        /*mDrawMatrix.setTranslate(Math.round((vwidth - dwidth) * 0.5f),
                Math.round((vheight - dheight) * 0.5f));*/
        //logMatrix("configureBounds",mDrawMatrix);
    }

    private void logMatrix(String name,Matrix matrix){
        if(matrix == null){
            return;
        }
        float[] values = new float[9];
        matrix.getValues(values);
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < values.length; i++) {
            builder.append(values[i]).append(i < (values.length - 1) ? "," : "");
        }
        builder.append("]");
        Log.e(TAG, name + ": "+builder.toString());
    }

    public View getChildView(){
        return getChildAt(0);
    }

    public ZoomViewContainerAttacher getAttacher() {
        return attacher;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        attacher.setOnLongClickListener(l);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        attacher.setOnClickListener(l);
    }

    public void setRotationTo(float rotationDegree) {
        attacher.setRotationTo(rotationDegree);
    }

    public void setRotationBy(float rotationDegree) {
        attacher.setRotationBy(rotationDegree);
    }

    @Deprecated
    public boolean isZoomEnabled() {
        return attacher.isZoomEnabled();
    }

    public boolean isZoomable() {
        return attacher.isZoomable();
    }

    public void setZoomable(boolean zoomable) {
        attacher.setZoomable(zoomable);
    }

    public RectF getDisplayRect() {
        return attacher.getDisplayRect();
    }

    public void getDisplayMatrix(Matrix matrix) {
        attacher.getDisplayMatrix(matrix);
    }

    public boolean setDisplayMatrix(Matrix finalRectangle) {
        return attacher.setDisplayMatrix(finalRectangle);
    }

    public void getSuppMatrix(Matrix matrix) {
        attacher.getSuppMatrix(matrix);
    }

    public boolean setSuppMatrix(Matrix matrix) {
        return attacher.setDisplayMatrix(matrix);
    }

    public float getMinimumScale() {
        return attacher.getMinimumScale();
    }

    public float getMediumScale() {
        return attacher.getMediumScale();
    }

    public float getMaximumScale() {
        return attacher.getMaximumScale();
    }

    public float getScale() {
        return attacher.getScale();
    }

    public void setAllowParentInterceptOnEdge(boolean allow) {
        attacher.setAllowParentInterceptOnEdge(allow);
    }

    public void setMinimumScale(float minimumScale) {
        attacher.setMinimumScale(minimumScale);
    }

    public void setMediumScale(float mediumScale) {
        attacher.setMediumScale(mediumScale);
    }

    public void setMaximumScale(float maximumScale) {
        attacher.setMaximumScale(maximumScale);
    }

    public void setScaleLevels(float minimumScale, float mediumScale, float maximumScale) {
        attacher.setScaleLevels(minimumScale, mediumScale, maximumScale);
    }

    public void setScale(float scale) {
        attacher.setScale(scale);
    }

    public void setScale(float scale, boolean animate) {
        attacher.setScale(scale, animate);
    }

    public void setScale(float scale, float focalX, float focalY, boolean animate) {
        attacher.setScale(scale, focalX, focalY, animate);
    }

    public void setZoomTransitionDuration(int milliseconds) {
        attacher.setZoomTransitionDuration(milliseconds);
    }

    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener onDoubleTapListener) {
        attacher.setOnDoubleTapListener(onDoubleTapListener);
    }

}
