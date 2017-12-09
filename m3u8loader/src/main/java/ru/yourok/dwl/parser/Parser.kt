package ru.yourok.dwl.parser

import android.net.Uri
import com.iheartradio.m3u8.*
import ru.yourok.dwl.client.Client
import ru.yourok.dwl.client.ClientBuilder
import ru.yourok.dwl.list.List
import ru.yourok.dwl.settings.Settings
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException

/**
 * Created by yourok on 07.11.17.
 */
class Parser(val name: String, val url: String, val downloadPath: String) {

    var stop = false
    var parseMedia: ParseMedia? = null

    fun parse(): MutableList<List> {
        var clientEx: Client? = null
        try {
            stop = false
            val retList = mutableListOf<List>()

            val client = ClientBuilder.new(Uri.parse(url))
            clientEx = client

            for (i in 0..Settings.errorRepeat)
                try {
                    client.connect()
                    break
                } catch (e: Exception) {
                    if (i == Settings.errorRepeat) {
                        throw e
                    }
                }

            val listStr = client.getInputStream()?.bufferedReader().use { it?.readText() ?: "" }
            client.close()
            val parser = PlaylistParser(ByteArrayInputStream(listStr.toByteArray()), Format.EXT_M3U, Encoding.UTF_8, ParsingMode.LENIENT)
            val playList = parser.parse()

            if (playList.hasMasterPlaylist()) {
                val mList = ParseMaster().parse(Uri.parse(client.getUrl()), playList.masterPlaylist)
                retList.addAll(mList)
            }
            if (playList.hasMediaPlaylist()) {
                val list = List()
                list.url = client.getUrl()
                list.title = name
                retList.add(list)
            }
            for (i in 0 until retList.size) {
                val list = retList[i]
                if (list.title.isEmpty()) {
                    var band = list.bandwidth
                    if (band == 0)
                        band = i
                    list.title = name + "_" + band
                }
                list.filePath = File(downloadPath, list.title + ".mp4").canonicalPath
                parseMedia = ParseMedia(downloadPath)
                retList.addAll(parseMedia!!.parse(list))
                if (list.items.size == 0) {
                    retList.remove(list)
                }
                if (stop) break
            }
            return retList

        } catch (e: ParseException) {
            e.printStackTrace()
            throw java.text.ParseException("Error parse list: " + clientEx?.getUrl() + " " + e.message, 0)
        } catch (e: Exception) {
            e.printStackTrace()
            throw IOException("Error loadList list: " + clientEx?.getUrl() + " " + clientEx?.getErrorMessage() + " " + e.message)
        }
    }

    fun stop() {
        stop = true
        if (parseMedia != null)
            parseMedia!!.stop()
    }
}