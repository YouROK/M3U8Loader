package ru.yourok.m3u8converter

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import ru.yourok.m3u8converter.converter.ConvertItem
import ru.yourok.m3u8converter.converter.Manager
import ru.yourok.m3u8converter.storage.Storage
import ru.yourok.m3u8converter.utils.Preferences
import ru.yourok.m3u8converter.utils.Theme
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private var tmpList: MutableList<ConvertItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.hasExtra("dark"))
            Preferences.set("ThemeDark", intent.getBooleanExtra("dark", true))
        Theme.set(this)

        if (!intent.hasExtra("hide")) {
            setContentView(R.layout.activity_main)
            listViewConverter.adapter = ConvertAdapter(this)
        }
        Storage.getListRoots()
        requestPermissionWithRationale()

        if (intent.hasExtra("hide")) {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun requestPermissionWithRationale() {
        thread {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
    }

    private var isShow: Boolean = false
    private fun update() {
        synchronized(isShow) {
            if (isShow)
                return
            isShow = true
        }
        thread {
            while (isShow) {
                runOnUiThread {
                    (listViewConverter.adapter as ConvertAdapter).notifyDataSetChanged()
                }
                Thread.sleep(500)
                if (Manager.size() == 0)
                    Timer().schedule(3000) {
                        if (Manager.size() == 0)
                            runOnUiThread {
                                finish()
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                            }
                    }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1202 && Preferences.get(Preferences.DocumentRootUri, "") as String != "" && tmpList.isNotEmpty())
            tmpList.forEach { Manager.add(it) }
    }

    override fun onResume() {
        super.onResume()
        update()
    }

    override fun onPause() {
        super.onPause()
        isShow = false
    }

    inner class ConvertAdapter(val context: Context) : BaseAdapter() {
        override fun getItem(p0: Int): Any {
            return Manager.get(p0)
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        override fun getCount(): Int {
            return Manager.size()
        }


        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(android.R.layout.two_line_list_item, null)

            val item = Manager.get(position)
            val text1 = view.findViewById<TextView>(android.R.id.text1)
            val text2 = view.findViewById<TextView>(android.R.id.text2)

            text1.setText(item.name)
            if (item.error.isNotEmpty())
                text2.text = item.error
            else if (item.state == 1)
                text2.text = "Converting"
            else
                text2.text = ""

            return view
        }
    }
}
