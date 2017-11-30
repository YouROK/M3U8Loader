package ru.yourok.dwl.downloader

object LoadState {
    const val ST_PAUSE = 0
    const val ST_LOADING = 1
    const val ST_COMPLETE = 2
    const val ST_ERROR = 3
}

class State {
    var url: String = ""
    var name: String = ""
    var file: String = ""
    var threads: Int = 0
    var speed: Float = 0.0F
    var isComplete: Boolean = false
    var error: String = ""
    var state: Int = 0

    var fragments: Int = 0
    var loadedFragments: Int = 0

    var size: Long = 0
    var loadedBytes: Long = 0

    var loadedItems: MutableList<ItemState> = mutableListOf()

    override fun toString(): String {
        return "State(name='$name', file='$file', threads=$threads, fragments=$fragments, loadedFragments=$loadedFragments, size=$size, loadedBytes=$loadedBytes)"
    }
}

class ItemState {
    var loaded: Long = 0
    var size: Long = 0
    var complete: Boolean = false
    var error: Boolean = false
}