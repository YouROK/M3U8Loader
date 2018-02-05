package ru.yourok.converter

import android.support.v4.content.ContextCompat
import processing.ffmpeg.videokit.LogLevel
import processing.ffmpeg.videokit.VideoKit
import ru.yourok.dwl.list.List
import ru.yourok.dwl.storage.Storage
import ru.yourok.m3u8loader.App
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by yourok on 05.02.18.
 */


object Converter {

    fun convert(list: List): String {
        try {
            val inFile = list.filePath
            if (!File(inFile).exists())
                throw IOException("file not found or permission denied: " + inFile)
            if (!File(inFile).canRead())
                throw IOException("file read access denied: " + inFile)

            val tmpPath = File(findTmpPath(inFile)).canonicalFile
            val tmpName = File(inFile).nameWithoutExtension + ".cnv.mp4"
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
                return "Error convert ${list.title}: ${res.code}"
            return moveFile(outFile.canonicalPath, list)
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error convert ${list.title}: " + e.message
        }
    }

    private fun moveFile(converted: String, dst: List): String {
        try {
            val toName = File(dst.filePath).name

            val from = Storage.getDocument(converted)
            var to = Storage.getDocument(dst.filePath)
            if (!to.exists())
                Storage.getDocument(File(dst.filePath).parent).createFile("*/*", toName)

            val infd = App.getContext().getContentResolver().openFileDescriptor(from.uri, "r")
            val inChannel = FileInputStream(infd!!.fileDescriptor).channel

            val outfd = App.getContext().getContentResolver().openFileDescriptor(to.uri, "rw")
            val outChannel = FileOutputStream(outfd!!.fileDescriptor).channel

            outChannel.truncate(0)
            inChannel.transferTo(0, inChannel.size(), outChannel)

            outChannel.close()
            outfd.close()
            inChannel.close()
            infd.close()

            from.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            return e.message ?: "Error copy converted file: "+dst.title
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