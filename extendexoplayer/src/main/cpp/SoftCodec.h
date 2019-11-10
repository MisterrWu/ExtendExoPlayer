//
// Created by wuhan on 2019-11-09.
//

#ifndef EXTENDEXOPLAYER_SOFTCODEC_H
#define EXTENDEXOPLAYER_SOFTCODEC_H

#include <android/log.h>

#define ANDROID_LOG(level, TAG, ...)    ((void)__android_log_print(level, TAG, __VA_ARGS__))

#define TAG "SoftCodec"

#define ANDROID_LOG_V(...)  ANDROID_LOG(ANDROID_LOG_VERBOSE,   TAG, __VA_ARGS__)
#define ANDROID_LOG_D(...)  ANDROID_LOG(ANDROID_LOG_DEBUG,     TAG, __VA_ARGS__)
#define ANDROID_LOG_I(...)  ANDROID_LOG(ANDROID_LOG_INFO,      TAG, __VA_ARGS__)
#define ANDROID_LOG_W(...)  ANDROID_LOG(ANDROID_LOG_WARN,      TAG, __VA_ARGS__)
#define ANDROID_LOG_E(...)  ANDROID_LOG(ANDROID_LOG_ERROR,     TAG, __VA_ARGS__)

#define RESULT_OK 1
#define RESULT_FAIL -1

#endif //EXTENDEXOPLAYER_SOFTCODEC_H
