//
// Created by wuhan on 2019-11-09.
//

#include <jni.h>
#include "SoftCodec.h"
#include "nativehelper/ScopedLocalRef.h"

namespace android {

    static jobject makeIntegerObject(JNIEnv *env, int32_t value) {
        ScopedLocalRef<jclass> clazz(env, env->FindClass("java/lang/Integer"));
        CHECK(clazz.get() != NULL);

        jmethodID integerConstructID =
                env->GetMethodID(clazz.get(), "<init>", "(I)V");
        CHECK(integerConstructID != NULL);

        return env->NewObject(clazz.get(), integerConstructID, value);
    }

    static jobject makeLongObject(JNIEnv *env, int64_t value) {
        ScopedLocalRef<jclass> clazz(env, env->FindClass("java/lang/Long"));
        CHECK(clazz.get() != NULL);

        jmethodID longConstructID = env->GetMethodID(clazz.get(), "<init>", "(J)V");
        CHECK(longConstructID != NULL);

        return env->NewObject(clazz.get(), longConstructID, value);
    }

    static jobject makeFloatObject(JNIEnv *env, float value) {
        ScopedLocalRef<jclass> clazz(env, env->FindClass("java/lang/Float"));
        CHECK(clazz.get() != NULL);

        jmethodID floatConstructID =
                env->GetMethodID(clazz.get(), "<init>", "(F)V");
        CHECK(floatConstructID != NULL);

        return env->NewObject(clazz.get(), floatConstructID, value);
    }

    static jobject makeByteBufferObject(
            JNIEnv *env, const void *data, size_t size) {
        jbyteArray byteArrayObj = env->NewByteArray(size);
        env->SetByteArrayRegion(byteArrayObj, 0, size, (const jbyte *) data);

        ScopedLocalRef<jclass> clazz(env, env->FindClass("java/nio/ByteBuffer"));
        CHECK(clazz.get() != NULL);

        jmethodID byteBufWrapID =
                env->GetStaticMethodID(
                        clazz.get(), "wrap", "([B)Ljava/nio/ByteBuffer;");
        CHECK(byteBufWrapID != NULL);

        jobject byteBufObj = env->CallStaticObjectMethod(
                clazz.get(), byteBufWrapID, byteArrayObj);

        env->DeleteLocalRef(byteArrayObj);
        byteArrayObj = NULL;

        return byteBufObj;
    }

    static void SetMapInt32(
            JNIEnv *env, jobject hashMapObj, jmethodID hashMapPutID,
            const char *key, int32_t value) {
        jstring keyObj = env->NewStringUTF(key);
        jobject valueObj = makeIntegerObject(env, value);

        env->CallObjectMethod(hashMapObj, hashMapPutID, keyObj, valueObj);

        env->DeleteLocalRef(valueObj);
        valueObj = NULL;
        env->DeleteLocalRef(keyObj);
        keyObj = NULL;
    }

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

    int32_t SoftCode::getInputBuffer(jint index, sp<MediaCodecBuffer> *buffer) { return RESULT_OK; }

    int32_t
    SoftCode::getOutputBuffer(jint index, sp<MediaCodecBuffer> *buffer) { return RESULT_OK; }

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
        sp<AMessage> msg;
        status_t err;
        //err = input ? getInputFormat(&msg) : getOutputFormat(&msg);
        if (err != OK) {
            return err;
        }
        return ConvertMessageToMap(env, msg, format);
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

    int32_t SoftCode::ConvertMessageToMap(JNIEnv *env, const sp<AMessage> &msg, jobject *map) {
        ScopedLocalRef<jclass> hashMapClazz(
                env, env->FindClass("java/util/HashMap"));

        if (hashMapClazz.get() == NULL) {
            return -EINVAL;
        }

        jmethodID hashMapConstructID =
                env->GetMethodID(hashMapClazz.get(), "<init>", "()V");

        if (hashMapConstructID == nullptr) {
            return -EINVAL;
        }

        jmethodID hashMapPutID =
                env->GetMethodID(
                        hashMapClazz.get(),
                        "put",
                        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

        if (hashMapPutID == NULL) {
            return -EINVAL;
        }

        jobject hashMap = env->NewObject(hashMapClazz.get(), hashMapConstructID);

        for (size_t i = 0; i < msg->countEntries(); ++i) {
            AMessage::Type valueType;
            const char *key = msg->getEntryNameAt(i, &valueType);

            if (!strncmp(key, "android._", 9)) {
                // don't expose private keys (starting with android._)
                continue;
            }

            jobject valueObj = NULL;

            switch (valueType) {
                case AMessage::kTypeInt32: {
                    int32_t val;
                    CHECK(msg->findInt32(key, &val));

                    valueObj = makeIntegerObject(env, val);
                    break;
                }

                case AMessage::kTypeInt64: {
                    int64_t val;
                    CHECK(msg->findInt64(key, &val));

                    valueObj = makeLongObject(env, val);
                    break;
                }

                case AMessage::kTypeFloat: {
                    float val;
                    CHECK(msg->findFloat(key, &val));

                    valueObj = makeFloatObject(env, val);
                    break;
                }

                case AMessage::kTypeString: {
                    AString val;
                    CHECK(msg->findString(key, &val));

                    valueObj = env->NewStringUTF(val.c_str());
                    break;
                }

                case AMessage::kTypeBuffer: {
                    sp<ABuffer> buffer;
                    CHECK(msg->findBuffer(key, &buffer));

                    valueObj = makeByteBufferObject(
                            env, buffer->data(), buffer->size());
                    break;
                }

                case AMessage::kTypeRect: {
                    int32_t left, top, right, bottom;
                    CHECK(msg->findRect(key, &left, &top, &right, &bottom));

                    SetMapInt32(
                            env,
                            hashMap,
                            hashMapPutID,
                            AStringPrintf("%s-left", key).c_str(),
                            left);

                    SetMapInt32(
                            env,
                            hashMap,
                            hashMapPutID,
                            AStringPrintf("%s-top", key).c_str(),
                            top);

                    SetMapInt32(
                            env,
                            hashMap,
                            hashMapPutID,
                            AStringPrintf("%s-right", key).c_str(),
                            right);

                    SetMapInt32(
                            env,
                            hashMap,
                            hashMapPutID,
                            AStringPrintf("%s-bottom", key).c_str(),
                            bottom);
                    break;
                }

                default:
                    break;
            }

            if (valueObj != NULL) {
                jstring keyObj = env->NewStringUTF(key);

                env->CallObjectMethod(hashMap, hashMapPutID, keyObj, valueObj);

                env->DeleteLocalRef(keyObj);
                keyObj = NULL;
                env->DeleteLocalRef(valueObj);
                valueObj = NULL;
            }
        }

        *map = hashMap;
        return RESULT_OK;
    }

}