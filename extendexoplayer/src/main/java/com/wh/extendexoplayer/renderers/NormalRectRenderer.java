package com.wh.extendexoplayer.renderers;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;

import com.google.android.exoplayer2.Player;
import com.wh.extendexoplayer.renderers.types.BitmapRenderer;
import com.wh.extendexoplayer.renderers.types.RectRenderer;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.microedition.khronos.opengles.GL10;

public class NormalRectRenderer extends BaseRenderer {

    private final String TAG = "NormalRectRenderer";
    private final AtomicBoolean drawBitmap = new AtomicBoolean(true);
    private BitmapRenderer bitmapRenderer;
    RectRenderer rectRenderer;

    public NormalRectRenderer(boolean fitXY){
        super();
        rectRenderer = new RectRenderer(fitXY);
        bitmapRenderer = new BitmapRenderer(fitXY);
    }

    @Override
    protected SurfaceTexture onSurfaceTextureCreate() {
        bitmapRenderer.init();
        return rectRenderer.init();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        bitmapRenderer.surfaceChanged(width,height);
        rectRenderer.surfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if(rectRenderer.frameAvailable()){
            drawBitmap.set(false);
        }
        if(drawBitmap.get()){
            bitmapRenderer.drawFrame();
        } else {
            rectRenderer.drawFrame();
        }
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

    public void setFirstBitmap(Bitmap bitmap) {
        bitmapRenderer.setBitmap(bitmap);
    }
}
