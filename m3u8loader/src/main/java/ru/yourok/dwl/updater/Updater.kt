package ru.yourok.dwl.updater

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.design.widget.Snackbar
import android.view.View
import org.json.JSONArray
import org.json.JSONObject
import ru.yourok.dwl.client.Http
import ru.yourok.m3u8loader.App
import ru.yourok.m3u8loader.BuildConfig
import ru.yourok.m3u8loader.R


/**
 * Created by yourok on 08.12.17.
 */
object Updater {
    private var jsUpdate: JSONObject? = null


    fun check(): Boolean {
        try {
            val http = Http(Uri.parse("https://raw.githubusercontent.com/YouROK/M3U8Loader/1.3.x/out/loader/release/output.json"))
            http.connect()
            val strJS = http.getInputStream()?.bufferedReader()?.readText() ?: ""
            http.close()
            if (strJS.isNotEmpty()) {
                jsUpdate = JSONArray(strJS).getJSONObject(0)
                jsUpdate?.let {
                    val js = it.getJSONObject("apkInfo")
                    val ver = js?.getInt("versionCode")
                    if (BuildConfig.VERSION_CODE < ver ?: 0) {
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun showSnackbar(mainActivity: Activity) {
        if (isChecked()) {
            try {
                Snackbar.make(mainActivity.findViewById(R.id.main_layout), R.string.permission_storage_msg, Snackbar.LENGTH_LONG)
                        .setText(R.string.download_new_version)
                        .setAction(R.string.download, object : View.OnClickListener {
                            override fun onClick(p0: View?) {
                                download()
                            }
                        }).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun download() {
        if (isChecked()) {
            var path = jsUpdate?.getString("path") ?: ""
            if (path.isNotEmpty()) {
                path = "https://raw.githubusercontent.com/YouROK/M3U8Loader/1.3.x/out/loader/release/" + path
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(path))
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                App.getContext().startActivity(browserIntent)
            }
        }
    }

    fun isChecked(): Boolean = jsUpdate != null

    fun hasNewUpdate(): Boolean {
        jsUpdate?.let {
            val js = it.getJSONObject("apkInfo")
            val ver = js?.getInt("versionCode")
            if (BuildConfig.VERSION_CODE < ver ?: 0) {
                return true
            }
        }
        return false
    }
}