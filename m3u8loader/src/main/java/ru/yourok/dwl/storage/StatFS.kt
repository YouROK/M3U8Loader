package ru.yourok.dwl.storage


/**
 * Created by yourok on 03.12.17.
 */

object StatFS {
    init {
        System.loadLibrary("fsutils")
    }

    private external fun sizeFd(fd: Int, isTotal: Int): Long
    private external fun sizeFPath(fpath: String, isTotal: Int): Long
    private external fun pathFd(fd: Int): String
    private external fun listFiles(fpath: String): Array<String>

    fun path(fd: Int): String? {
        return pathFd(fd)
    }

    fun size(fd: Int, isTotal: Boolean): Long {
        if (isTotal)
            return sizeFd(fd, 1)
        else
            return sizeFd(fd, 0)
    }

    fun size(fpath: String, isTotal: Boolean): Long {
        if (isTotal)
            return sizeFPath(fpath, 1)
        else
            return sizeFPath(fpath, 0)
    }

    fun listPaths(path: String): List<String>? {
        return listFiles(path).toList()
    }
}