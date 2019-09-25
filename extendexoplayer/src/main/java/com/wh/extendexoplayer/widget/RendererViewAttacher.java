package com.wh.extendexoplayer.widget;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;

import com.google.android.exoplayer2.util.Assertions;


/* package */ final class RendererViewAttacher extends GestureDetector.SimpleOnGestureListener
        implements View.OnTouchListener, OnGestureListener, OrientationListener.Listener {

    private GestureDetector mGestureDetector;
    private CustomGestureDetector mScaleDragDetector;

    private RendererView.OnScaleDragListener mScaleDragListener;
    private RendererView.OnMotionDownUpListener mDownUpListener;
    private RendererView.OnSingleTapListener mSingleTapListener;
    private RendererView.OnOrientationListener mOrientationListener;
    private GestureDetector.OnGestureListener mGestureListener;
    private GestureDetector.OnDoubleTapListener mDoubleTapListener;

    private final SensorManager sensorManager;
    private final Sensor orientationSensor;
    private final OrientationListener orientationListener;

    private boolean mBlockParentIntercept = false;

    private View mGLView;

    RendererViewAttacher(View glView) {
        this.mGLView = glView;
        glView.setOnTouchListener(this);
        mGestureDetector = new GestureDetector(glView.getContext(), this);
        mScaleDragDetector = new CustomGestureDetector(glView.getContext(), this);
        sensorManager = (SensorManager) glView.getContext().getSystemService(Context.SENSOR_SERVICE);
        Sensor orientationSensor = null;
        if (Build.VERSION.SDK_INT >= 18) {
            // TYPE_GAME_ROTATION_VECTOR is the easiest sensor since it handles all the complex math for
            // fusion. It's used instead of TYPE_ROTATION_VECTOR since the latter uses the magnetometer on
            // devices. When used indoors, the magnetometer can take some time to settle depending on the
            // device and amount of metal in the environment.
            orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        }
        if (orientationSensor == null) {
            orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        }
        this.orientationSensor = orientationSensor;
        WindowManager windowManager = (WindowManager) glView.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = Assertions.checkNotNull(windowManager).getDefaultDisplay();
        orientationListener = new OrientationListener(display, this);
    }

    void onResume() {
        if (orientationSensor != null) {
            sensorManager.registerListener(
                    orientationListener, orientationSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    void onPause() {
        if (orientationSensor != null) {
            sensorManager.unregisterListener(orientationListener);
        }
    }

    void setSingleTapListener(RendererView.OnSingleTapListener listener) {
        this.mSingleTapListener = listener;
    }

    public void setListener(RendererView.Renderer listener) {
        if (listener instanceof RendererView.OnScaleDragListener) {
            mScaleDragListener = (RendererView.OnScaleDragListener) listener;
        }
        if (listener instanceof RendererView.OnMotionDownUpListener) {
            mDownUpListener = (RendererView.OnMotionDownUpListener) listener;
        }
        if (listener instanceof GestureDetector.OnGestureListener) {
            mGestureListener = (GestureDetector.OnGestureListener) listener;
        }
        if (listener instanceof GestureDetector.OnDoubleTapListener) {
            mDoubleTapListener = (GestureDetector.OnDoubleTapListener) listener;
        }
        if (listener instanceof RendererView.OnOrientationListener) {
            mOrientationListener = (RendererView.OnOrientationListener) listener;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        boolean handled = false;

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                ViewParent parent = v.getParent();
                // First, disable the Parent from intercepting the touch
                // event
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }

                if (mDownUpListener != null) {
                    mDownUpListener.onMotionDown();
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                if (mDownUpListener != null) {
                    handled = mDownUpListener.onMotionUp();
                }
                break;
        }

        // Try the Scale/Drag detector
        if (mScaleDragDetector != null) {
            boolean wasScaling = mScaleDragDetector.isScaling();
            boolean wasDragging = mScaleDragDetector.isDragging();

            handled = mScaleDragDetector.onTouchEvent(ev);

            boolean didntScale = !wasScaling && !mScaleDragDetector.isScaling();
            boolean didntDrag = !wasDragging && !mScaleDragDetector.isDragging();

            mBlockParentIntercept = didntScale && didntDrag;
        }

        // Check to see if the user double tapped
        if (mGestureDetector != null && mGestureDetector.onTouchEvent(ev)) {
            handled = true;
        }

        return handled;
    }

    @Override
    public void onDrag(float dx, float dy) {
        if (mScaleDragDetector.isScaling()) {
            return;
        }
        int mScrollEdge = RendererView.EDGE_NONE;
        if (mScaleDragListener != null) {
            mScrollEdge = mScaleDragListener.onDrag(dx, dy);
        }

        ViewParent parent = mGLView.getParent();
        if (!mScaleDragDetector.isScaling() == !mBlockParentIntercept) {
            if (mScrollEdge == RendererView.EDGE_BOTH
                    || (mScrollEdge == RendererView.EDGE_LEFT && dx >= 1f)
                    || (mScrollEdge == RendererView.EDGE_RIGHT && dx <= -1f)) {
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(false);
                }
            }
        } else {
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }
    }

    @Override
    public void onFling(float startX, float startY, float velocityX, float velocityY) {
        if (mScaleDragListener != null) {
            mScaleDragListener.onFling(startX, startY, velocityX, velocityY);
        }
    }

    @Override
    public void onScale(float scaleFactor, float focusX, float focusY) {
        if (mScaleDragListener != null) {
            mScaleDragListener.onScale(scaleFactor, focusX, focusY);
        }
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (mGestureListener != null) {
            return mGestureListener.onSingleTapUp(e);
        }
        if (mSingleTapListener != null) {
            mSingleTapListener.onSingleTapUp(e);
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (mGestureListener != null) {
            mGestureListener.onLongPress(e);
        }
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (mGestureListener != null) {
            return mGestureListener.onScroll(e1, e2, distanceX, distanceY);
        }
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (mGestureListener != null) {
            return mGestureListener.onFling(e1, e2, velocityX, velocityY);
        }
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        if (mGestureListener != null) {
            mGestureListener.onShowPress(e);
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if (mGestureListener != null) {
            return mGestureListener.onDown(e);
        }
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (mDoubleTapListener != null) {
            return mDoubleTapListener.onDoubleTap(e);
        }
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        if (mDoubleTapListener != null) {
            mDoubleTapListener.onDoubleTapEvent(e);
        }
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (mDoubleTapListener != null) {
            mDoubleTapListener.onSingleTapConfirmed(e);
        }
        return false;
    }

    @Override
    public void onOrientationChange(float[] deviceOrientationMatrix, float deviceRoll) {
        if (mOrientationListener != null) {
            mOrientationListener.onOrientationChange(deviceOrientationMatrix, deviceRoll);
        }
    }
}
