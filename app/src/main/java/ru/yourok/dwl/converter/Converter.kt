package ru.yourok.dwl.converter

import android.util.Log
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import ru.yourok.dwl.settings.Settings


object Converter {
    private val ffmpeg: FFmpeg by lazy { FFmpeg.getInstance(Settings.context) }
    private var count: Int = 0

    init {
        try {
            ffmpeg.loadBinary(null)
        } catch (e: FFmpegNotSupportedException) {
            e.printStackTrace()
        }
    }

    fun getVersion(): String {
        return ffmpeg.getDeviceFFmpegVersion()
    }

    //-c:v libx264 -c:a copy -bsf:a aac_adtstoasc
    //-c copy -bsf:a aac_adtstoasc

    fun convert(inFile: String, outFile: String, onFinish: (() -> Unit)?, onFailure: ((message: String) -> Unit)?) {
        try {
//            val cmd = "-i \"$inFile\" -c copy -bsf:a aac_adtstoasc \"$outFile\""
            val cmd = mutableListOf<String>("-i", inFile, "-c", "copy", "-bsf:a", "aac_adtstoasc", outFile)
            count++
            ffmpeg.execute(cmd.toTypedArray(), object : ExecuteBinaryResponseHandler() {
                override fun onFinish() {
                    onFinish?.invoke()
                    Log.i("******Finish", "convert")
                    count--
                    if (count == 0) {
                        if (ffmpeg.isFFmpegCommandRunning)
                            ffmpeg.killRunningProcesses()
                    }
                }

                override fun onFailure(message: String?) {
                    Log.i("******Error", message)
                    onFailure?.invoke(message ?: "")
                }

                override fun onProgress(message: String?) {
                    Log.i("******Convert", message)
                }
            })
        } catch (e: Exception) {
        }
    }

    fun isConverting(): Boolean {
        return ffmpeg.isFFmpegCommandRunning
    }
}