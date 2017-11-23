package ru.yourok.dwl.client

import android.net.Uri
import java.io.InputStream


/**
 * Created by yourok on 07.11.17.
 */
interface Client {
    fun connect()
    fun isConnected(): Boolean
    fun getSize(): Long
    fun getUrl(): String
    fun getInputStream(): InputStream?
    fun read(b: ByteArray): Int
    fun getErrorMessage(): String
    fun close()
}

object ClientBuilder {
    fun new(url: Uri): Client {
        if (url.scheme.startsWith("http", 0, true)) {
            return Http(url)
        } else if (url.scheme.startsWith("content", 0, true)) {
            return Content(url)
        } else {
            return File(url)
        }
    }
}