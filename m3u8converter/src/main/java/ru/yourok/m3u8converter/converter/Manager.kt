package ru.yourok.m3u8converter.converter

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import ru.yourok.m3u8converter.App
import kotlin.concurrent.thread

/**
 * Created by yourok on 28.11.17.
 */
object Manager {
    @Volatile private var convList: MutableList<ConvertItem> = mutableListOf()
    private val lock = Any()
    private var converting = false


    fun add(item: ConvertItem): Int {
        synchronized(convList) {
            convList.add(item)
        }
        startLoading()
        return convList.size - 1
    }

    fun size(): Int {
        synchronized(convList) {
            return convList.size
        }
    }

    fun get(i: Int): ConvertItem {
        synchronized(convList) {
            return convList[i]
        }
    }

    fun execList(fn: (List<ConvertItem>) -> Unit) {
        synchronized(convList) {
            fn(convList)
        }
    }

    private fun startLoading() {
        synchronized(lock) {
            if (converting)
                return
            converting = true
        }
        thread {
            while (convList.size > 0) {
                var currentConvert: ConvertItem? = null
                synchronized(convList) {
                    currentConvert = convList[0]
                }
                val err = Converter.convert(currentConvert!!)
                Handler(Looper.getMainLooper()).post {
                    if (err.isNotEmpty())
                        Toast.makeText(App.getContext(), err, Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(App.getContext(), "Converted: " + currentConvert?.name, Toast.LENGTH_SHORT).show()
                }
                synchronized(convList) {
                    convList.removeAt(0)
                }
            }
            converting = false
        }
    }
}