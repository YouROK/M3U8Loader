package ru.yourok.m3u8converter.storage

import android.content.Intent
import android.support.v4.content.ContextCompat
import ru.yourok.m3u8converter.App
import ru.yourok.m3u8converter.utils.Preferences
import java.io.File

/**
 * Created by yourok on 25.11.17.
 */

object Storage {

    fun getListRoots(): List<String> {
        val ret = mutableListOf<String>()

        val paths = ContextCompat.getExternalFilesDirs(App.getContext(), null).toMutableList()
        if (paths[0] == null) {
            val primary_sd = System.getenv("EXTERNAL_STORAGE");
            if (primary_sd != null && File(primary_sd).canWrite())
                ret.add(primary_sd)
            val secondary_sd = System.getenv("SECONDARY_STORAGE");
            if (secondary_sd != null && File(secondary_sd).canWrite())
                ret.add(secondary_sd)
            val emulated = System.getenv("EMULATED_STORAGE_TARGET")
            if (emulated != null && File(emulated).canWrite())
                ret.add(emulated)
            paths.clear()
        }

        paths.forEach {
            val path = it.path
            val index = path.indexOf("Android/data")
            if (index != -1)
                ret.add(path.substring(0, index))
        }

        return ret.toList()
    }

    fun requestSDPermissions() {
        if (getListRoots().size > 1) {
            if (Preferences.get(Preferences.DocumentRootUri, "") as String == "") {
                val intent = Intent(App.getContext(), RequestStoragePermissionActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                App.getContext().startActivity(intent)
            }
        }
    }

}