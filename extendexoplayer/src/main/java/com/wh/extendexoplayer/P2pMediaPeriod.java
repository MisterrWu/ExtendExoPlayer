package com.wh.extendexoplayer;

import android.text.TextUtils;
import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.FormatHolder;
import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.decoder.DecoderInputBuffer;
import com.google.android.exoplayer2.source.MediaPeriod;
import com.google.android.exoplayer2.source.SampleStream;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

final class P2pMediaPeriod implements MediaPeriod, ReceiveDataListener {

    public static final String TAG = "P2pMediaPeriod ";

    public static final int DEF_LIVE_MAX_QUEUE_SIZE = 16;
    private final LinkedBlockingQueue<MediaInfo> mVideoQueue = new LinkedBlockingQueue<>(P2pMediaSource.MAX_QUEUE_SIZE);
    private final LinkedBlockingQueue<MediaInfo> mAudioQueue = new LinkedBlockingQueue<>(P2pMediaSource.MAX_QUEUE_SIZE);
    private List<MediaType> types = new ArrayList<>();
    private MediaUtil mediaUtil = new MediaUtil();
    private volatile int infoState = MediaInfo.STATE_NONE;
    private MediaInfoChangeListener infoChangeListener;

    P2pMediaPeriod(List<MediaType> types,MediaInfoChangeListener listener) {
        this.types.addAll(types);
        this.infoChangeListener = listener;
    }

    @Override
    public void prepare(Callback callback, long positionUs) {
        callback.onPrepared(this);
        Log.e(TAG, TAG + " prepare");
    }

    @Override
    public void maybeThrowPrepareError() throws IOException {
        Log.e(TAG, TAG + " maybeThrowPrepareError");
    }

    @Override
    public TrackGroupArray getTrackGroups() {
        List<Format> videoFormats = new ArrayList<>();
        List<Format> audioFormats = new ArrayList<>();

        for (int i = 0; i < types.size(); i++) {
            MediaType mediaType = types.get(i);
            if (mediaType == null) {
                throw new RuntimeException("mediaType is null");
            }
            if (mediaType.getType() == MediaType.TYPE_VIDEO) {
                Format format = Format.createVideoSampleFormat(String.valueOf(i), mediaType.getSampleMimeType(), null, Format.NO_VALUE,
                        Format.NO_VALUE, mediaType.getWidth(), mediaType.getHeight(), Format.NO_VALUE,
                        mediaType.getInitializationData(), Format.NO_VALUE, mediaType.getWidth() / mediaType.getHeight(), null);
                Log.e(TAG, TAG + " getTrackGroups: " + format.toString());
                videoFormats.add(format);
            } else if (mediaType.getType() == MediaType.TYPE_AUDIO) {
                Format format = Format.createAudioSampleFormat(String.valueOf(i), mediaType.getSampleMimeType(), null, Format.NO_VALUE,
                        Format.NO_VALUE, mediaType.getChannelCount(), mediaType.getSampleRate(), C.ENCODING_PCM_16BIT, mediaType.getInitializationData(), null, 0, "und");
                Log.e(TAG, TAG + " getTrackGroups: " + format.toString());
                audioFormats.add(format);
            } else {
                throw new RuntimeException("mediaType Unsupported types");
            }
        }
        TrackGroup videoGroup = new TrackGroup(videoFormats.toArray(new Format[0]));
        TrackGroup audioGroup = new TrackGroup(audioFormats.toArray(new Format[0]));
        return new TrackGroupArray(videoGroup, audioGroup);
    }

    @Override
    public long selectTracks(TrackSelection[] selections, boolean[] mayRetainStreamFlags, SampleStream[] streams, boolean[] streamResetFlags, long positionUs) {
        for (int i = 0; i < selections.length; i++) {
            if (streams[i] == null && selections[i] != null) {
                TrackSelection selection = selections[i];
                streams[i] = new SampleStreamImpl(selection.getTrackGroup());
                streamResetFlags[i] = true;
            }
        }
        Log.e(TAG, TAG + " selectTracks");
        return positionUs;
    }


    @Override
    public void discardBuffer(long positionUs, boolean toKeyframe) {
        Log.e(TAG, TAG + " discardBuffer positionUs: " + positionUs + ",toKeyframe: " + toKeyframe);
    }

    @Override
    public long readDiscontinuity() {
        return C.TIME_UNSET;
    }

    @Override
    public long seekToUs(long positionUs) {
        // 直播无法seek,直接返回 0
        return positionUs;
    }

    @Override
    public long getAdjustedSeekPositionUs(long positionUs, SeekParameters seekParameters) {
        // 直播无法seek,直接返回 0
        return positionUs;
    }

    @Override
    public long getBufferedPositionUs() {
        //Log.e(TAG, TAG + " getBufferedPositionUs");
        long largestQueuedTimestampUs = Long.MAX_VALUE;
        if(mAudioQueue.size() == 0 || mVideoQueue.size() == 0){
            return largestQueuedTimestampUs;
        }
        // Ignore non-AV tracks, which may be sparse or poorly interleaved.
        MediaInfo audioInfo = mAudioQueue.peek();
        Log.e(TAG, "getBufferedPositionUs audioInfo: " + audioInfo);
        if (audioInfo != null) {
            largestQueuedTimestampUs = Math.min(largestQueuedTimestampUs,
                    audioInfo.getTimeStamp() * 1000);
        }
        MediaInfo videoInfo = mVideoQueue.peek();
        Log.e(TAG, "getBufferedPositionUs videoInfo: " + videoInfo);
        if (videoInfo != null) {
            largestQueuedTimestampUs = Math.min(largestQueuedTimestampUs,
                    videoInfo.getTimeStamp() * 1000);
        }
        if (largestQueuedTimestampUs == Long.MAX_VALUE) {
            largestQueuedTimestampUs = C.TIME_END_OF_SOURCE;
        }
        Log.e(TAG, "getBufferedPositionUs largestQueuedTimestampUs: " + largestQueuedTimestampUs);
        return largestQueuedTimestampUs;
    }

    @Override
    public long getNextLoadPositionUs() {
        long loadPositionUs = getBufferedPositionUs();
        Log.e(TAG, TAG + " getNextLoadPositionUs loadPositionUs " + loadPositionUs);
        return loadPositionUs;
    }

    @Override
    public boolean continueLoading(long positionUs) {
        Log.e(TAG, TAG + "continueLoading");
        return true;
    }

    @Override
    public void reevaluateBuffer(long positionUs) {
        // Do nothing.
    }

    public void release() {
        clearDataQueue();
        Log.e(TAG, TAG + " release");
    }

    private void clearDataQueue() {
        Log.e(TAG, "clearDataQueue ");
        mVideoQueue.clear();
        mAudioQueue.clear();
    }

    @Override
    public void onReceiveVideoData(MediaInfo videoInfo) {
        Log.e(TAG, TAG + "readData ReceiveData onReceiveVideoData: " + mVideoQueue.size() + ",frmNo "+videoInfo.getNum());
        checkInfoStateAndClearQueue(videoInfo);
        checkVideoFrmNoAndClearQueue(videoInfo);
        checkLiveQueueSizeAndClearQueue();
        try {
            mVideoQueue.put(videoInfo);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceiveAudioData(MediaInfo audioInfo) {
        Log.e(TAG, TAG + "readData ReceiveData onReceiveAudioData: " + mAudioQueue.size());
        checkInfoStateAndClearQueue(audioInfo);
        try {
            mAudioQueue.put(audioInfo);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查直播跳转回看，或者回看跳转直播，需要清除数据队列保证画面快速切换。
     * @param info
     */
    private void checkInfoStateAndClearQueue(MediaInfo info) {
        int state = info.getInfoState();
        if (state != MediaInfo.STATE_NONE && infoState != state) {
            infoState = state;
            clearDataQueue();
        }
    }

    /**
     * 检查回看跳转到回看，需要清除数据队列保证画面快速切换。
     * @param videoInfo
     */
    private void checkVideoFrmNoAndClearQueue(MediaInfo videoInfo) {
        if(infoChangeListener != null) {
            if (infoChangeListener.checkVideoInfoAndClearQueue(videoInfo)) {
                clearDataQueue();
            }
        }
    }

    /**
     * 直播时检查队列中的数据个数不高于某个值，保证直播延时问题。
     * 队列中的数据也多，说明这个时候的视频延时就也高。
     * 网络抖动时，有时候没有接受到数据，有时候又突然接受到很多数据，导致后续播放产生延时。
     */
    private void checkLiveQueueSizeAndClearQueue() {
        if(infoState != MediaInfo.STATE_LIVE){
            return;
        }
        int maxQueueSize = infoChangeListener == null ? DEF_LIVE_MAX_QUEUE_SIZE:infoChangeListener.getLiveQueueMaxSize();
        if(maxQueueSize == mVideoQueue.size() || maxQueueSize == mAudioQueue.size()){
            clearDataQueue();
        }
    }

    private final class SampleStreamImpl implements SampleStream {

        private final TrackGroup mTrackGroup;

        SampleStreamImpl(TrackGroup trackGroup) {
            this.mTrackGroup = trackGroup;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void maybeThrowError() throws IOException {
            //Log.e(TAG, TAG + "maybeThrowError ");
        }

        @Override
        public int readData(FormatHolder formatHolder, DecoderInputBuffer buffer,
                            boolean formatRequired) {
            if (mTrackGroup == null || mTrackGroup.length <= 0
                    || mTrackGroup.getFormat(0) == null) {
                return C.RESULT_NOTHING_READ;
            }
            String mimeType = mTrackGroup.getFormat(0).sampleMimeType;
            Log.e(TAG, TAG + "readData mimeType: " + mimeType);
            if (TextUtils.isEmpty(mimeType)) {
                return C.RESULT_NOTHING_READ;
            }
            if (mimeType.contains(MimeTypes.BASE_TYPE_VIDEO)) {
                return readVideoData(formatHolder, buffer);
            } else if (mimeType.contains(MimeTypes.BASE_TYPE_AUDIO)) {
                return readAudioData(formatHolder, buffer);
            } else {
                return C.RESULT_NOTHING_READ;
            }
        }

        private int readVideoData(FormatHolder formatHolder, DecoderInputBuffer buffer) {
            Log.e(TAG, TAG + "readVideoData mVideoQueue: " + mVideoQueue.size() + ",mAudioQueue: " + mAudioQueue.size());
            if (mTrackGroup == null) {
                return C.RESULT_NOTHING_READ;
            }
            if (mVideoQueue.size() > 0) {
                MediaInfo videoInfo = mVideoQueue.peek();
                if (videoInfo != null) {
                    if (checkAndUpdateVideoFormat(formatHolder, videoInfo)) {
                        return C.RESULT_FORMAT_READ;
                    }
                    videoInfo = mVideoQueue.poll();
                    if(videoInfo == null ){
                        return C.RESULT_NOTHING_READ;
                    }
                    if (mediaUtil.isVideoInfoValid(videoInfo.isIFrame(), videoInfo.getNum(), videoInfo.getIFrameIndex())) {
                        byte[] data = videoInfo.getData();
                        buffer.addFlag(C.BUFFER_FLAG_KEY_FRAME);
                        buffer.timeUs = videoInfo.getTimeStamp() * 1000;
                        buffer.ensureSpaceForWrite(data.length);
                        buffer.data.put(data, 0, data.length);
                        Log.e(TAG, "readVideoData: TimeStampMS " + buffer.timeUs / 1000 + ",infoState " + videoInfo.getInfoState());
                        return C.RESULT_BUFFER_READ;
                    }
                }
            }
            return C.RESULT_NOTHING_READ;
        }

        private boolean checkAndUpdateVideoFormat(FormatHolder formatHolder, MediaInfo videoInfo) {
            Format oldFormat = formatHolder.format;
            if (oldFormat == null || isVideoInfoChange(oldFormat, videoInfo)) {
                Format newFormat = getVideoFormatByInfo(videoInfo);
                if (newFormat != null) {
                    formatHolder.format = newFormat;
                    return true;
                }
            }
            return false;
        }

        private boolean isVideoInfoChange(Format format, MediaInfo videoInfo) {
            return !Util.areEqual(format.sampleMimeType, videoInfo.getMimeType())
                    || format.width != videoInfo.getWidth()
                    || format.height != videoInfo.getHeight();
        }

        private Format getVideoFormatByInfo(MediaInfo videoInfo) {
            for (int i = 0; i < mTrackGroup.length; i++) {
                Format format = mTrackGroup.getFormat(i);
                if (format.height == videoInfo.getHeight()
                        && format.width == videoInfo.getWidth()
                        && Util.areEqual(format.sampleMimeType, videoInfo.getMimeType())) {
                    return format;
                }
            }
            return null;
        }

        private int readAudioData(FormatHolder formatHolder, DecoderInputBuffer buffer) {
            Log.e(TAG, TAG + "readAudioData mAudioQueue: " + mAudioQueue.size() + ",mVideoQueue: " + mVideoQueue.size());
            if (mTrackGroup == null) {
                return C.RESULT_NOTHING_READ;
            }
            if (mAudioQueue.size() > 0) {
                MediaInfo audioInfo = mAudioQueue.peek();
                if (checkAndUpdateAudioFormat(formatHolder, audioInfo)) {
                    return C.RESULT_FORMAT_READ;
                }
                /*if(mediaUtil.checkAudioTimeStampInvalid(audioInfo.getTimeStamp())){
                    return C.RESULT_NOTHING_READ;
                }*/
                audioInfo = mAudioQueue.poll();
                if (audioInfo != null) {
                    byte[] data = audioInfo.getData();
                    buffer.addFlag(C.BUFFER_FLAG_KEY_FRAME);
                    buffer.timeUs = audioInfo.getTimeStamp() * 1000;
                    buffer.ensureSpaceForWrite(data.length);
                    buffer.data.put(data, 0, data.length);
                    Log.e(TAG, "readAudioData: TimeStampMS " + buffer.timeUs / 1000);
                    return C.RESULT_BUFFER_READ;
                }
            }
            return C.RESULT_NOTHING_READ;
        }

        private boolean checkAndUpdateAudioFormat(FormatHolder formatHolder, MediaInfo audioInfo) {
            Format oldFormat = formatHolder.format;
            if (oldFormat == null || isAudioInfoChange(oldFormat, audioInfo)) {
                Format newFormat = getAudioFormatByInfo(audioInfo);
                if (newFormat != null) {
                    formatHolder.format = newFormat;
                    return true;
                }
            }
            return false;
        }

        private boolean isAudioInfoChange(Format format, MediaInfo audioInfo) {
            return !Util.areEqual(format.sampleMimeType, audioInfo.getMimeType())
                    || format.channelCount != audioInfo.getChannelCount()
                    || format.sampleRate != audioInfo.getSampleRate();
        }

        private Format getAudioFormatByInfo(MediaInfo audioInfo) {
            for (int i = 0; i < mTrackGroup.length; i++) {
                Format format = mTrackGroup.getFormat(i);
                if (format.channelCount == audioInfo.getChannelCount()
                        && format.sampleRate == audioInfo.getSampleRate()
                        && Util.areEqual(format.sampleMimeType, audioInfo.getMimeType())) {
                    return format;
                }
            }
            return null;
        }

        @Override
        public int skipData(long positionUs) {
            Log.e(TAG, "skipData: " + positionUs);
            return 0;
        }

    }

}
