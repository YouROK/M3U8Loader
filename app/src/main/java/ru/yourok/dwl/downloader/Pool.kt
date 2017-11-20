package ru.yourok.dwl.downloader

import android.util.Log
import ru.yourok.dwl.settings.Settings
import java.net.SocketTimeoutException
import kotlin.concurrent.thread


class Pool(private val workers: List<Pair<Worker, DownloadStatus>>) {

    @Volatile private var stop = true
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
                workers.forEach { item ->
                    val wrk = item.first
                    if (stop || !error.isNullOrEmpty())
                        return@forEach
                    synchronized(lock) {
                        currentWorker++
                    }
                    thread {
                        Thread.sleep(10)
                        for (i in 0..Settings.errorRepeat)
                            try {
                                if (!wrk.item.isCompleteLoad && !stop) {
                                    wrk.run()
                                    onFinish?.invoke()
                                }
                                break
                            } catch (e: SocketTimeoutException) {
                                if (i == Settings.errorRepeat) {
                                    error = "Error, connection timeout on load item " + wrk.item.index
                                    onError?.invoke(error)
                                }
                            } catch (e: Exception) {
                                if (i == Settings.errorRepeat) {
                                    error = "Error connection: " + e.message
                                    onError?.invoke(error)
                                }
                            }
                        synchronized(lock) {
                            currentWorker--
                        }
                    }
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
                if (!error.isNullOrEmpty())
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
}