package ru.yourok.m3u8loader.fragments.preference

import android.app.Activity
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
class PreferenceFragment : Fragment() {

    companion object {
        fun newInstance(): PreferenceFragment {
            return PreferenceFragment()
        }
    }

    var lastTheme = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_preference, null)
        view.findViewById<ImageButton>(R.id.imageButtonSearchDirectory).onClick {
            OpenFileDialog(activity)
                    .setOnlyFoldersFilter()
                    .setAccessDeniedMessage(getString(R.string.error_directory_permission))
                    .setOpenDialogListener {
                        view.findViewById<EditText>(R.id.editTextDirectoryPath).setText(it)
                    }
                    .show()
        }

        try {
            val pInfo: PackageInfo
            pInfo = activity!!.packageManager!!.getPackageInfo(activity?.packageName, 0)
            val version = "YouROK " + getText(R.string.app_name) + " " + pInfo.versionName
            (view.findViewById<TextView>(R.id.textViewVersion)).text = version
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        val adapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, resources.getStringArray(R.array.players_names))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        view.findViewById<Spinner>(R.id.spinnerChoosePlayer)?.adapter = adapter

        view.findViewById<Button>(R.id.buttonOk).onClick {
            saveSettings()
            finish()
        }

        view.findViewById<Button>(R.id.buttonCancel).onClick { finish() }

        return view
    }

    override fun onStart() {
        super.onStart()
        lastTheme = Preferences.get("ThemeDark", true) as Boolean
        loadSettings()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (lastTheme != Preferences.get("ThemeDark", true) as Boolean)
            Theme.changeNow(activity as Activity, Preferences.get("ThemeDark", true) as Boolean)
    }

    private fun finish() {
        activity?.supportFragmentManager?.popBackStack()
    }

    private fun loadSettings() {
        view?.findViewById<EditText>(R.id.editTextDirectoryPath)?.setText(Settings.downloadPath)
        view?.findViewById<EditText>(R.id.editTextThreads)?.setText(Settings.threads.toString())
        view?.findViewById<EditText>(R.id.editTextRepeatError)?.setText(Settings.errorRepeat.toString())
        view?.findViewById<EditText>(R.id.editTextCookies)?.setText(Settings.headers["Cookie"])
        view?.findViewById<EditText>(R.id.editTextUseragent)?.setText(Settings.headers["User-Agent"])
        view?.findViewById<CheckBox>(R.id.checkboxUseFFMPEG)?.setChecked(Settings.useFFMpeg)
        view?.findViewById<CheckBox>(R.id.checkboxLoadItemsSize)?.setChecked(Settings.preloadSize)
        view?.findViewById<CheckBox>(R.id.checkBoxChooseTheme)?.setChecked(Preferences.get("ThemeDark", true) as Boolean)
        view?.findViewById<Spinner>(R.id.spinnerChoosePlayer)?.setSelection(Preferences.get("Player", 0) as Int)
    }

    private fun saveSettings() {
        Settings.downloadPath = view?.findViewById<EditText>(R.id.editTextDirectoryPath)?.text.toString()
        Settings.threads = view?.findViewById<EditText>(R.id.editTextThreads)?.text.toString().toInt()
        Settings.errorRepeat = view?.findViewById<EditText>(R.id.editTextRepeatError)?.text.toString().toInt()
        Settings.headers["Cookie"] = view?.findViewById<EditText>(R.id.editTextCookies)?.text.toString()
        Settings.headers["User-Agent"] = view?.findViewById<EditText>(R.id.editTextUseragent)?.text.toString()
        Settings.useFFMpeg = view?.findViewById<CheckBox>(R.id.checkboxUseFFMPEG)?.isChecked!!
        Settings.preloadSize = view?.findViewById<CheckBox>(R.id.checkboxLoadItemsSize)?.isChecked!!
        val ch = view?.findViewById<CheckBox>(R.id.checkBoxChooseTheme)?.isChecked
        Preferences.set("ThemeDark", ch)
        val sel = view?.findViewById<Spinner>(R.id.spinnerChoosePlayer)?.selectedItemPosition
        Preferences.set("Player", sel)

        Utils.saveSettings()
    }
}