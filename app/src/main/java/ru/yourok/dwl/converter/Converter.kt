package ru.yourok.dwl.converter

import android.util.Log
import android.widget.Toast
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import ru.yourok.dwl.settings.Settings


object Converter {
    private val ffmpeg: FFmpeg by lazy { FFmpeg.getInstance(Settings.context) }

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

    fun convert(inFile: String, outFile: String, onFinish: (() -> Unit)?) {
        try {
//            val cmd = "-i \"$inFile\" -c copy -bsf:a aac_adtstoasc \"$outFile\""
            val cmd = mutableListOf<String>("-i", inFile, "-c", "copy", "-bsf:a", "aac_adtstoasc", outFile)
            ffmpeg.execute(cmd.toTypedArray(), object : ExecuteBinaryResponseHandler() {
                override fun onFinish() {
                    onFinish?.invoke()
                    Log.i("******Finish", "convert")
                }

                override fun onFailure(message: String?) {
                    Log.i("******Error", message)
                    Toast.makeText(Settings.context, "Error convert: " + message, Toast.LENGTH_SHORT).show()
                }

                override fun onProgress(message: String?) {
                    Log.i("******Convert", message)
                }
            })
        } catch (e: Exception) {
        }
    }
}