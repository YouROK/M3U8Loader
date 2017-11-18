package ru.yourok.m3u8loader.theme

import android.app.Activity
import android.content.Intent
import ru.yourok.dwl.settings.Preferences
import ru.yourok.m3u8loader.R


object Theme {
    fun set(activity: Activity) {
        when (Preferences.get("ThemeDark", true) as Boolean) {
            true ->
                setDarkTheme(activity)
            else ->
                setLightTheme(activity)
        }
    }

    fun setDarkTheme(activity: Activity) {
        activity.setTheme(R.style.AppThemeDark)
    }

    fun setLightTheme(activity: Activity) {
        activity.setTheme(R.style.AppThemeLight)
    }

    fun changeNow(activity: Activity, dark: Boolean) {
        Preferences.set("ThemeDark", dark)
        val intent = Intent(activity, activity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }
}