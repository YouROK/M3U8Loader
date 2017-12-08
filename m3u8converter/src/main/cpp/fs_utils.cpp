//
// Created by yourok on 03.12.17.
//

#include "fs_utils.h"

#include <sys/stat.h>
#include <sys/statfs.h>
#include <sys/types.h>
#include <fcntl.h>
#include <unistd.h>

#include <string>
#include <vector>
#include <errno.h>

JNIEXPORT jlong JNICALL
Java_ru_yourok_dwl_storage_StatFS_sizeFd(JNIEnv *, jobject, jint fd, jint isTotal) {
    struct statfs fileStat;
    if (fstatfs(fd, &fileStat) < 0)
        return -1;
    if (isTotal)
        return fileStat.f_blocks * fileStat.f_bsize;
    else
        return fileStat.f_bavail * fileStat.f_bsize;
}

JNIEXPORT jlong JNICALL
Java_ru_yourok_dwl_storage_StatFS_sizeFPath(JNIEnv *env, jobject, jstring fpath, jint isTotal) {
    const char *cpath = env->GetStringUTFChars(fpath, NULL);
    std::string spath = cpath;
    env->ReleaseStringChars(fpath, (const jchar *) cpath);

    struct statfs fileStat;
    if (statfs(spath.c_str(), &fileStat) < 0)
        return -1;
    if (isTotal)
        return fileStat.f_blocks * fileStat.f_bsize;
    else
        return fileStat.f_bavail * fileStat.f_bsize;
}

JNIEXPORT jstring JNICALL
Java_ru_yourok_dwl_storage_StatFS_pathFd(JNIEnv *env, jobject, jint fd) {
    char buffer[1024] = {0};

    std::string path = "/proc/self/fd/";
    snprintf(buffer, sizeof(buffer), "%d", fd);
    path += buffer;

    if (readlink(path.c_str(), buffer, sizeof(buffer) - 1) < 0)
        return NULL;
    return env->NewStringUTF(buffer);
}

JNIEXPORT jint JNICALL
Java_ru_yourok_dwl_storage_StatFS_chmodFD(JNIEnv *env, jobject, jint fd) {
    if (fchmod(fd, 0777) != 0)
        return errno;
    return 0;
}