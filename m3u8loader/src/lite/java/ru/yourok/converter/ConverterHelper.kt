package ru.yourok.converter

import ru.yourok.dwl.list.List

/**
 * Created by yourok on 05.02.18.
 */

object ConverterHelper {

    fun isSupport(): Boolean {
        return false
    }

    fun isConverting(): Boolean {
        return false
    }

    fun isConvert(list: List): Boolean {
        return false
    }

    fun convert(list: kotlin.collections.List<List>) {
    }

    fun startConvert() {
    }
}