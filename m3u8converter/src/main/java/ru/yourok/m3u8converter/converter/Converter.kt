package ru.yourok.m3u8converter.converter

import android.annotation.SuppressLint
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.support.v4.provider.DocumentFile
import processing.ffmpeg.videokit.LogLevel
import processing.ffmpeg.videokit.VideoKit
import ru.yourok.m3u8converter.App
import ru.yourok.m3u8converter.storage.LoaderContext
import ru.yourok.m3u8converter.storage.Storage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


@SuppressLint("StaticFieldLeak")
object Converter {
    fun convert(item: ConvertItem): String {
        try {
            val inFile = item.path
            if (!File(inFile).canRead())
                throw IOException("file not found")

            val tmpPath = File(findTmpPath(inFile)).canonicalFile
            val tmpName = File.createTempFile(File(inFile).nameWithoutExtension, ".mp4").name
            val outFile = File(tmpPath, tmpName)

            val videoKit = VideoKit()
            videoKit.setLogLevel(LogLevel.FULL)
            val command = videoKit.createCommand()
                    .overwriteOutput()
                    .inputPath(inFile)
                    .outputPath(outFile.canonicalPath)
                    .customCommand("-c copy -bsf:a aac_adtstoasc")
                    .build()
            val res = command.execute()
            if (res.code != 0)
                return "Error convert ${item.name}: ${res.code}"
            return moveFile(outFile.canonicalPath, item)
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error convert ${item.name}: " + e.message
        }
    }

    private fun moveFile(converted: String, dst: ConvertItem): String {
        try {
            var doc = DocumentFile.fromFile(File(dst.path))
            if (!doc.canWrite())
                doc = DocumentFile.fromTreeUri(LoaderContext.get(), Uri.parse(dst.uri))

            var tmpDoc = Storage.getDocument(converted)
            if (!tmpDoc.exists()) {
                val docDir = Storage.getPath(tmpDoc.parentFile)
                tmpDoc = Storage.getDocument(docDir).createFile("*/*", tmpDoc.name)
            }


            val infd = LoaderContext.get().getContentResolver().openFileDescriptor(doc.uri, "r")
            val inChannel = FileOutputStream(infd!!.fileDescriptor).channel

            val outfd = LoaderContext.get().getContentResolver().openFileDescriptor(doc.uri, "rw")
            val outChannel = FileOutputStream(outfd!!.fileDescriptor).channel
            outChannel.truncate(0)

            inChannel.transferTo(0, inChannel.size(), outChannel)

            outChannel.close()
            outfd.close()
            inChannel.close()
            infd.close()

            tmpDoc.delete()
        } catch (e: Exception) {
            return e.message ?: "Error copy converted file: " + dst
        }
        return ""
    }

    private fun findTmpPath(path: String): String {
        ContextCompat.getExternalFilesDirs(App.getContext(), null).forEach {
            val index = it.canonicalPath.indexOf("/Android/data/")
            val root = it.canonicalPath.substring(0, index)
            if (path.startsWith(root))
                return it.canonicalPath
        }

        val rootFile = File(path).parentFile
        if (rootFile.canWrite())
            return rootFile.canonicalPath

        return App.getContext().getCacheDir().canonicalPath
    }
}