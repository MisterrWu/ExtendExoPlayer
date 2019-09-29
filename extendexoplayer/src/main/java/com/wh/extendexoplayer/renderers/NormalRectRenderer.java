package com.wh.extendexoplayer.renderers;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLES20;

import com.google.android.exoplayer2.Player;
import com.wh.extendexoplayer.renderers.types.BitmapRenderer;
import com.wh.extendexoplayer.renderers.types.RectRenderer;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.microedition.khronos.opengles.GL10;

import static com.google.android.exoplayer2.util.GlUtil.checkGlError;

public class NormalRectRenderer extends BaseRenderer {

    private final String TAG = "NormalRectRenderer";
    private final AtomicBoolean drawBitmap = new AtomicBoolean(true);
    private BitmapRenderer bitmapRenderer;
    RectRenderer rectRenderer;
    private final float[] rgba = new float[]{0f, 0f, 0f, 1f};

    public NormalRectRenderer(boolean fitXY){
        super();
        rectRenderer = new RectRenderer(fitXY);
        bitmapRenderer = new BitmapRenderer(fitXY);
    }

    public void setClearColor(float red, float green, float blue, float alpha){
        rgba[0] = red;
        rgba[1] = green;
        rgba[2] = blue;
        rgba[3] = alpha;
    }

    public void setClearColor(float red, float green, float blue){
        rgba[0] = red;
        rgba[1] = green;
        rgba[2] = blue;
    }

    @Override
    protected SurfaceTexture onSurfaceTextureCreate() {
        bitmapRenderer.init();
        SurfaceTexture texture = rectRenderer.init();

        // Set the background frame color. This is only visible if the display mesh isn't a full sphere.
        GLES20.glClearColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        checkGlError();

        return texture;
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
