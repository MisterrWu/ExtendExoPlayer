package com.wh.extendexoplayer;

import android.util.Log;

import com.google.android.exoplayer2.source.BaseMediaSource;
import com.google.android.exoplayer2.source.MediaPeriod;
import com.google.android.exoplayer2.source.SinglePeriodTimeline;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.upstream.TransferListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class P2pMediaSource extends BaseMediaSource implements ReceiveDataListener{

    public static final int MAX_QUEUE_SIZE = 80;
    public static final String TAG = "P2pMediaSource";
    private List<MediaType> types;
    private ReceiveDataListener dataListener;
    private MediaInfoChangeListener infoChangeListener;

    private P2pMediaSource(List<MediaType> types, MediaInfoChangeListener listener) {
        this.types = types;
        this.infoChangeListener = listener;
    }

    @Override
    protected void prepareSourceInternal(TransferListener mediaTransferListener) {
        refreshSourceInfo(new SinglePeriodTimeline(
                Integer.MAX_VALUE, false, /* isDynamic= */ false, null),null);
        Log.e(TAG, TAG + " prepareSourceInternal");
    }

    @Override
    protected void releaseSourceInternal() {
        Log.e(TAG, TAG + " releaseSourceInternal");
    }

    @Override
    public void maybeThrowSourceInfoRefreshError() throws IOException {
        Log.e(TAG, TAG + " maybeThrowSourceInfoRefreshError");
    }

    @Override
    public MediaPeriod createPeriod(MediaPeriodId id, Allocator allocator, long startPositionUs) {
        Log.e(TAG, TAG + " createPeriod");
        P2pMediaPeriod period = new P2pMediaPeriod(types,infoChangeListener);
        dataListener = period;
        return period;
    }

    @Override
    public void releasePeriod(MediaPeriod mediaPeriod) {
        Log.e(TAG, TAG + " releasePeriod");
        ((P2pMediaPeriod)mediaPeriod).release();
    }

    @Override
    public void onReceiveVideoData(MediaInfo videoInfo) {
        if(dataListener != null){
            dataListener.onReceiveVideoData(videoInfo);
        }
    }

    @Override
    public void onReceiveAudioData(MediaInfo audioInfo) {
        if(dataListener != null){
            dataListener.onReceiveAudioData(audioInfo);
        }
    }

    public final static class Builder{

        private List<MediaType> types = new ArrayList<>();
        private MediaInfoChangeListener infoChangeListener;

        public Builder addMediaType(MediaType type){
            types.add(type);
            return this;
        }

        public Builder setMediaInfoChangeListener(MediaInfoChangeListener l){
            this.infoChangeListener = l;
            return this;
        }

        public P2pMediaSource build(){
            if(types.isEmpty()){
                throw new RuntimeException("types can not empty");
            }
            return new P2pMediaSource(types,infoChangeListener);
        }
    }
}
