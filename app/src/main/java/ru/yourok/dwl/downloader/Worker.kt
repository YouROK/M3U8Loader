package ru.yourok.dwl.downloader

import android.net.Uri
import ru.yourok.dwl.client.Client
import ru.yourok.dwl.client.ClientBuilder
import ru.yourok.dwl.list.Item
import ru.yourok.dwl.settings.Settings
import ru.yourok.m3u8loader.R
import java.io.ByteArrayOutputStream
import java.io.IOException


/**
 * Created by yourok on 10.11.17.
 */
class Worker(val item: Item, private val stat: DownloadStatus, private val file: File) : Runnable {
    private var stop = false
    private var client: Client? = null

    override fun run() {
        item.isCompleteLoad = false
        stat.isLoading = true

        stop = false
        client = ClientBuilder.new(Uri.parse(item.url))

        var buffer = ByteArray(32767)
        var outBuffer = ByteArrayOutputStream()
        val speed = Speed(stat)

        client!!.connect()
        if (item.size == 0L)
            item.size = client!!.getSize()

        stat.Clear()
        speed.startRead()
        while (!stop) {
            val readCount = client!!.read(buffer)
            if (readCount == -1)
                break
            speed.measure(readCount)
            outBuffer.write(buffer, 0, readCount)
            stat.loadedBytes += readCount
        }
        speed.stopRead()
        client!!.close()

        if (!stop) {
            if (item.encData != null)
                stat.buffer = item.encData!!.decrypt(outBuffer.toByteArray())
            else
                stat.buffer = outBuffer.toByteArray()

            if (stat.buffer != null && !isBinary(stat.buffer!!))
                throw IOException(Settings.context?.getString(R.string.error_fragment_not_binary) ?: "Error, fragment is html page")

            item.size = stat.buffer!!.size.toLong()
            item.isCompleteLoad = true

            file.write()
            return
        }
        stat.loadedBytes = 0
    }

    fun stop() {
        stop = true
        client?.close()
    }

    fun setMaxPrior() {
        if (Thread.currentThread().priority != Thread.MAX_PRIORITY)
            Thread.currentThread().priority = Thread.MAX_PRIORITY
    }

    private fun isBinary(buf: ByteArray): Boolean {
//        return !Charset.forName("UTF-8").newEncoder().canEncode(String(buf, Charset.defaultCharset()))
        return true
        //TODO
    }
}