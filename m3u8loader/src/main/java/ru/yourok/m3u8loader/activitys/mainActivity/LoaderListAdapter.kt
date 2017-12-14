package ru.yourok.m3u8loader.activitys.mainActivity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import ru.yourok.dwl.converter.Converter
import ru.yourok.dwl.downloader.LoadState
import ru.yourok.dwl.manager.Manager
import ru.yourok.m3u8loader.R

class LoaderListAdapter(val context: Context) : BaseAdapter() {

    fun autoupdate(a: Boolean) {
        //TODO проверить идет ли update в фоне
    }

    override fun getCount(): Int {
        return Manager.getLoadersSize()
    }

    override fun getItem(i: Int): Any? {
        return Manager.getLoader(i)
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(index: Int, convertView: View?, viewGroup: ViewGroup): View {
        val vi: View = convertView ?: (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.loader_list_adaptor, null)
        Manager.getLoader(index)?.let {
            vi.findViewById<TextView>(R.id.textViewNameItem).setText(it.list.title)
            val imgStatus = vi.findViewById<ImageView>(R.id.imageViewLoader)
            val state = it.getState()
            when {
                state.state == LoadState.ST_PAUSE -> {
                    if (Manager.inQueue(index))
                        imgStatus.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp)
                    else
                        imgStatus.setImageResource(R.drawable.ic_pause_black_24dp)
                }
                state.state == LoadState.ST_LOADING -> {
                    imgStatus.setImageResource(R.drawable.ic_file_download_black_24dp)
                }
                state.state == LoadState.ST_COMPLETE -> {
                    imgStatus.setImageResource(R.drawable.ic_check_black_24dp)
                }
                state.state == LoadState.ST_ERROR -> {
                    imgStatus.setImageResource(R.drawable.ic_report_problem_black_24dp)
                }
            }
            if (Converter.installed()) {
                val convList = Converter.stat()
                if (convList.find { state.name + state.file == it }?.isEmpty() == false) {
                    imgStatus.setImageResource(R.drawable.ic_convert_black)
                }
            }

            val err = state.error
            if (!err.isEmpty())
                vi.findViewById<TextView>(R.id.textViewError).setText(err)
            else
                vi.findViewById<TextView>(R.id.textViewError).setText("")

            val progress = vi.findViewById<ProgressView>(R.id.li_progress)
            progress.setIndexList(index)
        }
        return vi
    }
}