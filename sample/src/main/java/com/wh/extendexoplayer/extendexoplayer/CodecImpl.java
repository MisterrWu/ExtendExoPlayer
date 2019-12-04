package com.wh.extendexoplayer.extendexoplayer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import com.wh.extendexoplayer.soft.SoftCodec;

import java.io.IOException;
import java.nio.ByteBuffer;

public class CodecImpl {

    private SoftCodec softCodec;
    private MediaCodec mediaCodec;

    private CodecImpl(String name,boolean useHard){
        if(useHard){
            try {
                mediaCodec = MediaCodec.createDecoderByType(name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            softCodec = SoftCodec.createByCodecName(name);
        }
    }

    public static CodecImpl createByCodecName( String name,boolean useHard){
        return new CodecImpl(name, useHard);
    }

    public void start() {
        if(softCodec != null){
            softCodec.start();
        } else if(mediaCodec != null){
            mediaCodec.start();
        }
    }

    public int dequeueInputBuffer(int timeoutUs) {
        if(softCodec != null){
            return softCodec.dequeueInputBuffer(timeoutUs);
        } else if(mediaCodec != null){
            return mediaCodec.dequeueInputBuffer(timeoutUs);
        }
        return -1;
    }

    public ByteBuffer getInputBuffer(int index) {
        if(softCodec != null){
            return softCodec.getInputBuffer(index);
        } else if(mediaCodec != null){
            return mediaCodec.getInputBuffer(index);
        }
        return null;
    }

    public void queueInputBuffer(int index, int offset, int size, long presentationTimeUs, int flags) {
        if(softCodec != null){
            softCodec.queueInputBuffer(index, offset, size, presentationTimeUs, flags);
        } else if(mediaCodec != null){
            mediaCodec.queueInputBuffer(index, offset, size, presentationTimeUs, flags);
        }
    }

    public int dequeueOutputBuffer(MediaCodec.BufferInfo info, long timeoutUs) {
        if(softCodec != null){
            return softCodec.dequeueOutputBuffer(info, timeoutUs);
        } else if(mediaCodec != null){
            return mediaCodec.dequeueOutputBuffer(info, timeoutUs);
        }
        return -1;
    }

    public MediaFormat getOutputFormat() {
        if(softCodec != null){
            return softCodec.getOutputFormat();
        } else if(mediaCodec != null){
            return mediaCodec.getOutputFormat();
        }
        return null;
    }

    public ByteBuffer getOutputBuffer(int index) {
        if(softCodec != null){
            return softCodec.getOutputBuffer(index);
        } else if(mediaCodec != null){
            return mediaCodec.getOutputBuffer(index);
        }
        return null;
    }

    public void release() {
        if(softCodec != null){
            softCodec.release();
        } else if(mediaCodec != null){
            mediaCodec.release();
        }
    }

    public void releaseOutputBuffer(int index, boolean render) {
        if(softCodec != null){
            softCodec.releaseOutputBuffer(index, render);
        } else if(mediaCodec != null){
            mediaCodec.releaseOutputBuffer(index, render);
        }
    }

    public void configure(MediaFormat format, Surface surface, int flags) {
        if(softCodec != null){
            softCodec.configure(format, surface, flags);
        } else if(mediaCodec != null){
            mediaCodec.configure(format, surface, null, flags);
        }
    }
}
