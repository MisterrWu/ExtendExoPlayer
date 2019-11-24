//
// Created by wuhan on 2019-11-09.
//

#ifndef EXTENDEXOPLAYER_SOFTCODEC_H
#define EXTENDEXOPLAYER_SOFTCODEC_H

#define LOG_TAG "SoftCodec"

#include <android/log.h>
#include "nativehelper/ALog-priv.h"
#include "utils/include/StrongPointer.h"
#include <MediaCodecBuffer.h>
#include <AMessage.h>
#include <ABuffer.h>

#define RESULT_OK 1
#define RESULT_FAIL -1

namespace android {

    class SoftCode {

    private:

        jclass mClass;
        jweak mObject;
        jobject mSurfaceTextureClient;

        // java objects cached
        jclass mByteBufferClass;
        jobject mNativeByteOrderObj;
        jmethodID mByteBufferOrderMethodID;
        jmethodID mByteBufferPositionMethodID;
        jmethodID mByteBufferLimitMethodID;
        jmethodID mByteBufferAsReadOnlyBufferMethodID;

        int32_t getInputBuffer(jint i, sp <MediaCodecBuffer> *pSp);

        int32_t getOutputBuffer(jint i, sp <MediaCodecBuffer> *pSp);

        template<typename T>
        int32_t createByteBufferFromABuffer(
                JNIEnv *env, bool readOnly, bool clearBuffer, const sp <T> &buffer,
                jobject *buf) const;

        void cacheJavaObjects(JNIEnv *env);

        int32_t ConvertMessageToMap(
                JNIEnv *env, const sp<AMessage> &msg, jobject *map);

    public:
        SoftCode(
                JNIEnv *env, jobject thiz,
                const char *name, bool nameIsType, bool encoder);

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

        struct BufferInfo {
            BufferInfo();

            sp <MediaCodecBuffer> mData;
            bool mOwnedByClient;
        };
    };
}
#endif //EXTENDEXOPLAYER_SOFTCODEC_H
