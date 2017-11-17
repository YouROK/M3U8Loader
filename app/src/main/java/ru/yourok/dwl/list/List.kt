package ru.yourok.dwl.list

import java.io.Serializable

/**
 * Created by yourok on 07.11.17.
 */
class List : Serializable {
    var items: MutableList<Item> = mutableListOf()
    var url: String = ""
    var filePath: String = ""
    var info: Info = Info()
}

class Info {
    var bandwidth: Int = 0
    var frameRate: Float = 0.0F
    var title: String = ""
}

class Item {
    var index: Int = -1
    var url: String = ""
    var size: Long = 0
    var isLoad: Boolean = true
    var isCompleteLoad = false
    var duration: Float = 0.0F
    var encData: EncKey? = null
}

