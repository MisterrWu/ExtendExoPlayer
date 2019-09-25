package com.wh.extendexoplayer.widget;

import android.content.Context;
import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class TextureRendererView extends GLTextureView implements RendererView, GLTextureView.Renderer {

    protected RendererView.Renderer mRenderer;
    private RendererViewAttacher mAttacher;

    public TextureRendererView(Context context) {
        super(context);
        init(context);
    }

    public TextureRendererView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setEGLContextClientVersion(2);
        mAttacher = new RendererViewAttacher(this);
    }

    @Override
    public void setRenderer(RendererView.Renderer renderer) {
        this.mRenderer = renderer;
        setRenderer(this);
        mAttacher.setListener(renderer);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAttacher.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mAttacher.onPause();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mRenderer != null) {
            mRenderer.onDetachedFromWindow();
        }
    }

    @Override
    public void setSingleTapListener(OnSingleTapListener listener) {
        mAttacher.setSingleTapListener(listener);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        if (mRenderer != null) {
            mRenderer.onSurfaceCreated(gl, config);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (mRenderer != null) {
            mRenderer.onSurfaceChanged(gl, width, height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mRenderer != null) {
            mRenderer.onDrawFrame(gl);
        }
    }
}
