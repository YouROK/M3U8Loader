package ru.yourok.dwl.downloader

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import ru.yourok.dwl.client.ClientBuilder
import ru.yourok.dwl.converter.Converter
import ru.yourok.dwl.list.List
import ru.yourok.dwl.manager.Notifyer
import ru.yourok.dwl.settings.Settings
import ru.yourok.dwl.utils.Utils
import ru.yourok.m3u8loader.R.string.error_load_subs
import java.io.FileOutputStream
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

    private var completeLoad: Boolean = false
    private var isLoading: Boolean = false
    private var isConverting: Boolean = false
    private var stop: Boolean = false
    private var starting: Any = Any()

    init {
        var ncomp = true
        list.items.forEach {
            if (!it.isCompleteLoad) {
                ncomp = false
                return@forEach
            }
        }
        completeLoad = ncomp
    }

    fun load() {
        synchronized(starting) {
            synchronized(isLoading) {
                if (isLoading || isConverting)
                    return
                isLoading = true
            }
            try {
                stop = false
                isConverting = false
                isLoading = true
                completeLoad = false
                error = ""
                val file = File(java.io.File(Settings.downloadPath, list.filePath).path)
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

                loadSubtitles()
                pool = Pool(workers!!)
                pool!!.start()
                pool!!.onEnd {
                    completeLoad = true
                    workers!!.forEach {
                        if (!it.first.item.isCompleteLoad)
                            completeLoad = false
                    }

                    if (!resize && completeLoad) {
                        size = 0L
                        workers!!.forEach {
                            size += it.first.item.size
                        }
                        file.resize(size)
                    }
                    file.close()
                    Utils.saveList(list)
                    if (list.isConvert && !list.isConverted && completeLoad)
                        convertAsync()
                    isLoading = false
                    Notifyer.toastEnd(list, completeLoad, error)
                }

                pool!!.onFinishWorker {
                    Utils.saveList(list)
                }
                pool!!.onError {
                    error = it
                }

            } catch (e: Exception) {
                error = e.message ?: ""
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

    fun isLoading(): Boolean {
        return isLoading
    }

    fun isConverting(): Boolean {
        return isConverting
    }

    fun isComplete(): Boolean = completeLoad

    private fun convertAsync() {

        synchronized(completeLoad) {
            if (!completeLoad)
                return
        }

        synchronized(list.isConverted) {
            if (list.isConverted)
                return
            isConverting = true
        }

        while (Converter.isConverting()) {
            Thread.sleep(200)
        }

        val oldFile = java.io.File(Settings.downloadPath, list.filePath)
        val newFile = java.io.File(Settings.downloadPath, oldFile.nameWithoutExtension + ".non.mp4")
        if (oldFile.renameTo(newFile)) {
            list.filePath = newFile.path
            Converter.convert(newFile.path, oldFile.path, {
                //on Finish
                list.filePath = oldFile.path
                list.isConverted = true
                isConverting = false
                newFile.delete()
            }, { message ->
                //on Error
                oldFile.delete()
                newFile.renameTo(oldFile)
                list.filePath = oldFile.path
                error = message
                Toast.makeText(Settings.context, "Error convert: " + message, Toast.LENGTH_SHORT).show()
            })

            while (Converter.isConverting()) {
                Thread.sleep(200)
            }
        }
    }

    fun getState(): State {
        val state = State()
        synchronized(list) {
            state.name = list.info.title
            state.url = list.url
            state.file = java.io.File(Settings.downloadPath, list.filePath).path
            state.threads = pool?.size() ?: 0
            state.error = error
            state.isComplete = completeLoad
            if (isLoading)
                state.state = LoadState.ST_LOADING
            else if (isConverting)
                state.state = LoadState.ST_CONVERTING
            else if (completeLoad)
                state.state = LoadState.ST_COMPLETE
            else if (!error.isEmpty())
                state.state = LoadState.ST_ERROR


            if (workers?.size != 0 && pool?.isWorking() == true) {
                state.fragments = workers!!.size
                workers!!.forEach {
                    if (it.first.item.isLoad) {
                        val itmState = ItemState()
                        itmState.loaded = it.second.loadedBytes
                        itmState.size = it.first.item.size
                        itmState.complete = it.first.item.isCompleteLoad
                        itmState.error = it.second.isError
                        state.loadedItems.add(itmState)

                        state.size += it.first.item.size
                        if (it.first.item.isCompleteLoad)
                            state.loadedBytes += it.first.item.size
                        else
                            state.loadedBytes += it.second.loadedBytes
                        if (it.first.item.isCompleteLoad) {
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
                        itmState.complete = it.isCompleteLoad
                        if (it.isCompleteLoad)
                            itmState.loaded = itmState.size
                        state.loadedItems.add(itmState)

                        state.fragments++
                        state.size += it.size
                        if (it.isCompleteLoad) {
                            state.loadedBytes += itmState.size
                            state.loadedFragments++
                        }
                    }
                }
            }
        }
        return state
    }

    private fun loadSubtitles() {
        if (!list.subsUrl.isEmpty()) {
            try {
                val file = java.io.File(Settings.downloadPath, list.info.title + ".srt")
                if (!file.exists() || file.length() == 0L) {
                    val client = ClientBuilder.new(Uri.parse(list.subsUrl))
                    client.connect()
                    val subs = client.getInputStream()?.bufferedReader().use { it?.readText() ?: "" }
                    client.close()
                    if (!subs.isNullOrEmpty()) {
                        val oStream = FileOutputStream(file)
                        oStream.bufferedWriter().write(subs)
                        oStream.close()
                    }
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(Settings.context, error_load_subs, Toast.LENGTH_SHORT).show()
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
            Utils.saveList(list)
        }
    }
}