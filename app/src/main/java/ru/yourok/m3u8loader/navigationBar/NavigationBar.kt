package ru.yourok.m3u8loader.navigationBar

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.builders.footer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.badgeable.secondaryItem
import co.zsmb.materialdrawerkt.draweritems.divider
import com.mikepenz.materialdrawer.Drawer
import ru.yourok.dwl.manager.Manager
import ru.yourok.m3u8loader.R
import ru.yourok.m3u8loader.activitys.AddListActivity
import ru.yourok.m3u8loader.activitys.DonateActivity
import ru.yourok.m3u8loader.activitys.PreferenceActivity


object NavigationBar {
    fun setup(activity: AppCompatActivity): Drawer {
        with(activity) {
            return drawer {
                headerViewRes = R.layout.header

                primaryItem(R.string.add) {
                    icon = R.drawable.ic_add_black_24dp
                    selectable = false
                    onClick { _ ->
                        startActivity(Intent(activity, AddListActivity::class.java))
                        false
                    }
                }
                divider {}
                primaryItem(R.string.load_all) {
                    icon = R.drawable.ic_file_download_black_24dp
                    selectable = false
                    onClick { _ ->
                        Manager.loadAll()
                        false
                    }
                }
                primaryItem(R.string.stop_all) {
                    icon = R.drawable.ic_pause_black_24dp
                    selectable = false
                    onClick { _ ->
                        Manager.stopAll()
                        false
                    }
                }
                primaryItem(R.string.remove_all) {
                    icon = R.drawable.ic_clear_black_24dp
                    selectable = false
                    onClick { _ ->
                        Manager.removeAll(this@with)
                        false
                    }
                }

                divider {}
                footer {
                    secondaryItem(R.string.donation) {
                        icon = R.drawable.ic_donate_black
                        selectable = false
                        onClick { _ ->
                            startActivity(Intent(activity, DonateActivity::class.java))
                            false
                        }
                    }
                    secondaryItem(R.string.settings) {
                        icon = R.drawable.ic_settings_black_24dp
                        selectable = false
                        onClick { _ ->
                            startActivityForResult(Intent(activity, PreferenceActivity::class.java), PreferenceActivity.Result)
                            false
                        }
                    }
                }
                headerPadding = true
                stickyFooterDivider = true
                stickyFooterShadow = false
                selectedItem = -1
            }
        }
    }
}
