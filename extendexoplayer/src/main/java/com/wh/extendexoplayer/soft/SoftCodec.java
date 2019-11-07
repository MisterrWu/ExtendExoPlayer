package com.wh.extendexoplayer.soft;

public final class SoftCodec {

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("native-lib");
    }

    public final native String urlprotocolinfo();
    public final native String avformatinfo();
    public final native String avcodecinfo();
    public final native String avfilterinfo();
}
