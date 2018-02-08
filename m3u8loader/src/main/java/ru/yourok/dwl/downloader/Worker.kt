package ru.yourok.dwl.downloader

import android.net.Uri
import ru.yourok.dwl.client.ClientBuilder
import ru.yourok.dwl.list.Item
import java.io.ByteArrayOutputStream
import java.io.IOException


/**
 * Created by yourok on 10.11.17.
 */
class Worker(val item: Item, private val stat: DownloadStatus, private val file: FileWriter) : Runnable {
    private var stop = false

    override fun run() {
        if (item.isComplete)
            return
        stat.isLoading = true
        stop = false
        var isCompleteLoad = false
        var isMemWrite = item.encData != null
        val buffer = ByteArray(32767)
        val accumBuffer = ByteArrayOutputStream()
        val speed = Speed(stat)

        if (item.loaded >= item.size && item.loaded > 0)
            item.loaded--

        val client = ClientBuilder.new(Uri.parse(item.url))

        try {
            if (client.connect(item.loaded) == -1L) {
                item.loaded = 0
                isMemWrite = true
            }
            item.size = client.getSize()
            stat.Clear()
            speed.startRead()
            while (!stop) {
                val readCount = client.read(buffer)
                if (readCount == -1) {
                    isCompleteLoad = true
                    break
                }
                speed.measure(readCount)
                accumBuffer.write(buffer, 0, readCount)
                if (!isMemWrite) {
                    if (accumBuffer.size() > 65536 && file.write(item, accumBuffer)) {
                        item.loaded += accumBuffer.size().toLong()
                        accumBuffer.reset()
                    }
                } else
                    item.loaded = accumBuffer.size().toLong()
            }
            speed.stopRead()
            if (!isMemWrite && accumBuffer.size() > 0) {
                if (file.write(item, accumBuffer)) {
                    item.loaded += accumBuffer.size()
                    accumBuffer.reset()
                }
            }
        } catch (e: Exception) {
            client.close()
            if (isMemWrite)
                item.loaded = 0
            throw e
        } finally {
            client.close()
        }

        if (isCompleteLoad) {
            if (isMemWrite) {
                if (item.encData != null)
                    stat.buffer = item.encData!!.decrypt(accumBuffer.toByteArray())
                else
                    stat.buffer = accumBuffer.toByteArray()
                accumBuffer.reset()
                stat.buffer?.let {
                    if (it.isNotEmpty()) {
                        item.size = stat.buffer!!.size.toLong()
                        file.write()
                    }
                }
            } else {
                if (accumBuffer.size() > 0) {
                    var writeOk = false
                    var errs = 0
                    while (!writeOk) {
                        if (file.write(item, accumBuffer)) {
                            item.loaded += accumBuffer.size()
                            accumBuffer.reset()
                            writeOk = true
                        } else {
                            errs++
                            if (errs > 60)
                                break
                            Thread.sleep(1000)
                        }
                    }
                    if (!writeOk) {
                        item.loaded = 0
                        throw IOException("write before end")
                    }
                    item.size = item.loaded
                }
                item.isComplete = true
            }
        } else {
            if (isMemWrite)
                item.loaded = 0
        }
    }

//    fun run1() {
//        if (item.isComplete)
//            return
//        stat.isLoading = true
//
//        stop = false
//        var completeDw = false
//        client = ClientBuilder.new(Uri.parse(item.url))
//
//        val buffer = ByteArray(32767)
//        val outBuffer = ByteArrayOutputStream()
//        val speed = Speed(stat)
//        try {
//            var rng: Long = -1
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//                rng = client!!.connect(item.loaded)
//            else
//                synchronized(lockConnection) { rng = client!!.connect(item.loaded) }
//            if (rng == -1L)
//                item.loaded = 0
//
//            item.size = client!!.getSize()
//
//            stat.Clear()
//            speed.startRead()
//            while (!stop) {
//                val readCount = client!!.read(buffer)
//                if (readCount == -1) {
//                    completeDw = true
//                    break
//                }
//                speed.measure(readCount)
//                outBuffer.write(buffer, 0, readCount)
//                if (item.encData == null) {
//                    if (file.write(item, outBuffer)) {
//                        item.loaded += outBuffer.size()
//                        outBuffer.reset()
//                    }
//                } else
//                    item.loaded = outBuffer.size().toLong()
//            }
//            speed.stopRead()
//        } catch (e: Exception) {
//            client!!.close()
//            throw e
//        } finally {
//            client!!.close()
//        }
//
//        if (completeDw) {
//            stat.isCompleteLoad = true
//            if (item.encData != null) {
//                stat.buffer = item.encData!!.decrypt(outBuffer.toByteArray())
//                stat.buffer?.let {
//                    if (it.isNotEmpty()) {
//                        item.size = stat.buffer!!.size.toLong()
//                        file.write()
//                    }
//                }
//            } else {
//                if (outBuffer.size() > 0) {
//                    if (item.size == 0L)
//                        item.size = outBuffer.size().toLong()
//
//                    if (file.write(item, outBuffer)) {
//                        item.loaded += outBuffer.size()
//                        item.isComplete = true
//                    } else {
//                        stat.buffer = outBuffer.toByteArray()
//                        item.size = stat.buffer?.size?.toLong() ?: 0L
//                    }
//                    outBuffer.reset()
//                } else
//                    item.isComplete = true
//            }
//            return
//        }
//
//        //not complete
//        if (item.encData != null)
//            item.loaded = 0
//    }

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