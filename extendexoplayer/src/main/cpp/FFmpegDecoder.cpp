//
// Created by wuhan on 2019-12-01.
//


#include "FFmpegDecoder.h"

#define LOG_TAG "SoftCodec"

namespace android {

    static AString getErrorType(int err) {
        switch (err) {
            case AVERROR_EOF:
                return "AVERROR_EOF";
            case AVERROR_BSF_NOT_FOUND:
                return "AVERROR_BSF_NOT_FOUND";
            case AVERROR_BUG:
                return "AVERROR_BUG";
            case AVERROR_BUFFER_TOO_SMALL:
                return "AVERROR_BUFFER_TOO_SMALL";
            case AVERROR_DECODER_NOT_FOUND:
                return "AVERROR_DECODER_NOT_FOUND";
            case AVERROR_DEMUXER_NOT_FOUND:
                return "AVERROR_DEMUXER_NOT_FOUND";
            case AVERROR_ENCODER_NOT_FOUND:
                return "AVERROR_ENCODER_NOT_FOUND";
            case AVERROR_EXIT:
                return "AVERROR_EXIT";
            case AVERROR_EXTERNAL:
                return "AVERROR_EXTERNAL";
            case AVERROR_FILTER_NOT_FOUND:
                return "AVERROR_FILTER_NOT_FOUND";
            case AVERROR_INVALIDDATA:
                return "AVERROR_INVALIDDATA";
            case AVERROR_MUXER_NOT_FOUND:
                return "AVERROR_MUXER_NOT_FOUND";
            case AVERROR_OPTION_NOT_FOUND:
                return "AVERROR_OPTION_NOT_FOUND";
            case AVERROR_PATCHWELCOME:
                return "AVERROR_PATCHWELCOME";
            case AVERROR_PROTOCOL_NOT_FOUND:
                return "AVERROR_PROTOCOL_NOT_FOUND";
            case AVERROR_BUG2:
                return "AVERROR_BUG2";
            case AVERROR_UNKNOWN:
                return "AVERROR_UNKNOWN";
            case AVERROR_EXPERIMENTAL:
                return "AVERROR_EXPERIMENTAL";
            case AVERROR_INPUT_CHANGED:
                return "AVERROR_INPUT_CHANGED";
            case AVERROR_OUTPUT_CHANGED:
                return "AVERROR_OUTPUT_CHANGED";
            case AVERROR_HTTP_BAD_REQUEST:
                return "AVERROR_HTTP_BAD_REQUEST";
            case AVERROR_HTTP_UNAUTHORIZED:
                return "AVERROR_HTTP_UNAUTHORIZED";
            case AVERROR_HTTP_FORBIDDEN:
                return "AVERROR_HTTP_FORBIDDEN";
            case AVERROR_HTTP_OTHER_4XX:
                return "AVERROR_HTTP_OTHER_4XX";
            case AVERROR_HTTP_SERVER_ERROR:
                return "AVERROR_HTTP_SERVER_ERROR";
            case AV_ERROR_MAX_STRING_SIZE:
                return "AV_ERROR_MAX_STRING_SIZE";
            default:
                return "AVERROR_UNKNOWN";
        }
    }

    static void ffmpegLogCallback(void *ptr, int level, const char *fmt, va_list vl) {
        AString msgFmt = "FFmpeg_Log ";
        msgFmt.append(fmt);
        ALOGE(msgFmt.c_str(), vl);
    }

    FFmpegDecoder::FFmpegDecoder(const char *name, bool nameIsType, bool encoder) {
        mCodecName = name;
        mNameIsType = nameIsType;
        mEncoder = encoder;
    }

    int FFmpegDecoder::start() {
        if (mCodecName.endsWith("hevc")) {
            mCodec = avcodec_find_decoder(AV_CODEC_ID_HEVC);
        } else {
            mCodec = avcodec_find_decoder(AV_CODEC_ID_H264);
        }
        CHECK(mCodec != nullptr);
        mCodecCtx = avcodec_alloc_context3(mCodec);
        CHECK(mCodecCtx != nullptr);
        /*mCodecCtx->width = 640;
        mCodecCtx->height = 360;
        mCodecCtx->sample_rate = 21;
        mCodecCtx->level = 256;*/
        mCodecCtx->flags |= AV_CODEC_FLAG_TRUNCATED;
        int ret = avcodec_open2(mCodecCtx, mCodec, NULL);
        CHECK(ret >= 0);
        mParser = av_parser_init(mCodec->id);
        mParser->flags |= PARSER_FLAG_ONCE;//在打开解码器后初始化parase
        CHECK(mParser != nullptr);
        ALOGE("start codec:%s, ret:%d", mCodec->name, ret);
        av_log_set_level(AV_LOG_DEBUG);
        av_log_set_callback(ffmpegLogCallback);
        return 1;
    }

    int FFmpegDecoder::decode(uint8_t *data, size_t size, ino64_t timestampUs, int32_t flags) {
        ALOGE("decode......... %d \n", (int)size);
        isDecode = true;
        CHECK(data != nullptr);
        CHECK(mCodecCtx != nullptr);

        AVPacket *avpkt = av_packet_alloc();
        avpkt->pos = timestampUs;

        int in_len = (int) size;
        while (in_len) {
            int len = av_parser_parse2(mParser, mCodecCtx, &avpkt->data, &avpkt->size,
                                       data, in_len, avpkt->pts, avpkt->dts, avpkt->pos);
            ALOGE("av_parser_parse2 len %d \n", len);
            data += len;
            in_len -= len;
            ALOGE("av_parser_parse2 success size:%d, pts:%d, dts:%d, pos:%d \n", avpkt->size,
                  (int)avpkt->pts, (int)avpkt->dts, (int)avpkt->pos);
            if (avpkt->size)
                decode(avpkt);
        }
        return 1;
    }

/*{
    track-
    id = 1, file
    -
    format = video / mp4, level = 256, mime = video / avc, profile = 2, language =, display
    -
    width = 640, csd
    -1=java.nio.HeapByteBuffer[
    pos = 0
    lim = 8
    cap = 8
    ],
    durationUs = 9110000, display
    -
    height = 360, width = 640, rotation
    -
    degrees = 0, max
    -input-
    size = 230400, frame
    -
    rate = 21, height = 360, csd
    -0=java.nio.HeapByteBuffer[
    pos = 0
    lim = 16
    cap = 16
    ]
}*/

/*{
    track-
    id = 1, file
    -
    format = video / mp4, level = 256, mime = video / avc, profile = 2, language =, display
    -
    width = 640, csd
    -1=java.nio.HeapByteBuffer[
    pos = 0
    lim = 8
    cap = 8
    ],
    durationUs = 11225000, display
    -
    height = 360, width = 640, rotation
    -
    degrees = 0, max
    -input-
    size = 230400, frame
    -
    rate = 16, height = 360, csd
    -0=java.nio.HeapByteBuffer[
    pos = 0
    lim = 16
    cap = 16
    ]
}*/

/*{
    track-
    id = 1, file
    -
    format = video / mp4, level = 2048, mime = video / avc, profile = 2, language =, display
    -
    width = 640, csd
    -1=java.nio.HeapByteBuffer[
    pos = 0
    lim = 8
    cap = 8
    ],
    durationUs = 12051000, display
    -
    height = 360, width = 640, rotation
    -
    degrees = 0, max
    -input-
    size = 230400, frame
    -
    rate = 20, height = 360, csd
    -0=java.nio.HeapByteBuffer[
    pos = 0
    lim = 16
    cap = 16
    ]
}*/

    int FFmpegDecoder::decode(AVPacket *pPacket) {
        AVFrame *frame = av_frame_alloc();
        ALOGE("av_frame_alloc\n");
        if (!frame) {
            ALOGE("av_frame_alloc fail\n");
            return -1;
        }
        ALOGE("avcodec_send_packet %d \n", pPacket->size);
        int err = avcodec_send_packet(mCodecCtx, pPacket);
        if (err != 0) {
            ALOGE("avcodec_send_packet fail %s \n", getErrorType(err).c_str());
            return -1;
        }
        ALOGE("avcodec_send_packet success\n");
        if (avcodec_receive_frame(mCodecCtx, frame)) {
            ALOGE("avcodec_receive_frame fail\n");
            return -1;
        }
        ALOGE("avcodec_receive_frame success\n");

        av_frame_free(&frame);

        return 1;
    }

    int FFmpegDecoder::stop() {
        if (mCodecCtx) {
            avcodec_close(mCodecCtx);
            av_free(mCodecCtx);
        }
        if (mParser) {
            av_parser_close(mParser);
        }
        return 1;
    }
}