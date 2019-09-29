package com.wh.extendexoplayer.player;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.video.VideoListener;
import com.wh.extendexoplayer.renderers.NormalRectRenderer;
import com.wh.extendexoplayer.widget.RendererView;

public abstract class ExtendExoPlayer implements Player.EventListener, VideoListener {

    private static final int PLAY_STATE_STOP = 1;
    private static final int PLAY_STATE_PREPARED = 2;
    private static final int PLAY_STATE_PLAYING = 3;
    private static final int PLAY_STATE_COMPLETE = 4;

    final SimpleExoPlayer exoPlayer;
    private final NormalRectRenderer renderer;

    private OnPreparedListener onPreparedListener;
    private OnErrorListener onErrorListener;
    private OnCompletionListener onCompletionListener;
    private onRenderedFirstFrameListener onRenderedFirstFrameListener;
    private OnVideoSizeChangedListener onVideoSizeChangedListener;

    ExtendExoPlayer(RendererView rendererView, boolean isFitXY) {
        Context context = rendererView.getContext();
        //创建轨道选择器实例
        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        exoPlayer = createExoPlayer(context, trackSelector);
        exoPlayer.addListener(this);
        exoPlayer.addVideoListener(this);

        renderer = new NormalRectRenderer(isFitXY);
        rendererView.setRenderer(renderer);
        renderer.setVideoComponent(exoPlayer.getVideoComponent());
    }

    protected abstract SimpleExoPlayer createExoPlayer(Context context, TrackSelector trackSelector);

    public void setFirstBitmap(Bitmap bitmap) {
        renderer.setFirstBitmap(bitmap);
    }

    public void setClearColor(float red, float green, float blue, float alpha){
        renderer.setClearColor(red,green,blue,alpha);
    }

    public void setClearColor(float red, float green, float blue){
        renderer.setClearColor(red,green,blue);
    }

    public void start() {
        exoPlayer.setPlayWhenReady(true);
    }

    public void pause() {
        exoPlayer.setPlayWhenReady(false);
    }

    public void stop() {
        exoPlayer.stop();
    }

    public void release() {
        exoPlayer.removeListener(this);
        exoPlayer.removeVideoListener(this);
        exoPlayer.release();
    }

    public boolean isMute() {
        return exoPlayer.getVolume() == 0;
    }

    public void setMute(boolean mute) {
        if (mute) {
            exoPlayer.setVolume(0);
        } else {
            exoPlayer.setVolume(1);
        }
    }

    public boolean isPlaying() {
        if (exoPlayer.getPlaybackState() == PLAY_STATE_STOP
                || exoPlayer.getPlaybackState() == PLAY_STATE_COMPLETE) {
            return false;
        }
        return exoPlayer.getPlayWhenReady();
    }

    public int getPlaybackState() {
        return exoPlayer.getPlaybackState();
    }

    public boolean getPlayWhenReady() {
        return exoPlayer.getPlayWhenReady();
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == PLAY_STATE_PREPARED) {
            if (onPreparedListener != null) {
                onPreparedListener.onPrepared(this);
            }
        } else if (playbackState == PLAY_STATE_COMPLETE) {
            if (onCompletionListener != null) {
                onCompletionListener.onCompletion(this);
            }
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        if (onErrorListener != null) {
            onErrorListener.onError(this, error);
        }
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        if (onVideoSizeChangedListener != null) {
            onVideoSizeChangedListener.onVideoSizeChanged(this, width, height, unappliedRotationDegrees, pixelWidthHeightRatio);
        }
    }

    @Override
    public void onRenderedFirstFrame() {
        if (onRenderedFirstFrameListener != null) {
            onRenderedFirstFrameListener.onRenderedFirstFrame(this);
        }
    }

    public void setOnPreparedListener(OnPreparedListener l) {
        this.onPreparedListener = l;
    }

    public interface OnPreparedListener {
        void onPrepared(ExtendExoPlayer player);
    }

    public void setOnCompletionListener(OnCompletionListener l) {
        this.onCompletionListener = l;
    }

    public interface OnCompletionListener {
        void onCompletion(ExtendExoPlayer player);
    }

    public void setOnErrorListener(OnErrorListener l) {
        this.onErrorListener = l;
    }

    public interface OnErrorListener {
        void onError(ExtendExoPlayer player, ExoPlaybackException error);
    }

    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener l) {
        this.onVideoSizeChangedListener = l;
    }

    public interface OnVideoSizeChangedListener {
        void onVideoSizeChanged(ExtendExoPlayer player, int width, int height, int degrees, float ratio);
    }

    public void setOnRenderedFirstFrameListener(ExtendExoPlayer.onRenderedFirstFrameListener l) {
        this.onRenderedFirstFrameListener = l;
    }

    public interface onRenderedFirstFrameListener {
        void onRenderedFirstFrame(ExtendExoPlayer player);
    }
}
