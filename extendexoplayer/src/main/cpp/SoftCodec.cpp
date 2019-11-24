//
// Created by wuhan on 2019-11-09.
//

#include <jni.h>
#include "SoftCodec.h"
#include "nativehelper/ScopedLocalRef.h"

namespace android {

    ////////////////////////////////////////////////////////////////////////////////

    SoftCode::BufferInfo::BufferInfo() : mOwnedByClient(false) {}

    ////////////////////////////////////////////////////////////////////////////////

    SoftCode::SoftCode(JNIEnv *env, jobject thiz, const char *name, bool nameIsType, bool encoder)
            : mClass(nullptr),
              mObject(nullptr) {
        jclass clazz = env->GetObjectClass(thiz);
        CHECK(clazz != nullptr);

        mClass = (jclass) env->NewGlobalRef(clazz);
        mObject = env->NewWeakGlobalRef(thiz);

        cacheJavaObjects(env);
    }

    void SoftCode::cacheJavaObjects(JNIEnv *env) {
        auto *clazz = (jclass) env->FindClass("java/nio/ByteBuffer");
        mByteBufferClass = (jclass) env->NewGlobalRef(clazz);
        CHECK(mByteBufferClass != nullptr);

        ScopedLocalRef<jclass> byteOrderClass(
                env, env->FindClass("java/nio/ByteOrder"));
        CHECK(byteOrderClass.get() != nullptr);

        jmethodID nativeOrderID = env->GetStaticMethodID(
                byteOrderClass.get(), "nativeOrder", "()Ljava/nio/ByteOrder;");
        CHECK(nativeOrderID != nullptr);

        jobject nativeByteOrderObj =
                env->CallStaticObjectMethod(byteOrderClass.get(), nativeOrderID);
        mNativeByteOrderObj = env->NewGlobalRef(nativeByteOrderObj);
        CHECK(mNativeByteOrderObj != nullptr);
        env->DeleteLocalRef(nativeByteOrderObj);
        nativeByteOrderObj = nullptr;

        mByteBufferOrderMethodID = env->GetMethodID(
                mByteBufferClass,
                "order",
                "(Ljava/nio/ByteOrder;)Ljava/nio/ByteBuffer;");
        CHECK(mByteBufferOrderMethodID != nullptr);

        mByteBufferAsReadOnlyBufferMethodID = env->GetMethodID(
                mByteBufferClass, "asReadOnlyBuffer", "()Ljava/nio/ByteBuffer;");
        CHECK(mByteBufferAsReadOnlyBufferMethodID != nullptr);

        mByteBufferPositionMethodID = env->GetMethodID(
                mByteBufferClass, "position", "(I)Ljava/nio/Buffer;");
        CHECK(mByteBufferPositionMethodID != nullptr);

        mByteBufferLimitMethodID = env->GetMethodID(
                mByteBufferClass, "limit", "(I)Ljava/nio/Buffer;");
        CHECK(mByteBufferLimitMethodID != nullptr);
    }

    int32_t SoftCode::getInputBuffer(jint index, sp<MediaCodecBuffer> *buffer) { return RESULT_OK;}

    int32_t SoftCode::getOutputBuffer(jint index, sp<MediaCodecBuffer> *buffer) { return RESULT_OK;}

    // static
    template<typename T>
    int32_t SoftCode::createByteBufferFromABuffer(
            JNIEnv *env, bool readOnly, bool clearBuffer, const sp<T> &buffer,
            jobject *buf) const {
        // if this is an ABuffer that doesn't actually hold any accessible memory,
        // use a null ByteBuffer
        *buf = nullptr;

        if (buffer == nullptr) {
            return RESULT_OK;
        }

        if (buffer->base() == nullptr) {
            return RESULT_OK;
        }

        jobject byteBuffer =
                env->NewDirectByteBuffer(buffer->base(), buffer->capacity());
        if (readOnly && byteBuffer != nullptr) {
            jobject readOnlyBuffer = env->CallObjectMethod(
                    byteBuffer, mByteBufferAsReadOnlyBufferMethodID);
            env->DeleteLocalRef(byteBuffer);
            byteBuffer = readOnlyBuffer;
        }
        if (byteBuffer == nullptr) {
            return RESULT_FAIL;
        }
        jobject me = env->CallObjectMethod(
                byteBuffer, mByteBufferOrderMethodID, mNativeByteOrderObj);
        env->DeleteLocalRef(me);
        me = env->CallObjectMethod(
                byteBuffer, mByteBufferLimitMethodID,
                clearBuffer ? buffer->capacity() : (buffer->offset() + buffer->size()));
        env->DeleteLocalRef(me);
        me = env->CallObjectMethod(
                byteBuffer, mByteBufferPositionMethodID,
                clearBuffer ? 0 : buffer->offset());
        env->DeleteLocalRef(me);
        me = nullptr;

        *buf = byteBuffer;
        return RESULT_OK;
    }

    int SoftCode::getBuffer(JNIEnv *env, jboolean input, jint index, jobject *buf) {
        sp<MediaCodecBuffer> buffer;

        status_t err =
                input
                ? getInputBuffer(index, &buffer)
                : getOutputBuffer(index, &buffer);

        if (err != OK) {
            return err;
        }
        return createByteBufferFromABuffer(
                env, !input /* readOnly */, input /* clearBuffer */, buffer, buf);
    }

    int SoftCode::setParameters(JNIEnv *env, jobjectArray keys, jobjectArray values) {
        return 0;
    }

    int SoftCode::enableOnFrameRenderedListener(jboolean enable) {
        return 0;
    }

    void SoftCode::setVideoScalingMode(jint mode) {
    }

    int SoftCode::getFormat(JNIEnv *env, jboolean input, jobject *format) {
        return 0;
    }

    int
    SoftCode::releaseOutputBuffer(jint index, jboolean render, jboolean updatePTS, jlong timeNs) {
        return 0;
    }

    int
    SoftCode::dequeueOutputBuffer(JNIEnv *env, jobject bufferInfo, size_t *index, jlong timeoutUS) {
        return 0;
    }

    int SoftCode::dequeueInputBuffer(size_t *index, jlong timeoutUs) {
        return 0;
    }

    int
    SoftCode::queueInputBuffer(jint index, jint offset, jint size, jlong timestampUs, jint flags,
                               char **errorDetailMsg) {
        return 0;
    }

    int SoftCode::flush() {
        return 0;
    }

    int SoftCode::stop() {
        return 0;
    }

    int SoftCode::start() {
        return 0;
    }

    int SoftCode::configure(jobjectArray keys, jobjectArray values, jobject surface, jint flags) {
        return 0;
    }

    int SoftCode::setCallback(jobject callback) {
        return 0;
    }

    int SoftCode::setSurface(jobject surface) {
        return 0;
    }

    void SoftCode::release() {

    }
}