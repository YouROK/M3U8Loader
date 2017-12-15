package ru.yourok.dwl.downloader

import android.net.Uri
import android.os.Build
import ru.yourok.dwl.client.Client
import ru.yourok.dwl.client.ClientBuilder
import ru.yourok.dwl.list.Item
import ru.yourok.m3u8loader.App
import ru.yourok.m3u8loader.R
import java.io.ByteArrayOutputStream
import java.io.IOException


/**
 * Created by yourok on 10.11.17.
 */
class Worker(val item: Item, private val stat: DownloadStatus, private val file: FileWriter) : Runnable {
    companion object {
        val lockConnection = Any()
    }

    private var stop = false
    private var client: Client? = null

    override fun run() {
        if (item.isComplete)
            return
        stat.isLoading = true

        stop = false
        var completeDw = false
        client = ClientBuilder.new(Uri.parse(item.url))

        val buffer = ByteArray(32767)
        val outBuffer = ByteArrayOutputStream()
        val speed = Speed(stat)
        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                client!!.connect()
            else
                synchronized(lockConnection) { client!!.connect() }

            item.size = client!!.getSize()

            stat.Clear()
            speed.startRead()
            while (!stop) {
                val readCount = client!!.read(buffer)
                if (readCount == -1) {
                    completeDw = true
                    break
                }
                speed.measure(readCount)
                outBuffer.write(buffer, 0, readCount)
                stat.loadedBytes += readCount
            }
            speed.stopRead()
        } catch (e: Exception) {
            client!!.close()
            throw e
        } finally {
            client!!.close()
        }

        if (completeDw) {
            if (item.encData != null)
                stat.buffer = item.encData!!.decrypt(outBuffer.toByteArray())
            else
                stat.buffer = outBuffer.toByteArray()

            if (stat.buffer != null && !isBinary(stat.buffer!!))
                throw IOException(App.getContext().getString(R.string.error_fragment_not_binary) ?: "Error, fragment is html page")

            item.size = stat.buffer!!.size.toLong()
            stat.isCompleteLoad = true
            file.write()
            return
        }
        stat.loadedBytes = 0
    }

    fun stop() {
        stop = true
    }

    fun setMaxPrior() {
        if (Thread.currentThread().priority != Thread.NORM_PRIORITY)
            Thread.currentThread().priority = Thread.NORM_PRIORITY
    }

    private fun isBinary(buf: ByteArray): Boolean {
//        return !Charset.forName("UTF-8").newEncoder().canEncode(String(buf, Charset.defaultCharset()))
        return true
        //TODO
    }
}