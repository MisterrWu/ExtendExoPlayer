//
// Created by wuhan on 2019-12-01.
//


#include "FFmpegDecoder.h"

#define LOG_TAG "SoftCodec"

namespace android {

    static bool isLogArray = false;

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

    static void logArray(uint8_t *data, size_t size) {
        if (isLogArray) {
            return;
        }
        isLogArray = true;
        AString builder = "data: ";
        builder.append("[");
        for (int i = 0; i < size; i++) {
            builder.append((int) data[i]);
            builder.append(i != size - 1 ? "," : "");
        }
        builder.append("]");
        ALOGE(builder.c_str(), "");
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
        mCodecCtx->width = 640;
        mCodecCtx->height = 360;
        mCodecCtx->sample_rate = 21;
        mCodecCtx->level = 256;
        mCodecCtx->profile = 2;
        mCodecCtx->flags |= AV_CODEC_FLAG_GLOBAL_HEADER;
        mCodecCtx->extradata = new uint8_t[24]{0, 0, 0, 1, 103, 77, 64, 30, 151, 64, 80, 23, 252, 179, 96, 32, 0,
                                0, 0, 1, 104, 238, 60, 128};
        mCodecCtx->extradata_size = 24;
        int ret = avcodec_open2(mCodecCtx, mCodec, NULL);
        CHECK(ret >= 0);
        ALOGE("start codec:%s, ret:%d", mCodec->name, ret);
        av_log_set_level(AV_LOG_DEBUG);
        av_log_set_callback(ffmpegLogCallback);
        isLogArray = false;
        return 1;
    }

    int FFmpegDecoder::decode(uint8_t *data, size_t size, ino64_t timestampUs, int32_t flags) {
        ALOGE("decode......... %d \n", (int) size);
        CHECK(data != nullptr);
        CHECK(mCodecCtx != nullptr);
        logArray(data, size);
        AVPacket *avpkt = av_packet_alloc();
        avpkt->pos = timestampUs;
        avpkt->data = data;
        avpkt->size = (int) size;
        decode(avpkt);
        return 1;
    }

    int FFmpegDecoder::decode(AVPacket *pPacket) {
        AVFrame *frame = av_frame_alloc();
        ALOGE("av_frame_alloc %d\n", (pPacket->data[4] & 0x1f));
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
        return 1;
    }
}