//
// Created by wuhan on 2019-11-09.
//

#ifndef EXTENDEXOPLAYER_SOFTCODEC_H
#define EXTENDEXOPLAYER_SOFTCODEC_H

#define LOG_TAG "SoftCodec"
#include "nativehelper/ALog-priv.h"

#define RESULT_OK 1
#define RESULT_FAIL -1

class SoftCode{

private:
    int getInputBuffer();
    int getOutputBuffer();

public:
    int getBuffer(JNIEnv *env, jboolean input, jint index, jobject *buffer);

    int setParameters(JNIEnv *env, jobjectArray keys, jobjectArray values);

    int enableOnFrameRenderedListener(jboolean enable);

    void setVideoScalingMode(jint mode);

    int getFormat(JNIEnv *env, jboolean input, jobject *format);

    int releaseOutputBuffer(jint index, jboolean render, jboolean updatePTS, jlong timeNs);

    int dequeueOutputBuffer(JNIEnv *env, jobject bufferInfo, size_t *index, jlong timeoutUS);

    int dequeueInputBuffer(size_t *index, jlong timeoutUs);

    int queueInputBuffer(jint index, jint offset, jint size, jlong timestampUs, jint flags,
                         char **errorDetailMsg);

    int flush();

    int stop();

    int start();

    int configure(jobjectArray keys, jobjectArray values, jobject surface, jint flags);

    int setCallback(jobject callback);

    int setSurface(jobject surface);

    void release();
};

#endif //EXTENDEXOPLAYER_SOFTCODEC_H
