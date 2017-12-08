//
// Created by yourok on 03.12.17.
//

#ifndef M3U8LOADER_FS_UTILS_H
#define M3U8LOADER_FS_UTILS_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL
Java_ru_yourok_dwl_storage_StatFS_sizeFd(JNIEnv *, jobject, jint fd, jint isTotal);

JNIEXPORT jlong JNICALL
Java_ru_yourok_dwl_storage_StatFS_sizeFPath(JNIEnv *, jobject, jstring fpath, jint isTotal);

JNIEXPORT jstring JNICALL
Java_ru_yourok_dwl_storage_StatFS_pathFd(JNIEnv *, jobject, jint fd);

JNIEXPORT jint JNICALL
Java_ru_yourok_dwl_storage_StatFS_chmodFD(JNIEnv *env, jobject, jint fd);

#ifdef __cplusplus
}
#endif

#endif //M3U8LOADER_FS_UTILS_H
