package ru.yourok.dwl.updater

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.design.widget.Snackbar
import android.view.View
import org.json.JSONObject
import ru.yourok.dwl.client.Http
import ru.yourok.m3u8loader.App
import ru.yourok.m3u8loader.BuildConfig
import ru.yourok.m3u8loader.R
import ru.yourok.m3u8loader.activitys.updaterActivity.UpdaterActivity


/**
 * Created by yourok on 08.12.17.
 */
object Updater {
    private var jsVerson: JSONObject? = null
    private var jsChangelog: JSONObject? = null
    private var lastCheck: Long = 0

    private val updateVersionLink = "https://raw.githubusercontent.com/YouROK/M3U8Loader/1.3.x/dist/${BuildConfig.APPLICATION_ID}_${BuildConfig.FLAVOR}/version.json"
    private val changelogLink = "https://raw.githubusercontent.com/YouROK/M3U8Loader/1.3.x/dist/changelog.json"

    fun getVersionJS(force: Boolean): JSONObject? {
        if (System.currentTimeMillis() - lastCheck < 86400000 && !force)
            jsVerson?.let { return it }

        try {
            val http = Http(Uri.parse(updateVersionLink))
            http.connect()
            val strJS = http.getInputStream()?.bufferedReader()?.readText() ?: ""
            http.close()
            if (strJS.isNotEmpty()) {
                lastCheck = System.currentTimeMillis()
                jsVerson = JSONObject(strJS)
                jsVerson?.let {
                    val js = it.getJSONObject("update")
                    val ver = js?.getInt("version_code")
                    if (BuildConfig.VERSION_CODE < ver ?: 0) {
                        return it
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getChangelogJS(force: Boolean): JSONObject? {
        if (System.currentTimeMillis() - lastCheck < 86400000 && !force)
            jsChangelog?.let { return it }

        try {
            val http = Http(Uri.parse(changelogLink))
            http.connect()
            val strJS = http.getInputStream()?.bufferedReader()?.readText() ?: ""
            http.close()
            if (strJS.isNotEmpty()) {
                jsChangelog = JSONObject(strJS)
                jsChangelog?.let {
                    return it
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun download() {
        try {
            getVersionJS(false)?.let {
                val js = it.getJSONObject("update")
                val path = js.getString("link_github") ?: ""
                if (path.isNotEmpty()) {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(path))
                    browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    App.getContext().startActivity(browserIntent)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hasNewUpdate(): Boolean {
        try {
            getVersionJS(false)?.let {
                val js = it.getJSONObject("update")
                val ver = js?.getInt("version_code") ?: 0
                val path = js.getString("link_github") ?: ""
                if (BuildConfig.VERSION_CODE < ver && path.isNotEmpty()) {
                    try {
                        val http = Http(Uri.parse(path))
                        http.connect()
                        http.close()
                    } catch (e: Exception) {
                        return false
                    }
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun showSnackbar(mainActivity: Activity) {
        if (hasNewUpdate()) {
            try {
                Snackbar.make(mainActivity.findViewById(R.id.main_layout), R.string.permission_storage_msg, Snackbar.LENGTH_LONG)
                        .setText(R.string.release_new_version)
                        .setAction(R.string.download, object : View.OnClickListener {
                            override fun onClick(p0: View?) {
                                mainActivity.startActivity(Intent(mainActivity, UpdaterActivity::class.java))
                            }
                        }).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}