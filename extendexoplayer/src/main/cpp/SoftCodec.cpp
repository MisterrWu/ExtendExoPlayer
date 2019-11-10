//
// Created by wuhan on 2019-11-09.
//

#include <jni.h>
#include "SoftCodec.h"

int SoftCode::getBuffer(JNIEnv *env, jboolean input, jint index, jobject *buffer) {
    return RESULT_OK;
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

int SoftCode::releaseOutputBuffer(jint index, jboolean render, jboolean updatePTS, jlong timeNs) {
    return 0;
}

int SoftCode::dequeueOutputBuffer(JNIEnv *env, jobject bufferInfo, size_t *index, jlong timeoutUS) {
    return 0;
}

int SoftCode::dequeueInputBuffer(size_t *index, jlong timeoutUs) {
    return 0;
}

int SoftCode::queueInputBuffer(jint index, jint offset, jint size, jlong timestampUs, jint flags,
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
