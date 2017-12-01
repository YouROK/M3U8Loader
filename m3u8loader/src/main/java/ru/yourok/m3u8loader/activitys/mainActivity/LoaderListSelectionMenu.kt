package ru.yourok.m3u8loader.activitys.mainActivity

import android.app.Activity
import android.content.Intent
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.AbsListView
import android.widget.ListView
import android.widget.Toast
import ru.yourok.dwl.converter.Converter
import ru.yourok.dwl.list.List
import ru.yourok.dwl.manager.Manager
import ru.yourok.m3u8loader.R
import ru.yourok.m3u8loader.activitys.editorActivity.EditorActivity
import kotlin.concurrent.thread

/**
 * Created by yourok on 19.11.17.
 */
class LoaderListSelectionMenu(val activity: Activity) : AbsListView.MultiChoiceModeListener {

    private val selected: MutableSet<Int> = mutableSetOf()

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.menuInflater?.inflate(R.menu.loader_selector_menu, menu)
        selected.clear()
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.itemLoad -> {
                selected.forEach {
                    Manager.load(it)
                }
            }
            R.id.itemPause -> {
                selected.forEach {
                    Manager.stop(it)
                }
            }
            R.id.itemConvert -> {
                if (!Converter.installed()) {
                    Toast.makeText(activity, R.string.warn_install_convertor, Toast.LENGTH_SHORT).show()
                    return false
                }
                val sendList = mutableListOf<List>()
                selected.forEach {
                    Manager.getLoader(it)?.let {
                        sendList.add(it.list)
                    }
                }
                Converter.convert(sendList)
            }
            R.id.itemEdit -> {
                thread {
                    EditorActivity.editorList.clear()
                    selected.forEach {
                        Manager.getLoader(it)?.let {
                            it.stop()
                            it.waitEnd()
                            EditorActivity.editorList.add(it.list)
                        }
                    }
                    if (EditorActivity.editorList.size > 0)
                        activity.startActivity(Intent(activity, EditorActivity::class.java))
                }
            }
            R.id.itemRemove -> {
                Manager.removes(selected, activity)
            }
            else -> return false
        }
        activity.findViewById<ListView>(R.id.listViewLoader).choiceMode = ListView.CHOICE_MODE_NONE
        activity.findViewById<ListView>(R.id.listViewLoader).adapter = LoaderListAdapter(activity)
        mode?.finish()
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
    }

    override fun onItemCheckedStateChanged(mode: ActionMode?, position: Int, id: Long, checked: Boolean) {
        if (checked)
            selected.add(position)
        else
            selected.remove(position)
    }
}