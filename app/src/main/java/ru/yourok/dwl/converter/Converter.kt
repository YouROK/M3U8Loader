package ru.yourok.dwl.converter


class Converter {

    fun setup(): Boolean {
        /*if (Settings.context == null)
            return false
        val ffmpeg = FFmpeg.getInstance(Settings.context)
        try {
            ffmpeg!!.loadBinary(null)
        } catch (e: FFmpegNotSupportedException) {
            return false
        }
        return true*/
        return false
    }

    //-c:v libx264 -c:a copy -bsf:a aac_adtstoasc
    //-c copy -bsf:a aac_adtstoasc

    fun convert(inFile: String, outFile: String) {
//        if (Settings.context == null)
//            return
//        val ffmpeg = FFmpeg.getInstance(Settings.context)
//        try {
//            val cmd = "-i $inFile -c copy -bsf:a aac_adtstoasc $outFile"
//            val cmds = cmd.split(" ").toTypedArray()
//            ffmpeg!!.execute(cmds, object : ExecuteBinaryResponseHandler() {
//                override fun onFinish() {
//                    Log.i("******", "Finish")
//                    super.onFinish()
//                }
//
//                override fun onSuccess(message: String?) {
//                    super.onSuccess(message)
//                }
//
//                override fun onFailure(message: String?) {
//                    Log.i("******", "Error: " + message)
//                    super.onFailure(message)
//                }
//
//                override fun onProgress(message: String?) {
//                    Log.i("******", message)
//                    super.onProgress(message)
//                }
//
//                override fun onStart() {
//                    super.onStart()
//                }
//            })
//        } catch (e: Exception) {
//        }
    }
}