package ru.yourok.dwl.parser

import android.net.Uri
import com.iheartradio.m3u8.data.EncryptionData
import ru.yourok.dwl.client.Client
import ru.yourok.dwl.client.ClientBuilder
import ru.yourok.dwl.client.Util
import ru.yourok.dwl.list.EncKey
import ru.yourok.dwl.list.Item
import ru.yourok.dwl.list.List
import ru.yourok.dwl.settings.Settings
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Created by yourok on 09.11.17.
 */
class ParseMedia {
    val executor = Executors.newFixedThreadPool(20)!!
    var stop = false
    var error = ""

    fun parse(list: List) {
        val listUri = Uri.parse(list.url)
        val client = ClientBuilder.new(listUri)
        val playList = Loader().loadList(client)
        if (!playList.hasMediaPlaylist())
            throw NoSuchElementException("No such element in list")

        playList.mediaPlaylist.tracks.forEachIndexed { index, trackData ->
            val itm = Item()
            itm.index = index
            itm.isLoad = true
            itm.url = Util.concatUriList(listUri, trackData.uri)
            if (trackData.hasTrackInfo())
                itm.duration = trackData.trackInfo.duration
            if (trackData.hasEncryptionData()) {
                try {
                    val encUrl = Util.concatUriList(listUri, trackData.encryptionData.uri)
                    val encClient = ClientBuilder.new(Uri.parse(encUrl))
                    itm.encData = parseKey(encClient, trackData.encryptionData, index)
                } catch (e: Exception) {
                    throw IOException("Error parse encyption data: " + e.message)
                }
            }

            if (Settings.preloadSize) {
                val worker = Runnable {
                    var err = ""
                    for (i in 1..Settings.errorRepeat)
                        try {
                            val clientPS = ClientBuilder.new(Uri.parse(itm.url))
                            clientPS.connect()
                            itm.size = clientPS.getSize()
                            clientPS.close()
                            return@Runnable
                        } catch (e: Exception) {
                            err = e.message ?: ""
                        }
                    if (!err.isNullOrEmpty()) {
                        error = err
                        executor.shutdownNow()
                    }
                }
                executor.execute(worker)
            }
            list.items.add(itm)
            if (stop)
                return
        }
        if (Settings.preloadSize) {
            executor.shutdown()
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)
            if (!error.isNullOrEmpty())
                throw IOException(error)
        }
    }

    fun stop() {
        stop = true
        executor.shutdownNow()
    }

    fun parseKey(client: Client, eData: EncryptionData, index: Int): EncKey {
        var buf = Loader().loadBin(client)
        var iv: ByteArray

        if (eData.hasInitializationVector()) {
            iv = eData.initializationVector.toByteArray()
        } else {
            iv = toByteArray16(index)
        }

        val key = EncKey()
        key.key = buf
        key.iv = iv
        return key
    }

    private fun toByteArray16(i: Int): ByteArray {
        return byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0/**/, 0, 0, 0, 0, 0, 0, ((i shr 8) and 0xff).toByte(), (i and 0xff).toByte())
    }
}