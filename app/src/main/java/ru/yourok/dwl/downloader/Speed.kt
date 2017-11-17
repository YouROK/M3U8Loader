package ru.yourok.dwl.downloader

/**
 * Created by yourok on 10.11.17.
 */
class Speed(private val stat: DownloadStatus) {
    private var startTime: Long = 0
    private var countBytes: Int = 0


    fun startRead() {
        startTime = System.currentTimeMillis() - 200 //закачка началась в фоне, убираем скачок
        countBytes = 0
        stat.isLoading = true
    }

    fun measure(bytes: Int) {
        val countTime = (System.currentTimeMillis() - startTime).toDouble() / 1000.0
        countBytes += bytes

        var speed = 0.0
        if (countTime > 0.0)
            speed = countBytes.toDouble() / countTime
        if (countTime > 5)
            startRead()

        stat.speed = speed.toFloat()
    }

    fun stopRead() {
        stat.isLoading = false
        stat.speed = 0.0F
    }
}