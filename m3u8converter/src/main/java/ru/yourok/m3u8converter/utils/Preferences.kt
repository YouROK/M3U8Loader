package ru.yourok.m3u8converter.utils

import android.preference.PreferenceManager
import ru.yourok.m3u8converter.App

/**
 * Created by yourok on 01.12.17.
 */

object Preferences {
    val DocumentRootUri = "DocumentRootUri"


    fun get(name: String, def: Any): Any? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext())
        if (prefs.all.containsKey(name))
            return prefs.all[name]
        return def
    }

    fun set(name: String, value: Any?) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext())
        when (value) {
            is String -> prefs.edit().putString(name, value).apply()
            is Boolean -> prefs.edit().putBoolean(name, value).apply()
            is Float -> prefs.edit().putFloat(name, value).apply()
            is Int -> prefs.edit().putInt(name, value).apply()
            is Long -> prefs.edit().putLong(name, value).apply()
            is MutableSet<*>? -> prefs.edit().putStringSet(name, value as MutableSet<String>?).apply()
            else -> prefs.edit().putString(name, value.toString()).apply()
        }
    }
}