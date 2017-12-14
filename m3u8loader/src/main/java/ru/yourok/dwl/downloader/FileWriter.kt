package ru.yourok.dwl.downloader

import android.net.Uri
import ru.yourok.dwl.storage.Storage
import ru.yourok.dwl.writer.NativeFile
import ru.yourok.dwl.writer.UriFile
import ru.yourok.dwl.writer.Writer
import java.io.File
import java.io.IOException

/**
 * Created by yourok on 09.11.17.
 */
class FileWriter(fileName: String) {

    private var workers: List<Pair<Worker, DownloadStatus>> = mutableListOf()
    private val writer: Writer
    private val lock = Any()

    init {

        if (!Storage.getDocument(File(fileName).parent).exists())
            throw IOException("Error directory not found: " + File(fileName).parent)

        if (File(fileName).canWrite()) {
            writer = NativeFile()
            writer.open(Uri.fromFile(File(fileName)))
        } else {
            var doc = Storage.getDocument(fileName)
            if (!doc.exists()) {
                doc = Storage.getDocument(File(fileName).parent)
                doc = doc.createFile("*/*", File(fileName).name)
            }
            if (doc == null)
                throw IOException("Error showSnackbar file: " + fileName)
            writer = UriFile()
            writer.open(doc.uri)
        }
    }

    fun resize(len: Long) {
        writer.truncate(len)
    }

    fun close() {
        writer.close()
    }

    fun setWorkers(workers: List<Pair<Worker, DownloadStatus>>) {
        this.workers = workers
    }

    fun write() {
        synchronized(lock) {
            var off = 0L

            workers.forEach {
                if (it.first.item.size == 0L)
                    return
                if (it.second.buffer != null) {
                    write(it.second.buffer!!, off)
                    it.second.buffer = null
                    it.first.item.isComplete = true
                }
                off += it.first.item.size
            }
        }
    }

    fun write(buffer: ByteArray, off: Long) {
        writer.write(buffer, off)
    }
}