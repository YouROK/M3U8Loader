package ru.yourok.dwl.client

import java.io.FileInputStream
import java.io.InputStream

/**
 * Created by yourok on 07.11.17.
 */
class File(private val filePath: String) : Client {
    private var isOpen: Boolean = false
    private var file: java.io.File? = null
    private var input: FileInputStream? = null
    private var errMsg: String = ""

    override fun connect() {
        file = java.io.File(filePath)
        input = file!!.inputStream()
        isOpen = true
    }

    override fun isConnected(): Boolean {
        return isOpen
    }

    override fun getSize(): Long {
        if (isOpen)
            return file!!.length()
        return 0
    }

    override fun getUrl(): String {
        return filePath
    }

    override fun getInputStream(): InputStream? {
        return input
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