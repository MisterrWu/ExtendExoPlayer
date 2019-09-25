package com.wh.extendexoplayer;

public interface MediaInfoChangeListener {

    boolean checkVideoInfoAndClearQueue(MediaInfo info);

    int getLiveQueueMaxSize();
}
