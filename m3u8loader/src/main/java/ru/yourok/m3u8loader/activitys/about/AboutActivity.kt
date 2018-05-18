package ru.yourok.m3u8loader.activitys.about

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import ru.yourok.m3u8loader.BuildConfig
import ru.yourok.m3u8loader.R
import ru.yourok.m3u8loader.theme.Theme

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Theme.set(this)
        setContentView(R.layout.activity_about)
        val version = "" + getText(R.string.app_name) + " ${BuildConfig.FLAVOR} ${BuildConfig.VERSION_NAME}"
        (findViewById<TextView>(R.id.textViewInfo)).text = version
    }
}
