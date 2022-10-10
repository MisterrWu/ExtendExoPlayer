package com.wh.extendexoplayer;

import android.content.Context;
import android.os.Handler;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.AudioSink;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class P2pRenderersFactory extends DefaultRenderersFactory {

    private static final String TAG = "P2pRenderersFactory";
    public P2pRenderersFactory(Context context) {
        super(context);
    }

    @Override
    protected void buildAudioRenderers(
            Context context,
            @ExtensionRendererMode int extensionRendererMode,
            MediaCodecSelector mediaCodecSelector,
            boolean enableDecoderFallback,
            AudioSink audioSink,
            Handler eventHandler,
            AudioRendererEventListener eventListener,
            ArrayList<Renderer> out) {
        P2pMediaCodecAudioRenderer audioRenderer =
                new P2pMediaCodecAudioRenderer(
                        context,
                        getCodecAdapterFactory(),
                        mediaCodecSelector,
                        enableDecoderFallback,
                        eventHandler,
                        eventListener,
                        audioSink);
        out.add(audioRenderer);

        if (extensionRendererMode == EXTENSION_RENDERER_MODE_OFF) {
            return;
        }
        int extensionRendererIndex = out.size();
        if (extensionRendererMode == EXTENSION_RENDERER_MODE_PREFER) {
            extensionRendererIndex--;
        }

        try {
            Class<?> clazz = Class.forName("com.google.android.exoplayer2.decoder.midi.MidiRenderer");
            Constructor<?> constructor = clazz.getConstructor();
            Renderer renderer = (Renderer) constructor.newInstance();
            out.add(extensionRendererIndex++, renderer);
            com.google.android.exoplayer2.util.Log.i(TAG, "Loaded MidiRenderer.");
        } catch (ClassNotFoundException e) {
            // Expected if the app was built without the extension.
        } catch (Exception e) {
            // The extension is present, but instantiation failed.
            throw new RuntimeException("Error instantiating MIDI extension", e);
        }

        try {
            // Full class names used for constructor args so the LINT rule triggers if any of them move.
            Class<?> clazz = Class.forName("com.google.android.exoplayer2.ext.opus.LibopusAudioRenderer");
            Constructor<?> constructor =
                    clazz.getConstructor(
                            android.os.Handler.class,
                            com.google.android.exoplayer2.audio.AudioRendererEventListener.class,
                            com.google.android.exoplayer2.audio.AudioSink.class);
            Renderer renderer =
                    (Renderer) constructor.newInstance(eventHandler, eventListener, audioSink);
            out.add(extensionRendererIndex++, renderer);
            com.google.android.exoplayer2.util.Log.i(TAG, "Loaded LibopusAudioRenderer.");
        } catch (ClassNotFoundException e) {
            // Expected if the app was built without the extension.
        } catch (Exception e) {
            // The extension is present, but instantiation failed.
            throw new RuntimeException("Error instantiating Opus extension", e);
        }

        try {
            // Full class names used for constructor args so the LINT rule triggers if any of them move.
            Class<?> clazz = Class.forName("com.google.android.exoplayer2.ext.flac.LibflacAudioRenderer");
            Constructor<?> constructor =
                    clazz.getConstructor(
                            android.os.Handler.class,
                            com.google.android.exoplayer2.audio.AudioRendererEventListener.class,
                            com.google.android.exoplayer2.audio.AudioSink.class);
            Renderer renderer =
                    (Renderer) constructor.newInstance(eventHandler, eventListener, audioSink);
            out.add(extensionRendererIndex++, renderer);
            com.google.android.exoplayer2.util.Log.i(TAG, "Loaded LibflacAudioRenderer.");
        } catch (ClassNotFoundException e) {
            // Expected if the app was built without the extension.
        } catch (Exception e) {
            // The extension is present, but instantiation failed.
            throw new RuntimeException("Error instantiating FLAC extension", e);
        }

        try {
            // Full class names used for constructor args so the LINT rule triggers if any of them move.
            Class<?> clazz =
                    Class.forName("com.google.android.exoplayer2.ext.ffmpeg.FfmpegAudioRenderer");
            Constructor<?> constructor =
                    clazz.getConstructor(
                            android.os.Handler.class,
                            com.google.android.exoplayer2.audio.AudioRendererEventListener.class,
                            com.google.android.exoplayer2.audio.AudioSink.class);
            Renderer renderer =
                    (Renderer) constructor.newInstance(eventHandler, eventListener, audioSink);
            out.add(extensionRendererIndex++, renderer);
            com.google.android.exoplayer2.util.Log.i(TAG, "Loaded FfmpegAudioRenderer.");
        } catch (ClassNotFoundException e) {
            // Expected if the app was built without the extension.
        } catch (Exception e) {
            // The extension is present, but instantiation failed.
            throw new RuntimeException("Error instantiating FFmpeg extension", e);
        }
    }

    @Override
    protected void buildVideoRenderers(
            Context context,
            @ExtensionRendererMode int extensionRendererMode,
            MediaCodecSelector mediaCodecSelector,
            boolean enableDecoderFallback,
            Handler eventHandler,
            VideoRendererEventListener eventListener,
            long allowedVideoJoiningTimeMs,
            ArrayList<Renderer> out) {
        P2pMediaCodecVideoRenderer videoRenderer =
                new P2pMediaCodecVideoRenderer(
                        context,
                        getCodecAdapterFactory(),
                        mediaCodecSelector,
                        allowedVideoJoiningTimeMs,
                        enableDecoderFallback,
                        eventHandler,
                        eventListener,
                        MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY);
        out.add(videoRenderer);

        if (extensionRendererMode == EXTENSION_RENDERER_MODE_OFF) {
            return;
        }
        int extensionRendererIndex = out.size();
        if (extensionRendererMode == EXTENSION_RENDERER_MODE_PREFER) {
            extensionRendererIndex--;
        }

        try {
            // Full class names used for constructor args so the LINT rule triggers if any of them move.
            Class<?> clazz = Class.forName("com.google.android.exoplayer2.ext.vp9.LibvpxVideoRenderer");
            Constructor<?> constructor =
                    clazz.getConstructor(
                            long.class,
                            android.os.Handler.class,
                            com.google.android.exoplayer2.video.VideoRendererEventListener.class,
                            int.class);
            Renderer renderer =
                    (Renderer)
                            constructor.newInstance(
                                    allowedVideoJoiningTimeMs,
                                    eventHandler,
                                    eventListener,
                                    MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY);
            out.add(extensionRendererIndex++, renderer);
            com.google.android.exoplayer2.util.Log.i(TAG, "Loaded LibvpxVideoRenderer.");
        } catch (ClassNotFoundException e) {
            // Expected if the app was built without the extension.
        } catch (Exception e) {
            // The extension is present, but instantiation failed.
            throw new RuntimeException("Error instantiating VP9 extension", e);
        }

        try {
            // Full class names used for constructor args so the LINT rule triggers if any of them move.
            Class<?> clazz = Class.forName("com.google.android.exoplayer2.ext.av1.Libgav1VideoRenderer");
            Constructor<?> constructor =
                    clazz.getConstructor(
                            long.class,
                            android.os.Handler.class,
                            com.google.android.exoplayer2.video.VideoRendererEventListener.class,
                            int.class);
            Renderer renderer =
                    (Renderer)
                            constructor.newInstance(
                                    allowedVideoJoiningTimeMs,
                                    eventHandler,
                                    eventListener,
                                    MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY);
            out.add(extensionRendererIndex++, renderer);
            com.google.android.exoplayer2.util.Log.i(TAG, "Loaded Libgav1VideoRenderer.");
        } catch (ClassNotFoundException e) {
            // Expected if the app was built without the extension.
        } catch (Exception e) {
            // The extension is present, but instantiation failed.
            throw new RuntimeException("Error instantiating AV1 extension", e);
        }
    }
}
