//
// Created by wuhan on 2019-11-09.
//

#ifndef EXTENDEXOPLAYER_SOFTCODEC_H
#define EXTENDEXOPLAYER_SOFTCODEC_H

#define LOG_TAG "SoftCodec"

#include <vector>

#include <android/log.h>
#include "nativehelper/ALog-priv.h"
#include "utils/include/StrongPointer.h"
#include "FFmpegDecoder.h"
#include "media/include/MediaCodecBuffer.h"
#include "utils/include/List.h"
#include "utils/include/Mutex.h"

#define RESULT_OK 1
#define RESULT_FAIL -1

namespace android {

    class SoftCode {

    private:

        enum BufferFlags {
            BUFFER_FLAG_SYNCFRAME     = 1,
            BUFFER_FLAG_CODECCONFIG   = 2,
            BUFFER_FLAG_EOS           = 4,
            BUFFER_FLAG_PARTIAL_FRAME = 8,
            BUFFER_FLAG_MUXER_DATA    = 16,
        };

        enum {
            kPortIndexInput         = 0,
            kPortIndexOutput        = 1,
        };

        struct BufferInfo {
            BufferInfo();

            sp<MediaCodecBuffer> mData;
            bool mOwnedByClient;
        };

        void *mVideoData;
        List<size_t> mAvailPortBuffers[2];
        std::vector<BufferInfo> mPortBuffers[2];
        Mutex mBufferLock;

        FFmpegDecoder *mFFmpegDecoder;
        jobject mByteBuffer;
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

        int32_t getBuffer(jint i, sp <MediaCodecBuffer> *pSp, size_t portIndex);

        //template<typename T>
        int32_t createByteBufferFromABuffer(
                JNIEnv *env, bool readOnly, bool clearBuffer, void *videoData,
                jobject *buf);

        void cacheJavaObjects(JNIEnv *env);

        int32_t ConvertMessageToMap(
                JNIEnv *env, const sp<AMessage> &msg, jobject *map);

        void returnBuffersToCodecOnPort(int32_t portIndex);

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

        int dequeueInputBuffer(jlong timeoutUs);

        int queueInputBuffer(JNIEnv *env, jint index, jint offset, jint size, jlong timestampUs, jint flags,
                             char **errorDetailMsg);

        int flush();

        int stop();

        int start();

        int configure(jobjectArray keys, jobjectArray values, jobject surface, jint flags);

        int setCallback(jobject callback);

        int setSurface(jobject surface);

        void release();
    };
}
#endif //EXTENDEXOPLAYER_SOFTCODEC_H
