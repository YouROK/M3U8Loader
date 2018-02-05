package ru.yourok.dwl.downloader

class DownloadStatus {
    var buffer: ByteArray? = null
    var speed: Float = 0.0F
    var isLoading: Boolean = false
    var isCompleteLoad = false
    var isError: Boolean = false

    fun Clear() {
        buffer = null
        speed = 0.0F
        isLoading = false
    }
}