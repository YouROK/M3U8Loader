package ru.yourok.dwl.downloader

class State {
    var url: String = ""
    var name: String = ""
    var path: String = ""
    var threads: Int = 0
    var speed: Float = 0.0F
    var isComplete: Boolean = false
    var error: String = ""

    var fragments: Int = 0
    var loadedFragments: Int = 0

    var size: Long = 0
    var loadedBytes: Long = 0

    var duration: Float = 0.0F
    var loadedDuration: Float = 0.0F

    var loadedItems: MutableList<ItemState> = mutableListOf()

    override fun toString(): String {
        return "State(name='$name', path='$path', threads=$threads, fragments=$fragments, loadedFragments=$loadedFragments, size=$size, loadedBytes=$loadedBytes, duration=$duration, loadedDuration=$loadedDuration)"
    }
}

class ItemState {
    var loaded: Long = 0
    var size: Long = 0
    var complete: Boolean = false
}