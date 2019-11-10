//
// Created by wuhan on 2019-11-09.
//

#include "SoftCodec_jni.h"
#include "nativehelper/ScopedLocalRef.h"
#include <pthread.h>

#ifndef NELEM
# define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))
#endif
namespace android {
    struct fields_t {
        jmethodID postEventFromNativeID;
        jfieldID mNativeContext;
    };

    static fields_t gFields;
}

using namespace android;

static const char *const kClassPathName = "com/wh/extendexoplayer/soft/SoftCodec";
static pthread_mutex_t *codec_lock = nullptr;

static SoftCode *getSoftCode(JNIEnv *env, jobject thiz) {
    if (codec_lock)pthread_mutex_lock(codec_lock);
    auto *p = (SoftCode *) env->GetLongField(thiz, gFields.mNativeContext);
    if (codec_lock)pthread_mutex_unlock(codec_lock);
    return p;
}

static SoftCode *setSoftCode(JNIEnv *env, jobject thiz, SoftCode *context) {
    if (codec_lock)pthread_mutex_lock(codec_lock);
    auto *old = (SoftCode *) env->GetLongField(thiz, gFields.mNativeContext);
    if (old != nullptr) {
        old->release();
    }
    env->SetLongField(thiz, gFields.mNativeContext, (long) context);
    if (codec_lock)pthread_mutex_unlock(codec_lock);
    return old;
}

static void com_wh_extendexoplayer_soft_SoftCodec_release(JNIEnv *env, jobject thiz) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_release");
    setSoftCode(env,thiz, nullptr);
}

static void com_wh_extendexoplayer_soft_SoftCodec_setSurface(JNIEnv *env, jobject thiz,
                                                             jobject surface) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_setSurface");
    auto *codec = getSoftCode(env, thiz);

    if (codec == nullptr) {
        jniThrowIllegalStateException(env, " setSurface codec is nullptr ");
        return;
    }

    int ret = codec->setSurface(surface);
    if(ret == RESULT_OK){
        return;
    }
    jniThrowRuntimeException(env, " setSurface failed! ");
}

static void com_wh_extendexoplayer_soft_SoftCodec_setCallback(JNIEnv *env, jobject thiz,
                                                              jobject cb) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_setCallback");
    auto *codec = getSoftCode(env, thiz);

    if (codec == nullptr) {
        jniThrowIllegalStateException(env, " setCallback codec is nullptr ");
        return;
    }

    int ret = codec->setCallback(cb);
    if(ret == RESULT_OK){
        return;
    }

    jniThrowRuntimeException(env, " setCallback failed! ");
}

static void com_wh_extendexoplayer_soft_SoftCodec_configure(JNIEnv *env, jobject thiz,
                                                            jobjectArray keys, jobjectArray values,
                                                            jobject surface, jint flags) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_configure");
    auto *codec = getSoftCode(env, thiz);

    if (codec == nullptr) {
        jniThrowIllegalStateException(env, " configure codec is nullptr ");
        return;
    }

    int ret = codec->configure(keys, values, surface, flags);
    if(ret == RESULT_OK){
        return;
    }
    jniThrowRuntimeException(env, " configure failed! ");
}

static void com_wh_extendexoplayer_soft_SoftCodec_start(JNIEnv *env, jobject thiz) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_start");
    auto *codec = getSoftCode(env, thiz);

    if (codec == nullptr) {
        jniThrowIllegalStateException(env, " start codec is nullptr ");
        return;
    }

    int ret = codec->start();
    if(ret == RESULT_OK){
        return;
    }

    jniThrowRuntimeException(env, "start failed!");
}

static void com_wh_extendexoplayer_soft_SoftCodec_stop(JNIEnv *env, jobject thiz) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_stop");
    auto *codec = getSoftCode(env, thiz);

    if (codec == nullptr) {
        jniThrowIllegalStateException(env, " stop codec is nullptr ");
        return;
    }

    int ret = codec->stop();
    if(ret == RESULT_OK){
        return;
    }
    jniThrowRuntimeException(env, "stop failed!");
}

static void com_wh_extendexoplayer_soft_SoftCodec_flush(JNIEnv *env, jobject thiz) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_flush");
    auto *codec = getSoftCode(env, thiz);

    if (codec == nullptr) {
        jniThrowIllegalStateException(env, "flush codec is nullptr ");
        return;
    }

    int ret = codec->flush();
    if(ret == RESULT_OK){
        return;
    }

    jniThrowRuntimeException(env, "flush failed!");
}

static void com_wh_extendexoplayer_soft_SoftCodec_queueInputBuffer(JNIEnv *env, jobject thiz,
                                                                   jint index, jint offset,
                                                                   jint size,
                                                                   jlong timestampUs,
                                                                   jint flags) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_queueInputBuffer");
    auto *codec = getSoftCode(env, thiz);

    if (codec == nullptr) {
        jniThrowIllegalStateException(env, "queueInputBuffer codec is nullptr ");
        return;
    }

    char *errorDetailMsg;

    int ret = codec->queueInputBuffer(
            index, offset, size, timestampUs, flags, &errorDetailMsg);
    if (ret == RESULT_OK) {
        return;
    }

    jniThrowRuntimeException(env, "queueInputBuffer failed");
}

static jint com_wh_extendexoplayer_soft_SoftCodec_dequeueInputBuffer(JNIEnv *env, jobject thiz,
                                                                     jlong timeoutUs) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_dequeueInputBuffer");
    auto *codec = getSoftCode(env, thiz);

    if (codec == nullptr) {
        jniThrowIllegalStateException(env, " dequeueInputBuffer codec is nullptr ");
        return -1;
    }

    size_t index;
    int ret = codec->dequeueInputBuffer(&index, timeoutUs);

    if (ret == RESULT_OK) {
        return (jint) index;
    }

    return RESULT_FAIL;
}

static jint com_wh_extendexoplayer_soft_SoftCodec_dequeueOutputBuffer(JNIEnv *env, jobject thiz,
                                                                      jobject info,
                                                                      jlong timeoutUs) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_dequeueOutputBuffer");
    auto *codec = getSoftCode(env, thiz);

    if (codec == nullptr) {
        jniThrowIllegalStateException(env, " dequeueOutputBuffer codec is nullptr ");
        return 0;
    }

    size_t index;
    int ret = codec->dequeueOutputBuffer(
            env, info, &index, timeoutUs);

    if (ret == RESULT_OK) {
        return (jint) index;
    }

    return RESULT_FAIL;
}

static void com_wh_extendexoplayer_soft_SoftCodec_releaseOutputBuffer(JNIEnv *env, jobject thiz,
                                                                      jint index, jboolean render,
                                                                      jboolean updatePTS,
                                                                      jlong timeNs) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_releaseOutputBuffer");
    auto *codec = getSoftCode(env, thiz);

    if (codec == nullptr) {
        jniThrowIllegalStateException(env, " releaseOutputBuffer codec is nullptr ");
        return;
    }

    int ret = codec->releaseOutputBuffer(index, render, updatePTS, timeNs);
    if (ret == RESULT_OK) {
        return;
    }

    jniThrowRuntimeException(env, " releaseOutputBuffer failed! ");
}

static jobject com_wh_extendexoplayer_soft_SoftCodec_getFormatNative(JNIEnv *env, jobject thiz,
                                                                     jboolean input) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_getFormatNative");
    auto *codec = getSoftCode(env, thiz);

    if (codec == nullptr) {
        jniThrowIllegalStateException(env, " getFormatNative codec is nullptr ");
        return nullptr;
    }

    jobject format;
    int ret = codec->getFormat(env, input, &format);

    if (ret == RESULT_OK) {
        return format;
    }

    jniThrowRuntimeException(env, " getFormatNative failed! ");

    return nullptr;
}

static void com_wh_extendexoplayer_soft_SoftCodec_setVideoScalingMode(JNIEnv *env, jobject thiz,
                                                                      jint mode) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_setVideoScalingMode");
    auto *codec = getSoftCode(env, thiz);

    if (codec == nullptr) {
        jniThrowIllegalStateException(env, " setVideoScalingMode codec is nullptr ");
        return;
    }

    codec->setVideoScalingMode(mode);
}

static void
com_wh_extendexoplayer_soft_SoftCodec_enableOnFrameRenderedListener(JNIEnv *env, jobject thiz,
                                                                    jboolean enable) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_enableOnFrameRenderedListener");
    auto *codec = getSoftCode(env, thiz);

    if (codec == nullptr) {
        jniThrowIllegalStateException(env, " enableOnFrameRenderedListener codec is nullptr ");
        return;
    }

    int ret = codec->enableOnFrameRenderedListener(enable);
    if (ret == RESULT_OK) {
        return;
    }

    jniThrowRuntimeException(env, " enableOnFrameRenderedListener failed! ");
}

static void com_wh_extendexoplayer_soft_SoftCodec_setParameter(JNIEnv *env, jobject thiz,
                                                               jobjectArray keys,
                                                               jobjectArray values) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_setParameter");
    auto *codec = getSoftCode(env, thiz);

    if (codec == nullptr) {
        jniThrowIllegalStateException(env, " setParameter codec is nullptr ");
        return;
    }

    int ret = codec->setParameters(env, keys, values);
    if (ret == RESULT_OK) {
        return;
    }

    jniThrowRuntimeException(env, " setParameter failed! ");
}

static jobject com_wh_extendexoplayer_soft_SoftCodec_getBuffer(JNIEnv *env, jobject thiz,
                                                               jboolean input, jint index) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_getBuffer");
    auto *codec = getSoftCode(env, thiz);
    if (codec == nullptr) {
        jniThrowIllegalStateException(env, " getBuffer codec is nullptr ");
        return nullptr;
    }
    jobject buffer;
    int ret = codec->getBuffer(env, input, index, &buffer);

    if (ret == RESULT_OK) {
        return buffer;
    }

    // if we're out of memory, an exception was already thrown
    jniThrowRuntimeException(env, " getBuffer failed! ");

    return nullptr;
}

static void com_wh_extendexoplayer_soft_SoftCodec_init(JNIEnv *env) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_init");
    ScopedLocalRef<jclass> clazz(
            env, env->FindClass(kClassPathName));
    CHECK(clazz.get() != NULL);
    gFields.postEventFromNativeID =
            env->GetMethodID(
                    clazz.get(), "postEventFromNative", "(IIILjava/lang/Object;)V");
    CHECK(gFields.postEventFromNativeID != nullptr);
    gFields.mNativeContext = env->GetFieldID(clazz.get(), "mNativeContext", "J");
    CHECK(gFields.mNativeContext != nullptr);
    if (nullptr == codec_lock) {
        auto *mtx = (pthread_mutex_t *) malloc(sizeof(pthread_mutex_t));
        if (mtx == nullptr) {
            return;
        }
        int res = pthread_mutex_init(mtx, nullptr);
        if (0 != res) {
            codec_lock = mtx;
        }
    }
}

static void com_wh_extendexoplayer_soft_SoftCodec_setup(JNIEnv *env, jobject thiz,
                                                        jstring name, jboolean nameIsType,
                                                        jboolean encoder) {
    ALOGE("com_wh_extendexoplayer_soft_SoftCodec_setup");
    if (name == nullptr) {
        jniThrowNullPointerException(env, "codec name is null");
        return;
    }
    const char *tmp = env->GetStringUTFChars(name, nullptr);

    if (tmp == nullptr) {
        return;
    }
    auto *codec = new SoftCode();
    setSoftCode(env, thiz, codec);
}

static void com_wh_extendexoplayer_soft_SoftCodec_finalize(JNIEnv *env, jobject thiz) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_finalize");
    com_wh_extendexoplayer_soft_SoftCodec_release(env, thiz);
}

static const JNINativeMethod gMethods[] = {
        {"native_release",                       "()V", (void *) com_wh_extendexoplayer_soft_SoftCodec_release},
        {"native_setSurface",
                                                 "(Landroid/view/Surface;)V",
                                                        (void *) com_wh_extendexoplayer_soft_SoftCodec_setSurface},
        {"native_setCallback",
                                                 "(Lcom/wh/extendexoplayer/soft/SoftCodec$Callback;)V",
                                                        (void *) com_wh_extendexoplayer_soft_SoftCodec_setCallback},
        {"native_configure",
                                                 "([Ljava/lang/String;[Ljava/lang/Object;Landroid/view/Surface;I)V",
                                                        (void *) com_wh_extendexoplayer_soft_SoftCodec_configure},
        {"native_start",                         "()V", (void *) com_wh_extendexoplayer_soft_SoftCodec_start},
        {"native_stop",                          "()V", (void *) com_wh_extendexoplayer_soft_SoftCodec_stop},
        {"native_flush",                         "()V", (void *) com_wh_extendexoplayer_soft_SoftCodec_flush},
        {"native_queueInputBuffer",              "(IIIJI)V",
                                                        (void *) com_wh_extendexoplayer_soft_SoftCodec_queueInputBuffer},
        {"native_dequeueInputBuffer",            "(J)I",
                                                        (void *) com_wh_extendexoplayer_soft_SoftCodec_dequeueInputBuffer},
        {"native_dequeueOutputBuffer",           "(Lcom/wh/extendexoplayer/soft/SoftCodec$BufferInfo;J)I",
                                                        (void *) com_wh_extendexoplayer_soft_SoftCodec_dequeueOutputBuffer},
        {"releaseOutputBuffer",                  "(IZZJ)V",
                                                        (void *) com_wh_extendexoplayer_soft_SoftCodec_releaseOutputBuffer},
        {"getFormatNative",                      "(Z)Ljava/util/Map;",
                                                        (void *) com_wh_extendexoplayer_soft_SoftCodec_getFormatNative},
        {"setVideoScalingMode",                  "(I)V",
                                                        (void *) com_wh_extendexoplayer_soft_SoftCodec_setVideoScalingMode},
        {"native_enableOnFrameRenderedListener", "(Z)V",
                                                        (void *) com_wh_extendexoplayer_soft_SoftCodec_enableOnFrameRenderedListener},
        {"setParameters",                        "([Ljava/lang/String;[Ljava/lang/Object;)V",
                                                        (void *) com_wh_extendexoplayer_soft_SoftCodec_setParameter},
        {"getBuffer",                            "(ZI)Ljava/nio/ByteBuffer;",
                                                        (void *) com_wh_extendexoplayer_soft_SoftCodec_getBuffer},
        {"native_init",                          "()V",
                                                        (void *) com_wh_extendexoplayer_soft_SoftCodec_init},
        {"native_setup",                         "(Ljava/lang/String;ZZ)V",
                                                        (void *) com_wh_extendexoplayer_soft_SoftCodec_setup},
        {"native_finalize",                      "()V",
                                                        (void *) com_wh_extendexoplayer_soft_SoftCodec_finalize},
};

/*----------------------------------------------------------------------
|    JNI_OnLoad
+---------------------------------------------------------------------*/
jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = nullptr;

    if (vm == nullptr) {
        return RESULT_FAIL;
    }

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return RESULT_FAIL;
    }
    if (env == nullptr) {
        return RESULT_FAIL;
    }

    if (jniRegisterNativeMethods(env, kClassPathName, gMethods, NELEM(gMethods)) < 0) {
        return RESULT_FAIL;
    }
    // success -- return valid version number
    return JNI_VERSION_1_4;
}


void JNI_OnUnload(JavaVM *vm, void *reserved) {
    if (codec_lock != nullptr) {
        pthread_mutex_destroy(codec_lock);
        free(codec_lock);
        codec_lock = nullptr;
    }
}