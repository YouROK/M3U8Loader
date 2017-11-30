package ru.yourok.dwl.converter

import android.content.ComponentName
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import ru.yourok.dwl.list.List
import ru.yourok.dwl.settings.Settings
import kotlin.concurrent.thread


object Converter {

    private val PROVIDER_NAME = "ru.yourok.m3u8converter.provider.items"
    private val CONTENT_URI = Uri.parse("content://$PROVIDER_NAME/items")

    private var isConverting = false

    fun convert(list: kotlin.collections.List<List>) {
        if (list.isEmpty())
            return
        thread {
            if (!isConverting)
                start()
            list.forEach {
                val cv = ContentValues()
                cv.put("name", it.info.title)
                cv.put("path", it.filePath)
                Settings.context!!.contentResolver.insert(CONTENT_URI, cv)
            }
            isConverting = true
        }
    }

    fun stat(): kotlin.collections.List<String> {
        if (!isConverting)
            return emptyList()

        val retList = mutableListOf<String>()
        val cursor = Settings.context!!.contentResolver.query(CONTENT_URI, null, null, null, null)
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndex("name"))
            val path = cursor.getString(cursor.getColumnIndex("path"))
            retList.add(name + path)
        }
        cursor.close()
        if (retList.isEmpty())
            isConverting = false
        return retList
    }

    fun start() {
        val intent = Intent()
        intent.component = ComponentName("ru.yourok.m3u8converter", "ru.yourok.m3u8converter.MainActivity")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("hide", true)
        if (intent.resolveActivity(Settings.context!!.packageManager) != null) {
            Settings.context!!.startActivity(intent)
            Thread.sleep(2000)
        }
    }

    fun installed(): Boolean {
        val pm = Settings.context!!.getPackageManager()
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        packages.forEach {
            if (it.packageName == "ru.yourok.m3u8converter")
                return true
        }
        return false
    }
}