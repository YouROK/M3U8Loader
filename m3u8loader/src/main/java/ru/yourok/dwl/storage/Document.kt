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
            return getRoot(fullPath)
        var path: DocumentFile? = getRoot(fullPath)
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
            return getRoot(fullPath)
        var path: DocumentFile? = getRoot(fullPath)
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
        Storage.getListRoots().forEach {
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

    fun getRoot(path: String): DocumentFile {
        if (File(path).canWrite()) {
            var root = File(path)
            while (root.parentFile.canWrite())
                root = root.parentFile
            return DocumentFile.fromFile(root)
        }

        val uri = Preferences.get(DocumentRootUri, "") as String
        return DocumentFile.fromTreeUri(Settings.context, Uri.parse(uri))
    }
}