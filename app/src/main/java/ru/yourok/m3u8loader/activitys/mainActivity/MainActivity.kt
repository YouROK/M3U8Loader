package ru.yourok.m3u8loader.activitys.mainActivity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import kotlinx.android.synthetic.main.activity_main.*
import ru.yourok.dwl.manager.Manager
import ru.yourok.dwl.settings.Preferences
import ru.yourok.m3u8loader.R
import ru.yourok.m3u8loader.activitys.PreferenceActivity
import ru.yourok.m3u8loader.navigationBar.NavigationBar
import ru.yourok.m3u8loader.theme.Theme
import java.util.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private var isShow: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Theme.set(this)
        setContentView(R.layout.activity_main)
        NavigationBar.setup(this)
        listViewLoader.adapter = LoaderListAdapter(this)
        listViewLoader.setOnItemClickListener { adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->
            if (Manager.inQueue(i))
                Manager.stop(i)
            else
                Manager.load(i)
        }
        update()
        cpuinfo()
    }

    private fun update() {
        synchronized(isShow) {
            if (isShow)
                return
            isShow = true
        }
        thread {
            while (isShow) {
                runOnUiThread { (listViewLoader.adapter as LoaderListAdapter).notifyDataSetChanged() }
                Thread.sleep(500)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        update()
    }

    override fun onPause() {
        super.onPause()
        isShow = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PreferenceActivity.Result && PreferenceActivity.changTheme) {
            Theme.changeNow(this, Preferences.get("ThemeDark", true) as Boolean)
        }
    }

    private fun cpuinfo() {
        Log.i("", "CPU_ABI : " + Build.CPU_ABI);
        Log.i("", "CPU_ABI2 : " + Build.CPU_ABI2);
        Log.i("", "OS.ARCH : " + System.getProperty("os.arch"));

        Log.i("", "SUPPORTED_ABIS : " + Arrays.toString(Build.SUPPORTED_ABIS));
        Log.i("", "SUPPORTED_32_BIT_ABIS : " + Arrays.toString(Build.SUPPORTED_32_BIT_ABIS));
        Log.i("", "SUPPORTED_64_BIT_ABIS : " + Arrays.toString(Build.SUPPORTED_64_BIT_ABIS));
    }
}
