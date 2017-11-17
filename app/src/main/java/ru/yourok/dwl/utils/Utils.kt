package ru.yourok.dwl.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import ru.yourok.dwl.settings.Settings
import java.io.File

object Utils {
    fun byteFmt(bytes: Double): String {
        if (bytes < 1024)
            return bytes.toString() + " B"
        val exp = (Math.log(bytes) / Math.log(1024.0)).toInt()
        val pre = "KMGTPE"[exp - 1].toString()
        return "%.1f %sB".format(bytes / Math.pow(1024.0, exp.toDouble()), pre)
    }

    fun byteFmt(bytes: Float): String {
        return byteFmt(bytes.toDouble())
    }

    fun byteFmt(bytes: Long): String {
        return byteFmt(bytes.toDouble())
    }

    fun byteFmt(bytes: Int): String {
        return byteFmt(bytes.toDouble())
    }

    fun saveSettings(path: String) {
        Saver.save(File(path, "settings.cfg").path, Settings)
    }

    fun loadSettings(path: String) {
        val vals = Saver.load(File(path, "settings.cfg").path)
        if (vals is LinkedHashMap<*, *>) {
            Settings.threads = vals["threads"] as Int
            Settings.errorRepeat = vals["errorRepeat"] as Int

            Settings.downloadPath = vals["downloadPath"] as String
            Settings.preloadSize = vals["preloadSize"] as Boolean
            Settings.useFFMpeg = vals["useFFMpeg"] as Boolean

            Settings.headers = mutableMapOf()
            if (vals["headers"] != null)
                Settings.headers = (vals["headers"] as LinkedHashMap<String, String>).toMutableMap()
        }
    }
}

object Saver {
    fun save(filePath: String, clazz: Any) {
        val mapper = jacksonObjectMapper()
        mapper.writerWithDefaultPrettyPrinter().writeValue(File(filePath), clazz)
    }

    fun load(path: String): Any {
        val mapper = jacksonObjectMapper()
        return mapper.readValue(File(path))
    }
}