package com.wh.extendexoplayer;

public final class MediaInfo {

    public static final int STATE_NONE = -1;
    public static final int STATE_LIVE = 0;
    public static final int STATE_RECORD = 1;

    private String mimeType;
    private byte[] data;
    private long num;
    private int width;
    private int height;
    private long timeStamp;//毫秒
    private boolean isIFrame;
    private int channelCount;
    private int sampleRate;
    private int iFrameIndex;
    private int infoState = STATE_NONE;//是否直播流

    public int getInfoState() {
        return infoState;
    }

    public void setInfoState(int state) {
        infoState = state;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public long getNum() {
        return num;
    }

    public void setNum(long num) {
        this.num = num;
    }

    public int getIFrameIndex() {
        return iFrameIndex;
    }

    public void setIFrameIndex(int iFrameIndex) {
        this.iFrameIndex = iFrameIndex;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isIFrame() {
        return isIFrame;
    }

    public void setIFrame(boolean IFrame) {
        isIFrame = IFrame;
    }

    public int getChannelCount() {
        return channelCount;
    }

    public void setChannelCount(int channelCount) {
        this.channelCount = channelCount;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    @Override
    public String toString() {
        return "MediaInfo["
                + "mimeType : " + mimeType + ","
                + "num : " + num + ","
                + "width : " + width + ","
                + "height : " + height + ","
                + "timeStamp : " + timeStamp + ","
                + "isIFrame : " + isIFrame + ","
                + "channelCount : " + channelCount + ","
                + "sampleRate : " + sampleRate
                + "]";
    }

    public static MediaInfo newVideoInfo(String mimeType, byte[] data, long num, long timeStamp, int videoWidth, int videoHeight, boolean isIFrame, int state){
        MediaInfo info = new MediaInfo();
        info.setMimeType(mimeType);
        info.setData(data);
        info.setNum(num);
        info.setTimeStamp(timeStamp);
        info.setWidth(videoWidth);
        info.setHeight(videoHeight);
        info.setIFrame(isIFrame);
        info.setInfoState(state);
        return info;
    }

    public static MediaInfo newAudioInfo(String mimeType, byte[] data, long timeStamp, int channelCount, int sampleRate){
        MediaInfo info = new MediaInfo();
        info.setMimeType(mimeType);
        info.setData(data);
        info.setTimeStamp(timeStamp);
        info.setChannelCount(channelCount);
        info.setSampleRate(sampleRate);
        return info;
    }
}
