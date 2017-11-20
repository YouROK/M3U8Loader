package ru.yourok.m3u8loader

import android.app.Application
import android.os.Environment
import ru.yourok.dwl.settings.Settings
import ru.yourok.dwl.utils.Utils

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        Settings.context = applicationContext

        Utils.loadSettings()

        if (Settings.downloadPath.isNullOrEmpty())
            Settings.downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
    }
}
