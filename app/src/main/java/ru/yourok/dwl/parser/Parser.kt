package ru.yourok.dwl.parser

import android.net.Uri
import com.iheartradio.m3u8.*
import com.iheartradio.m3u8.data.Playlist
import ru.yourok.dwl.client.Client
import ru.yourok.dwl.client.ClientBuilder
import ru.yourok.dwl.list.List
import ru.yourok.dwl.settings.Settings
import java.io.File
import java.io.IOException

/**
 * Created by yourok on 07.11.17.
 */
class Parser(val name: String, val url: String) {

    var stop = false
    var parseMedia: ParseMedia? = null

    fun parse(): MutableList<List> {
        stop = false
        val retList = mutableListOf<List>()
        var playList: Playlist? = null
        var client: Client? = null
        try {
            client = ClientBuilder.new(Uri.parse(url))
            client.connect()
            val parser = PlaylistParser(client.getInputStream(), Format.EXT_M3U, Encoding.UTF_8, ParsingMode.LENIENT)
            playList = parser.parse()
            client.close()
        } catch (e: ParseException) {
            throw java.text.ParseException("Error parse list: " + client!!.getUrl(), 0)
        } catch (e: Exception) {
            throw IOException("Error loadList list: " + client!!.getUrl() + " " + client!!.getErrorMessage() + " " + e.message)
        }

        if (playList.hasMasterPlaylist()) {
            retList.addAll(ParseMaster().parse(Uri.parse(client.getUrl()), playList.masterPlaylist))
        }
        if (playList.hasMediaPlaylist()) {
            val list = List()
            list.url = client.getUrl()
            list.info.title = name
            list.filePath = File(Settings.downloadPath, list.info.title + ".mp4").path
            retList.add(list)
        }
        retList.forEachIndexed { index, list ->
            if (list.info.title.isNullOrEmpty()) {
                var band = list.info.bandwidth
                if (band == 0)
                    band = index
                list.info.title = name + "_" + band
            }
            list.filePath = File(Settings.downloadPath, list.info.title + ".mp4").path
            parseMedia = ParseMedia()
            parseMedia!!.parse(list)
            if (stop) return@forEachIndexed
        }
        return retList
    }

    fun stop() {
        stop = true
        if (parseMedia != null)
            parseMedia!!.stop()
    }
}