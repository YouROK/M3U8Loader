package ru.yourok.m3u8loader.activitys.mainActivity

import android.app.Activity
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.AbsListView
import android.widget.ListView
import ru.yourok.dwl.manager.Manager
import ru.yourok.m3u8loader.R
import ru.yourok.m3u8loader.player.PlayIntent

/**
 * Created by yourok on 19.11.17.
 */
class LoaderListSelectionMenu(val activity: Activity) : AbsListView.MultiChoiceModeListener {

    private val selected: MutableSet<Int> = mutableSetOf()

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.menuInflater?.inflate(R.menu.loader_selector_menu, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.itemPlay -> {
                if (selected.size == 1) {
                    val stat = Manager.getLoaderStat(selected.first())
                    if (stat != null)
                        PlayIntent.start(activity, stat.file, stat.name)
                }
            }
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
            R.id.itemRemove -> {
                selected.forEach {
                    Manager.remove(it, activity)
                }
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
        mode?.menu?.getItem(0)?.setVisible(selected.size == 1)
    }
}