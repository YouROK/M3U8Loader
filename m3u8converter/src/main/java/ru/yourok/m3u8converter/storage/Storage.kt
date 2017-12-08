package ru.yourok.m3u8converter.storage

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.support.v4.provider.DocumentFile
import java.io.File


/**
 * Created by yourok on 03.12.17.
 */

object Storage {

    fun getDocument(file: String): DocumentFile {
        if (File(file).canWrite())
            return DocumentFile.fromFile(File(file))
        val roots = Storage.getRoots()
        roots.forEach {
            Storage.walkTo(it, file)?.let { return it }
        }
        return DocumentFile.fromFile(File(file))
    }

    fun getRoots(): List<DocumentFile> {
        val list = mutableListOf<DocumentFile>()
        list.add(DocumentFile.fromFile(File(Storage.getInternalPublic())))
        val set = Storage.getRootsPermissionUri()
        set.forEach {
            val file = DocumentFile.fromTreeUri(LoaderContext.get(), Uri.parse(it))
            if (file.canWrite())
                list.add(file)
        }
        return list
    }

    fun walkTo(root: DocumentFile, path: String): DocumentFile? {
        var pathDoc: DocumentFile? = root

        val rootPath = Storage.getPath(root)
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
        val loaderContext = LoaderContext.get()
        val prefs = loaderContext.getSharedPreferences(Storage.prefsFile, Context.MODE_PRIVATE)
        if (prefs.contains("roots")) {
            return prefs.getStringSet("roots", setOf())
        }
        return setOf()
    }

    private fun getFD(uri: Uri, mode: String): ParcelFileDescriptor? {
        return LoaderContext.get().getContentResolver().openFileDescriptor(uri, mode)
    }

    fun getPath(doc: DocumentFile): String {
        var path = ""
        try {
            val fd = Storage.getFD(doc.uri, "r")
            if (fd != null) {
                path = StatFS.path(fd.fd) ?: ""
                fd.close()
                if (path.isNotEmpty() && path.startsWith("/mnt/")) {
                    val tmp = path.replace("/mnt/", "/storage/")
                    if (File(tmp).exists())
                        path = tmp
                }
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
        val file = File(Storage.getPath(doc))
        if (file.canRead()) {
            if (isTotal)
                size = file.totalSpace
            else
                size = file.freeSpace
        }
        if (size == 0L) {
            try {
                val fd = Storage.getFD(doc.uri, "r") ?: return 0
                size = StatFS.size(fd.fd, isTotal)
                fd.close()
            } catch (e: Exception) {
            }
        }
        return size
    }

    fun addRootUri(uri: String) {
        val roots = Storage.getRootsPermissionUri().toMutableSet()
        roots.add(uri)
        val prefs = LoaderContext.get().getSharedPreferences(Storage.prefsFile, Context.MODE_PRIVATE)
        val edit = prefs.edit()
        edit.putStringSet("roots", roots)
        edit.apply()
    }
}