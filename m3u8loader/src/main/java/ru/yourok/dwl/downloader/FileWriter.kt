package ru.yourok.dwl.downloader

import android.net.Uri
import android.support.v4.provider.DocumentFile
import ru.yourok.dwl.list.Item
import ru.yourok.dwl.storage.Storage
import ru.yourok.dwl.writer.NativeFile
import ru.yourok.dwl.writer.UriFile
import ru.yourok.dwl.writer.Writer
import java.io.ByteArrayOutputStream
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
            var doc: DocumentFile? = Storage.getDocument(fileName)
            if (doc != null && !doc.exists()) {
                doc = Storage.getDocument(File(fileName).parent)
                doc = doc?.createFile("*/*", File(fileName).name) ?: null
            }
            if (doc == null)
                throw IOException("Error open file: " + fileName)
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

    fun write(item: Item, buf: ByteArrayOutputStream): Boolean {
        synchronized(lock) {
            var off = 0L

            for (i in 0 until item.index) {
                val sz = workers[i].first.item.size
                if (sz == 0L)
                    return false
                off += sz
            }
            off += item.loaded
            write(buf.toByteArray(), off)
            return true
        }
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