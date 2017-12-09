package ru.yourok.dwl.updater

import android.content.Intent
import android.net.Uri
import org.json.JSONObject
import ru.yourok.dwl.client.Http
import ru.yourok.m3u8loader.App
import ru.yourok.m3u8loader.BuildConfig
import java.io.FileOutputStream


/**
 * Created by yourok on 08.12.17.
 */
object Updater {
    private var jsUpdate: JSONObject? = null


    fun check(): String {
        var version = ""
        try {
            val http = Http(Uri.parse("https://raw.githubusercontent.com/YouROK/M3U8Loader/1.3.x/out/loader/release/output.json"))
            http.connect()
            val strJS = http.getInputStream()?.bufferedReader().use { it?.readText() ?: "" }
            http.close()
            FileOutputStream("/sdcard/out.js").write(strJS.toByteArray())
            if (strJS.isNotEmpty()) {
                jsUpdate = JSONObject(strJS)
                val js = jsUpdate?.getJSONObject("apkInfo")
                val ver = js?.getInt("versionCode")
                if (BuildConfig.VERSION_CODE < ver ?: 0) {
                    version = ver.toString()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return version
    }

    fun open() {
        if (jsUpdate != null) {
            try {
                val js = jsUpdate?.getJSONObject("apkInfo")
                var path = js?.getString("path") ?: ""
                if (path.isNotEmpty()) {
                    path = "https://raw.githubusercontent.com/YouROK/M3U8Loader/1.3.x/out/loader/release/" + path
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(path))
                    App.getContext().startActivity(browserIntent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}