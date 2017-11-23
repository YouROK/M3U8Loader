package ru.yourok.dwl.downloader

class DownloadStatus {
    var buffer: ByteArray? = null
    var speed: Float = 0.0F
    var loadedBytes: Long = 0
    var isLoading: Boolean = false
    var isError: Boolean = false

    fun Clear() {
        buffer = null
        speed = 0.0F
        loadedBytes = 0
        isLoading = false
    }
}