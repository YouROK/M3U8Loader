package ru.yourok.m3u8loader

import android.app.Application
import ru.yourok.dwl.settings.Settings

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Settings.context = applicationContext
    }
}
