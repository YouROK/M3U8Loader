package ru.yourok.dwl.utils

import java.nio.charset.Charset

object Utils {
    fun byteFmt(bytes: Double): String {
        if (bytes < 1024)
            return bytes.toString() + " B"
        val exp = (Math.log(bytes) / Math.log(1024.0)).toInt()
        val pre = "KMGTPE"[exp - 1].toString()
        return "%.1f %sB".format(bytes / Math.pow(1024.0, exp.toDouble()), pre)
    }

    fun byteFmt(bytes: Float): String {
        return byteFmt(bytes.toDouble())
    }

    fun byteFmt(bytes: Long): String {
        return byteFmt(bytes.toDouble())
    }

    fun byteFmt(bytes: Int): String {
        return byteFmt(bytes.toDouble())
    }

    fun durationFmt(duration: Float): String {
        if ((duration / 3600).toInt() == 0)
            return "%02d:%02d".format(((duration % 3600) / 60).toInt(), (duration % 60).toInt())
        else
            return "%d:%02d:%02d".format((duration / 3600).toInt(), ((duration % 3600) / 60).toInt(), (duration % 60).toInt())
    }

    fun isTextBuffer(buffer: ByteArray): Boolean {
        val size = if (buffer.size > 40) 40 else buffer.size
        val chkBuf = buffer.copyOf(size)
        var chkStr = chkBuf.toString(Charset.defaultCharset()).replace("\u0000|\r?\n".toRegex(), "")
        val clnStr = chkStr.replace("\\p{C}".toRegex(), "")
        return clnStr == chkStr
    }

    fun cleanFileName(file: String): String {
        val ReservedCharsReg = "[|\\\\?*<\\\":>+/']"
        var ret = file.replace(ReservedCharsReg.toRegex(), "_").replace("_+".toRegex(), "_")
        ret = ret.trim { it <= ' ' }
        return ret
    }
}
