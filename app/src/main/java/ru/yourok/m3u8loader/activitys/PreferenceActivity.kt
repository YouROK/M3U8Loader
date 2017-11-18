package ru.yourok.m3u8loader.activitys

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.*
import kotlinx.android.synthetic.main.activity_preference.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.sdk25.coroutines.onClick
import ru.yourok.dwl.settings.Preferences
import ru.yourok.dwl.settings.Settings
import ru.yourok.dwl.utils.Utils
import ru.yourok.m3u8loader.R
import ru.yourok.m3u8loader.dialogs.OpenFileDialog
import ru.yourok.m3u8loader.theme.Theme


/**
 * Created by yourok on 18.11.17.
 */
class PreferenceActivity : AppCompatActivity() {
    private var lastTheme = true

    companion object {
        val Result = 1000
        var changTheme = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Theme.set(this)
        setContentView(R.layout.activity_preference)
        lastTheme = Preferences.get("ThemeDark", true) as Boolean
        if (savedInstanceState == null)
            loadSettings()


        findViewById<ImageButton>(R.id.imageButtonSearchDirectory).onClick {
            OpenFileDialog(this@PreferenceActivity)
                    .setOnlyFoldersFilter()
                    .setAccessDeniedMessage(getString(R.string.error_directory_permission))
                    .setOpenDialogListener {
                        editTextDirectoryPath.setText(it)
                    }
                    .show()
        }

        try {
            val pInfo = packageManager!!.getPackageInfo(packageName, 0)
            val version = "YouROK " + getText(R.string.app_name) + " " + pInfo.versionName
            (findViewById<TextView>(R.id.textViewVersion)).text = version
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, resources.getStringArray(R.array.players_names))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerChoosePlayer.adapter = adapter

        findViewById<Button>(R.id.buttonOk).onClick {
            saveSettings()
            finish()
        }

        findViewById<Button>(R.id.buttonCancel).onClick { finish() }

        findViewById<Button>(R.id.buttonConfConv).onClick {
            startActivity(intentFor<ConvertConfigActivity>())
        }
    }

    override fun onPause() {
        super.onPause()
        changTheme = Preferences.get("ThemeDark", true) as Boolean != lastTheme
    }

    private fun loadSettings() {
        findViewById<EditText>(R.id.editTextDirectoryPath)?.setText(Settings.downloadPath)
        findViewById<EditText>(R.id.editTextThreads)?.setText(Settings.threads.toString())
        findViewById<EditText>(R.id.editTextRepeatError)?.setText(Settings.errorRepeat.toString())
        findViewById<EditText>(R.id.editTextCookies)?.setText(Settings.headers["Cookie"])
        findViewById<EditText>(R.id.editTextUseragent)?.setText(Settings.headers["User-Agent"])
        findViewById<CheckBox>(R.id.checkboxUseFFMPEG)?.setChecked(Settings.useFFMpeg)
        findViewById<CheckBox>(R.id.checkboxLoadItemsSize)?.setChecked(Settings.preloadSize)
        findViewById<CheckBox>(R.id.checkBoxChooseTheme)?.setChecked(Preferences.get("ThemeDark", true) as Boolean)
        findViewById<Spinner>(R.id.spinnerChoosePlayer)?.setSelection(Preferences.get("Player", 0) as Int)
    }

    private fun saveSettings() {
        Settings.downloadPath = editTextDirectoryPath.text.toString()
        Settings.threads = editTextThreads.text.toString().toInt()
        Settings.errorRepeat = editTextRepeatError.text.toString().toInt()
        Settings.headers["Cookie"] = editTextCookies.text.toString()
        Settings.headers["User-Agent"] = editTextUseragent.text.toString()
        Settings.useFFMpeg = checkboxUseFFMPEG.isChecked!!
        Settings.preloadSize = checkboxLoadItemsSize.isChecked!!
        val ch = checkBoxChooseTheme.isChecked
        Preferences.set("ThemeDark", ch)
        val sel = spinnerChoosePlayer.selectedItemPosition
        Preferences.set("Player", sel)

        Utils.saveSettings()
    }
}