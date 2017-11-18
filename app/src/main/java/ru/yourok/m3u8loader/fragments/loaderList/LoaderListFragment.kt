package ru.yourok.m3u8loader.fragments.loaderList


import android.os.Bundle
import android.support.v4.app.ListFragment
import android.view.View
import android.widget.ListView
import ru.yourok.dwl.manager.Manager
import kotlin.concurrent.thread

class LoaderListFragment : ListFragment() {

    companion object {
        fun newInstance(): LoaderListFragment {
            return LoaderListFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listAdapter = LoaderListAdapter(activity!!.baseContext)
        update()
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)
        if (Manager.inQueue(position))
            Manager.stop(position)
        else
            Manager.load(position)
    }

    private fun update() {
        thread {
            while (true) {
                activity?.runOnUiThread {
                    (listAdapter as LoaderListAdapter).notifyDataSetChanged()
                }
                Thread.sleep(500)
            }
        }
    }
}
