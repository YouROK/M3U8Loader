package ru.yourok.m3u8loader.activitys

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_list.*
import ru.yourok.converter.ConverterHelper
import ru.yourok.dwl.list.List
import ru.yourok.dwl.manager.Manager
import ru.yourok.dwl.manager.Notifyer
import ru.yourok.dwl.parser.Parser
import ru.yourok.dwl.settings.Settings
import ru.yourok.dwl.storage.Storage
import ru.yourok.dwl.utils.Utils
import ru.yourok.m3u8loader.R
import ru.yourok.m3u8loader.activitys.preferenceActivity.DirectoryActivity
import ru.yourok.m3u8loader.theme.Theme
import kotlin.concurrent.thread


class AddListActivity : AppCompatActivity() {

    private var list: MutableList<List>? = null
    private var downloadPath = Settings.downloadPath
    private var showNotify = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Theme.set(this)
        setContentView(R.layout.activity_add_list)

        textViewError.text = ""

        var download = false

        try {
            if (intent.extras != null) {
                val bundle = intent.extras
                for (key in bundle.keySet()) {
                    if (key.toLowerCase().contains("name") || key.toLowerCase().contains("title")) {
                        val value = bundle.get(key)
                        if (value != null) {
                            val name = cleanFileName(value.toString().trim { it <= ' ' })
                            if (!name.isEmpty()) editTextFileName.setText(name)
                        }
                    }
                    if (key.toLowerCase().contains("subs") || key.toLowerCase().contains("subtitles")) {
                        val value = bundle.get(key)
                        if (value != null) editTextSubtitles.setText(value.toString().trim { it <= ' ' })
                    }
                    if (key.toLowerCase().contains("download")) {
                        download = true
                    }
                }
            }

            if (intent.action != null && intent.action.equals(Intent.ACTION_SEND)) {
                if (intent.getStringExtra(Intent.EXTRA_TEXT) != null) {
                    editTextUrl.setText(intent.getStringExtra(Intent.EXTRA_TEXT))
                    showNotify = true
                }
                if (intent.extras.get(Intent.EXTRA_STREAM) != null) {
                    editTextUrl.setText(intent.extras.get(Intent.EXTRA_STREAM).toString())
                    showNotify = true
                }
            }

            if (intent.data != null && !intent.data.toString().isEmpty()) {
                val path = intent.data.toString()
                editTextUrl.setText(path)
                showNotify = true
            }

            if (editTextFileName.getText().toString().isEmpty()) {
                var count = 0
                var found = true
                while (found) {
                    found = false
                    for (i in 0 until Manager.getLoadersSize()) {
                        val info = Manager.getLoaderStat(i) ?: continue
                        val name = info.name
                        if (name == "video" + count) {
                            count++
                            found = true
                            break
                        }
                    }
                }
                editTextFileName.setText("video" + count)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: " + e.localizedMessage, Toast.LENGTH_SHORT).show()
            finish()
        }

        checkboxConvertAdd.visibility = if (ConverterHelper.isSupport()) View.VISIBLE else View.GONE

        checkboxConvertAdd.setOnCheckedChangeListener { _, b ->
            if (b) {
                if (!ConverterHelper.isSupport()) {
                    checkboxConvertAdd.isChecked = false
                    Toast.makeText(this, R.string.warn_install_convertor, Toast.LENGTH_SHORT).show()
                }
            }
        }
        checkboxConvertAdd.isChecked = Settings.convertVideo && ConverterHelper.isSupport()

        updateDownloadPath()
        buttonSetDownloadPath.setOnClickListener {
            val intent = Intent(this, DirectoryActivity::class.java)
            intent.data = Uri.parse(downloadPath)
            startActivityForResult(intent, 1202)
        }

        if (download) downloadBtnClick(buttonDownload)
    }

    fun updateDownloadPath() {
        textViewDirectoryPathAdd.setText(downloadPath)
        val totalSpace = Storage.getSpace(Storage.getDocument(downloadPath), true)
        val freeSpace = Storage.getSpace(Storage.getDocument(downloadPath), false)
        textViewDiskSize.text = "%s / %s".format(Utils.byteFmt(totalSpace - freeSpace), Utils.byteFmt(totalSpace))
        progressBarFreeSpace.progress = 100 - (freeSpace * 100 / (totalSpace + 1)).toInt()
    }

    fun addBtnClick(view: View) {
        addList(false)
    }

    fun downloadBtnClick(view: View) {
        addList(true)
    }

    fun cancelBtnClick(view: View) {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    fun addList(download: Boolean) {
        val Name = cleanFileName(editTextFileName.getText().toString().trim())
        val Url = editTextUrl.getText().toString().trim()
        val SubsUrl = editTextSubtitles.getText().toString().trim()

        if (Url.isEmpty()) {
            toastErr(R.string.error_empty_url)
            return
        }

        if (Name.isEmpty()) {
            toastErr(R.string.error_empty_name)
            return
        }
        waitView(true)
        thread {
            try {
                val lists = Parser(Name, Url, downloadPath).parse()
                lists.forEach {
                    it.subsUrl = SubsUrl
                    it.isConvert = (checkboxConvertAdd?.isChecked ?: Settings.convertVideo) && ConverterHelper.isSupport()
                }
                Manager.addList(lists)
                if (download) {
                    val start = Manager.getLoadersSize() - lists.size
                    for (i in start until Manager.getLoadersSize()) Manager.load(i)
                } else {
                    val names = lists.joinToString { it.title }
                    Notifyer.sendNotification(this, Notifyer.TYPE_NOTIFYLOAD, getString(R.string.added), names)
                }
                this.setResult(RESULT_OK)
                this.finish()
            } catch (e: java.text.ParseException) {
                if (e.errorOffset == -1) {
                    runOnUiThread {
                        AlertDialog.Builder(this).setTitle(R.string.error_wrong_format).setMessage(R.string.warn_wrong_format).setPositiveButton(android.R.string.yes) { p0, p1 ->
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setDataAndType(Uri.parse(Url), "video/*")
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            intent.putExtra("title", Name)
                            val chooser = Intent.createChooser(intent, "")
                            chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(chooser)
                            finish()
                        }.setNegativeButton(android.R.string.no) { p0, p1 ->
                                    if (e.message != null) toastErr(e.message!!)
                                }.setNeutralButton("", null).show()
                    }
                } else if (e.message != null) toastErr(e.message!!)
            } catch (e: Exception) {
                if (e.message != null) toastErr(e.message!!)
            }
            waitView(false)
        }
    }

    private fun cleanFileName(file: String): String {
        val ReservedCharsReg = "[|\\\\?*<\\\":>+/']"
        var ret = file.replace(ReservedCharsReg.toRegex(), "_").replace("_+".toRegex(), "_")
        ret = ret.trim { it <= ' ' }
        return ret
    }

    private fun toastErr(msg: String) {
        if (msg.isNotEmpty()) runOnUiThread {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            textViewError.setText(msg)
        }
    }

    private fun toastErr(msg: Int) {
        runOnUiThread {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            textViewError.setText(msg)
        }
    }

    private fun waitView(set: Boolean) {
        runOnUiThread {
            if (set) {
                findViewById<View>(R.id.progressBar).visibility = View.VISIBLE
                textViewError.text = ""
            } else findViewById<View>(R.id.progressBar).visibility = View.GONE
            findViewById<View>(R.id.buttonAdd).isEnabled = !set
            findViewById<View>(R.id.buttonDownload).isEnabled = !set
            findViewById<View>(R.id.buttonCancel).isEnabled = !set
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1202 && data != null) {
            downloadPath = data.getStringExtra("filename")
            updateDownloadPath()
        }
    }
}