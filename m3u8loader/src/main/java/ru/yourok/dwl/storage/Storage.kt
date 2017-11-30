package ru.yourok.dwl.storage

import android.content.Context
import android.content.Intent
import android.os.storage.StorageManager
import android.support.v4.content.ContextCompat
import ru.yourok.dwl.settings.Preferences
import ru.yourok.dwl.settings.Settings
import java.io.File

/**
 * Created by yourok on 25.11.17.
 */

object Storage {

    fun getListRoots(): List<String> {
        val ret = mutableListOf<String>()

        val paths = ContextCompat.getExternalFilesDirs(Settings.context!!, null).toMutableList()
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
        val mntPaths = getMountedPaths()
        mntPaths.forEach { mit ->
            val fpath = File(ret.find { File(it).canonicalPath == File(mit).canonicalPath }).canonicalPath
            if (fpath != File(mit).canonicalPath)
                ret.add(mit)
        }
        return ret.toList()
    }

    fun requestSDPermissions() {
        if (getListRoots().size > 1) {
            if (Preferences.get(Preferences.DocumentRootUri, "") as String == "")
                Settings.context!!.startActivity(Intent(Settings.context!!, RequestStoragePermissionActivity::class.java))
        }
    }

    fun canWrite(): Boolean {
        return File(Settings.downloadPath).canWrite()
    }

    /*
           Use reflection for detecting all storages as android do it
           probably doesn't work with USB-OTG
           works only on API 19+
     */
    private fun getMountedPaths(): List<String> {
        val allPaths = mutableListOf<String>()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            try {
                val storageManager = Settings.context!!.getSystemService(Context.STORAGE_SERVICE) as StorageManager
                val storageVolumeClass = Class.forName("android.os.storage.StorageVolume")
                val getVolumeList = storageManager.javaClass.getMethod("getVolumeList")
                val getPath = storageVolumeClass.getMethod("getPath")
                val getState = storageVolumeClass.getMethod("getState")
                val getVolumeResult = getVolumeList.invoke(storageManager) as Array<*>

                for (i in 0 until getVolumeResult.size) {
                    val storageVolumeElem = getVolumeResult[i]
                    val mountStatus = getState.invoke(storageVolumeElem) as String?
                    if (mountStatus != null && mountStatus == "mounted") {
                        val path = getPath.invoke(storageVolumeElem) as String?
                        if (path != null) {
                            allPaths.add(path)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return allPaths
    }
}