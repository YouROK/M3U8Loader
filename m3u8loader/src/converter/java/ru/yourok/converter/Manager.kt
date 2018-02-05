package ru.yourok.m3u8converter.converter

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import ru.yourok.converter.Converter
import ru.yourok.dwl.list.List
import ru.yourok.m3u8loader.App
import kotlin.concurrent.thread


/**
 * Created by yourok on 28.11.17.
 */
object Manager {
    @Volatile
    private var convList: MutableList<List> = mutableListOf()
    private val lock = Any()
    private var converting = false
    private var currentConvert: List? = null

    fun add(item: List) {
        synchronized(convList) {
            if (!convList.contains(item))
                convList.add(item)
        }
    }

    fun contain(item: List): Boolean {
        synchronized(convList) {
            return (convList.contains(item))
        }
    }

    fun clear() {
        synchronized(convList) {
            convList.clear()
        }
    }

    fun startConvert(onEndConvertList: ((list: List?) -> Unit)?) {
        synchronized(lock) {
            if (converting)
                return
            converting = true
        }
        thread {
            while (convList.size > 0 && converting) {
                synchronized(convList) {
                    currentConvert = convList[0]
                }
                currentConvert?.let {
                    val err = Converter.convert(it)
                    onEndConvertList?.invoke(it)
                    Handler(Looper.getMainLooper()).post {
                        if (err.isNotEmpty())
                            Toast.makeText(App.getContext(), err, Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(App.getContext(), "Converted: " + it.title, Toast.LENGTH_SHORT).show()
                    }
                }
                synchronized(convList) {
                    convList.removeAt(0)
                }
            }
            onEndConvertList?.invoke(null)
            converting = false
        }
    }

    fun stop() {
        converting = false
    }
}