package ru.yourok.m3u8loader.activitys.updaterActivity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_updater.*
import ru.yourok.dwl.updater.Updater
import ru.yourok.m3u8loader.App
import ru.yourok.m3u8loader.BuildConfig
import ru.yourok.m3u8loader.R
import ru.yourok.m3u8loader.theme.Theme
import kotlin.concurrent.thread

class UpdaterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Theme.set(this)
        setContentView(R.layout.activity_updater)

        current_info.text = "${getString(R.string.version)}: ${App.getContext().packageName} ${BuildConfig.VERSION_NAME}"
        buttonCheckUpdate.setOnClickListener {
            checkUpdate()
        }

        checkUpdate()
        update_button.setOnClickListener {
            Updater.download()
        }
    }


    private fun checkUpdate() {
        progress_bar.visibility = View.VISIBLE
        update_info.visibility = View.GONE
        thread {
            Updater.getVersionJS(true)
            Updater.getChangelogJS(true)

            if (Updater.hasNewUpdate()) {
                val js = Updater.getVersionJS(false)
                js?.let {
                    it.getJSONObject("update")?.let {
                        val packageName = it.getString("app_id")
                        val versionName = it.getString("version_name")
                        val buildDate = it.getString("build_date")
                        var changelog = ""
                        if (Updater.getChangelogJS(false)?.has(BuildConfig.VERSION_NAME)
                                        ?: false) {
                            val jsArr = Updater.getChangelogJS(false)!!.getJSONArray(BuildConfig.VERSION_NAME)
                            for (i in 0 until jsArr.length())
                                changelog += jsArr.getString(i) + "\n"
                        }
                        runOnUiThread {
                            findViewById<Button>(R.id.update_button).visibility = View.VISIBLE
                            findViewById<TextView>(R.id.update_info).setText("""
${getString(R.string.new_version)}:
$packageName
$versionName
$buildDate
$changelog
""")
                        }
                    }
                }
            } else {
                runOnUiThread {
                    findViewById<TextView>(R.id.update_info).setText(R.string.no_updates)
                    findViewById<Button>(R.id.update_button).visibility = View.GONE
                }
            }

            Updater.getChangelogJS(false)?.let { jsChangelog ->
                var changeLog = ""
                jsChangelog.keys().forEach {
                    changeLog += it + ":\n"
                    val jsArr = jsChangelog.getJSONArray(it)
                    for (i in 0 until jsArr.length())
                        changeLog += jsArr.getString(i) + "\n"
                    changeLog += "\n"
                }
                runOnUiThread {
                    findViewById<TextView>(R.id.change_log).setText(changeLog)
                }
            }


            runOnUiThread {
                progress_bar.visibility = View.GONE
                update_info.visibility = View.VISIBLE
            }
        }
    }
}
