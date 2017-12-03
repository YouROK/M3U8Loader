package ru.yourok.dwl.settings

import android.preference.PreferenceManager
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.yourok.m3u8loader.App


/**
 * Created by yourok on 07.11.17.
 */
@JsonIgnoreProperties("context")
object Settings {
    var threads: Int = 20
    var errorRepeat: Int = 5
    var downloadPath: String = ""
    var preloadSize: Boolean = false
    var convertVideo: Boolean = false

    var headers: MutableMap<String, String> = mutableMapOf()
}

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