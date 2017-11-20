package ru.yourok.dwl.list

/**
 * Created by yourok on 07.11.17.
 */
class List {
    var items: MutableList<Item> = mutableListOf()
    var url: String = ""
    var filePath: String = ""
    var info: Info = Info()
    var isConverted: Boolean = false
    var subsUrl: String = ""
}

class Info {
    var bandwidth: Int = 0
    var title: String = ""
}

class Item {
    var index: Int = -1
    var url: String = ""
    var size: Long = 0
    var isLoad: Boolean = true
    var isCompleteLoad = false
    var encData: EncKey? = null
}

