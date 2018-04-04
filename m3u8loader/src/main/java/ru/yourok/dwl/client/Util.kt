package ru.yourok.dwl.client

import android.net.Uri
import java.net.URLDecoder

/**
 * Created by yourok on 07.11.17.
 */
object Util {
    fun concatUriList(baseUrl: Uri, segment: String): String {
        if (segment.startsWith("http://") || segment.startsWith("https://") || segment.startsWith("file://") || segment.startsWith("content://"))
            return segment

        var path = baseUrl.path

        if (!path.endsWith("/")) {
            val tmp = java.io.File(path).parent
            if (tmp != null)
                path = tmp
        }
        if (segment.startsWith("/"))
            path = segment
        else
            path = java.io.File(path, segment).path
        return copyArguments(baseUrl, path)
    }

    private fun copyArguments(argUrl: Uri, path: String): String {
        val builder = argUrl.buildUpon()
        builder.path(path)
        return URLDecoder.decode(builder.build().toString(), "UTF-8")
    }
}