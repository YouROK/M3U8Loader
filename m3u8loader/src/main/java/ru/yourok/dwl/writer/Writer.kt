package ru.yourok.dwl.writer

import android.net.Uri

/**
 * Created by yourok on 25.11.17.
 */
interface Writer {
    fun open(file: Uri)
    fun write(buffer: ByteArray, offset: Long): Int
    fun truncate(size: Long): Long
    fun size(): Long
    fun close()
}
