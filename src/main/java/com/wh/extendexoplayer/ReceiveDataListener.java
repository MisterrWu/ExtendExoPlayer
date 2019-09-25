package com.wh.extendexoplayer;

public interface ReceiveDataListener {

    void onReceiveVideoData(MediaInfo videoInfo);

    void onReceiveAudioData(MediaInfo audioInfo);
}
