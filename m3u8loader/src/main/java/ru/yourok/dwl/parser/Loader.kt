package ru.yourok.dwl.parser

import android.net.Uri
import com.iheartradio.m3u8.*
import com.iheartradio.m3u8.data.Playlist
import ru.yourok.dwl.client.Client
import ru.yourok.dwl.client.ClientBuilder
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Created by yourok on 08.11.17.
 */
class Loader {
    fun loadList(url: Uri): Playlist {
        val client = ClientBuilder.new(url)
        return loadList(client)
    }

    fun loadList(client: Client): Playlist {
        try {
            client.connect()
            val parser = PlaylistParser(client.getInputStream(), Format.EXT_M3U, Encoding.UTF_8, ParsingMode.LENIENT)
            val playlist = parser.parse()
            client.close()
            return playlist
        } catch (e: ParseException) {
            throw java.text.ParseException("Error parse list: " + client.getUrl(), 0)
        } catch (e: Exception) {
            throw IOException("Error loadList list: " + client.getUrl() + " " + client.getErrorMessage())
        }
    }

    fun loadBin(client: Client): ByteArray {
        client.connect()
        val ba = ByteArrayOutputStream()
        client.getInputStream()!!.copyTo(ba)
        return ba.toByteArray()
    }
}