package ru.yourok.dwl.downloader

import ru.yourok.dwl.settings.Settings
import java.net.SocketTimeoutException
import kotlin.concurrent.thread


class Pool(private val workers: MutableList<Pair<Worker, DownloadStatus>>) {

    private var stop = true
    private var currentWorker = 0
    private var thread: Thread? = null
    private var lock: Any = Any()

    fun start(onEnd: () -> Unit) {
        stop = false

        this.thread = thread {
            try {
                currentWorker = 0
                for ((wrk, _) in workers) {
                    if (stop)
                        break
                    synchronized(lock) {
                        currentWorker++
                    }
                    thread {
                        Thread.sleep(10)
                        for (i in 0..Settings.errorRepeat)
                            try {
                                if (!wrk.item.isCompleteLoad && !stop)
                                    wrk.run()
                                break
                            } catch (e: SocketTimeoutException) {
                                if (i == Settings.errorRepeat)
                                    throw SocketTimeoutException("Error, connection timeout on loadList: url=" + wrk.item.url + " index=" + wrk.item.index)
                            } catch (e: Exception) {
                                if (i == Settings.errorRepeat)
                                    throw e
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
                onEnd()
                stop = true
            } catch (e: Exception) {
                onEnd()
                stop = true
                throw e
            }
        }
    }

    fun stop() {
        stop = true
        for ((wrk, _) in workers)
            wrk.stop()
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