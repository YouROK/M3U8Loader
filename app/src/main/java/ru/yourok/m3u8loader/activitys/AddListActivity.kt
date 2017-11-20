package ru.yourok.m3u8loader.activitys

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_list.*
import ru.yourok.dwl.list.List
import ru.yourok.dwl.manager.Manager
import ru.yourok.dwl.parser.Parser
import ru.yourok.m3u8loader.R
import ru.yourok.m3u8loader.theme.Theme
import kotlin.concurrent.thread


class AddListActivity : AppCompatActivity() {

    var list: MutableList<List>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Theme.set(this)
        setContentView(R.layout.activity_add_list)

        textViewError.text = ""

        val intent = intent
        var download = false

        try {
            if (intent.extras != null) {
                val bundle = intent.extras
                for (key in bundle.keySet()) {
                    if (key.toLowerCase().contains("name") || key.toLowerCase().contains("title")) {
                        val value = bundle.get(key)
                        if (value != null) {
                            val name = cleanFileName(value.toString().trim { it <= ' ' })
                            if (!name.isEmpty())
                                editTextFileName.setText(name)
                        }
                    }
                    if (key.toLowerCase().contains("subs") || key.toLowerCase().contains("subtitles")) {
                        val value = bundle.get(key)
                        if (value != null)
                            editTextSubtitles.setText(value.toString().trim { it <= ' ' })
                    }
                    if (key.toLowerCase().contains("download")) {
                        download = true
                    }
                }
            }

            if (intent.action != null && intent.action.equals(Intent.ACTION_SEND)) {
                if (intent.getStringExtra(Intent.EXTRA_TEXT) != null)
                    editTextUrl.setText(intent.getStringExtra(Intent.EXTRA_TEXT))
                if (intent.extras.get(Intent.EXTRA_STREAM) != null)
                    editTextUrl.setText(intent.extras.get(Intent.EXTRA_STREAM).toString())
            }

            if (intent.data != null && !intent.data.toString().isEmpty()) {
                val path = intent.data.toString()
                editTextUrl.setText(path)
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
        if (download)
            downloadBtnClick(buttonDownload)
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
                val lists = Parser(Name, Url).parse()
                if (!SubsUrl.isEmpty())
                    lists.forEach { it.subsUrl = SubsUrl }
                Manager.addList(lists)
                if (download) {
                    val start = Manager.getLoadersSize() - lists.size
                    for (i in start until Manager.getLoadersSize())
                        Manager.load(i)
                }
                waitView(false)
                finish()
            } catch (e: Exception) {
                if (e.message != null)
                    toastErr(e.message!!)
                waitView(false)
            }
        }
    }

    private fun cleanFileName(file: String): String {
        val ReservedCharsReg = "[|\\\\?*<\\\":>+/']"
        var ret = file.replace(ReservedCharsReg.toRegex(), "_").replace("_+".toRegex(), "_")
        ret = ret.trim { it <= ' ' }
        return ret
    }

    private fun toastErr(msg: String) {
        if (msg.isNotEmpty())
            runOnUiThread {
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
            } else
                findViewById<View>(R.id.progressBar).visibility = View.GONE
            findViewById<View>(R.id.buttonAdd).isEnabled = !set
            findViewById<View>(R.id.buttonDownload).isEnabled = !set
            findViewById<View>(R.id.buttonCancel).isEnabled = !set
        }
    }
}