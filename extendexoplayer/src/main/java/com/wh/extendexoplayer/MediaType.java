package com.wh.extendexoplayer;

import java.util.ArrayList;
import java.util.List;

public final class MediaType {

    public static final int TYPE_VIDEO = 0x001;
    public static final int TYPE_AUDIO = 0x002;

    private List<byte[]> initializationData = new ArrayList<>();
    private int type;
    private int width;
    private int height;
    private int channelCount;
    private int sampleRate;
    private String sampleMimeType;

    private MediaType(int type) {
        this.type = type;
    }

    public void addCSD(byte[] bytes) {
        this.initializationData.add(bytes);
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setChannelCount(int channelCount) {
        this.channelCount = channelCount;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void setSampleMimeType(String sampleMimeType) {
        this.sampleMimeType = sampleMimeType;
    }

    public List<byte[]> getInitializationData() {
        return initializationData;
    }

    public int getType() {
        return type;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getChannelCount() {
        return channelCount;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public String getSampleMimeType() {
        return sampleMimeType;
    }

    @Override
    public String toString() {
        return "MediaType["
                + "type : " + type + ","
                + "width : " + width + ","
                + "height : " + height + ","
                + "channelCount : " + channelCount + ","
                + "sampleRate : " + sampleRate + ","
                + "mimeType : " + sampleMimeType
                + "]";
    }

    public static MediaType newVideoType(String mimeType, int videoWidth, int videoHeight) {
        MediaType mediaType = new MediaType(TYPE_VIDEO);
        mediaType.setSampleMimeType(mimeType);
        mediaType.setWidth(videoWidth);
        mediaType.setHeight(videoHeight);
        return mediaType;
    }

    public static MediaType newAudioType(String mimeType, int channelCount, int sampleRate, byte[] csd) {
        MediaType mediaType = new MediaType(TYPE_AUDIO);
        mediaType.setSampleMimeType(mimeType);
        mediaType.setChannelCount(channelCount);
        mediaType.setSampleRate(sampleRate);
        mediaType.addCSD(csd);
        return mediaType;
    }
}
