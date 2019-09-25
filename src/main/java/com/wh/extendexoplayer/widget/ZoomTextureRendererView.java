package com.wh.extendexoplayer.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;

public class ZoomTextureRendererView extends TextureRendererView implements MediaPlayer.OnVideoSizeChangedListener {

    private ZoomTextureRendererViewAttacher attacher;
    private VideoInfo mVideoInfo;

    public ZoomTextureRendererView(Context context) {
        super(context);
        init();
    }

    public ZoomTextureRendererView(Context context, AttributeSet attr) {
        super(context, attr);
        init();
    }

    private void init() {
        attacher = new ZoomTextureRendererViewAttacher(this);
    }

    @Override
    public void setRenderer(RendererView.Renderer renderer) {
        super.setRenderer(renderer);
        if (renderer != null) {
            renderer.addOnVideoSizeChangedListener(this);
        }
    }

    @Override
    public void setTransform(Matrix transform) {
        super.setTransform(transform);
        logMatrix("setTransform", transform);
        invalidate();
    }

    private void logMatrix(String name, Matrix matrix) {
        if (matrix == null) {
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
        Log.e("wuhan", name + ": " + builder.toString());
    }

    public ZoomTextureRendererViewAttacher getAttacher() {
        return attacher;
    }

    @Override
    public void setOnLongClickListener(View.OnLongClickListener l) {
        attacher.setOnLongClickListener(l);
    }

    @Override
    public void setOnClickListener(View.OnClickListener l) {
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

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        if (mVideoInfo == null) {
            mVideoInfo = new VideoInfo();
        }
        mVideoInfo.width = width;
        mVideoInfo.height = height;
        attacher.update();
    }

    public VideoInfo getVideoInfo() {
        return mVideoInfo;
    }

    public class VideoInfo {
        private int width;
        private int height;

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}
