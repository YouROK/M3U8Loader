package ru.yourok.m3u8converter.utils

import android.app.Activity
import android.content.Intent
import ru.yourok.m3u8converter.R
import java.util.*
import kotlin.concurrent.schedule

/**
 * Created by yourok on 01.12.17.
 */
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
        Timer().schedule(500) {
            var intent = Intent(activity, activity::class.java)
            activity.finish()
            activity.overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out)
            activity.startActivity(intent)
        }
    }
}