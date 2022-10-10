package com.wh.extendexoplayer;

import android.content.Context;
import android.os.Handler;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.mediacodec.MediaCodecAdapter;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.video.MediaCodecVideoRenderer;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.nio.ByteBuffer;

/**
 * 解决视频同步跳转到回看视频停止渲染。
 */
public class P2pMediaCodecVideoRenderer extends MediaCodecVideoRenderer {


    public P2pMediaCodecVideoRenderer(Context context, MediaCodecSelector mediaCodecSelector) {
        super(context, mediaCodecSelector);
    }

    public P2pMediaCodecVideoRenderer(Context context, MediaCodecSelector mediaCodecSelector, long allowedJoiningTimeMs) {
        super(context, mediaCodecSelector, allowedJoiningTimeMs);
    }

    public P2pMediaCodecVideoRenderer(Context context, MediaCodecSelector mediaCodecSelector, long allowedJoiningTimeMs, Handler eventHandler, VideoRendererEventListener eventListener, int maxDroppedFramesToNotify) {
        super(context, mediaCodecSelector, allowedJoiningTimeMs, eventHandler, eventListener, maxDroppedFramesToNotify);
    }

    public P2pMediaCodecVideoRenderer(Context context, MediaCodecSelector mediaCodecSelector, long allowedJoiningTimeMs, boolean enableDecoderFallback, Handler eventHandler, VideoRendererEventListener eventListener, int maxDroppedFramesToNotify) {
        super(context, mediaCodecSelector, allowedJoiningTimeMs, enableDecoderFallback, eventHandler, eventListener, maxDroppedFramesToNotify);
    }

    public P2pMediaCodecVideoRenderer(Context context, MediaCodecAdapter.Factory codecAdapterFactory, MediaCodecSelector mediaCodecSelector, long allowedJoiningTimeMs, boolean enableDecoderFallback, Handler eventHandler, VideoRendererEventListener eventListener, int maxDroppedFramesToNotify) {
        super(context, codecAdapterFactory, mediaCodecSelector, allowedJoiningTimeMs, enableDecoderFallback, eventHandler, eventListener, maxDroppedFramesToNotify);
    }

    public P2pMediaCodecVideoRenderer(Context context, MediaCodecAdapter.Factory codecAdapterFactory, MediaCodecSelector mediaCodecSelector, long allowedJoiningTimeMs, boolean enableDecoderFallback, Handler eventHandler, VideoRendererEventListener eventListener, int maxDroppedFramesToNotify, float assumedMinimumCodecOperatingRate) {
        super(context, codecAdapterFactory, mediaCodecSelector, allowedJoiningTimeMs, enableDecoderFallback, eventHandler, eventListener, maxDroppedFramesToNotify, assumedMinimumCodecOperatingRate);
    }

    @Override
    protected boolean processOutputBuffer(long positionUs, long elapsedRealtimeUs, MediaCodecAdapter codec, ByteBuffer buffer, int bufferIndex, int bufferFlags, int sampleCount, long bufferPresentationTimeUs, boolean isDecodeOnlyBuffer, boolean isLastBuffer, Format format) throws ExoPlaybackException {
        long earlyUs = bufferPresentationTimeUs - positionUs;
        /*
         * 同步时间大于五秒，当前视频帧需要直接播放，bufferPresentationTimeUs 是当前视频帧时间戳，positionUs 是同步时间戳，是递增的。
         * 跳转到回看的时候 positionUs 可能会变的很小，bufferPresentationTimeUs 同步到 positionUs 时会等待 positionUs 值增长到播放时间范围内。
         * 范围大概是 50000 Us,所以会导致视频卡住不渲染。例如：跳转回看到几天前，视频会等待 positionUs 增长几天的时间！才会渲染。
         */
        if(earlyUs > 5000000){
            // 直接播放。
            bufferPresentationTimeUs = positionUs + 50000;
        }
        return super.processOutputBuffer(positionUs, elapsedRealtimeUs, codec, buffer, bufferIndex, bufferFlags, sampleCount, bufferPresentationTimeUs, isDecodeOnlyBuffer, isLastBuffer, format);
    }
}
