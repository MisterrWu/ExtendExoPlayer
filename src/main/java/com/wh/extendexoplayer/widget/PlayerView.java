package com.wh.extendexoplayer.widget;


import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.SurfaceView;
import android.view.TextureView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.wh.extendexoplayer.renderers.NormalRectRenderer;

public final class PlayerView {

    private ExoPlayerWrapper playerWrapper;

    private PlayerView() {}

    public void setExoPlayerWrapper(ExoPlayerWrapper wrapper) {
        this.playerWrapper = wrapper;
        wrapper.exoPlayer.addListener(eventListener);
    }

    public void changeSource(String videoPath) {
        if(playerWrapper != null){
            playerWrapper.changeSource(videoPath);
        }
    }

    public void changeSource(MediaSource source){
        if(playerWrapper != null){
            playerWrapper.exoPlayer.prepare(source);
        }
    }

    public boolean isPlaying() {
        if (playerWrapper != null) {
            return playerWrapper.exoPlayer.getPlayWhenReady();
        }
        return false;
    }

    public void release() {
        if (playerWrapper != null) {
            playerWrapper.exoPlayer.release();
        }
    }

    public void pause() {
        if (playerWrapper != null) {
            playerWrapper.exoPlayer.setPlayWhenReady(false);
        }
    }

    public void start() {
        if (playerWrapper != null) {
            playerWrapper.exoPlayer.setPlayWhenReady(true);
        }
    }

    public void stop() {
        if (playerWrapper != null) {
            playerWrapper.exoPlayer.stop();
        }
    }

    public void seekTo(int pos) {
        if (playerWrapper != null) {
            playerWrapper.exoPlayer.seekTo(pos);
        }
    }

    public long getCurrentPosition() {
        if (playerWrapper != null) {
            return playerWrapper.exoPlayer.getCurrentPosition();
        }
        return 0;
    }

    public long getDuration() {
        if (playerWrapper != null) {
            return playerWrapper.exoPlayer.getDuration();
        }
        return 0;
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener listener) {
    }

    public void setOnErrorListener(MediaPlayer.OnErrorListener listener) {
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
    }

    private Player.EventListener eventListener = new Player.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

        }
    };

    public void onResume(boolean isPlay) {
        if (playerWrapper != null && isPlay) {
            playerWrapper.exoPlayer.setPlayWhenReady(true);
        }
    }

    public void onPause() {
        if (playerWrapper != null) {
            playerWrapper.exoPlayer.setPlayWhenReady(false);
        }
    }

    private static class ExoPlayerWrapper {

        //创建加载数据的工厂
        private final DefaultDataSourceFactory dataSourceFactory;
        private final SimpleExoPlayer exoPlayer;

        private ExoPlayerWrapper(Context context) {
            //创建轨道选择器实例
            DefaultTrackSelector trackSelector = new DefaultTrackSelector();
            //创建播放器
            exoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
            dataSourceFactory = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, "ExoPlayer"));
        }

        void changeSource(String videoPath) {
            //传入Uri、加载数据的工厂、解析数据的工厂，就能创建出MediaSource
            Uri mp4VideoUri = Uri.parse(videoPath);
            MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mp4VideoUri);
            exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
            exoPlayer.prepare(videoSource);
        }
    }

    public static final class Builder {


        public RendererViewBuilder setRendererView(RendererView rendererView) {
            return new RendererViewBuilder().setRendererView(rendererView);
        }

        public VideoViewBuilder setRendererView(TextureView rendererView) {
            return new VideoViewBuilder().setRendererView(rendererView);
        }

        public VideoViewBuilder setRendererView(SurfaceView rendererView) {
            return new VideoViewBuilder().setRendererView(rendererView);
        }

        private ExoPlayerWrapper createExoPlayer(Context context) {
            return new ExoPlayerWrapper(context);
        }

        public final class VideoViewBuilder {

            private TextureView mTextureView;

            private SurfaceView mSurfaceView;

            private int videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING;

            VideoViewBuilder setRendererView(TextureView rendererView) {
                this.mTextureView = rendererView;
                return this;
            }

            VideoViewBuilder setRendererView(SurfaceView rendererView) {
                this.mSurfaceView = rendererView;
                return this;
            }

            public VideoViewBuilder setVideoScalingMode(int videoScalingMode) {
                this.videoScalingMode = videoScalingMode;
                return this;
            }

            public PlayerView build() {
                if(mTextureView == null && mSurfaceView == null){
                    throw new RuntimeException("TextureView or SurfaceView can not null");
                }
                PlayerView playerView = new PlayerView();
                Context context = mTextureView != null ? mTextureView.getContext() : mSurfaceView.getContext();
                ExoPlayerWrapper wrapper = createExoPlayer(context);
                if(mTextureView != null){
                    wrapper.exoPlayer.setVideoTextureView(mTextureView);
                }
                if(mSurfaceView != null){
                    wrapper.exoPlayer.setVideoSurfaceView(mSurfaceView);
                }
                wrapper.exoPlayer.setVideoScalingMode(videoScalingMode);
                playerView.setExoPlayerWrapper(wrapper);
                return playerView;
            }
        }

        public final class RendererViewBuilder {

            private RendererView.Renderer mRenderer;

            private RendererView mRendererView;

            RendererViewBuilder setRendererView(RendererView rendererView) {
                this.mRendererView = rendererView;
                return this;
            }

            public RendererViewBuilder setRenderer(RendererView.Renderer renderer) {
                this.mRenderer = renderer;
                return this;
            }

            public PlayerView build() {
                if (mRendererView == null) {
                    throw new RuntimeException("RendererView can not null");
                }
                if (mRenderer == null) {
                    mRenderer = new NormalRectRenderer(false);
                }

                PlayerView playerView = new PlayerView();
                mRendererView.setRenderer(mRenderer);
                ExoPlayerWrapper wrapper = createExoPlayer(mRendererView.getContext());
                mRenderer.setVideoComponent(wrapper.exoPlayer.getVideoComponent());
                playerView.setExoPlayerWrapper(wrapper);
                return playerView;
            }
        }
    }
}
