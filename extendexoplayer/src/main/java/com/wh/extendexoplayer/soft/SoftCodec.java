package com.wh.extendexoplayer.soft;

import android.graphics.SurfaceTexture;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public final class SoftCodec {

    /**
     * Per buffer metadata includes an offset and size specifying
     * the range of valid data in the associated codec (output) buffer.
     */
    public final static class BufferInfo {
        /**
         * Update the buffer metadata information.
         *
         * @param newOffset the start-offset of the data in the buffer.
         * @param newSize   the amount of data (in bytes) in the buffer.
         * @param newTimeUs the presentation timestamp in microseconds.
         * @param newFlags  buffer flags associated with the buffer.  This
         * should be a combination of  {@link #BUFFER_FLAG_KEY_FRAME} and
         * {@link #BUFFER_FLAG_END_OF_STREAM}.
         */
        public void set(
                int newOffset, int newSize, long newTimeUs, @BufferFlag int newFlags) {
            offset = newOffset;
            size = newSize;
            presentationTimeUs = newTimeUs;
            flags = newFlags;
        }

        /**
         * The start-offset of the data in the buffer.
         */
        public int offset;

        /**
         * The amount of data (in bytes) in the buffer.  If this is {@code 0},
         * the buffer has no data in it and can be discarded.  The only
         * use of a 0-size buffer is to carry the end-of-stream marker.
         */
        public int size;

        /**
         * The presentation timestamp in microseconds for the buffer.
         * This is derived from the presentation timestamp passed in
         * with the corresponding input buffer.  This should be ignored for
         * a 0-sized buffer.
         */
        public long presentationTimeUs;

        /**
         * Buffer flags associated with the buffer.  A combination of
         * {@link #BUFFER_FLAG_KEY_FRAME} and {@link #BUFFER_FLAG_END_OF_STREAM}.
         *
         * <p>Encoded buffers that are key frames are marked with
         * {@link #BUFFER_FLAG_KEY_FRAME}.
         *
         * <p>The last output buffer corresponding to the input buffer
         * marked with {@link #BUFFER_FLAG_END_OF_STREAM} will also be marked
         * with {@link #BUFFER_FLAG_END_OF_STREAM}. In some cases this could
         * be an empty buffer, whose sole purpose is to carry the end-of-stream
         * marker.
         */
        @BufferFlag
        public int flags;

        /** @hide */
        public SoftCodec.BufferInfo dup() {
            SoftCodec.BufferInfo copy = new SoftCodec.BufferInfo();
            copy.set(offset, size, presentationTimeUs, flags);
            return copy;
        }
    };

    // The follow flag constants MUST stay in sync with their equivalents
    // in SoftCodec.h !

    /**
     * This indicates that the (encoded) buffer marked as such contains
     * the data for a key frame.
     *
     * @deprecated Use {@link #BUFFER_FLAG_KEY_FRAME} instead.
     */
    public static final int BUFFER_FLAG_SYNC_FRAME = 1;

    /**
     * This indicates that the (encoded) buffer marked as such contains
     * the data for a key frame.
     */
    public static final int BUFFER_FLAG_KEY_FRAME = 1;

    /**
     * This indicated that the buffer marked as such contains codec
     * initialization / codec specific data instead of media data.
     */
    public static final int BUFFER_FLAG_CODEC_CONFIG = 2;

    /**
     * This signals the end of stream, i.e. no buffers will be available
     * after this, unless of course, {@link #flush} follows.
     */
    public static final int BUFFER_FLAG_END_OF_STREAM = 4;

    /**
     * This indicates that the buffer only contains part of a frame,
     * and the decoder should batch the data until a buffer without
     * this flag appears before decoding the frame.
     */
    public static final int BUFFER_FLAG_PARTIAL_FRAME = 8;

    /**
     * This indicates that the buffer contains non-media data for the
     * muxer to process.
     *
     * All muxer data should start with a FOURCC header that determines the type of data.
     *
     * For example, when it contains Exif data sent to a MediaMuxer track of
     * {@link MediaFormat#MIMETYPE_IMAGE_ANDROID_HEIC} type, the data must start with
     * Exif header ("Exif\0\0"), followed by the TIFF header (See JEITA CP-3451C Section 4.5.2.)
     *
     * @hide
     */
    public static final int BUFFER_FLAG_MUXER_DATA = 16;

    /** @hide */
    @Retention(RetentionPolicy.SOURCE)
    public @interface BufferFlag {}

    private EventHandler mEventHandler;
    private EventHandler mOnFrameRenderedHandler;
    private EventHandler mCallbackHandler;
    private SoftCodec.Callback mCallback;
    private SoftCodec.OnFrameRenderedListener mOnFrameRenderedListener;
    private final Object mListenerLock = new Object();
    private MediaCodecInfo mCodecInfo;
    private final Object mCodecInfoLock = new Object();

    private static final int EVENT_CALLBACK = 1;
    private static final int EVENT_SET_CALLBACK = 2;
    private static final int EVENT_FRAME_RENDERED = 3;

    private static final int CB_INPUT_AVAILABLE = 1;
    private static final int CB_OUTPUT_AVAILABLE = 2;
    private static final int CB_ERROR = 3;
    private static final int CB_OUTPUT_FORMAT_CHANGE = 4;

    private class EventHandler extends Handler {
        private SoftCodec mCodec;

        public EventHandler(SoftCodec codec, Looper looper) {
            super(looper);
            mCodec = codec;
        }

        @Override
        public void handleMessage( Message msg) {
            switch (msg.what) {
                case EVENT_CALLBACK:
                {
                    handleCallback(msg);
                    break;
                }
                case EVENT_SET_CALLBACK:
                {
                    mCallback = (SoftCodec.Callback) msg.obj;
                    break;
                }
                case EVENT_FRAME_RENDERED:
                    synchronized (mListenerLock) {
                        Map<String, Object> map = (Map<String, Object>)msg.obj;
                        for (int i = 0; ; ++i) {
                            Object mediaTimeUs = map.get(i + "-media-time-us");
                            Object systemNano = map.get(i + "-system-nano");
                            if (mediaTimeUs == null || systemNano == null
                                    || mOnFrameRenderedListener == null) {
                                break;
                            }
                            mOnFrameRenderedListener.onFrameRendered(
                                    mCodec, (long)mediaTimeUs, (long)systemNano);
                        }
                        break;
                    }
                default:
                {
                    break;
                }
            }
        }

        private void handleCallback( Message msg) {
            if (mCallback == null) {
                return;
            }

            switch (msg.arg1) {
                case CB_INPUT_AVAILABLE:
                {
                    int index = msg.arg2;
                    mCallback.onInputBufferAvailable(mCodec, index);
                    break;
                }

                case CB_OUTPUT_AVAILABLE:
                {
                    int index = msg.arg2;
                    SoftCodec.BufferInfo info = (SoftCodec.BufferInfo) msg.obj;
                    mCallback.onOutputBufferAvailable(
                            mCodec, index, info);
                    break;
                }

                case CB_ERROR:
                {
                    mCallback.onError(mCodec, (SoftCodec.CodecException) msg.obj);
                    break;
                }

                case CB_OUTPUT_FORMAT_CHANGE:
                {
                    mCallback.onOutputFormatChanged(mCodec,null/* todo */);
                    break;
                }

                default:
                {
                    break;
                }
            }
        }
    }

    private boolean mHasSurface = false;

    /**
     * If you know the exact name of the component you want to instantiate
     * use this method to instantiate it. Use with caution.
     * Likely to be used with information obtained from {@link android.media.MediaCodecList}
     * @param name The name of the codec to be instantiated.
     * @throws IllegalArgumentException if name is not valid.
     * @throws NullPointerException if name is null.
     */

    public static SoftCodec createByCodecName( String name){
        return new SoftCodec(
                name, false /* nameIsType */, false /* unused */);
    }

    private SoftCodec(
             String name, boolean nameIsType, boolean encoder) {
        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else {
            mEventHandler = null;
        }
        mCallbackHandler = mEventHandler;
        mOnFrameRenderedHandler = mEventHandler;

        mBufferLock = new Object();

        native_setup(name, nameIsType, encoder);
    }

    @Override
    protected void finalize() {
        native_finalize();
    }

    /**
     * Free up resources used by the codec instance.
     *
     * Make sure you call this when you're done to free up any opened
     * component instance instead of relying on the garbage collector
     * to do this for you at some point in the future.
     */
    public final void release() {
        freeAllTrackedBuffers(); // free buffers first
        native_release();
    }

    private native final void native_release();

    /**
     * If this codec is to be used as an encoder, pass this flag.
     */
    public static final int CONFIGURE_FLAG_ENCODE = 1;

    /** @hide */
    @Retention(RetentionPolicy.SOURCE)
    public @interface ConfigureFlag {}

    /**
     * Configures a component.
     *
     * @param format The format of the input data (decoder) or the desired
     *               format of the output data (encoder). Passing {@code null}
     *               as {@code format} is equivalent to passing an
     *               {@link MediaFormat#MediaFormat an empty mediaformat}.
     * @param surface Specify a surface on which to render the output of this
     *                decoder. Pass {@code null} as {@code surface} if the
     *                codec does not generate raw video output (e.g. not a video
     *                decoder) and/or if you want to configure the codec for
     *                {@link ByteBuffer} output.
     * @param flags   Specify {@link #CONFIGURE_FLAG_ENCODE} to configure the
     *                component as an encoder.
     * @throws IllegalArgumentException if the surface has been released (or is invalid),
     * or the format is unacceptable (e.g. missing a mandatory key),
     * or the flags are not set properly
     * (e.g. missing {@link #CONFIGURE_FLAG_ENCODE} for an encoder).
     * @throws IllegalStateException if not in the Uninitialized state.
     * @throws SoftCodec.CryptoException upon DRM error.
     * @throws SoftCodec.CodecException upon codec error.
     */
    public void configure(
             MediaFormat format,
             Surface surface,  @ConfigureFlag int flags) {
        String[] keys = null;
        Object[] values = null;

        if (format != null) {
            Map<String, Object> formatMap = null /* todo ormat.getMap()*/;
            keys = new String[formatMap.size()];
            values = new Object[formatMap.size()];

            int i = 0;
            for (Map.Entry<String, Object> entry: formatMap.entrySet()) {
                keys[i] = entry.getKey();
                values[i] = entry.getValue();
                ++i;
            }
        }

        mHasSurface = surface != null;

        native_configure(keys, values, surface, flags);
    }

    /**
     *  Dynamically sets the output surface of a codec.
     *  <p>
     *  This can only be used if the codec was configured with an output surface.  The
     *  new output surface should have a compatible usage type to the original output surface.
     *  E.g. codecs may not support switching from a SurfaceTexture (GPU readable) output
     *  to ImageReader (software readable) output.
     *  @param surface the output surface to use. It must not be {@code null}.
     *  @throws IllegalStateException if the codec does not support setting the output
     *            surface in the current state.
     *  @throws IllegalArgumentException if the new surface is not of a suitable type for the codec.
     */
    public void setOutputSurface( Surface surface) {
        if (!mHasSurface) {
            throw new IllegalStateException("codec was not configured for an output surface");
        }
        native_setSurface(surface);
    }

    private native void native_setSurface( Surface surface);

    private native void native_setCallback(SoftCodec.Callback cb);

    private native void native_configure(
             String[] keys,  Object[] values,
             Surface surface, @ConfigureFlag int flags);

    /**
     * After successfully configuring the component, call {@code start}.
     * <p>
     * Call {@code start} also if the codec is configured in asynchronous mode,
     * and it has just been flushed, to resume requesting input buffers.
     * @throws IllegalStateException if not in the Configured state
     *         or just after {@link #flush} for a codec that is configured
     *         in asynchronous mode.
     * @throws SoftCodec.CodecException upon codec error. Note that some codec errors
     * for start may be attributed to future method calls.
     */
    public final void start() {
        native_start();
    }
    private native void native_start();

    /**
     * Finish the decode/encode session, note that the codec instance
     * remains active and ready to be {@link #start}ed again.
     * To ensure that it is available to other client call {@link #release}
     * and don't just rely on garbage collection to eventually do this for you.
     * @throws IllegalStateException if in the Released state.
     */
    public final void stop() {
        native_stop();
        freeAllTrackedBuffers();

        synchronized (mListenerLock) {
            if (mCallbackHandler != null) {
                mCallbackHandler.removeMessages(EVENT_SET_CALLBACK);
                mCallbackHandler.removeMessages(EVENT_CALLBACK);
            }
            if (mOnFrameRenderedHandler != null) {
                mOnFrameRenderedHandler.removeMessages(EVENT_FRAME_RENDERED);
            }
        }
    }

    private native final void native_stop();

    /**
     * Flush both input and output ports of the component.
     * <p>
     * Upon return, all indices previously returned in calls to {@link #dequeueInputBuffer
     * dequeueInputBuffer} and {@link #dequeueOutputBuffer dequeueOutputBuffer} &mdash; or obtained
     * via {@link SoftCodec.Callback#onInputBufferAvailable onInputBufferAvailable} or
     * {@link SoftCodec.Callback#onOutputBufferAvailable onOutputBufferAvailable} callbacks &mdash; become
     * invalid, and all buffers are owned by the codec.
     * <p>
     * If the codec is configured in asynchronous mode, call {@link #start}
     * after {@code flush} has returned to resume codec operations. The codec
     * will not request input buffers until this has happened.
     * <strong>Note, however, that there may still be outstanding {@code onOutputBufferAvailable}
     * callbacks that were not handled prior to calling {@code flush}.
     * The indices returned via these callbacks also become invalid upon calling {@code flush} and
     * should be discarded.</strong>
     * <p>
     * If the codec is configured in synchronous mode, codec will resume
     * automatically if it is configured with an input surface.  Otherwise, it
     * will resume when {@link #dequeueInputBuffer dequeueInputBuffer} is called.
     *
     * @throws IllegalStateException if not in the Executing state.
     * @throws SoftCodec.CodecException upon codec error.
     */
    public final void flush() {
        synchronized(mBufferLock) {
            mDequeuedInputBuffers.clear();
            mDequeuedOutputBuffers.clear();
        }
        native_flush();
    }

    private native final void native_flush();

    /**
     * Thrown when an internal codec error occurs.
     */
    public final static class CodecException extends IllegalStateException {
        CodecException(int errorCode, int actionCode,  String detailMessage) {
            super(detailMessage);
            mErrorCode = errorCode;
            mActionCode = actionCode;

            // TODO get this from codec
            final String sign = errorCode < 0 ? "neg_" : "";
            mDiagnosticInfo =
                    "android.media.SoftCodec.error_" + sign + Math.abs(errorCode);
        }

        /**
         * Returns true if the codec exception is a transient issue,
         * perhaps due to resource constraints, and that the method
         * (or encoding/decoding) may be retried at a later time.
         */
        public boolean isTransient() {
            return mActionCode == ACTION_TRANSIENT;
        }

        /**
         * Returns true if the codec cannot proceed further,
         * but can be recovered by stopping, configuring,
         * and starting again.
         */
        public boolean isRecoverable() {
            return mActionCode == ACTION_RECOVERABLE;
        }

        /**
         * Retrieve the error code associated with a CodecException
         */
        public int getErrorCode() {
            return mErrorCode;
        }

        /**
         * Retrieve a developer-readable diagnostic information string
         * associated with the exception. Do not show this to end-users,
         * since this string will not be localized or generally
         * comprehensible to end-users.
         */
        public  String getDiagnosticInfo() {
            return mDiagnosticInfo;
        }

        /**
         * This indicates required resource was not able to be allocated.
         */
        public static final int ERROR_INSUFFICIENT_RESOURCE = 1100;

        /**
         * This indicates the resource manager reclaimed the media resource used by the codec.
         * <p>
         * With this exception, the codec must be released, as it has moved to terminal state.
         */
        public static final int ERROR_RECLAIMED = 1101;

        @Retention(RetentionPolicy.SOURCE)
        public @interface ReasonCode {}

        /* Must be in sync with android_media_MediaCodec.cpp */
        private final static int ACTION_TRANSIENT = 1;
        private final static int ACTION_RECOVERABLE = 2;

        private final String mDiagnosticInfo;
        private final int mErrorCode;
        private final int mActionCode;
    }

    /**
     * Thrown when a crypto error occurs while queueing a secure input buffer.
     */
    public final static class CryptoException extends RuntimeException {
        public CryptoException(int errorCode,  String detailMessage) {
            super(detailMessage);
            mErrorCode = errorCode;
        }

        /**
         * This indicates that the requested key was not found when trying to
         * perform a decrypt operation.  The operation can be retried after adding
         * the correct decryption key.
         */
        public static final int ERROR_NO_KEY = 1;

        /**
         * This indicates that the key used for decryption is no longer
         * valid due to license term expiration.  The operation can be retried
         * after updating the expired keys.
         */
        public static final int ERROR_KEY_EXPIRED = 2;

        /**
         * This indicates that a required crypto resource was not able to be
         * allocated while attempting the requested operation.  The operation
         * can be retried if the app is able to release resources.
         */
        public static final int ERROR_RESOURCE_BUSY = 3;

        /**
         * This indicates that the output protection levels supported by the
         * device are not sufficient to meet the requirements set by the
         * content owner in the license policy.
         */
        public static final int ERROR_INSUFFICIENT_OUTPUT_PROTECTION = 4;

        /**
         * This indicates that decryption was attempted on a session that is
         * not opened, which could be due to a failure to open the session,
         * closing the session prematurely, or the session being reclaimed
         * by the resource manager.
         */
        public static final int ERROR_SESSION_NOT_OPENED = 5;

        /**
         * This indicates that an operation was attempted that could not be
         * supported by the crypto system of the device in its current
         * configuration.  It may occur when the license policy requires
         * device security features that aren't supported by the device,
         * or due to an internal error in the crypto system that prevents
         * the specified security policy from being met.
         */
        public static final int ERROR_UNSUPPORTED_OPERATION = 6;

        @Retention(RetentionPolicy.SOURCE)
        public @interface CryptoErrorCode {}

        /**
         * Retrieve the error code associated with a CryptoException
         */
        @CryptoErrorCode
        public int getErrorCode() {
            return mErrorCode;
        }

        private int mErrorCode;
    }

    /**
     * After filling a range of the input buffer at the specified index
     * submit it to the component. Once an input buffer is queued to
     * the codec, it MUST NOT be used until it is later retrieved by
     * {@link #getInputBuffer} in response to a {@link #dequeueInputBuffer}
     * return value or a {@link SoftCodec.Callback#onInputBufferAvailable}
     * callback.
     * <p>
     * Many decoders require the actual compressed data stream to be
     * preceded by "codec specific data", i.e. setup data used to initialize
     * the codec such as PPS/SPS in the case of AVC video or code tables
     * in the case of vorbis audio.
     * The class {@link android.media.MediaExtractor} provides codec
     * specific data as part of
     * the returned track format in entries named "csd-0", "csd-1" ...
     * <p>
     * These buffers can be submitted directly after {@link #start} or
     * {@link #flush} by specifying the flag {@link
     * #BUFFER_FLAG_CODEC_CONFIG}.  However, if you configure the
     * codec with a {@link MediaFormat} containing these keys, they
     * will be automatically submitted by SoftCodec directly after
     * start.  Therefore, the use of {@link
     * #BUFFER_FLAG_CODEC_CONFIG} flag is discouraged and is
     * recommended only for advanced users.
     * <p>
     * To indicate that this is the final piece of input data (or rather that
     * no more input data follows unless the decoder is subsequently flushed)
     * specify the flag {@link #BUFFER_FLAG_END_OF_STREAM}.
     * <p class=note>
     * <strong>Note:</strong> Prior to {@link android.os.Build.VERSION_CODES#M},
     * {@code presentationTimeUs} was not propagated to the frame timestamp of (rendered)
     * Surface output buffers, and the resulting frame timestamp was undefined.
     * Use {@link #releaseOutputBuffer(int, long)} to ensure a specific frame timestamp is set.
     * Similarly, since frame timestamps can be used by the destination surface for rendering
     * synchronization, <strong>care must be taken to normalize presentationTimeUs so as to not be
     * mistaken for a system time. (See {@linkplain #releaseOutputBuffer(int, long)
     * SurfaceView specifics}).</strong>
     *
     * @param index The index of a client-owned input buffer previously returned
     *              in a call to {@link #dequeueInputBuffer}.
     * @param offset The byte offset into the input buffer at which the data starts.
     * @param size The number of bytes of valid input data.
     * @param presentationTimeUs The presentation timestamp in microseconds for this
     *                           buffer. This is normally the media time at which this
     *                           buffer should be presented (rendered). When using an output
     *                           surface, this will be propagated as the {@link
     *                           SurfaceTexture#getTimestamp timestamp} for the frame (after
     *                           conversion to nanoseconds).
     * @param flags A bitmask of flags
     *              {@link #BUFFER_FLAG_CODEC_CONFIG} and {@link #BUFFER_FLAG_END_OF_STREAM}.
     *              While not prohibited, most codecs do not use the
     *              {@link #BUFFER_FLAG_KEY_FRAME} flag for input buffers.
     * @throws IllegalStateException if not in the Executing state.
     * @throws SoftCodec.CodecException upon codec error.
     * @throws SoftCodec.CryptoException if a crypto object has been specified in
     *         {@link #configure}
     */
    public final void queueInputBuffer(
            int index,
            int offset, int size, long presentationTimeUs, int flags)
            throws SoftCodec.CryptoException {
        synchronized(mBufferLock) {
            mDequeuedInputBuffers.remove(index);
        }
        native_queueInputBuffer(
                index, offset, size, presentationTimeUs, flags);
    }

    private native void native_queueInputBuffer(
            int index,
            int offset, int size, long presentationTimeUs, int flags)
            throws SoftCodec.CryptoException;

    public static final int CRYPTO_MODE_UNENCRYPTED = 0;
    public static final int CRYPTO_MODE_AES_CTR     = 1;
    public static final int CRYPTO_MODE_AES_CBC     = 2;

    /**
     * Returns the index of an input buffer to be filled with valid data
     * or -1 if no such buffer is currently available.
     * This method will return immediately if timeoutUs == 0, wait indefinitely
     * for the availability of an input buffer if timeoutUs &lt; 0 or wait up
     * to "timeoutUs" microseconds if timeoutUs &gt; 0.
     * @param timeoutUs The timeout in microseconds, a negative timeout indicates "infinite".
     * @throws IllegalStateException if not in the Executing state,
     *         or codec is configured in asynchronous mode.
     * @throws SoftCodec.CodecException upon codec error.
     */
    public final int dequeueInputBuffer(long timeoutUs) {
        return native_dequeueInputBuffer(timeoutUs);
    }

    private native int native_dequeueInputBuffer(long timeoutUs);

    /**
     * If a non-negative timeout had been specified in the call
     * to {@link #dequeueOutputBuffer}, indicates that the call timed out.
     */
    public static final int INFO_TRY_AGAIN_LATER        = -1;

    /**
     * The output format has changed, subsequent data will follow the new
     * format. {@link #getOutputFormat()} returns the new format.  Note, that
     * you can also use the new {@link #getOutputFormat()} method to
     * get the format for a specific output buffer.  This frees you from
     * having to track output format changes.
     */
    public static final int INFO_OUTPUT_FORMAT_CHANGED  = -2;

    /**
     * The output buffers have changed, the client must refer to the new
     * set of output buffers returned by from
     * this point on.
     *
     * <p>Additionally, this event signals that the video scaling mode
     * may have been reset to the default.</p>
     *
     * @deprecated This return value can be ignored as {@link
     * } has been deprecated.  Client should
     * request a current buffer using on of the get-buffer or
     * get-image methods each time one has been dequeued.
     */
    public static final int INFO_OUTPUT_BUFFERS_CHANGED = -3;

    /** @hide */
    @Retention(RetentionPolicy.SOURCE)
    public @interface OutputBufferInfo {}

    /**
     * Dequeue an output buffer, block at most "timeoutUs" microseconds.
     * Returns the index of an output buffer that has been successfully
     * decoded or one of the INFO_* constants.
     * @param info Will be filled with buffer meta data.
     * @param timeoutUs The timeout in microseconds, a negative timeout indicates "infinite".
     * @throws IllegalStateException if not in the Executing state,
     *         or codec is configured in asynchronous mode.
     * @throws SoftCodec.CodecException upon codec error.
     */
    @OutputBufferInfo
    public final int dequeueOutputBuffer(
             SoftCodec.BufferInfo info, long timeoutUs) {
        int res = native_dequeueOutputBuffer(info, timeoutUs);
        synchronized(mBufferLock) {
            if (res >= 0) {
                if (mHasSurface) {
                    mDequeuedOutputInfos.put(res, info.dup());
                }
            }
        }
        return res;
    }

    private native final int native_dequeueOutputBuffer(
             SoftCodec.BufferInfo info, long timeoutUs);

    /**
     * If you are done with a buffer, use this call to return the buffer to the codec
     * or to render it on the output surface. If you configured the codec with an
     * output surface, setting {@code render} to {@code true} will first send the buffer
     * to that output surface. The surface will release the buffer back to the codec once
     * it is no longer used/displayed.
     *
     * Once an output buffer is released to the codec, it MUST NOT
     * be used until it is later retrieved by {@link #getOutputBuffer} in response
     * to a {@link #dequeueOutputBuffer} return value or a
     * {@link SoftCodec.Callback#onOutputBufferAvailable} callback.
     *
     * @param index The index of a client-owned output buffer previously returned
     *              from a call to {@link #dequeueOutputBuffer}.
     * @param render If a valid surface was specified when configuring the codec,
     *               passing true renders this output buffer to the surface.
     * @throws IllegalStateException if not in the Executing state.
     * @throws SoftCodec.CodecException upon codec error.
     */
    public final void releaseOutputBuffer(int index, boolean render) {
        SoftCodec.BufferInfo info = null;
        synchronized(mBufferLock) {
            mDequeuedOutputBuffers.remove(index);
            if (mHasSurface) {
                info = mDequeuedOutputInfos.remove(index);
            }
        }
        releaseOutputBuffer(index, render, false /* updatePTS */, 0 /* dummy */);
    }

    /**
     * If you are done with a buffer, use this call to update its surface timestamp
     * and return it to the codec to render it on the output surface. If you
     * have not specified an output surface when configuring this video codec,
     * this call will simply return the buffer to the codec.<p>
     *
     * The timestamp may have special meaning depending on the destination surface.
     *
     * <table>
     * <tr><th>SurfaceView specifics</th></tr>
     * <tr><td>
     * If you render your buffer on a {@link android.view.SurfaceView},
     * you can use the timestamp to render the buffer at a specific time (at the
     * VSYNC at or after the buffer timestamp).  For this to work, the timestamp
     * needs to be <i>reasonably close</i> to the current {@link System#nanoTime}.
     * Currently, this is set as within one (1) second. A few notes:
     *
     * <ul>
     * <li>the buffer will not be returned to the codec until the timestamp
     * has passed and the buffer is no longer used by the {@link android.view.Surface}.
     * <li>buffers are processed sequentially, so you may block subsequent buffers to
     * be displayed on the {@link android.view.Surface}.  This is important if you
     * want to react to user action, e.g. stop the video or seek.
     * <li>if multiple buffers are sent to the {@link android.view.Surface} to be
     * rendered at the same VSYNC, the last one will be shown, and the other ones
     * will be dropped.
     * <li>if the timestamp is <em>not</em> "reasonably close" to the current system
     * time, the {@link android.view.Surface} will ignore the timestamp, and
     * display the buffer at the earliest feasible time.  In this mode it will not
     * drop frames.
     * <li>for best performance and quality, call this method when you are about
     * two VSYNCs' time before the desired render time.  For 60Hz displays, this is
     * about 33 msec.
     * </ul>
     * </td></tr>
     * </table>
     *
     * Once an output buffer is released to the codec, it MUST NOT
     * be used until it is later retrieved by {@link #getOutputBuffer} in response
     * to a {@link #dequeueOutputBuffer} return value or a
     * {@link SoftCodec.Callback#onOutputBufferAvailable} callback.
     *
     * @param index The index of a client-owned output buffer previously returned
     *              from a call to {@link #dequeueOutputBuffer}.
     * @param renderTimestampNs The timestamp to associate with this buffer when
     *              it is sent to the Surface.
     * @throws IllegalStateException if not in the Executing state.
     * @throws SoftCodec.CodecException upon codec error.
     */
    public final void releaseOutputBuffer(int index, long renderTimestampNs) {
        SoftCodec.BufferInfo info = null;
        synchronized(mBufferLock) {
            mDequeuedOutputBuffers.remove(index);
            if (mHasSurface) {
                info = mDequeuedOutputInfos.remove(index);
            }
        }
        releaseOutputBuffer(
                index, true /* render */, true /* updatePTS */, renderTimestampNs);
    }

    private native final void releaseOutputBuffer(
            int index, boolean render, boolean updatePTS, long timeNs);

    /**
     * Call this after dequeueOutputBuffer signals a format change by returning
     * {@link #INFO_OUTPUT_FORMAT_CHANGED}.
     * You can also call this after {@link #configure} returns
     * successfully to get the output format initially configured
     * for the codec.  Do this to determine what optional
     * configuration parameters were supported by the codec.
     *
     * @throws IllegalStateException if not in the Executing or
     *                               Configured state.
     * @throws SoftCodec.CodecException upon codec error.
     */

    public final MediaFormat getOutputFormat() {
        return new MediaFormat(/* todo getFormatNative(false *//* input *//*)*/);
    }


    private native final Map<String, Object> getFormatNative(boolean input);


    // used to track dequeued buffers
    private static class BufferMap {
        // various returned representations of the codec buffer
        private static class CodecBuffer {
            private ByteBuffer mByteBuffer;

            public void free() {
                if (mByteBuffer != null) {
                    // all of our ByteBuffers are direct
                    //java.nio.NioUtils.freeDirectBuffer(mByteBuffer);
                    mByteBuffer = null;
                }
            }


            public void setByteBuffer( ByteBuffer buffer) {
                free();
                mByteBuffer = buffer;
            }
        }

        private final Map<Integer, CodecBuffer> mMap =
                new HashMap<Integer, CodecBuffer>();

        public void remove(int index) {
            CodecBuffer buffer = mMap.get(index);
            if (buffer != null) {
                buffer.free();
                mMap.remove(index);
            }
        }

        public void put(int index,  ByteBuffer newBuffer) {
            CodecBuffer buffer = mMap.get(index);
            if (buffer == null) { // likely
                buffer = new CodecBuffer();
                mMap.put(index, buffer);
            }
            buffer.setByteBuffer(newBuffer);
        }

        public void clear() {
            for (CodecBuffer buffer: mMap.values()) {
                buffer.free();
            }
            mMap.clear();
        }
    }

    private final BufferMap mDequeuedInputBuffers = new BufferMap();
    private final BufferMap mDequeuedOutputBuffers = new BufferMap();
    private final Map<Integer, SoftCodec.BufferInfo> mDequeuedOutputInfos =
            new HashMap<Integer, SoftCodec.BufferInfo>();
    final private Object mBufferLock;

    private final void invalidateByteBuffer(
             ByteBuffer[] buffers, int index) {
        if (buffers != null && index >= 0 && index < buffers.length) {
            ByteBuffer buffer = buffers[index];
            if (buffer != null) {
                /* todo buffer.setAccessible(false);*/
            }
        }
    }

    private final void validateInputByteBuffer(
             ByteBuffer[] buffers, int index) {
        if (buffers != null && index >= 0 && index < buffers.length) {
            ByteBuffer buffer = buffers[index];
            if (buffer != null) {
                /*todo buffer.setAccessible(true);*/
                buffer.clear();
            }
        }
    }

    private final void revalidateByteBuffer(
             ByteBuffer[] buffers, int index) {
        synchronized(mBufferLock) {
            if (buffers != null && index >= 0 && index < buffers.length) {
                ByteBuffer buffer = buffers[index];
                if (buffer != null) {
                    /* todo buffer.setAccessible(true);*/
                }
            }
        }
    }

    private final void validateOutputByteBuffer(
             ByteBuffer[] buffers, int index,  SoftCodec.BufferInfo info) {
        if (buffers != null && index >= 0 && index < buffers.length) {
            ByteBuffer buffer = buffers[index];
            if (buffer != null) {
                /* todo buffer.setAccessible(true);*/
                buffer.limit(info.offset + info.size).position(info.offset);
            }
        }
    }

    private final void invalidateByteBuffers( ByteBuffer[] buffers) {
        if (buffers != null) {
            for (ByteBuffer buffer: buffers) {
                if (buffer != null) {
                    /* todo buffer.setAccessible(false);*/
                }
            }
        }
    }

    private final void freeByteBuffer( ByteBuffer buffer) {
        if (buffer != null /* && buffer.isDirect() */) {
            // all of our ByteBuffers are direct
            /* todo java.nio.NioUtils.freeDirectBuffer(buffer);*/
        }
    }

    private final void freeByteBuffers( ByteBuffer[] buffers) {
        if (buffers != null) {
            for (ByteBuffer buffer: buffers) {
                freeByteBuffer(buffer);
            }
        }
    }

    private final void freeAllTrackedBuffers() {
        synchronized(mBufferLock) {
            mDequeuedInputBuffers.clear();
            mDequeuedOutputBuffers.clear();
        }
    }

    /**
     * Returns a {@link java.nio.Buffer#clear cleared}, writable ByteBuffer
     * object for a dequeued input buffer index to contain the input data.
     *
     * After calling this method any ByteBuffer or Image object
     * previously returned for the same input index MUST no longer
     * be used.
     *
     * @param index The index of a client-owned input buffer previously
     *              returned from a call to {@link #dequeueInputBuffer},
     *              or received via an onInputBufferAvailable callback.
     *
     * @return the input buffer, or null if the index is not a dequeued
     * input buffer, or if the codec is configured for surface input.
     *
     * @throws IllegalStateException if not in the Executing state.
     * @throws SoftCodec.CodecException upon codec error.
     */

    public ByteBuffer getInputBuffer(int index) {
        ByteBuffer newBuffer = getBuffer(true /* input */, index);
        synchronized(mBufferLock) {
            mDequeuedInputBuffers.put(index, newBuffer);
        }
        return newBuffer;
    }

    /**
     * Returns a read-only ByteBuffer for a dequeued output buffer
     * index. The position and limit of the returned buffer are set
     * to the valid output data.
     *
     * After calling this method, any ByteBuffer or Image object
     * previously returned for the same output index MUST no longer
     * be used.
     *
     * @param index The index of a client-owned output buffer previously
     *              returned from a call to {@link #dequeueOutputBuffer},
     *              or received via an onOutputBufferAvailable callback.
     *
     * @return the output buffer, or null if the index is not a dequeued
     * output buffer, or the codec is configured with an output surface.
     *
     * @throws IllegalStateException if not in the Executing state.
     * @throws SoftCodec.CodecException upon codec error.
     */

    public ByteBuffer getOutputBuffer(int index) {
        ByteBuffer newBuffer = getBuffer(false /* input */, index);
        synchronized(mBufferLock) {
            mDequeuedOutputBuffers.put(index, newBuffer);
        }
        return newBuffer;
    }

    /**
     * The content is scaled to the surface dimensions
     */
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT               = 1;

    /**
     * The content is scaled, maintaining its aspect ratio, the whole
     * surface area is used, content may be cropped.
     * <p class=note>
     * This mode is only suitable for content with 1:1 pixel aspect ratio as you cannot
     * configure the pixel aspect ratio for a {@link Surface}.
     * <p class=note>
     * As of {@link android.os.Build.VERSION_CODES#N} release, this mode may not work if
     * the video is {@linkplain MediaFormat#KEY_ROTATION rotated} by 90 or 270 degrees.
     */
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING = 2;

    @Retention(RetentionPolicy.SOURCE)
    public @interface VideoScalingMode {}

    /**
     * If a surface has been specified in a previous call to {@link #configure}
     * specifies the scaling mode to use. The default is "scale to fit".
     * <p class=note>
     * The scaling mode may be reset to the <strong>default</strong> each time an
     * {@link #INFO_OUTPUT_BUFFERS_CHANGED} event is received from the codec; therefore, the client
     * must call this method after every buffer change event (and before the first output buffer is
     * released for rendering) to ensure consistent scaling mode.
     * <p class=note>
     * Since the {@link #INFO_OUTPUT_BUFFERS_CHANGED} event is deprecated, this can also be done
     * after each {@link #INFO_OUTPUT_FORMAT_CHANGED} event.
     *
     * @throws IllegalArgumentException if mode is not recognized.
     * @throws IllegalStateException if in the Released state.
     */
    public native final void setVideoScalingMode(@VideoScalingMode int mode);


    /**
     * Change a video encoder's target bitrate on the fly. The value is an
     * Integer object containing the new bitrate in bps.
     */
    public static final String PARAMETER_KEY_VIDEO_BITRATE = "video-bitrate";

    /**
     * Temporarily suspend/resume encoding of input data. While suspended
     * input data is effectively discarded instead of being fed into the
     * encoder. This parameter really only makes sense to use with an encoder
     * in "surface-input" mode, as the client code has no control over the
     * input-side of the encoder in that case.
     * The value is an Integer object containing the value 1 to suspend
     * or the value 0 to resume.
     */
    public static final String PARAMETER_KEY_SUSPEND = "drop-input-frames";

    /**
     * Request that the encoder produce a sync frame "soon".
     * Provide an Integer with the value 0.
     */
    public static final String PARAMETER_KEY_REQUEST_SYNC_FRAME = "request-sync";

    /**
     * Communicate additional parameter changes to the component instance.
     * <b>Note:</b> Some of these parameter changes may silently fail to apply.
     *
     * @param params The bundle of parameters to set.
     * @throws IllegalStateException if in the Released state.
     */
    public final void setParameters( Bundle params) {
        if (params == null) {
            return;
        }

        String[] keys = new String[params.size()];
        Object[] values = new Object[params.size()];

        int i = 0;
        for (final String key: params.keySet()) {
            keys[i] = key;
            values[i] = params.get(key);
            ++i;
        }

        setParameters(keys, values);
    }

    /**
     * Sets an asynchronous callback for actionable SoftCodec events.
     *
     * If the client intends to use the component in asynchronous mode,
     * a valid callback should be provided before {@link #configure} is called.
     *
     * When asynchronous callback is enabled, the client should not call
     * {@link #dequeueInputBuffer(long)} or {@link #dequeueOutputBuffer(SoftCodec.BufferInfo, long)}.
     * <p>
     * Also, {@link #flush} behaves differently in asynchronous mode.  After calling
     * {@code flush}, you must call {@link #start} to "resume" receiving input buffers,
     * even if an input surface was created.
     *
     * @param cb The callback that will run.  Use {@code null} to clear a previously
     *           set callback (before {@link #configure configure} is called and run
     *           in synchronous mode).
     * @param handler Callbacks will happen on the handler's thread. If {@code null},
     *           callbacks are done on the default thread (the caller's thread or the
     *           main thread.)
     */
    public void setCallback( /* SoftCodec. */ SoftCodec.Callback cb,  Handler handler) {
        if (cb != null) {
            synchronized (mListenerLock) {
                EventHandler newHandler = getEventHandlerOn(handler, mCallbackHandler);
                // NOTE: there are no callbacks on the handler at this time, but check anyways
                // even if we were to extend this to be callable dynamically, it must
                // be called when codec is flushed, so no messages are pending.
                if (newHandler != mCallbackHandler) {
                    mCallbackHandler.removeMessages(EVENT_SET_CALLBACK);
                    mCallbackHandler.removeMessages(EVENT_CALLBACK);
                    mCallbackHandler = newHandler;
                }
            }
        } else if (mCallbackHandler != null) {
            mCallbackHandler.removeMessages(EVENT_SET_CALLBACK);
            mCallbackHandler.removeMessages(EVENT_CALLBACK);
        }

        if (mCallbackHandler != null) {
            // set java callback on main handler
            Message msg = mCallbackHandler.obtainMessage(EVENT_SET_CALLBACK, 0, 0, cb);
            mCallbackHandler.sendMessage(msg);

            // set native handler here, don't post to handler because
            // it may cause the callback to be delayed and set in a wrong state.
            // Note that native codec may start sending events to the callback
            // handler after this returns.
            native_setCallback(cb);
        }
    }

    /**
     * Sets an asynchronous callback for actionable SoftCodec events on the default
     * looper.
     * <p>
     * Same as {@link #setCallback(SoftCodec.Callback, Handler)} with handler set to null.
     * @param cb The callback that will run.  Use {@code null} to clear a previously
     *           set callback (before {@link #configure configure} is called and run
     *           in synchronous mode).
     * @see #setCallback(SoftCodec.Callback, Handler)
     */
    public void setCallback( /* SoftCodec. */ SoftCodec.Callback cb) {
        setCallback(cb, null /* handler */);
    }

    /**
     * Listener to be called when an output frame has rendered on the output surface
     *
     * @see SoftCodec#setOnFrameRenderedListener
     */
    public interface OnFrameRenderedListener {

        /**
         * Called when an output frame has rendered on the output surface.
         * <p>
         * <strong>Note:</strong> This callback is for informational purposes only: to get precise
         * render timing samples, and can be significantly delayed and batched. Some frames may have
         * been rendered even if there was no callback generated.
         *
         * @param codec the SoftCodec instance
         * @param presentationTimeUs the presentation time (media time) of the frame rendered.
         *          This is usually the same as specified in {@link #queueInputBuffer}; however,
         *          some codecs may alter the media time by applying some time-based transformation,
         *          such as frame rate conversion. In that case, presentation time corresponds
         *          to the actual output frame rendered.
         * @param nanoTime The system time when the frame was rendered.
         *
         * @see System#nanoTime
         */
        public void onFrameRendered(
                 SoftCodec codec, long presentationTimeUs, long nanoTime);
    }

    /**
     * Registers a callback to be invoked when an output frame is rendered on the output surface.
     * <p>
     * This method can be called in any codec state, but will only have an effect in the
     * Executing state for codecs that render buffers to the output surface.
     * <p>
     * <strong>Note:</strong> This callback is for informational purposes only: to get precise
     * render timing samples, and can be significantly delayed and batched. Some frames may have
     * been rendered even if there was no callback generated.
     *
     * @param listener the callback that will be run
     * @param handler the callback will be run on the handler's thread. If {@code null},
     *           the callback will be run on the default thread, which is the looper
     *           from which the codec was created, or a new thread if there was none.
     */
    public void setOnFrameRenderedListener(
             SoftCodec.OnFrameRenderedListener listener,  Handler handler) {
        synchronized (mListenerLock) {
            mOnFrameRenderedListener = listener;
            if (listener != null) {
                EventHandler newHandler = getEventHandlerOn(handler, mOnFrameRenderedHandler);
                if (newHandler != mOnFrameRenderedHandler) {
                    mOnFrameRenderedHandler.removeMessages(EVENT_FRAME_RENDERED);
                }
                mOnFrameRenderedHandler = newHandler;
            } else if (mOnFrameRenderedHandler != null) {
                mOnFrameRenderedHandler.removeMessages(EVENT_FRAME_RENDERED);
            }
            native_enableOnFrameRenderedListener(listener != null);
        }
    }

    private native void native_enableOnFrameRenderedListener(boolean enable);

    private EventHandler getEventHandlerOn(
             Handler handler,  EventHandler lastHandler) {
        if (handler == null) {
            return mEventHandler;
        } else {
            Looper looper = handler.getLooper();
            if (lastHandler.getLooper() == looper) {
                return lastHandler;
            } else {
                return new EventHandler(this, looper);
            }
        }
    }

    /**
     * SoftCodec callback interface. Used to notify the user asynchronously
     * of various SoftCodec events.
     */
    public static abstract class Callback {
        /**
         * Called when an input buffer becomes available.
         *
         * @param codec The SoftCodec object.
         * @param index The index of the available input buffer.
         */
        public abstract void onInputBufferAvailable( SoftCodec codec, int index);

        /**
         * Called when an output buffer becomes available.
         *
         * @param codec The SoftCodec object.
         * @param index The index of the available output buffer.
         * @param info Info regarding the available output buffer {@link SoftCodec.BufferInfo}.
         */
        public abstract void onOutputBufferAvailable(
                 SoftCodec codec, int index,  SoftCodec.BufferInfo info);

        /**
         * Called when the SoftCodec encountered an error
         *
         * @param codec The SoftCodec object.
         * @param e The {@link SoftCodec.CodecException} object describing the error.
         */
        public abstract void onError( SoftCodec codec,  SoftCodec.CodecException e);

        /**
         * Called when the output format has changed
         *
         * @param codec The SoftCodec object.
         * @param format The new output format.
         */
        public abstract void onOutputFormatChanged(
                 SoftCodec codec,  MediaFormat format);
    }

    private void postEventFromNative(
            int what, int arg1, int arg2,  Object obj) {
        synchronized (mListenerLock) {
            EventHandler handler = mEventHandler;
            if (what == EVENT_CALLBACK) {
                handler = mCallbackHandler;
            } else if (what == EVENT_FRAME_RENDERED) {
                handler = mOnFrameRenderedHandler;
            }
            if (handler != null) {
                Message msg = handler.obtainMessage(what, arg1, arg2, obj);
                handler.sendMessage(msg);
            }
        }
    }

    private native void setParameters(String[] keys, Object[] values);

    private native ByteBuffer getBuffer(boolean input, int index);

    private static native void native_init();

    private native void native_setup(
             String name, boolean nameIsType, boolean encoder);

    private native void native_finalize();

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("native-lib");
        native_init();
    }

    private long mNativeContext;

}
