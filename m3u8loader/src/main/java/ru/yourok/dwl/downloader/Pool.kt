package ru.yourok.dwl.downloader

import ru.yourok.dwl.settings.Settings
import java.net.SocketException
import java.net.SocketTimeoutException
import kotlin.concurrent.thread


class Pool(private val workers: List<Pair<Worker, DownloadStatus>>) {

    @Volatile
    private var stop = true
    private var currentWorker = 0
    private var thread: Thread? = null
    private var lock: Any = Any()
    private var error: String = ""

    private var onEnd: (() -> Unit)? = null
    private var onFinish: (() -> Unit)? = null
    private var onError: ((err: String) -> Unit)? = null

    fun onEnd(onEnd: () -> Unit) {
        this.onEnd = onEnd
    }

    fun onFinishWorker(onFinish: () -> Unit) {
        this.onFinish = onFinish
    }

    fun onError(onError: (err: String) -> Unit) {
        this.onError = onError
    }

    fun start() {
        stop = false

        this.thread = thread {
            try {
                currentWorker = 0
                var priorityIndex = -1
                workers.forEach { item ->
                    val wrk = item.first
                    val dstat = item.second
                    if (stop || !error.isEmpty())
                        return@forEach
                    synchronized(lock) {
                        currentWorker++
                    }
                    thread {
                        Thread.currentThread().priority = Thread.MIN_PRIORITY
                        for (i in 0..Settings.errorRepeat)
                            try {
                                if (!wrk.item.isComplete && !stop) {
                                    wrk.run()
                                    Runtime.getRuntime().gc()
                                    dstat.isError = false
                                    onFinish?.invoke()
                                }
                                break
                            } catch (e: SocketException) {
                                dstat.isError = true
                                if (i == Settings.errorRepeat) {
                                    error = (e.message
                                            ?: "Error, read or connect") + " " + wrk.item.url + " " + wrk.item.index
                                    onError?.invoke(error)
                                }
                            } catch (e: SocketTimeoutException) {
                                dstat.isError = true
                                if (i == Settings.errorRepeat) {
                                    error = "Error, connection timeout on load item " + wrk.item.index
                                    onError?.invoke(error)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                dstat.isError = true
                                error = "Error: " + e.message ?: "unknown error"
                                onError?.invoke(error)
                            }
                        synchronized(lock) {
                            currentWorker--
                        }
                    }
                    priorityIndex = setPrior(priorityIndex)
                    while (currentWorker >= Settings.threads)
                        Thread.sleep(100)
                }

                while (currentWorker > 0)
                    Thread.sleep(100)
                onEnd?.invoke()
                stop = true
            } catch (e: Exception) {
                onEnd?.invoke()
                stop = true
                if (!error.isEmpty())
                    onError?.invoke(e.message ?: "Error load items")
            }
        }
    }

    fun stop() {
        stop = true
        for ((wrk, _) in workers)
            wrk.stop()
        thread?.join()
    }

    fun waitEnd() {
        thread?.join()
    }

    fun size(): Int {
        return currentWorker
    }

    fun isWorking(): Boolean {
        return !stop || currentWorker > 0
    }

    private fun setPrior(lastIndex: Int): Int {
        if (lastIndex == -1) {//Find and set
            workers.forEachIndexed { index, pair ->
                if (pair.second.isLoading) {
                    pair.first.setMaxPrior()
                    return index
                }
            }
            return -1
        }
        if (lastIndex in 0..workers.size && workers[lastIndex].second.isCompleteLoad)
            return -1
        return lastIndex
    }
}