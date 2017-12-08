package ru.yourok.m3u8loader

import android.app.Application
import android.content.Context
import android.os.Environment
import ru.yourok.dwl.manager.Manager
import ru.yourok.dwl.settings.Settings
import ru.yourok.dwl.utils.Utils


class App : Application() {
    companion object {
        private lateinit var contextApp: Context

        fun getContext(): Context {
            return contextApp
        }
    }


    override fun onCreate() {
        super.onCreate()

        ACR.get(this)
                .setEmailAddresses("8yourok8@gmail.com")
                .setEmailSubject(getString(R.string.app_name) + " Crash Report")
                .start({ Manager.saveLists() })

        contextApp = applicationContext

        Utils.loadSettings()

        if (Settings.downloadPath.isEmpty())
            Settings.downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
    }
}
