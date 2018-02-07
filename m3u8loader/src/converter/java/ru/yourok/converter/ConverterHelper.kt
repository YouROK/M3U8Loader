package ru.yourok.converter

import ru.yourok.dwl.list.List
import ru.yourok.m3u8converter.converter.Manager
import ru.yourok.m3u8loader.App

/**
 * Created by yourok on 05.02.18.
 */

object ConverterHelper {

    fun isSupport(): Boolean {
        return true
    }

    fun getCurrentConvert(): List? {
        return Manager.getCurrent()
    }

    fun isConvert(list: List): Boolean {
        return Manager.contain(list)
    }

    fun convert(list: kotlin.collections.List<List>) {
        list.forEach {
            Manager.add(it)
        }
    }

    fun startConvert() {
        App.wakeLock(1000)
        Manager.startConvert({
            if (it != null)
                App.wakeLock(1000)
        })
    }
}