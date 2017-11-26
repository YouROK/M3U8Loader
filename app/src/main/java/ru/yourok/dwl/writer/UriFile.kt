package ru.yourok.dwl.writer

import android.net.Uri
import android.os.ParcelFileDescriptor
import ru.yourok.dwl.settings.Settings
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer


/**
 * Created by yourok on 25.11.17.
 */
class UriFile : Writer {
    private var pfdOutput: ParcelFileDescriptor? = null
    private var fos: FileOutputStream? = null

    override fun open(file: Uri) {
        pfdOutput = Settings.context!!.getContentResolver().openFileDescriptor(file, "rw")
        fos = FileOutputStream(pfdOutput!!.fileDescriptor)
    }

    override fun write(buffer: ByteArray, offset: Long): Int {
        if (fos == null)
            return -1
        try {
            val fch = fos!!.getChannel()
            fch.position(offset)
            val bytesWrite = fch.write(ByteBuffer.wrap(buffer))
            pfdOutput!!.fileDescriptor.sync()
            return bytesWrite
        } catch (e: IOException) {
            e.printStackTrace()
            return -1
        }
    }

    override fun truncate(size: Long): Long {
        if (fos == null)
            return -1
        val fch = fos!!.getChannel()
        try {
            fch.truncate(size)
            pfdOutput!!.fileDescriptor.sync()
            return fch.size()
        } catch (e: Exception) { // Attention! Truncate is broken on removable SD card of Android 5.0
            e.printStackTrace()
            return -1
        }
    }

    override fun size(): Long {
        if (fos != null)
            return fos!!.channel.size()
        return -1
    }

    override fun close() {
        if (pfdOutput != null)
            pfdOutput!!.fileDescriptor.sync()
        if (fos != null) {
            val fch = fos!!.getChannel()
            fch.close()
            fos!!.close()
        }
        if (pfdOutput != null)
            pfdOutput!!.close()
    }
}