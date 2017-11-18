package ru.yourok.m3u8loader.navigationBar

import android.support.v7.app.AppCompatActivity
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.builders.footer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.badgeable.secondaryItem
import co.zsmb.materialdrawerkt.draweritems.divider
import org.jetbrains.anko.intentFor
import ru.yourok.m3u8loader.R
import ru.yourok.m3u8loader.activitys.AddListActivity
import ru.yourok.m3u8loader.fragments.preference.PreferenceFragment


object NavigationBar {
    fun setup(activity: AppCompatActivity) {
        with(activity) {
            drawer {
                headerViewRes = R.layout.header

                primaryItem(R.string.add) {
                    icon = R.drawable.ic_add_black_24dp
                    selectable = false
                    onClick { _ ->
                        startActivity(intentFor<AddListActivity>())
                        false
                    }
                }
                divider {}
                primaryItem(R.string.load_all) {
                    icon = R.drawable.ic_file_download_black_24dp
                    selectable = false
                }
                primaryItem(R.string.pause_all) {
                    icon = R.drawable.ic_pause_black_24dp
                    selectable = false
                }
                primaryItem(R.string.clear_all) {
                    icon = R.drawable.ic_clear_black_24dp
                    selectable = false
                }

                divider {}
                footer {
                    secondaryItem("Settings") {
                        icon = R.drawable.ic_settings_black_24dp
                        selectable = false
                        onClick { _ ->
                            if (supportFragmentManager.backStackEntryCount == 0)
                                supportFragmentManager
                                        .beginTransaction()
                                        .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                        .replace(R.id.fragmentContainer, PreferenceFragment.newInstance(), "1")
                                        .addToBackStack("1")
                                        .commit()
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
