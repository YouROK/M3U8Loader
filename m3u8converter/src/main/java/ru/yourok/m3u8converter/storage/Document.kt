package ru.yourok.m3u8converter.storage

import android.net.Uri
import android.support.v4.provider.DocumentFile
import ru.yourok.m3u8converter.App
import ru.yourok.m3u8converter.converter.ConvertItem
import ru.yourok.m3u8converter.storage.Preferences.DocumentRootUri
import java.io.File

/**
 * Created by yourok on 25.11.17.
 */
object Document {

    fun openFile(item: ConvertItem): DocumentFile? {
        val relPath = getRelPath(item.path) ?: return null

        if (relPath.isEmpty())
            return getRoot(item)
        var path: DocumentFile? = getRoot(item)
        relPath.split("/").filter { !it.isEmpty() }.forEach {
            path = path?.findFile(it)
            if (path == null)
                return null
        }
        return path
    }

    fun createFile(item: ConvertItem): DocumentFile? {
        val relPath = getRelPath(item.path) ?: return null

        if (relPath.isEmpty())
            return getRoot(item)
        var path: DocumentFile? = getRoot(item)
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

    fun getRoot(item: ConvertItem): DocumentFile {
        if (File(item.path).canWrite()) {
            var root = File(item.path)
            while (root.parentFile.canWrite())
                root = root.parentFile
            return DocumentFile.fromFile(root)
        }
        val uri = Preferences.get(DocumentRootUri, "") as String
        return DocumentFile.fromTreeUri(App.getContext(), Uri.parse(uri))
    }
}