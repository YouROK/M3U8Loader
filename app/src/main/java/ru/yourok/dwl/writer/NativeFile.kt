package ru.yourok.dwl.writer

import android.net.Uri
import java.io.RandomAccessFile

/**
 * Created by yourok on 25.11.17.
 */
class NativeFile : Writer {

    private var file: RandomAccessFile? = null

    override fun open(fileUri: Uri) {
        file = RandomAccessFile(fileUri.path, "rwd")
    }

    override fun write(buffer: ByteArray, offset: Long): Int {
        if (file != null) {
            file!!.seek(offset)
            file!!.write(buffer)
            file!!.fd.sync()
        }
        return -1
    }

    override fun truncate(size: Long): Long {
        if (file != null) {
            file!!.setLength(size)
            file!!.fd.sync()
            return file!!.length()
        }
        return -1
    }

    override fun size(): Long {
        if (file != null)
            return file!!.length()
        return -1
    }

    override fun close() {
        file!!.fd.sync()
        file!!.close()
    }

}