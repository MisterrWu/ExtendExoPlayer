package com.wh.extendexoplayer.player;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.wh.extendexoplayer.widget.RendererView;

public final class DefaultExoPlayer extends ExtendExoPlayer {

    private final DefaultDataSourceFactory dataSourceFactory;

    private DefaultExoPlayer(RendererView rendererView, boolean isFitXY) {
        super(rendererView, isFitXY);
        Context context = rendererView.getContext();
        dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "ExtendExoPlayer"));
    }

    @Override
    protected SimpleExoPlayer createExoPlayer(Context context) {
        return new SimpleExoPlayer.Builder(context).build();
    }

    public void seekTo(long msec) {
        exoPlayer.seekTo(msec);
    }

    public float getCurrentPosition() {
        return exoPlayer.getCurrentPosition();
    }

    public float getDuration() {
        return exoPlayer.getDuration();
    }

    public void prepareSource(String path) {
        Uri videoUri = Uri.parse(path);
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(videoUri));
        exoPlayer.prepare(videoSource);
    }

    public void prepareSource(Uri videoUri) {
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(videoUri));
        exoPlayer.prepare(videoSource);
    }

    public static final class Builder extends AbstractBuilder<Builder> {

        public Builder() {
            t = this;
        }

        public DefaultExoPlayer build() {
            checkRendererViewNotNULL();
            return new DefaultExoPlayer(mRendererView, isFitXY);
        }
    }
}
