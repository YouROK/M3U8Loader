package ru.yourok.dwl.storage

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.support.v4.provider.DocumentFile
import ru.yourok.m3u8loader.App
import java.io.File

/**
 * Created by yourok on 03.12.17.
 */

object Storage {
    fun getDocument(file: String): DocumentFile {
        if (File(file).canWrite())
            return DocumentFile.fromFile(File(file))
        val roots = getRoots()
        roots.forEach {
            walkTo(it, file)?.let { return it }
        }
        return DocumentFile.fromFile(File(file))
    }

    fun getRoots(): List<DocumentFile> {
        val list = mutableListOf<DocumentFile>()
        list.add(DocumentFile.fromFile(File(getInternalPublic())))
        val set = getRootsPermissionUri()
        set.forEach {
            val file = DocumentFile.fromTreeUri(App.getContext(), Uri.parse(it))
            if (file.canWrite())
                list.add(file)
        }
        return list
    }

    fun walkTo(root: DocumentFile, path: String): DocumentFile? {
        var pathDoc: DocumentFile? = root

        val rootPath = getPath(root)
        if (rootPath == path)
            return root
        if (!path.startsWith(rootPath))
            return null

        val pathParts = path.substring(rootPath.length).split("/").filter { it.isNotEmpty() }
        pathParts.forEach {
            pathDoc = pathDoc?.findFile(it) ?: return null
        }
        return pathDoc
    }

    private val prefsFile = "ru.yourok.m3u8.storage"

    private fun getInternalPublic(): String {
        val file = Environment.getExternalStorageDirectory()
        if (file.canWrite())
            return file.canonicalPath
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).canonicalPath
    }

    private fun getRootsPermissionUri(): Set<String> {
        val prefs = App.getContext().getSharedPreferences(prefsFile, Context.MODE_PRIVATE)
        if (prefs.contains("roots")) {
            return prefs.getStringSet("roots", setOf())
        }
        return setOf()
    }

    private fun getFD(uri: Uri, mode: String): ParcelFileDescriptor? {
        return App.getContext().getContentResolver().openFileDescriptor(uri, mode)
    }

    fun getPath(doc: DocumentFile): String {
        var path = ""
        try {
            val fd = getFD(doc.uri, "r")
            if (fd != null) {
                path = StatFS.path(fd.fd) ?: ""
                fd.close()
//                if (path.isNotEmpty() && path.startsWith("/mnt/media_rw/")) {
//                    val tmp = path.replace("/mnt/media_rw/", "/storage/")
//                    if (File(tmp).exists())
//                        path = tmp
//                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (path.isEmpty()) {
            var ppath: DocumentFile? = doc
            val parts = mutableListOf<String>()
            while (ppath != null) {
                parts.add(ppath.name)
                ppath = ppath.parentFile
            }
            return parts.asReversed().joinToString("/", "/")
        }
        return path
    }

    fun getSpace(doc: DocumentFile, isTotal: Boolean): Long {
        var size = 0L
        val file = File(getPath(doc))
        if (file.canRead()) {
            if (isTotal)
                size = file.totalSpace
            else
                size = file.freeSpace
        }
        if (size == 0L) {
            try {
                val fd = getFD(doc.uri, "r") ?: return 0
                size = StatFS.size(fd.fd, isTotal)
                fd.close()
            } catch (e: Exception) {
            }
        }
        return size
    }

    fun addRootUri(uri: String) {
        val roots = getRootsPermissionUri().toMutableSet()
        roots.add(uri)
        val prefs = App.getContext().getSharedPreferences(prefsFile, Context.MODE_PRIVATE)
        val edit = prefs.edit()
        edit.putStringSet("roots", roots)
        edit.apply()
    }
}