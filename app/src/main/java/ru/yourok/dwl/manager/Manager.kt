package ru.yourok.dwl.manager

import android.app.Activity
import org.jetbrains.anko.alert
import ru.yourok.dwl.downloader.Downloader
import ru.yourok.dwl.downloader.State
import ru.yourok.dwl.list.List
import ru.yourok.dwl.utils.Utils
import ru.yourok.m3u8loader.R
import java.io.File
import kotlin.concurrent.thread


object Manager {
    private var loaderList: MutableList<Downloader> = mutableListOf()
    private var queueList: MutableList<Int> = mutableListOf()
    private val lock: Any = Any()

    init {
        val list = Utils.loadLists()
        if (list != null)
            list.forEach {
                loaderList.add(Downloader(it))
            }
    }

    fun getLoader(index: Int): Downloader? {
        synchronized(loaderList) {
            if (index in 0 until loaderList.size)
                return loaderList[index]
            return null
        }
    }

    fun getLoadersSize(): Int {
        synchronized(loaderList) {
            return loaderList.size
        }
    }

    fun getLoaderStat(i: Int): State? {
        synchronized(loaderList) {
            return getLoader(i)?.getState()
        }
    }

    fun addList(list: MutableList<List>) {
        synchronized(loaderList) {
            list.forEach {
                var isFindUrl = false
                loaderList.forEach { item ->
                    if (it.url == item.list.url)
                        isFindUrl = true
                }
                if (!isFindUrl) {
                    //find equal url
                    val flist = loaderList.find { downloader ->
                        downloader.getState().name == it.info.title
                    }
                    //replace urls if find eq
                    if (flist != null) {
                        flist.list.url = it.url
                        if (flist.list.items.size != it.items.size) {
                            it.items = flist.list.items
                        } else
                            flist.list.items.forEachIndexed { index, item ->
                                if (index < it.items.size)
                                    item.url = it.items[index].url
                            }

                    } else
                        loaderList.add(Downloader(it))
                }
            }
            list.forEach { Utils.saveList(it) }
        }
    }

    fun remove(index: Int, activity: Activity) {
        synchronized(loaderList) {
            val loader = getLoader(index)
            if (loader != null) {
                val file = File(loader.list.filePath)
                if (file.exists()) {
                    with(activity) {
                        this.alert {
                            title = this@with.getString(R.string.delete_all_items) + "?"
                            positiveButton(R.string.delete_with_files, {
                                loader.stop()
                                loader.waitEnd()
                                file.delete()
                                Utils.removeList(loader.list)
                                loaderList.removeAt(index)
                            })
                            negativeButton(R.string.remove_from_list, {
                                loader.stop()
                                loader.waitEnd()
                                Utils.removeList(loader.list)
                                loaderList.removeAt(index)
                            })
                            neutralPressed(" ", {})
                        }.show()
                    }
                } else {
                    loader.stop()
                    loader.waitEnd()
                    Utils.removeList(loader.list)
                    loaderList.removeAt(index)
                }
            }
        }
    }

    fun removeAll(activity: Activity) {
        synchronized(loaderList) {
            var isFile = false
            loaderList.forEach {
                if (File(it.list.filePath).exists()) {
                    isFile = true
                    return@forEach
                }
            }
            if (isFile) {
                with(activity) {
                    this.alert {
                        title = this@with.getString(R.string.delete_all_items) + "?"
                        positiveButton(R.string.delete_with_files, {
                            stopAll()
                            loaderList.forEach {
                                it.waitEnd()
                                Utils.removeList(it.list)
                                val f = File(it.list.filePath)
                                if (f.exists()) f.delete()
                            }
                            loaderList.clear()
                        })
                        negativeButton(R.string.remove_from_list, {
                            stopAll()
                            loaderList.forEach {
                                it.waitEnd()
                                Utils.removeList(it.list)
                            }
                            loaderList.clear()
                        })
                        neutralPressed(" ", {})
                    }.show()
                }
            } else {
                stopAll()
                loaderList.forEach {
                    it.waitEnd()
                    Utils.removeList(it.list)
                }
                loaderList.clear()
            }
        }
    }

    /////////Queue funcs
    private val lockQueue: Any = Any()
    private var loading: Boolean = false
    private var currentLoader = -1

    fun load(index: Int) {
        if (index in 0 until loaderList.size) {
            if (loaderList[index].isComplete())
                return
            if (!inQueue(index))
                queueList.add(index)
            synchronized(lockQueue) {
                thread { startLoading() }
            }
        }
    }

    fun loadAll() {
        loaderList.forEachIndexed { index, downloader ->
            if (!downloader.isComplete() && !inQueue(index))
                queueList.add(index)
        }
        synchronized(lockQueue) {
            thread { startLoading() }
        }
    }

    fun stop(index: Int) {
        synchronized(lockQueue) {
            if (currentLoader == index)
                loaderList[index].stop()
            queueList.remove(index)
            if (queueList.size == 0)
                currentLoader = -1
        }
    }

    fun stopAll() {
        synchronized(lockQueue) {
            queueList.clear()
            loaderList.forEach { it.stop() }
            currentLoader = -1
        }
    }

    private fun startLoading() {
        synchronized(lockQueue) {
            if (queueList.isEmpty())
                return
            if (loading)
                return
            loading = true
        }
        thread {
            currentLoader = -1
            while (queueList.size > 0) {
                if (!loading)
                    break
                synchronized(lockQueue) {
                    currentLoader = queueList[0]
                    loaderList[currentLoader].load()
                    queueList.removeAt(0)
                }
                Thread.sleep(100)
                loaderList[currentLoader].waitEnd()
            }
            loading = false
            currentLoader = -1
        }
    }

    fun inQueue(index: Int): Boolean {
        synchronized(lockQueue) {
            if (index == currentLoader)
                return true
            if (index in 0 until loaderList.size)
                queueList.forEach { if (it == index) return true }
            return false
        }
    }
}