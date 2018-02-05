package ru.yourok.dwl.downloader

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import ru.yourok.converter.ConverterHelper
import ru.yourok.dwl.client.ClientBuilder
import ru.yourok.dwl.list.List
import ru.yourok.dwl.manager.Notifyer
import ru.yourok.dwl.settings.Settings
import ru.yourok.dwl.storage.Storage
import ru.yourok.dwl.utils.Saver
import ru.yourok.m3u8loader.App
import ru.yourok.m3u8loader.R.string.error_load_subs
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


/**
 * Created by yourok on 09.11.17.
 */
class Downloader(val list: List) {
    private var pool: Pool? = null
    private var workers: kotlin.collections.List<Pair<Worker, DownloadStatus>>? = null
    private var executor: ExecutorService? = null

    private var error: String = ""

    private var complete: Boolean = false
    private var isLoading: Boolean = false
    private var stop: Boolean = false
    private var starting: Any = Any()

    init {
        var ncomp = true
        list.items.forEach {
            if (!it.isComplete) {
                ncomp = false
                return@forEach
            }
        }
        complete = ncomp
    }

    fun load() {
        synchronized(starting) {
            synchronized(isLoading) {
                if (isLoading)
                    return
                isLoading = true
            }
            try {
                stop = false
                isLoading = true
                complete = false
                error = ""

                loadSubtitles()

                val file = FileWriter(list.filePath)
                var resize = true
                var size = 0L
                workers = null
                if (stop) {
                    isLoading = false
                    return@synchronized
                }
                preloadSize()
                val tmpWorkers = mutableListOf<Pair<Worker, DownloadStatus>>()
                list.items.forEach {
                    if (it.isLoad) {
                        val stat = DownloadStatus()
                        val wrk = Worker(it, stat, file)
                        tmpWorkers.add(Pair(wrk, stat))
                        if (it.size == 0L)
                            resize = false
                        size += it.size
                    }
                }
                if (resize)
                    file.resize(size)

                tmpWorkers.sortBy { it.first.item.index }
                workers = tmpWorkers.toList()

                file.setWorkers(workers!!)
                if (stop) {
                    isLoading = false
                    return@synchronized
                }
                list.isPlayed = false
                pool = Pool(workers!!)
                pool!!.start()
                pool!!.onEnd {
                    complete = true
                    workers!!.forEach {
                        if (!it.first.item.isComplete)
                            complete = false
                    }

                    if (!resize && complete) {
                        size = 0L
                        workers!!.forEach {
                            size += it.first.item.size
                        }
                        file.resize(size)
                    }
                    file.close()
                    Saver.saveList(list)
                    isLoading = false
                    if (complete && list.isConvert) {
                        ConverterHelper.convert(mutableListOf(list))
                        ConverterHelper.startConvert()
                    }
                    Notifyer.toastEnd(list, complete, error)
                }

                pool!!.onFinishWorker {
                    Saver.saveList(list)
                }
                pool!!.onError {
                    error = it
                    workers?.forEach { it.first.stop() }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                error = e.message ?: ""
                isLoading = false
            }
        }
    }

    fun stop() {
        stop = true
        pool?.stop()
        executor?.shutdownNow()
    }

    fun waitEnd() {
        synchronized(starting) {}
        if (pool != null)
            pool!!.waitEnd()
    }

    fun isComplete(): Boolean = complete
    fun isLoading(): Boolean = isLoading

    fun clear() {
        stop()
        waitEnd()
        complete = false
    }

    fun getState(): State {
        val state = State()
        synchronized(list) {
            state.name = list.title
            state.url = list.url
            state.file = list.filePath
            state.threads = pool?.size() ?: 0
            state.error = error
            state.isComplete = complete
            state.isPlayed = list.isPlayed
            if (isLoading)
                state.state = LoadState.ST_LOADING
            else if (complete)
                state.state = LoadState.ST_COMPLETE
            else if (!error.isEmpty())
                state.state = LoadState.ST_ERROR


            if (workers?.size != 0 && pool?.isWorking() == true) {
                state.fragments = workers!!.size
                workers!!.forEach {
                    if (it.first.item.isLoad) {
                        val itmState = ItemState()
                        itmState.loaded = it.first.item.loaded
                        itmState.size = it.first.item.size
                        itmState.complete = it.first.item.isComplete
                        itmState.error = it.second.isError
                        state.loadedItems.add(itmState)

                        state.size += it.first.item.size
                        if (it.first.item.isComplete)
                            state.loadedBytes += it.first.item.size
                        else
                            state.loadedBytes += it.first.item.loaded
                        if (it.first.item.isComplete) {
                            state.loadedFragments++
                        }
                        if (it.second.isLoading)
                            state.speed += it.second.speed
                    }
                }
            } else {
                list.items.forEach {
                    if (it.isLoad) {
                        val itmState = ItemState()
                        itmState.size = it.size
                        itmState.complete = it.isComplete
                        itmState.loaded = it.loaded
                        state.loadedItems.add(itmState)

                        state.fragments++
                        state.size += it.size
                        if (it.isComplete) {
                            state.loadedBytes += itmState.size
                            state.loadedFragments++
                        } else
                            state.loadedBytes += itmState.loaded
                    }
                }
                if (state.isComplete) {
                    val fSize = Storage.getDocument(list.filePath).length()
                    if (fSize > 0)
                        state.size = fSize
                }
            }
        }
        return state
    }

    private fun loadSubtitles() {
        if (!list.subsUrl.isEmpty()) {
            try {
                val file = File(Settings.downloadPath, list.title + ".srt")
                if (!file.exists() || file.length() == 0L) {
                    val client = ClientBuilder.new(Uri.parse(list.subsUrl))
                    client.connect()
                    val subs = client.getInputStream()?.bufferedReader()?.readText() ?: ""
                    client.close()
                    if (subs.isNotEmpty()) {
                        val writer = FileWriter(File(Settings.downloadPath, list.title + ".srt").path)
                        writer.resize(0)
                        writer.write(subs.toByteArray(), 0)
                        writer.close()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(App.getContext(), error_load_subs, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun preloadSize() {
        if (Settings.preloadSize) {
            executor = Executors.newFixedThreadPool(20)
            list.items.forEach {
                if (it.isLoad && it.size == 0L && !stop) {
                    val worker = Runnable {
                        for (i in 1..Settings.errorRepeat)
                            try {
                                val clientPS = ClientBuilder.new(Uri.parse(it.url))
                                clientPS.connect()
                                it.size = clientPS.getSize()
                                clientPS.close()
                                if (it.size == 0L)
                                    break
                                return@Runnable
                            } catch (e: Exception) {
                            }
                        executor?.shutdownNow()
                    }
                    executor?.execute(worker)
                }
            }
            executor?.shutdown()
            executor?.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)
            executor = null
            Saver.saveList(list)
        }
    }
}