package ru.yourok.m3u8loader.activitys.mainActivity

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import com.mikepenz.materialdrawer.Drawer
import kotlinx.android.synthetic.main.activity_main.*
import ru.yourok.dwl.downloader.LoadState
import ru.yourok.dwl.manager.Manager
import ru.yourok.dwl.settings.Preferences
import ru.yourok.dwl.updater.Updater
import ru.yourok.m3u8loader.BuildConfig
import ru.yourok.m3u8loader.R
import ru.yourok.m3u8loader.activitys.DonateActivity
import ru.yourok.m3u8loader.activitys.preferenceActivity.PreferenceActivity
import ru.yourok.m3u8loader.navigationBar.NavigationBar
import ru.yourok.m3u8loader.player.PlayIntent
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

        requestPermissionWithRationale()

        listViewLoader.adapter = LoaderListAdapter(this)
        drawer = NavigationBar.setup(this, listViewLoader.getAdapter() as LoaderListAdapter)
        listViewLoader.setMultiChoiceModeListener(LoaderListSelectionMenu(this, listViewLoader.getAdapter() as LoaderListAdapter))
        listViewLoader.setOnItemClickListener { adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->
            thread {
                val loader = Manager.getLoader(i)
                if (loader?.getState()?.state == LoadState.ST_COMPLETE) {
                    PlayIntent(this).start(loader)
                } else {
                    if (Manager.inQueue(i))
                        Manager.stop(i)
                    else
                        Manager.load(i)
                }
                update()
            }
        }

        listViewLoader.setOnItemLongClickListener { adapterView, view, i, l ->
            listViewLoader.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
            listViewLoader.setItemChecked(i, true)
            true
        }

        update()
        showMenuHelp()

        Timer().schedule(1000) {
            if (Updater.hasNewUpdate())
                Updater.showSnackbar(this@MainActivity)
        }
    }

    private fun update() {
        runOnUiThread { (listViewLoader.adapter as LoaderListAdapter).notifyDataSetChanged() }
        synchronized(isShow) {
            if (isShow)
                return
            isShow = true
        }
        thread {
            while (isShow) {
                runOnUiThread { (listViewLoader.adapter as LoaderListAdapter).notifyDataSetChanged() }
                if (Manager.isLoading())
                    Thread.sleep(500)
                else
                    Thread.sleep(3000)
            }
        }
    }

    private fun showMenuHelp() {
        if (Manager.getLoadersSize() == 0)
            Timer().schedule(1000) {
                if (Manager.getLoadersSize() == 0)
                    runOnUiThread { drawer.openDrawer() }
            }
        else drawer.closeDrawer()
    }

    override fun onResume() {
        super.onResume()
        update()
        showDonate()
    }

    override fun onPause() {
        super.onPause()
        isShow = false
    }

    override fun onStop() {
        super.onStop()
        Manager.saveLists()
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen) {
            drawer.closeDrawer()
            return
        }

        if (Manager.isLoading())
            moveTaskToBack(true)
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PreferenceActivity.Result && PreferenceActivity.changTheme) {
            Theme.changeNow(this, Preferences.get("ThemeDark", true) as Boolean)
        }
    }

    private fun requestPermissionWithRationale() {
        thread {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Snackbar.make(findViewById<View>(R.id.main_layout), R.string.permission_storage_msg, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.permission_btn, { ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1) })
                        .show()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && BuildConfig.FLAVOR != "lite" && !(Preferences.get("DozeRequestCancel", false) as Boolean)) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                val inWhiteList = powerManager.isIgnoringBatteryOptimizations(packageName)
                if (!inWhiteList) {
                    runOnUiThread {
                        AlertDialog.Builder(this)
                                .setTitle(R.string.doze_request_title)
                                .setMessage(R.string.doze_request)
                                .setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener { dialogInterface, i ->
                                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + packageName))
                                    startActivity(intent)
                                })
                                .setNeutralButton("", null)
                                .setNegativeButton(android.R.string.no, DialogInterface.OnClickListener { dialogInterface, i ->
                                    Preferences.set("DozeRequestCancel", true)
                                })
                                .show()
                    }
                }
            }
        }
    }

    private fun showDonate() {
        thread {

            val last: Long = Preferences.get("LastViewDonate", 0L) as Long
            if (last == -1L || System.currentTimeMillis() - last < 5 * 60 * 1000)
                return@thread
            
            Preferences.set("LastViewDonate", System.currentTimeMillis())

            val snackbar = Snackbar.make(findViewById(R.id.main_layout), R.string.donation, Snackbar.LENGTH_INDEFINITE)
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                snackbar
                        .setAction(android.R.string.ok) {
                            Preferences.set("LastViewDonate", System.currentTimeMillis())
                            startActivity(Intent(this@MainActivity, DonateActivity::class.java))
                        }
                        .show()
            }, 5000)
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                if (snackbar.isShown)
                    snackbar.dismiss()
            }, 15000)
        }
    }
}
