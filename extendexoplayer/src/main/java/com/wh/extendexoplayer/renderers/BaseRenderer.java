package com.wh.extendexoplayer.renderers;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;

import com.google.android.exoplayer2.Player;
import com.wh.extendexoplayer.widget.RendererView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public abstract class BaseRenderer implements RendererView.Renderer {

    private SurfaceTexture surfaceTexture;
    private Surface surface;
    private Player.VideoComponent videoComponent;
    final Handler mainHandler;

    BaseRenderer() {
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public final synchronized void onSurfaceCreated(GL10 gl, EGLConfig config) {
        onSurfaceTextureAvailable(onSurfaceTextureCreate());
    }

    protected abstract SurfaceTexture onSurfaceTextureCreate();

    @Override
    public void onDetachedFromWindow() {
        mainHandler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        if (surface != null) {
                            if (videoComponent != null) {
                                videoComponent.clearVideoSurface(surface);
                            }
                            releaseSurface(surfaceTexture, surface);
                            surfaceTexture = null;
                            surface = null;
                        }
                    }
                });
    }

    @Override
    public final void setVideoComponent(Player.VideoComponent newVideoComponent) {
        if (newVideoComponent == videoComponent) {
            return;
        }
        if (videoComponent != null) {
            if (surface != null) {
                videoComponent.clearVideoSurface(surface);
            }
        }
        setupOldVideoComponent(videoComponent);
        videoComponent = newVideoComponent;
        setupNewVideoComponent(videoComponent);
        if(videoComponent != null){
            newVideoComponent.setVideoSurface(surface);
        }
    }

    protected void setupNewVideoComponent(Player.VideoComponent newVideoComponent){
        newVideoComponent.setVideoSurface(surface);
    }

    protected void setupOldVideoComponent(Player.VideoComponent oldVideoComponent) {}

    private static void releaseSurface(
            SurfaceTexture oldSurfaceTexture, Surface oldSurface) {
        if (oldSurfaceTexture != null) {
            oldSurfaceTexture.release();
        }
        if (oldSurface != null) {
            oldSurface.release();
        }
    }

    // Called on GL thread.
    private void onSurfaceTextureAvailable(final SurfaceTexture surfaceTexture) {
        mainHandler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        SurfaceTexture oldSurfaceTexture = BaseRenderer.this.surfaceTexture;
                        Surface oldSurface = BaseRenderer.this.surface;
                        BaseRenderer.this.surfaceTexture = surfaceTexture;
                        BaseRenderer.this.surface = new Surface(surfaceTexture);
                        if (videoComponent != null) {
                            videoComponent.setVideoSurface(surface);
                        }
                        releaseSurface(oldSurfaceTexture, oldSurface);
                    }
                });
    }
}
