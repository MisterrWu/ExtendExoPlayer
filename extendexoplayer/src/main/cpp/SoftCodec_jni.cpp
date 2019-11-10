//
// Created by wuhan on 2019-11-09.
//

#include "SoftCodec_jni.h"

#ifndef NELEM
# define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))
#endif

static const char *const kClassPathName = "com/wh/extendexoplayer/soft/SoftCodec";

static void com_wh_extendexoplayer_soft_SoftCodec_release(JNIEnv *env, jobject thiz) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_release");
}

static void com_wh_extendexoplayer_soft_SoftCodec_setSurface(JNIEnv *env, jobject thiz,
                                                             jobject surface) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_setSurface");
}

static void com_wh_extendexoplayer_soft_SoftCodec_setCallback(JNIEnv *env, jobject thiz,
                                                              jobject cb) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_setCallback");
}

static void com_wh_extendexoplayer_soft_SoftCodec_configure(JNIEnv *env, jobject thiz,
                                                            jobjectArray keys, jobjectArray values,
                                                            jobject surface, jobject crypto,
                                                            jint flags) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_configure");
}

static void com_wh_extendexoplayer_soft_SoftCodec_start(JNIEnv *env, jobject thiz) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_start");
}

static void com_wh_extendexoplayer_soft_SoftCodec_stop(JNIEnv *env, jobject thiz) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_stop");
}

static void com_wh_extendexoplayer_soft_SoftCodec_flush(JNIEnv *env, jobject thiz) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_flush");
}

static void com_wh_extendexoplayer_soft_SoftCodec_queueInputBuffer(JNIEnv *env, jobject thiz,
                                                                   jint index, jint offset,
                                                                   jint size,
                                                                   jlong presentationTimeUs,
                                                                   jint flags) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_queueInputBuffer");
}

static jint com_wh_extendexoplayer_soft_SoftCodec_dequeueInputBuffer(JNIEnv *env, jobject thiz,
                                                                     jboolean input) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_dequeueInputBuffer");
}

static jint com_wh_extendexoplayer_soft_SoftCodec_dequeueOutputBuffer(JNIEnv *env, jobject thiz,
                                                                      jobject info,
                                                                      jlong timeoutUs) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_dequeueOutputBuffer");
}

static void com_wh_extendexoplayer_soft_SoftCodec_releaseOutputBuffer(JNIEnv *env, jobject thiz,
                                                                      jint index, jboolean render,
                                                                      jboolean updatePTS,
                                                                      jlong timeNs) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_releaseOutputBuffer");
}

static jobject com_wh_extendexoplayer_soft_SoftCodec_getFormatNative(JNIEnv *env, jobject thiz,
                                                                     jboolean input) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_getFormatNative");
}

static void com_wh_extendexoplayer_soft_SoftCodec_setVideoScalingMode(JNIEnv *env, jobject thiz,
                                                                      jint mode) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_setVideoScalingMode");
}

static void
com_wh_extendexoplayer_soft_SoftCodec_enableOnFrameRenderedListener(JNIEnv *env, jobject thiz,
                                                                    jboolean enable) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_enableOnFrameRenderedListener");
}

static void com_wh_extendexoplayer_soft_SoftCodec_setParameter(JNIEnv *env, jobject thiz,
                                                               jobjectArray keys,
                                                               jobjectArray values) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_setParameter");
}

static jobjectArray com_wh_extendexoplayer_soft_SoftCodec_getBuffers(JNIEnv *env, jobject thiz,
                                                                     jboolean input) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_getBuffers");
}

static jobject com_wh_extendexoplayer_soft_SoftCodec_getBuffer(JNIEnv *env, jobject thiz,
                                                               jboolean input, jint index) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_getBuffer");
}

static void com_wh_extendexoplayer_soft_SoftCodec_init(JNIEnv *env) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_init");
}

static void com_wh_extendexoplayer_soft_SoftCodec_setup(JNIEnv *env, jobject thiz,
                                                        jstring name, jboolean nameIsType,
                                                        jboolean encoder) {
    ALOGE("com_wh_extendexoplayer_soft_SoftCodec_setup");
}

static void com_wh_extendexoplayer_soft_SoftCodec_finalize(JNIEnv *env, jobject thiz) {
    ALOGD("com_wh_extendexoplayer_soft_SoftCodec_finalize");
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
        {"getBuffers",                           "(Z)[Ljava/nio/ByteBuffer;",
                                                        (void *) com_wh_extendexoplayer_soft_SoftCodec_getBuffers},
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
}