package ru.yourok.m3u8loader

import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Build.MANUFACTURER
import android.os.Build.MODEL
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import android.util.Log
import ru.yourok.dwl.settings.Settings
import ru.yourok.dwl.utils.Utils
import java.io.PrintWriter
import java.io.StringWriter


class App : Application() {
    override fun onCreate() {
        super.onCreate()

//        Thread.setDefaultUncaughtExceptionHandler { thread, e -> handleUncaughtException(thread, e) }

        Settings.context = applicationContext

        Utils.loadSettings()

        if (Settings.downloadPath.isNullOrEmpty())
            Settings.downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
    }

    private fun handleUncaughtException(thread: Thread, e: Throwable) {
        val sw = StringWriter()
        e.printStackTrace(PrintWriter(sw))
        val exceptionAsString = sw.toString()
        Log.e("M3U8LoaderCrash", exceptionAsString)

        var msg = ""
        msg += "Manufacture: " +MANUFACTURER + "\n"
        msg += "Model: " +MODEL + "\n"
        msg += "Api: " + SDK_INT.toString() + "\n"
        msg += "OS: " +Build.VERSION.RELEASE + "\n"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            msg += Build.SUPPORTED_ABIS.joinToString() + "\n"
        else
            msg += Build.CPU_ABI + "\n\n"
        msg += exceptionAsString

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "plain/text"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("8yourok8@gmail.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, "Crash M3U8Loader")
        intent.putExtra(Intent.EXTRA_TEXT, msg)
        startActivity(intent)
        System.exit(1);
    }
}
