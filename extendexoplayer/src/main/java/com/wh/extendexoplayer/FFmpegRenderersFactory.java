package com.wh.extendexoplayer;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.video.MediaCodecVideoRenderer;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import com.wh.extendexoplayer.soft.SoftCodecVideoRenderer;

import java.util.ArrayList;

public class FFmpegRenderersFactory extends DefaultRenderersFactory {

    public FFmpegRenderersFactory(Context context) {
        super(context);
    }

    @Override
    protected void buildVideoRenderers(Context context, int extensionRendererMode, MediaCodecSelector mediaCodecSelector, DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, boolean playClearSamplesWithoutKeys, boolean enableDecoderFallback, Handler eventHandler, VideoRendererEventListener eventListener, long allowedVideoJoiningTimeMs, ArrayList<Renderer> out) {
        super.buildVideoRenderers(context, extensionRendererMode, mediaCodecSelector, drmSessionManager, playClearSamplesWithoutKeys, enableDecoderFallback, eventHandler, eventListener, allowedVideoJoiningTimeMs, out);
        for (Renderer renderer : out) {
            if (renderer instanceof MediaCodecVideoRenderer) {
                out.remove(renderer);
                break;
            }
        }
        out.add(new SoftCodecVideoRenderer(
                context,
                mediaCodecSelector,
                allowedVideoJoiningTimeMs,
                drmSessionManager,
                playClearSamplesWithoutKeys,
                enableDecoderFallback,
                eventHandler,
                eventListener,
                MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY));
    }
}
