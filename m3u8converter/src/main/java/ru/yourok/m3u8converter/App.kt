package ru.yourok.m3u8converter

import android.app.Application
import android.content.Context

/**
 * Created by yourok on 28.11.17.
 */
class App : Application() {
    companion object {
        private lateinit var contextApp: Context

        fun getContext(): Context {
            return contextApp
        }
    }

    override fun onCreate() {
        super.onCreate()
        contextApp = applicationContext
    }
}