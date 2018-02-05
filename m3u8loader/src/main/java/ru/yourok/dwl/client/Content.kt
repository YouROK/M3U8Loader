package ru.yourok.dwl.client

import android.net.Uri
import ru.yourok.m3u8loader.App
import java.io.InputStream

/**
 * Created by yourok on 17.11.17.
 */
class Content(val fileName: Uri) : Client {
    //context.getContentResolver().openInputStream(uri);

    private var isOpen: Boolean = false
    private var input: InputStream? = null
    private var errMsg: String = ""

    override fun connect() {
        input = App.getContext().contentResolver.openInputStream(fileName)
        isOpen = true
    }

    override fun connect(pos: Long): Long {
        connect()
        input!!.skip(pos)
        return getSize()
    }

    override fun isConnected(): Boolean {
        return isOpen
    }

    override fun getSize(): Long {
        if (isOpen)
            return input!!.available().toLong()
        return 0
    }

    override fun getUrl(): String {
        return fileName.toString()
    }

    override fun getInputStream(): InputStream? {
        return input
    }

    override fun read(b: ByteArray): Int {
        if (!isOpen)
            return -1
        return input!!.read(b)
    }

    override fun getErrorMessage(): String {
        return errMsg
    }

    override fun close() {
        if (isOpen) {
            input!!.close()
            isOpen = false
        }
    }
}