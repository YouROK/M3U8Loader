package ru.yourok.m3u8loader.player

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.support.v4.content.FileProvider
import android.widget.Toast
import ru.yourok.dwl.settings.Preferences
import ru.yourok.dwl.storage.Document
import ru.yourok.m3u8loader.BuildConfig
import java.io.File


/**
 * Created by yourok on 20.11.17.
 */
class PlayIntent(val context: Context) {
    fun start(filename: String, title: String) {
        try {
            val player = Preferences.get("Player", 0) as Int
            Handler(Looper.getMainLooper()).post {
                val intent = when (player) {
                //Chooser
                    0 -> getChooser(filename, title)
                    1 -> getDefaultPlayer(filename, title)
                //MX Player
                    2 -> getMXPlayer(context, filename, title)
                    3 -> getKodiPlayer(context, filename, title)
                //Default
                    else -> getDefaultPlayer(filename, title)
                }

                val pm = context.getPackageManager()
                if (intent.resolveActivity(pm) != null) {
                    context.startActivity(intent)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error open player: " + e.message, Toast.LENGTH_SHORT)
        }
    }

    private fun getChooser(filename: String, title: String): Intent {
        return Intent.createChooser(getDefaultPlayer(filename, title), "   ")
    }

    private fun getDefaultPlayer(filename: String, title: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        var uri = getUri(filename)
        intent.setDataAndType(uri, "video/mp4")
        intent.putExtra("title", title)
        return intent
    }

    private fun getMXPlayer(context: Context, filename: String, title: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        var pkg = ""

        val pm = context.getPackageManager()
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (packageInfo in packages)
            if (packageInfo.packageName == "com.mxtech.videoplayer.pro") {
                pkg = "com.mxtech.videoplayer.pro"
                break
            }

        if (pkg.isEmpty())
            for (packageInfo in packages)
                if (packageInfo.packageName == "com.mxtech.videoplayer.ad") {
                    pkg = "com.mxtech.videoplayer.ad"
                    break
                }

        if (pkg.isEmpty())
            return getChooser(filename, title)

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val uri = getUri(filename)
        intent.setDataAndType(uri, "video/mp4")
        intent.putExtra("title", title)
        intent.`package` = pkg
        return intent
    }

    private fun getKodiPlayer(context: Context, filename: String, title: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        var pkg = ""

        val pm = context.getPackageManager()
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (packageInfo in packages)
            if (packageInfo.packageName == "org.xbmc.kodi") {
                pkg = "org.xbmc.kodi"
                break
            }

        if (pkg.isEmpty())
            return getChooser(filename, title)

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val uri = getUri(filename)
        intent.setDataAndType(uri, "video/mp4")
        intent.putExtra("title", title)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.`package` = pkg
        return intent
    }

    private fun getUri(filename: String): Uri {
        var uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", File(filename))
        if (!File(filename).canWrite())
            uri = Document.openFile(filename)?.uri
        return uri
    }
}