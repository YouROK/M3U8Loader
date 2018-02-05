package ru.yourok.dwl.utils

import android.util.Base64
import org.json.JSONObject
import ru.yourok.dwl.list.EncKey
import ru.yourok.dwl.list.Item
import ru.yourok.dwl.list.List
import ru.yourok.dwl.settings.Settings
import ru.yourok.m3u8loader.App
import java.io.File
import java.io.FileInputStream

/**
 * Created by yourok on 09.12.17.
 */
object Loader {

    fun loadSettings() {
        try {
            val path = App.getContext().filesDir
            val file = File(path, "settings.cfg")
            if (!file.exists())
                return
            val stream = FileInputStream(file)
            val str = stream.bufferedReader().use { it.readText() }
            stream.close()

            val js = JSONObject(str)

            Settings.threads = js.getInt("threads")
            Settings.errorRepeat = js.getInt("errorRepeat")
            Settings.downloadPath = js.getString("downloadPath")
            Settings.preloadSize = js.getBoolean("preloadSize")
            Settings.convertVideo = js.getBoolean("convertVideo")
            Settings.headers = mutableMapOf()
            if (js.has("headers")) {
                val jsH = js.getJSONObject("headers")
                val keys = jsH.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val v = jsH.getString(key)
                    Settings.headers.put(key, v)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadLists(): MutableList<List>? {
        try {
            val lists: MutableList<List> = mutableListOf()
            val path = App.getContext().filesDir
            if (path != null)
                path.walk().forEach {
                    if (it.path.endsWith(".lst")) {
                        if (it.isFile) {
                            try {
                                val list = loadList(it.canonicalPath)
                                lists.add(list)
                            } catch (e: Exception) {
                                it.delete()
                            }
                        }
                    }
                }
            return lists
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun loadList(filePath: String): List {
        val file = File(filePath)
        val stream = FileInputStream(file)
        val str = stream.bufferedReader().use { it.readText() }
        stream.close()
        val js = JSONObject(str)
        val list = List()
        list.url = js.getString("url")
        list.filePath = js.getString("filePath")
        list.title = js.getString("title")
        list.bandwidth = js.get("bandwidth", 0)
        list.isConvert = js.get("isConvert", false)
        list.isPlayed = js.get("isPlayed", false)
        list.subsUrl = js.get("subsUrl", "")
        val jsarr = js.getJSONArray("items")
        for (i in 0 until jsarr.length()) {
            val itm = Item()
            val jsitm = jsarr.getJSONObject(i)
            itm.index = jsitm.getInt("index")
            itm.url = jsitm.getString("url")
            itm.loaded = jsitm.get("loaded", 0L)
            itm.size = jsitm.getLong("size")
            itm.duration = jsitm.getDouble("duration").toFloat()
            itm.isLoad = jsitm.getBoolean("isLoad")
            itm.isComplete = jsitm.getBoolean("isComplete")
            if (jsitm.has("encDataKey")) {
                itm.encData = EncKey()
                val keyStr = jsitm.getString("encDataKey")
                val key = Base64.decode(keyStr, Base64.NO_PADDING or Base64.NO_WRAP)
                itm.encData!!.key = key
            }

            if (jsitm.has("encDataIV")) {
                val ivStr = jsitm.getString("encDataIV")
                val iv = Base64.decode(ivStr, Base64.NO_PADDING or Base64.NO_WRAP)
                itm.encData!!.iv = iv
            }
            list.items.add(itm)
        }
        return list
    }
}

private fun <T> JSONObject.get(name: String, def: T): T {
    if (!this.has(name))
        return def

    if (this.has(name))
        return this.get(name) as T
    return def
}