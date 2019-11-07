#include <jni.h>
#include <string>

extern "C" {
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavfilter/avfilter.h>
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_wh_extendexoplayer_soft_SoftCodec_urlprotocolinfo(JNIEnv *env, jobject instance) {
    char info[40000] = {0};
    struct URLProtocol *pup = NULL;
    struct URLProtocol **p_temp = &pup;
    avio_enum_protocols((void **) p_temp, 0);
    while ((*p_temp) != NULL) {
        sprintf(info, "%sInput: %s ", info, avio_enum_protocols((void **) p_temp, 0));
    }
    pup = NULL;
    avio_enum_protocols((void **) p_temp, 1);
    while ((*p_temp) != NULL) {
        sprintf(info, "%sInput: %s ", info, avio_enum_protocols((void **) p_temp, 1));
    }

    return env->NewStringUTF(info);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_wh_extendexoplayer_soft_SoftCodec_avformatinfo(JNIEnv *env, jobject instance) {

    char info[40000] = {0};

    void *opaque = NULL;
    const AVInputFormat *iformat;
    while ((iformat = av_demuxer_iterate(&opaque))) {
        sprintf(info, "%sInput: %s ", info, iformat->name);
    }
    void *j = NULL;
    const AVOutputFormat *oformat;
    while ((oformat = av_muxer_iterate(&j))) {
        sprintf(info, "%sOutput: %s ", info, oformat->name);
    }
    return env->NewStringUTF(info);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_wh_extendexoplayer_soft_SoftCodec_avcodecinfo(JNIEnv *env, jobject instance) {
    char info[40000] = {0};

    const AVCodec *codec;
    void *opaque = NULL;
    while ((codec = av_codec_iterate(&opaque))) {
        if (codec->decode != NULL) {
            sprintf(info, "%sdecode:", info);
        } else {
            sprintf(info, "%sencode:", info);
        }
        switch (codec->type) {
            case AVMEDIA_TYPE_VIDEO:
                sprintf(info, "%s(video):", info);
                break;
            case AVMEDIA_TYPE_AUDIO:
                sprintf(info, "%s(audio):", info);
                break;
            default:
                sprintf(info, "%s(other):", info);
                break;
        }
        sprintf(info, "%s[%10s] ", info, codec->name);
    }

    return env->NewStringUTF(info);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_wh_extendexoplayer_soft_SoftCodec_avfilterinfo(JNIEnv *env, jobject instance) {
    char info[40000] = {0};
    void *opaque = NULL;
    const AVFilter *filter;
    while ((filter = av_filter_iterate(&opaque))){
        sprintf(info, "%s%s ", info, filter->name);
    }
    return env->NewStringUTF(info);
}