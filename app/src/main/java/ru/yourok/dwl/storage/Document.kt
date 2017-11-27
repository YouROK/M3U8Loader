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

    fun openFile(fullPath: String): DocumentFile? {
        val relPath = getRelPath(fullPath) ?: return null

        if (relPath.isEmpty())
            return getRoot()
        var path: DocumentFile? = getRoot()
        relPath.split("/").filter { !it.isEmpty() }.forEach {
            path = path?.findFile(it)
            if (path == null)
                return null
        }
        return path
    }

    fun createFile(fullPath: String): DocumentFile? {
        val relPath = getRelPath(fullPath) ?: return null

        if (relPath.isEmpty())
            return getRoot()
        var path: DocumentFile? = getRoot()
        var spath = relPath.split("/").filter { !it.isEmpty() }
        spath.forEach {
            if (path?.findFile(it) == null) {
                if (spath.last() != it)
                    path = path?.createDirectory(it)
                else
                    return path?.createFile("*/*", it)
            } else
                path = path?.findFile(it)
        }
        return path
    }

    private fun getRelPath(path: String): String? {
        Storage.getListDirs().forEach {
            val fp = File(path)
            val fit = File(it)
            if (fp.canonicalPath == fit.canonicalPath)
                return ""

            if (fp.canonicalPath.startsWith(fit.canonicalPath)) {
                return fp.canonicalPath.substring(it.length)
            }
        }
        return null
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