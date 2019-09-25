package com.wh.extendexoplayer.renderers;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.OverScroller;

import com.wh.extendexoplayer.widget.RendererView;


public class ZoomRectRenderer extends NormalRectRenderer implements RendererView.OnScaleDragListener
        ,GestureDetector.OnDoubleTapListener, RendererView.OnMotionDownUpListener {

    private final String TAG = "ZoomRectR";
    private Context mContext;

    private static float DEFAULT_MAX_SCALE = 3.0f;
    private static float DEFAULT_MID_SCALE = 1.75f;
    private static float DEFAULT_MIN_SCALE = 1.0f;
    private static int DEFAULT_ZOOM_DURATION = 200;

    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private int mZoomDuration = DEFAULT_ZOOM_DURATION;
    private float mMinScale = DEFAULT_MIN_SCALE;
    private float mMidScale = DEFAULT_MID_SCALE;
    private float mMaxScale = DEFAULT_MAX_SCALE;

    // These are set so we don't keep allocating them on the heap
    private final Matrix mBaseMatrix = new Matrix();
    private final Matrix mDrawMatrix = new Matrix();
    private final Matrix mSuppMatrix = new Matrix();
    private final RectF mDisplayRect = new RectF();
    private final float[] mMatrixValues = new float[9];

    private FlingRunnable mCurrentFlingRunnable;

    private int mScrollEdge;

    public ZoomRectRenderer(Context context){
        super(false);
        this.mContext = context;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent ev) {
        try {
            float scale = getScale();
            float x = ev.getX();
            float y = ev.getY();

            if (scale < getMediumScale()) {
                setScale(getMediumScale(), x, y, true);
            } else if (scale >= getMediumScale() && scale < getMaximumScale()) {
                setScale(getMaximumScale(), x, y, true);
            } else {
                setScale(getMinimumScale(), x, y, true);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // Can sometimes happen when getX() and getY() is called
        }

        return true;
    }

    @Override
    public void onMotionDown() {
        cancelFling();
    }

    @Override
    public boolean onMotionUp() {
        if (getScale() < mMinScale) {
            RectF rect = getDisplayRect();
            if (rect != null) {
                mainHandler.post(new AnimatedZoomRunnable(getScale(), mMinScale,
                        rect.centerX(), rect.centerY()));
                return true;
            }
        } else if (getScale() > mMaxScale) {
            RectF rect = getDisplayRect();
            if (rect != null) {
                mainHandler.post(new AnimatedZoomRunnable(getScale(), mMaxScale,
                        rect.centerX(), rect.centerY()));
                return true;
            }
        }
        return false;
    }

    @Override
    public int onDrag(float dx, float dy) {
        //Log.e(TAG, "onDrag: dx "+dx+",dy "+dy);
        mSuppMatrix.postTranslate(dx, dy);
        checkAndDisplayMatrix();
        return mScrollEdge;
    }

    @Override
    public void onFling(float startX, float startY, float velocityX, float velocityY) {
        mCurrentFlingRunnable = new FlingRunnable(mContext);
        mCurrentFlingRunnable.fling(rectRenderer.getSurfaceWidth(),
                rectRenderer.getSurfaceHeight(), (int) velocityX, (int) velocityY);
        mainHandler.post(mCurrentFlingRunnable);
    }

    @Override
    public void onScale(float scaleFactor, float focusX, float focusY) {
        if ((getScale() < mMaxScale || scaleFactor < 1f) && (getScale() > mMinScale || scaleFactor > 1f)) {
            Log.e(TAG, "onScale: scaleFactor " + scaleFactor );
            mSuppMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
            checkAndDisplayMatrix();
        }
    }

    public float getMinimumScale() {
        return mMinScale;
    }

    public float getMediumScale() {
        return mMidScale;
    }

    public float getMaximumScale() {
        return mMaxScale;
    }

    public void setScale(float scale, float focalX, float focalY,
                         boolean animate) {
        // Check to see if the scale is within bounds
        if (scale < mMinScale || scale > mMaxScale) {
            throw new IllegalArgumentException("Scale must be within the range of minScale and maxScale");
        }

        if (animate) {
            mainHandler.post(new AnimatedZoomRunnable(getScale(), scale,
                    focalX, focalY));
        } else {
            mSuppMatrix.setScale(scale, scale, focalX, focalY);
            checkAndDisplayMatrix();
        }
    }

    private void cancelFling() {
        if (mCurrentFlingRunnable != null) {
            mCurrentFlingRunnable.cancelFling();
            mCurrentFlingRunnable = null;
        }
    }

    public RectF getDisplayRect() {
        checkMatrixBounds();
        return getDisplayRect(getDrawMatrix());
    }

    private RectF getDisplayRect(Matrix matrix) {
        if (rectRenderer != null) {
            mDisplayRect.set(0, 0, rectRenderer.getSurfaceWidth(),
                    rectRenderer.getSurfaceHeight());
            matrix.mapRect(mDisplayRect);
            return mDisplayRect;
        }
        return null;
    }

    private Matrix getDrawMatrix() {
        mDrawMatrix.set(mBaseMatrix);
        mDrawMatrix.postConcat(mSuppMatrix);
        return mDrawMatrix;
    }

    private boolean checkMatrixBounds() {

        final RectF rect = getDisplayRect(getDrawMatrix());
        if (rect == null) {
            return false;
        }

        final float height = rect.height(), width = rect.width();
        float deltaX = 0, deltaY = 0;

        final int viewHeight = rectRenderer.getSurfaceHeight();
        if (height <= viewHeight) {
            deltaY = (viewHeight - height) / 2 - rect.top;
        } else if (rect.top > 0) {
            deltaY = -rect.top;
        } else if (rect.bottom < viewHeight) {
            deltaY = viewHeight - rect.bottom;
        }

        final int viewWidth = rectRenderer.getSurfaceWidth();
        if (width <= viewWidth) {
            deltaX = (viewWidth - width) / 2 - rect.left;
            mScrollEdge = RendererView.EDGE_BOTH;
        } else if (rect.left > 0) {
            mScrollEdge = RendererView.EDGE_LEFT;
            deltaX = -rect.left;
        } else if (rect.right < viewWidth) {
            deltaX = viewWidth - rect.right;
            mScrollEdge = RendererView.EDGE_RIGHT;
        } else {
            mScrollEdge = RendererView.EDGE_NONE;
        }

        // Finally actually translate the matrix
        mSuppMatrix.postTranslate(deltaX, deltaY);
        float[] values = new float[9];
        mSuppMatrix.getValues(values);
        logMatrix("checkMatrixBounds",values);
        return true;
    }

    private void logMatrix(String name,float[] values){
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < values.length; i++) {
            builder.append(values[i]).append(i < (values.length - 1) ? "," : "");
        }
        builder.append("]");
        Log.e(TAG, name + ": "+builder.toString());
    }

    private void checkAndDisplayMatrix() {
        if (checkMatrixBounds()) {
            setVideoMatrix(getDrawMatrix());
        }
    }

    public float getScale() {
        return (float) Math.sqrt((float) Math.pow(getValue(mSuppMatrix, Matrix.MSCALE_X), 2) + (float) Math.pow(getValue(mSuppMatrix, Matrix.MSKEW_Y), 2));
    }

    private float getValue(Matrix matrix, int whichValue) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
    }

    private void setVideoMatrix(Matrix matrix) {
        rectRenderer.setVideoMatrix(matrix);
    }

    private class AnimatedZoomRunnable implements Runnable {

        private final float mFocalX, mFocalY;
        private final long mStartTime;
        private final float mZoomStart, mZoomEnd;

        public AnimatedZoomRunnable(final float currentZoom, final float targetZoom,
                                    final float focalX, final float focalY) {
            mFocalX = focalX;
            mFocalY = focalY;
            mStartTime = System.currentTimeMillis();
            mZoomStart = currentZoom;
            mZoomEnd = targetZoom;
        }

        @Override
        public void run() {

            float t = interpolate();
            float scale = mZoomStart + t * (mZoomEnd - mZoomStart);
            float deltaScale = scale / getScale();

            onScale(deltaScale, mFocalX, mFocalY);

            // We haven't hit our target scale yet, so post ourselves again
            if (t < 1f) {
                mainHandler.post(this);
            }
        }

        private float interpolate() {
            float t = 1f * (System.currentTimeMillis() - mStartTime) / mZoomDuration;
            t = Math.min(1f, t);
            t = mInterpolator.getInterpolation(t);
            return t;
        }
    }

    private class FlingRunnable implements Runnable {

        private final OverScroller mScroller;
        private int mCurrentX, mCurrentY;

        public FlingRunnable(Context context) {
            mScroller = new OverScroller(context);
        }

        public void cancelFling() {
            mScroller.forceFinished(true);
        }

        public void fling(int viewWidth, int viewHeight, int velocityX,
                          int velocityY) {
            final RectF rect = getDisplayRect();
            if (rect == null) {
                return;
            }

            final int startX = Math.round(-rect.left);
            final int minX, maxX, minY, maxY;

            if (viewWidth < rect.width()) {
                minX = 0;
                maxX = Math.round(rect.width() - viewWidth);
            } else {
                minX = maxX = startX;
            }

            final int startY = Math.round(-rect.top);
            if (viewHeight < rect.height()) {
                minY = 0;
                maxY = Math.round(rect.height() - viewHeight);
            } else {
                minY = maxY = startY;
            }

            mCurrentX = startX;
            mCurrentY = startY;

            // If we actually can move, fling the scroller
            if (startX != maxX || startY != maxY) {
                mScroller.fling(startX, startY, velocityX, velocityY, minX,
                        maxX, minY, maxY, 0, 0);
            }
        }

        @Override
        public void run() {
            if (mScroller.isFinished()) {
                return; // remaining post that should not be handled
            }

            if (mScroller.computeScrollOffset()) {

                final int newX = mScroller.getCurrX();
                final int newY = mScroller.getCurrY();

                int dx = mCurrentX - newX;
                int dy = mCurrentY - newY;

                onDrag(dx,dy);

                mCurrentX = newX;
                mCurrentY = newY;

                // Post On animation
                mainHandler.post(this);
            }
        }
    }
}
