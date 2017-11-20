package ru.yourok.m3u8loader.activitys.mainActivity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import com.mikepenz.materialdrawer.Drawer
import kotlinx.android.synthetic.main.activity_main.*
import ru.yourok.dwl.manager.Manager
import ru.yourok.dwl.settings.Preferences
import ru.yourok.m3u8loader.R
import ru.yourok.m3u8loader.activitys.PreferenceActivity
import ru.yourok.m3u8loader.navigationBar.NavigationBar
import ru.yourok.m3u8loader.theme.Theme
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private var isShow: Boolean = false
    private lateinit var drawer: Drawer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Theme.set(this)
        setContentView(R.layout.activity_main)
        drawer = NavigationBar.setup(this)
        listViewLoader.adapter = LoaderListAdapter(this)
        listViewLoader.setMultiChoiceModeListener(LoaderListSelectionMenu(this))
        listViewLoader.setOnItemClickListener { adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->
            thread {
                if (Manager.inQueue(i))
                    Manager.stop(i)
                else
                    Manager.load(i)
            }
        }

        listViewLoader.setOnItemLongClickListener { adapterView, view, i, l ->
            listViewLoader.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
            listViewLoader.setItemChecked(i, true)
            true
        }

        update()
        showMenuHelp()
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

    private fun showMenuHelp() {
        if (Preferences.get("isFirstRun", "") == "") {
            Timer().schedule(1000) {
                runOnUiThread { drawer.openDrawer() }

                if (Manager.getLoadersSize() != 0)
                    Timer().schedule(1000) {
                        runOnUiThread { drawer.closeDrawer() }
                    }
                Preferences.set("isFirstRun", "nop")
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
}
