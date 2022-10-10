package com.wh.extendexoplayer.player;

import android.content.Context;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.wh.extendexoplayer.MediaInfoChangeListener;
import com.wh.extendexoplayer.MediaType;
import com.wh.extendexoplayer.P2pMediaSource;
import com.wh.extendexoplayer.ReceiveDataListener;
import com.wh.extendexoplayer.widget.RendererView;

import java.util.ArrayList;
import java.util.List;

public final class StreamExoPlayer extends ExtendExoPlayer{

    private List<MediaType> mediaTypes = new ArrayList<>();

    private StreamExoPlayer(RendererView rendererView, boolean isFitXY) {
        super(rendererView,isFitXY);
    }

    @Override
    protected SimpleExoPlayer createExoPlayer(Context context) {
        return new SimpleExoPlayer.Builder(context).build();
    }

    private void setMediaTypes(List<MediaType> mediaTypes) {
        this.mediaTypes.addAll(mediaTypes);
    }

    public ReceiveDataListener prepareSource(MediaInfoChangeListener listener){
        P2pMediaSource.Builder builder = new P2pMediaSource.Builder();
        for (MediaType mediaType : mediaTypes) {
            builder.addMediaType(mediaType);
        }
        P2pMediaSource mediaSource = builder.setMediaInfoChangeListener(listener).build();
        exoPlayer.prepare(mediaSource);
        return mediaSource;
    }

    public static final class Builder extends AbstractBuilder<Builder>{

        private List<MediaType> mediaTypes = new ArrayList<>();

        public Builder(){
            t = this;
        }

        public Builder addMediaType(MediaType mediaType){
            mediaTypes.add(mediaType);
            return this;
        }

        public StreamExoPlayer build(){
            checkRendererViewNotNULL();
            checkMediaTypeNotEmpty();
            StreamExoPlayer exoPlayer = new StreamExoPlayer(mRendererView, isFitXY);
            exoPlayer.setMediaTypes(mediaTypes);
            return exoPlayer;
        }

        private void checkMediaTypeNotEmpty(){
            if(mediaTypes.isEmpty()){
                throw new RuntimeException("Hard decoding type cannot be empty");
            }
        }
    }
}
