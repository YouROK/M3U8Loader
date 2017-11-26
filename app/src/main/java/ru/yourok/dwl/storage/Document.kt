package ru.yourok.dwl.storage

import android.net.Uri
import android.support.v4.provider.DocumentFile
import ru.yourok.dwl.settings.Preferences
import ru.yourok.dwl.settings.Preferences.DocumentRootUri
import ru.yourok.dwl.settings.Settings
import java.io.File

/**
 * Created by yourok on 25.11.17.
 */
object Document {
    fun getFile(fileNamePath: String): DocumentFile? {
        val root = getRoot()
        var path: DocumentFile? = root
        val spath = fileNamePath.split("/")
        spath.forEach {
            path = path?.findFile(it)
            if (path == null)
                return null
        }
        return path
    }

    fun createFile(fileNamePath: String): DocumentFile? {
        val root = getRoot()
        var path: DocumentFile? = root
        val spath = fileNamePath.split("/")
        spath.forEach {
            if (spath.last() == it) {
                return path?.createFile("*/*", spath.last())
            }
            path = path?.findFile(it)
            if (path == null)
                return null
        }
        return path
    }

    fun createFileOrOpen(path: String, name: String): DocumentFile? {
        val root = getRoot()
        var file: DocumentFile? = root
        path.split("/").forEach {
            file = file?.findFile(it)
            if (file == null)
                return null
        }
        val ret = file?.findFile(name)
        if (ret != null)
            return ret
        return file?.createFile("*/*", name)
    }

    fun getRoot(): DocumentFile {
        if (File(Settings.downloadPath).canWrite())
            return DocumentFile.fromFile(File(Settings.downloadPath))

        val uri = Preferences.get(DocumentRootUri, "") as String
        if (uri.isEmpty())
            return DocumentFile.fromFile(File(Settings.downloadPath))
        return DocumentFile.fromTreeUri(Settings.context, Uri.parse(uri))
    }
}