package ru.yourok.dwl.downloader

import android.net.Uri
import ru.yourok.dwl.settings.Settings
import ru.yourok.dwl.storage.Document
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
        if (java.io.File(Settings.downloadPath).canWrite()) {
            writer = NativeFile()
            writer.open(Uri.fromFile(File(Settings.downloadPath, fileName)))
        } else {
            val fullPath = java.io.File(Settings.downloadPath, fileName).canonicalPath
            var doc = Document.openFile(fullPath)
            if (doc == null)
                doc = Document.createFile(fullPath)
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

    fun write() {
        synchronized(lock) {
            var off = 0L

            workers.forEach {
                if (!it.first.item.isCompleteLoad)
                    return
                if (it.second.buffer != null) {
                    write(it.second.buffer!!, off)
                    it.second.buffer = null
                }
                off += it.first.item.size
            }
        }
    }

    fun write(buffer: ByteArray, off: Long) {
        writer.write(buffer, off)
    }
}