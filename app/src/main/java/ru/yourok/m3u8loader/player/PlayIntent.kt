package ru.yourok.m3u8loader.player

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES.M
import android.support.v4.content.FileProvider
import ru.yourok.dwl.settings.Preferences
import java.io.File


/**
 * Created by yourok on 20.11.17.
 */
object PlayIntent {
    fun start(context: Context, filename: String, title: String) {
        val player = Preferences.get("Player", 0) as Int
        var fpath: Uri = Uri.fromFile(File(filename))
        if (Build.VERSION.SDK_INT > M)
            FileProvider.getUriForFile(context, "ru.yourok.m3u8loader", File(filename))

        val intent = when (player) {
        //Chooser
            0 -> getChooser(fpath, title)
            1 -> getDefaultPlayer(fpath, title)
        //MX Player
            2 -> getMXPlayer(context, fpath, title)
            3 -> getKodiPlayer(context, fpath, title)
        //Default
            else -> getDefaultPlayer(fpath, title)
        }

        if (Build.VERSION.SDK_INT > M)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        if (intent.resolveActivity(context.getPackageManager()) != null)
            context.startActivity(intent)
    }

    private fun getChooser(filename: Uri, title: String): Intent {
        return Intent.createChooser(getDefaultPlayer(filename, title), "   ")
    }

    private fun getDefaultPlayer(filename: Uri, title: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(filename, "video/mp4")
        intent.putExtra("title", title)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

    private fun getMXPlayer(context: Context, filename: Uri, title: String): Intent {
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

        intent.setDataAndType(filename, "video/*")
        intent.putExtra("title", title)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.`package` = pkg
        return intent
    }

    private fun getKodiPlayer(context: Context, filename: Uri, title: String): Intent {
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

        intent.setDataAndType(filename, "video/*")
        intent.putExtra("title", title)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.`package` = pkg
        return intent
    }
}