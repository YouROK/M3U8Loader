package ru.yourok.dwl.downloader

import java.io.RandomAccessFile

/**
 * Created by yourok on 09.11.17.
 */
class File(pathName: String) {

    private var workers: MutableList<Pair<Worker, DownloadStatus>> = mutableListOf()
    private val file: RandomAccessFile = RandomAccessFile(pathName, "rwd")
    private val lock = Any()

    fun resize(len: Long) {
        file.setLength(len)
    }

    fun close() {
        file.close()
    }

    fun setWorkers(workers: MutableList<Pair<Worker, DownloadStatus>>) {
        this.workers = workers
    }

    fun write() {
        synchronized(lock) {
            var off = 0L

            workers.forEach {
                if (!it.first.item.isCompleteLoad)
                    return
                if (it.second.buffer != null) {
                    file.seek(off)
                    file.write(it.second.buffer, 0, it.second.buffer!!.size)
                    it.second.buffer = null
                }
                off += it.first.item.size
            }
        }
    }
}