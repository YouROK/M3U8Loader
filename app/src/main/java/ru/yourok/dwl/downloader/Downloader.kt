package ru.yourok.dwl.downloader

import ru.yourok.dwl.list.List
import ru.yourok.dwl.utils.Utils

/**
 * Created by yourok on 09.11.17.
 */
class Downloader(val list: List) {
    private var pool: Pool? = null
    private val workers: MutableList<Pair<Worker, DownloadStatus>> = mutableListOf()
    private var error: String = ""
    private var complete: Boolean = false

    fun load() {
        try {
            complete = false
            error = ""
            val file = File(list.filePath)
            var resize = true
            var size = 0L
            workers.clear()
            list.items.forEach {
                if (it.isLoad) {
                    val stat = DownloadStatus()
                    val wrk = Worker(it, stat, file)
                    workers.add(Pair(wrk, stat))
                    if (it.size == 0L)
                        resize = false
                    size += it.size
                }
            }
            if (resize)
                file.resize(size)

            workers.sortBy { it.first.item.index }

            file.setWorkers(workers)

            pool = Pool(workers)
            pool!!.start()
            pool!!.onEnd {
                complete = true
                workers.forEach {
                    if (!it.first.item.isCompleteLoad)
                        complete = false
                }

                if (!resize && complete) {
                    size = 0L
                    workers.forEach {
                        size += it.first.item.size
                    }
                    file.resize(size)
                }
                file.close()
                Utils.saveList(list)
            }

            pool!!.onFinishWorker {
                Utils.saveList(list)
            }
        } catch (e: Exception) {
            error = e.message ?: ""
        }
    }

    fun stop() {
        pool?.stop()
    }

    fun waitEnd() {
        if (pool != null)
            pool!!.waitEnd()
    }

    fun isLoading(): Boolean {
        return pool?.isWorking() ?: false
    }

    fun isComplete(): Boolean = complete

    fun getState(): State {
        val state = State()
        state.name = list.info.title
        state.url = list.url
        state.fragments = workers.size
        state.threads = pool?.size() ?: 0
        state.error = error
        state.isComplete = complete
        if (workers.size != 0)
            workers.forEach {
                if (it.first.item.isLoad) {
                    val itmState = ItemState()
                    itmState.loaded = it.second.loadedBytes
                    itmState.size = it.first.item.size
                    itmState.complete = it.first.item.isCompleteLoad
                    state.loadedItems.add(itmState)

                    state.size += it.first.item.size
                    state.loadedBytes += it.second.loadedBytes
                    state.duration += it.first.item.duration
                    if (it.first.item.isCompleteLoad) {
                        state.loadedFragments++
                        state.loadedDuration += it.first.item.duration
                    }
                    if (it.second.isLoading)
                        state.speed += it.second.speed
                }
            }
        else {
            list.items.forEach {
                if (it.isLoad) {
                    val itmState = ItemState()
                    itmState.loaded = 0
                    itmState.size = it.size
                    itmState.complete = it.isCompleteLoad
                    state.loadedItems.add(itmState)

                    state.fragments++
                    state.size += it.size
                    state.duration += it.duration
                    if (it.isCompleteLoad) {
                        state.loadedFragments++
                        state.loadedDuration += it.duration
                    }
                }
            }
        }
        return state
    }

    fun getWorkers(): kotlin.collections.List<Pair<Worker, DownloadStatus>> {
        return workers.toList()
    }
}