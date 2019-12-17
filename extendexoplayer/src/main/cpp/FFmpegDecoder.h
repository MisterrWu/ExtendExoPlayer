//
// Created by wuhan on 2019-12-01.
//

#ifndef EXTENDEXOPLAYER_FFMPEGDECODER_H
#define EXTENDEXOPLAYER_FFMPEGDECODER_H

#include "nativehelper/ALog-priv.h"
#include "foundation/include/AString.h"

extern "C" {
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include <libavutil/frame.h>
};

namespace android {
    class FFmpegDecoder {

    private:
        AString mCodecName;
        bool mNameIsType;
        bool mEncoder;
        AVCodec* mCodec;
        AVCodecContext* mCodecCtx;

        int decode(AVPacket *pPacket);

    public:
        FFmpegDecoder(const char *name, bool nameIsType, bool encoder);

        int start();

        int decode(uint8_t *data, size_t size, ino64_t timestampUs, int32_t flags);

        int stop();
    };
}

#endif //EXTENDEXOPLAYER_FFMPEGDECODER_H
