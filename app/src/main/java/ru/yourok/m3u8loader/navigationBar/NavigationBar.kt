package ru.yourok.m3u8loader.navigationBar

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.builders.footer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.badgeable.secondaryItem
import co.zsmb.materialdrawerkt.draweritems.divider
import ru.yourok.m3u8loader.AddListActivity
import ru.yourok.m3u8loader.R


object NavigationBar {
    fun setup(activity: AppCompatActivity) {
        with(activity) {
            drawer {
                headerViewRes = R.layout.header

                primaryItem("Add") {
                    icon = R.drawable.ic_add_black_24dp
                    selectable = false
                    onClick { _ ->
                        val intent = Intent(activity, AddListActivity::class.java)
                        startActivity(intent)
                        false
                    }
                }
                divider {}
                primaryItem("Load all") {
                    icon = R.drawable.ic_file_download_black_24dp
                    selectable = false
                }
                primaryItem("Pause") {
                    icon = R.drawable.ic_pause_black_24dp
                    selectable = false
                }
                primaryItem("Clear all") {
                    icon = R.drawable.ic_clear_black_24dp
                    selectable = false
                }

                divider {}
                footer {
                    secondaryItem("Settings") {
                        icon = R.drawable.ic_settings_black_24dp
                        selectable = false
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
