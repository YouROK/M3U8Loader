package ru.yourok.m3u8converter.converter

import android.annotation.SuppressLint
import processing.ffmpeg.videokit.LogLevel
import processing.ffmpeg.videokit.VideoKit
import ru.yourok.m3u8converter.App
import ru.yourok.m3u8converter.BuildConfig
import ru.yourok.m3u8converter.storage.Document
import ru.yourok.m3u8converter.storage.Storage
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


@SuppressLint("StaticFieldLeak")
object Converter {
    fun convert(item: ConvertItem): String {
        try {
            val inFile = item.path
            if (!File(inFile).exists())
                throw IOException("file not found")

            val tmpPath = File(findTmpPath(inFile), "Android/data/" + BuildConfig.APPLICATION_ID + "/files").canonicalFile
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
            if (!File(dst.path).canWrite()) {
                val doc = Document.openFile(dst)
                val tmpDoc = File(converted)

                val inChannel = FileInputStream(tmpDoc).channel

                val outfd = App.getContext().getContentResolver().openFileDescriptor(doc!!.uri, "rw")
                val outChannel = FileOutputStream(outfd!!.fileDescriptor).channel
                outChannel.truncate(0)

                inChannel.transferTo(0, inChannel.size(), outChannel)

                outChannel.close()
                outfd.close()
                inChannel.close()

                tmpDoc.delete()
            } else {
                val doc = File(dst.path)
                val tmpDoc = File(converted)

                val inChannel = FileInputStream(tmpDoc).channel
                val outChannel = FileOutputStream(doc).channel
                outChannel.truncate(0)

                inChannel.transferTo(0, inChannel.size(), outChannel)
                outChannel.close()
                inChannel.close()
                tmpDoc.delete()
            }
        } catch (e: Exception) {
            return e.message ?: "Error copy converted file: " + dst
        }
        return ""
    }

    private fun findTmpPath(path: String): String {
        Storage.getListRoots().forEach {
            if (File(path).canonicalPath.startsWith(File(it).canonicalPath))
                return File(it).canonicalPath
        }
        return Storage.getListRoots()[0]
    }
}