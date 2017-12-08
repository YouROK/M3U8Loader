package ru.yourok.m3u8converter.storage

import android.content.Context
import android.widget.Toast
import ru.yourok.m3u8converter.App

/**
 * Created by yourok on 07.12.17.
 */

object LoaderContext {
    private var loaderContext: Context? = null

    fun get(): Context {
        try {
            if (loaderContext == null)
                loaderContext = App.getContext().createPackageContext("ru.yourok.m3u8loader", Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY)
            return loaderContext!!
        } catch (e: Exception) {
            Toast.makeText(App.getContext(), "M3U8Loader not found", Toast.LENGTH_SHORT)
            System.exit(0)
            throw e
        }
    }
}