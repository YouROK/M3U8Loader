package ru.yourok.dwl.utils

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import ru.yourok.dwl.list.Item
import ru.yourok.dwl.list.List
import ru.yourok.dwl.settings.Settings
import ru.yourok.m3u8loader.App
import java.io.File
import java.io.FileOutputStream

/**
 * Created by yourok on 09.12.17.
 */

object Saver {

    fun removeList(list: List) {
        val path = App.getContext().filesDir?.path
        val file = File(path, list.title + ".lst")
        if (file.exists())
            file.delete()
    }

    fun saveList(list: List) {
        try {
            synchronized(list) {
                val js = list2Json(list)
                val path = App.getContext().filesDir?.path
                val file = File(path, list.title + ".lst")
                val str = js.toString(1)
                val stream = FileOutputStream(file)
                stream.write(str.toByteArray())
                stream.flush()
                stream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveSettings() {
        try {
            val js = JSONObject()
            js.put("threads", Settings.threads)
            js.put("errorRepeat", Settings.errorRepeat)
            js.put("downloadPath", Settings.downloadPath)
            js.put("preloadSize", Settings.preloadSize)
            js.put("convertVideo", Settings.convertVideo)
            js.put("headers", JSONObject(Settings.headers))
            val path = App.getContext().filesDir
            val file = File(path, "settings.cfg")
            val str = js.toString(1)
            val stream = FileOutputStream(file)
            stream.write(str.toByteArray())
            stream.flush()
            stream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun list2Json(list: List): JSONObject {
        val js = JSONObject()
        js.put("url", list.url)
        js.put("filePath", list.filePath)
        js.put("title", list.title)
        js.put("bandwidth", list.bandwidth)
        js.put("isConvert", list.isConvert)
        js.put("isPlayed", list.isPlayed)
        js.put("subsUrl", list.subsUrl)
        js.put("items", items2Json(list.items))

        return js
    }

    private fun items2Json(items: kotlin.collections.List<Item>): JSONArray {
        val jsarr = JSONArray()

        items.forEach {
            val js = JSONObject()
            js.put("index", it.index)
            js.put("url", it.url)
            js.put("size", it.size)
            js.put("duration", it.duration.toDouble())
            js.put("isLoad", it.isLoad)
            js.put("isComplete", it.isComplete)
            it.encData?.key?.let {
                val key = Base64.encodeToString(it, Base64.NO_PADDING or Base64.NO_WRAP)
                js.put("encDataKey", key)
            }
            it.encData?.key?.let {
                val iv = Base64.encodeToString(it, Base64.NO_PADDING or Base64.NO_WRAP)
                js.put("encDataIV", iv)
            }
            jsarr.put(js)
        }
        return jsarr
    }
}