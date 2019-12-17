package com.wh.extendexoplayer.extendexoplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;

public class NativeTestActivity extends Activity {

    private static final int STATUS_STOP = 0x001;
    private static final int STATUS_START = 0x002;
    private static final String TAG = "NativeTest";

    private volatile int status = -1;
    private volatile boolean isEOS = false;
    private CodecImpl codec;
    private MediaExtractor extractor;
    private MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

    private static final String DATA = "data";

    private String videoPath;
    private boolean isLogArray = false;

    public static void start(Context context, String path) {
        Intent intent = new Intent(context, NativeTestActivity.class);
        intent.putExtra(DATA, path);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.native_test_activity);
        videoPath = getIntent().getStringExtra(DATA);
    }

    @Override
    protected void onStop() {
        super.onStop();
        status = STATUS_STOP;
    }

    public void test(View view) {
        Toast.makeText(this, "开始测试！", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            doInit();
            while (status == STATUS_START) {
                doWork();
            }
            doRelease();
        }).start();
    }

    private void doInit() {
        extractor = new MediaExtractor();
        try {
            extractor.setDataSource(videoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                extractor.selectTrack(i); // 选择视频数据
                try {
                    codec = CodecImpl.createByCodecName(mime,false);
                    codec.configure(format, null, 0);
                    codec.start();
                    Log.e(TAG, "start..... ");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        status = STATUS_START;
    }

    private void doWork() {
        if(codec == null){
            status = STATUS_STOP;
            return;
        }
        if (!isEOS) {
            int inIndex = codec.dequeueInputBuffer(10000);
            Log.e(TAG, "dequeueInputBuffer inIndex " + inIndex);
            if (inIndex >= 0) {
                ByteBuffer buffer = codec.getInputBuffer(inIndex);

                if(buffer != null) {
                    Log.e(TAG, "dequeueInputBuffer capacity " + buffer.capacity() + ",position "+buffer.limit());
                    int sampleSize = extractor.readSampleData(buffer, 0);
                    Log.e(TAG, "dequeueInputBuffer capacity " + buffer.capacity() + ",position "+buffer.limit());
                    if (sampleSize < 0) {
                        // We shouldn't stop the playback at this point, just pass the EOS
                        // flag to softCodec, we will get it again from the
                        // dequeueOutputBuffer
                        Log.e(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                        codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        isEOS = true;
                    } else {
                        codec.queueInputBuffer(inIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                        extractor.advance();
                    }
                }
            }
        }

        /*int outIndex = codec.dequeueOutputBuffer(info, 10000);
        switch (outIndex) {
            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                Log.e(TAG, "New format " + codec.getOutputFormat());
                break;
            case MediaCodec.INFO_TRY_AGAIN_LATER:
                Log.e(TAG, "dequeueOutputBuffer timed out!");
                break;
            default:
                ByteBuffer buffer = codec.getOutputBuffer(outIndex);
                Log.v(TAG, "We can't use this buffer but render it due to the API limit, " + buffer);

                // We use a very simple clock to keep the video FPS, or the video
                // playback will be too fast
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
                //将缓冲区中数据渲染到surface,render=true就会渲染到surface
                codec.releaseOutputBuffer(outIndex, true);
                break;
        }*/

        // All decoded frames have been rendered, we can stop playing now
        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            Log.e(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
            status = STATUS_STOP;
        }
    }

    private void logArray(byte[] data) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < data.length; i++) {
            builder.append(data[i]).append(i != data.length -1 ? ",":"");
        }
        builder.append("]");
        Log.e("SoftCodec", "logArray: " + builder.toString());
    }

    private void doRelease() {
        Log.e(TAG, "doRelease: " + codec);
        if (codec != null) {
            codec.release();
        }
        if (extractor != null) {
            extractor.release();
        }
    }
}
