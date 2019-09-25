package com.wh.extendexoplayer.renderers;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;

import com.google.android.exoplayer2.Player;
import com.wh.extendexoplayer.renderers.types.RectRenderer;

import javax.microedition.khronos.opengles.GL10;

public class NormalRectRenderer extends BaseRenderer {

    private final String TAG = "NormalRectRenderer";
    RectRenderer rectRenderer;

    public NormalRectRenderer(boolean fitXY){
        super();
        rectRenderer = new RectRenderer(fitXY);
    }

    @Override
    protected SurfaceTexture onSurfaceTextureCreate() {
        return rectRenderer.init();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        rectRenderer.surfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        rectRenderer.drawFrame();
    }

    @Override
    protected void setupOldVideoComponent(Player.VideoComponent oldVideoComponent) {
        if (oldVideoComponent != null) {
            oldVideoComponent.removeVideoListener(rectRenderer);
        }
    }

    @Override
    protected void setupNewVideoComponent(Player.VideoComponent newVideoComponent) {
        if (newVideoComponent != null) {
            newVideoComponent.addVideoListener(rectRenderer);
        }
    }

    @Override
    public void addOnVideoSizeChangedListener(MediaPlayer.OnVideoSizeChangedListener listener) {
        rectRenderer.addOnVideoSizeChangedListener(listener);
    }
}
