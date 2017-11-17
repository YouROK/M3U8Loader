package ru.yourok.dwl.downloader

import android.net.Uri
import ru.yourok.dwl.client.Client
import ru.yourok.dwl.client.ClientBuilder
import ru.yourok.dwl.list.Item
import java.io.ByteArrayOutputStream


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

        val inputStream = client!!.getInputStream()!!.buffered()
        stat.Clear()
        speed.startRead()
        while (!stop) {
            val readCount = inputStream.read(buffer)
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

            //TODO check

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

//    private fun checkBinary(buf: ByteArray) {
//        var count = 0
//        var isBin = 0
//        buf.forEach {
//            val block = Character.UnicodeBlock.of(it.toChar())
//
//        }
//
//    }
}