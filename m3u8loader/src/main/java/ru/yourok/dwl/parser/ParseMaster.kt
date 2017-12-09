package ru.yourok.dwl.parser

import android.net.Uri
import com.iheartradio.m3u8.data.MasterPlaylist
import ru.yourok.dwl.client.Util
import ru.yourok.dwl.list.List

/**
 * Created by yourok on 09.11.17.
 */
class ParseMaster {
    fun parse(url: Uri, masterPlaylist: MasterPlaylist): MutableList<List> {
        val retList = mutableListOf<List>()
        masterPlaylist.playlists.forEach {
            val list = List()
            list.url = Util.concatUriList(url, it.uri)
            list.title = it.streamInfo.closedCaptions ?: ""
            list.bandwidth = it.streamInfo.bandwidth
            retList.add(list)
        }
        return retList
    }
}