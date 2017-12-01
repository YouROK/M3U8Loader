package ru.yourok.dwl.parser

import android.net.Uri
import com.iheartradio.m3u8.Encoding
import com.iheartradio.m3u8.Format
import com.iheartradio.m3u8.ParsingMode
import com.iheartradio.m3u8.PlaylistParser
import com.iheartradio.m3u8.data.EncryptionData
import ru.yourok.dwl.client.Client
import ru.yourok.dwl.client.ClientBuilder
import ru.yourok.dwl.client.Util
import ru.yourok.dwl.list.EncKey
import ru.yourok.dwl.list.Item
import ru.yourok.dwl.list.List
import java.io.IOException

/**
 * Created by yourok on 09.11.17.
 */
class ParseMedia(val downloadPath: String) {
    var stop = false
    var error = ""

    fun parse(list: List): MutableList<List> {
        val client = ClientBuilder.new(Uri.parse(list.url))
        client.connect()
        val parser = PlaylistParser(client.getInputStream(), Format.EXT_M3U, Encoding.UTF_8, ParsingMode.LENIENT)
        val playList = parser.parse()
        client.close()
        val retList = mutableListOf<List>()

        if (playList.hasMediaPlaylist()) {
            val listUri = Uri.parse(list.url)
            playList.mediaPlaylist.tracks.forEachIndexed { index, trackData ->
                val itm = Item()
                itm.index = index
                itm.isLoad = true
                itm.url = Util.concatUriList(listUri, trackData.uri.toString())
                itm.duration = trackData.trackInfo.duration
                if (trackData.isEncrypted) {
                    try {
                        val encUrl = Util.concatUriList(listUri, trackData.encryptionData.uri.toString())
                        val encClient = ClientBuilder.new(Uri.parse(encUrl))
                        itm.encData = parseKey(encClient, trackData.encryptionData, index)
                    } catch (e: Exception) {
                        throw IOException("Error parse encyption data: " + e.message)
                    }
                }

                if (itm.url.toLowerCase().endsWith("m3u8") || itm.url.toLowerCase().endsWith("m3u")) {
                    val name = if (!trackData.trackInfo.title.isNullOrEmpty())
                        trackData.trackInfo.title
                    else
                        list.info.title
                    retList.addAll(Parser(name, itm.url, downloadPath).parse())
                } else {
                    list.items.add(itm)
                    if (stop)
                        return retList
                }
            }
        }
        return retList
    }

    fun stop() {
        stop = true
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