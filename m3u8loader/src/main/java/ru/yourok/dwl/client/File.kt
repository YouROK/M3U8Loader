package ru.yourok.dwl.client

import android.net.Uri
import java.io.FileInputStream
import java.io.InputStream
import java.io.RandomAccessFile

/**
 * Created by yourok on 07.11.17.
 */
class File(private val filePath: Uri) : Client {
    private var isOpen: Boolean = false
    private var input: FileInputStream? = null
    private var rndFile: RandomAccessFile? = null
    private var errMsg: String = ""

    override fun connect() {
        val br = FileInputStream(filePath.path)
        input = br
        isOpen = true
    }

    override fun connect(pos: Long): Long {
        val br = RandomAccessFile(filePath.path, "r")
        br.seek(pos)
        rndFile = br
        input = FileInputStream(br.fd)
        isOpen = true
        return pos
    }

    override fun isConnected(): Boolean {
        return isOpen
    }

    override fun getSize(): Long {
        if (isOpen)
            return input?.available()?.toLong() ?: 0
        return 0
    }

    override fun getUrl(): String {
        return filePath.toString()
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
        rndFile?.close()
        input?.close()
        isOpen = false
        rndFile = null
        input = null
    }
}