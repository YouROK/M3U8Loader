package ru.yourok.dwl.converter

import processing.ffmpeg.videokit.LogLevel
import processing.ffmpeg.videokit.VideoKit
import ru.yourok.dwl.settings.Settings
import ru.yourok.dwl.storage.Document
import ru.yourok.dwl.storage.Storage
import ru.yourok.m3u8loader.BuildConfig
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


object Converter {
    fun convert(file: File): String {
        val inFile = file.path

        val tmpPath = File(findTmpPath(inFile), "Android/data/" + BuildConfig.APPLICATION_ID + "/files").canonicalFile
        val tmpName = File.createTempFile(file.nameWithoutExtension, ".mp4").name
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
            return "Error convert: ${res.code} " + file.name
        return moveFile(outFile.canonicalPath, inFile)
    }

    private fun moveFile(src: String, dst: String): String {
        try {
            if (!Storage.canWrite()) {
                val doc = Document.openFile(dst)
                if (doc != null)
                    doc.delete()

                val tmpDoc = Document.openFile(src)
                val out = Document.createFile(dst)

                val infd = Settings.context!!.getContentResolver().openFileDescriptor(tmpDoc!!.uri, "r")
                val inChannel = FileInputStream(infd!!.fileDescriptor).channel

                val outfd = Settings.context!!.getContentResolver().openFileDescriptor(out!!.uri, "rw")
                val outChannel = FileOutputStream(outfd!!.fileDescriptor).channel

                inChannel.transferTo(0, inChannel.size(), outChannel)
                outChannel.close()
                outfd.close()
                inChannel.close()
                infd.close()

                tmpDoc.delete()
            } else {
                val doc = File(dst)
                if (doc.exists())
                    doc.delete()

                val tmpDoc = File(src)
                val out = File(dst)
                out.createNewFile()

                val inChannel = FileInputStream(tmpDoc).channel

                val outChannel = FileOutputStream(out).channel

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
            if (it.startsWith(File(path).canonicalPath))
                return File(it).canonicalPath
        }
        return Storage.getListRoots()[0]
    }
}