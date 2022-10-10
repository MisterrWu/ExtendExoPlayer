package com.wh.extendexoplayer;

import android.content.Context;
import android.media.MediaFormat;
import android.os.Handler;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.AudioSink;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecAdapter;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.util.MediaFormatUtil;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

/**
 * 解决音频解码器需要额外的参数
 */
public class P2pMediaCodecAudioRenderer extends MediaCodecAudioRenderer {


    public P2pMediaCodecAudioRenderer(Context context, MediaCodecSelector mediaCodecSelector) {
        super(context, mediaCodecSelector);
    }

    public P2pMediaCodecAudioRenderer(Context context, MediaCodecSelector mediaCodecSelector, Handler eventHandler, AudioRendererEventListener eventListener) {
        super(context, mediaCodecSelector, eventHandler, eventListener);
    }

    public P2pMediaCodecAudioRenderer(Context context, MediaCodecSelector mediaCodecSelector, Handler eventHandler, AudioRendererEventListener eventListener, AudioCapabilities audioCapabilities, AudioProcessor... audioProcessors) {
        super(context, mediaCodecSelector, eventHandler, eventListener, audioCapabilities, audioProcessors);
    }

    public P2pMediaCodecAudioRenderer(Context context, MediaCodecSelector mediaCodecSelector, Handler eventHandler, AudioRendererEventListener eventListener, AudioSink audioSink) {
        super(context, mediaCodecSelector, eventHandler, eventListener, audioSink);
    }

    public P2pMediaCodecAudioRenderer(Context context, MediaCodecSelector mediaCodecSelector, boolean enableDecoderFallback, Handler eventHandler, AudioRendererEventListener eventListener, AudioSink audioSink) {
        super(context, mediaCodecSelector, enableDecoderFallback, eventHandler, eventListener, audioSink);
    }

    public P2pMediaCodecAudioRenderer(Context context, MediaCodecAdapter.Factory codecAdapterFactory, MediaCodecSelector mediaCodecSelector, boolean enableDecoderFallback, Handler eventHandler, AudioRendererEventListener eventListener, AudioSink audioSink) {
        super(context, codecAdapterFactory, mediaCodecSelector, enableDecoderFallback, eventHandler, eventListener, audioSink);
    }

    @Override
    protected MediaFormat getMediaFormat(Format format, String codecMimeType, int codecMaxInputSize, float codecOperatingRate) {
        MediaFormat mediaFormat = new MediaFormat();
        // Set format parameters that should always be set.
        mediaFormat.setString(MediaFormat.KEY_MIME, codecMimeType);
        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, format.channelCount);
        mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, format.sampleRate);
        MediaFormatUtil.setCsdBuffers(mediaFormat, format.initializationData);
        // 摄像头音频解码必须设置以下参数
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 24000);
        mediaFormat.setInteger(MediaFormat.KEY_IS_ADTS, 1);
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, 1);
        // Set codec max values.
        MediaFormatUtil.maybeSetInteger(mediaFormat, MediaFormat.KEY_MAX_INPUT_SIZE, codecMaxInputSize);
        // Set codec configuration values.
        if (Util.SDK_INT >= 23) {
            mediaFormat.setInteger(MediaFormat.KEY_PRIORITY, 0 /* realtime priority */);
        }
        if (Util.SDK_INT <= 28 && MimeTypes.AUDIO_AC4.equals(format.sampleMimeType)) {
            // On some older builds, the AC-4 decoder expects to receive samples formatted as raw frames
            // not sync frames. Set a format key to override this.
            mediaFormat.setInteger("ac4-is-sync", 1);
        }
        return mediaFormat;
    }
}
