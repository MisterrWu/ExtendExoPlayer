//
// Created by wuhan on 2019-11-10.
//

#ifndef EXTENDEXOPLAYER_ALOG_PRIV_H
#define EXTENDEXOPLAYER_ALOG_PRIV_H

#include <android/log.h>
#include <syslog.h>
#ifndef LOG_NDEBUG
#ifdef NDEBUG
#define LOG_NDEBUG 1
#else
#define LOG_NDEBUG 0
#endif
#endif
/*
 * Basic log message macros intended to emulate the behavior of log/log.h
 * in system core.  This should be dependent only on ndk exposed logging
 * functionality.
 */
#ifndef ALOG
#define ALOG(priority, tag, fmt...) \
    __android_log_print(ANDROID_##priority, tag, fmt)
#endif
#ifndef ALOGV
#if LOG_NDEBUG
#define ALOGV(...)   ((void)0)
#else
#define ALOGV(...) ((void)ALOG(LOG_VERBOSE, LOG_TAG, __VA_ARGS__))
#endif
#endif
#ifndef ALOGD
#define ALOGD(...) ((void)ALOG(LOG_DEBUG, LOG_TAG, __VA_ARGS__))
#endif
#ifndef ALOGI
#define ALOGI(...) ((void)ALOG(LOG_INFO, LOG_TAG, __VA_ARGS__))
#endif
#ifndef ALOGW
#define ALOGW(...) ((void)ALOG(LOG_WARN, LOG_TAG, __VA_ARGS__))
#endif
#ifndef ALOGE
#define ALOGE(...) ((void)ALOG(LOG_ERROR, LOG_TAG, __VA_ARGS__))
#endif
/*
 * Log a fatal error if cond is true. The condition test is inverted from
 * assert(3) semantics. The test and message are not stripped from release
 * builds
 */
#ifndef ALOG_ALWAYS_FATAL_IF
#define ALOG_ALWAYS_FATAL_IF(cond, ...) \
    if (cond) __android_log_assert(#cond, LOG_TAG, __VA_ARGS__)
#endif

#define LITERAL_TO_STRING_INTERNAL(x)    #x
#define LITERAL_TO_STRING(x) LITERAL_TO_STRING_INTERNAL(x)

#define CHECK(condition)                                \
    ALOG_ALWAYS_FATAL_IF(                                \
            !(condition),                               \
            "%s",                                       \
            __FILE__ ":" LITERAL_TO_STRING(__LINE__)    \
            " CHECK(" #condition ") failed.")

#endif //EXTENDEXOPLAYER_ALOG_PRIV_H
