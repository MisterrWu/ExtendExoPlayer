//
// Created by wuhan on 2019-11-10.
//

#ifndef EXTENDEXOPLAYER_SCOPED_LOCAL_REF_H
#define EXTENDEXOPLAYER_SCOPED_LOCAL_REF_H

#include <cstddef>
#include <jni.h>
// A smart pointer that deletes a JNI local reference when it goes out of scope.
template<typename T>
class ScopedLocalRef {
public:
    ScopedLocalRef(JNIEnv* env, T localRef) : mEnv(env), mLocalRef(localRef) {
    }
    ScopedLocalRef(ScopedLocalRef&& s) noexcept : mEnv(s.mEnv), mLocalRef(s.release()) {
    }
    explicit ScopedLocalRef(JNIEnv* env) : mEnv(env), mLocalRef(nullptr) {
    }
    ~ScopedLocalRef() {
        reset();
    }
    void reset(T ptr = NULL) {
        if (ptr != mLocalRef) {
            if (mLocalRef != NULL) {
                mEnv->DeleteLocalRef(mLocalRef);
            }
            mLocalRef = ptr;
        }
    }
    T release() __attribute__((warn_unused_result)) {
        T localRef = mLocalRef;
        mLocalRef = NULL;
        return localRef;
    }
    T get() const {
        return mLocalRef;
    }
    // We do not expose an empty constructor as it can easily lead to errors
    // using common idioms, e.g.:
    //   ScopedLocalRef<...> ref;
    //   ref.reset(...);
    // Move assignment operator.
    ScopedLocalRef& operator=(ScopedLocalRef&& s) noexcept {
        reset(s.release());
        mEnv = s.mEnv;
        return *this;
    }
    // Allows "if (scoped_ref == nullptr)"
    bool operator==(std::nullptr_t) const {
        return mLocalRef == nullptr;
    }
    // Allows "if (scoped_ref != nullptr)"
    bool operator!=(std::nullptr_t) const {
        return mLocalRef != nullptr;
    }
private:
    JNIEnv* mEnv;
    T mLocalRef;
    DISALLOW_COPY_AND_ASSIGN(ScopedLocalRef);
};

#endif //EXTENDEXOPLAYER_SCOPED_LOCAL_REF_H
