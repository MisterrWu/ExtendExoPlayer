package com.wh.extendexoplayer;

public interface MediaInfoChangeListener {

    default boolean checkVideoInfoAndClearQueue(MediaInfo info){return false;}

    default int getLiveQueueMaxSize(){return P2pMediaPeriod.DEF_LIVE_MAX_QUEUE_SIZE;}
}
