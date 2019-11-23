//
// Created by wuhan on 2019-11-17.
//

#ifndef EXTENDEXOPLAYER_ABASE_H
#define EXTENDEXOPLAYER_ABASE_H

#ifndef ARRAY_SIZE
#define ARRAY_SIZE(a) (sizeof(a) / sizeof(*(a)))
#endif

#define DISALLOW_EVIL_CONSTRUCTORS(name) \
    name(const name &); \
    name &operator=(const name &) /* NOLINT */

/* Returns true if the size parameter is safe for new array allocation (32-bit)
 *
 * Example usage:
 *
 * if (!isSafeArraySize<uint32_t>(arraySize)) {
 *     return BAD_VALUE;
 * }
 * ...
 * uint32_t *myArray = new uint32_t[arraySize];
 *
 * There is a bug in gcc versions earlier than 4.8 where the new[] array allocation
 * will overflow in the internal 32 bit heap allocation, resulting in an
 * underallocated array. This is a security issue that allows potential overwriting
 * of other heap data.
 *
 * An alternative to checking is to create a safe new array template function which
 * either throws a std::bad_alloc exception or returns NULL/nullptr_t; NULL considered
 * safe since normal access of NULL throws an exception.
 *
 * https://securityblog.redhat.com/2012/10/31/array-allocation-in-cxx/
 */
template <typename T, typename S>
bool isSafeArraySize(S size) {
    return size >= 0                            // in case S is signed, ignored if not.
           && size <= 0xffffffff / sizeof(T);  // max-unsigned-32-bit-int / element-size.
}

#endif //EXTENDEXOPLAYER_ABASE_H
