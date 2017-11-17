package ru.yourok.m3u8loader.fragments.loaderList

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import ru.yourok.dwl.manager.Manager
import ru.yourok.dwl.utils.Utils
import ru.yourok.m3u8loader.R

class LoaderListAdapter(val context: Context) : BaseAdapter() {

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
        var vi: View = convertView ?: (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.fragment_loader_list_adaptor, null)
        Manager.getLoader(index)?.let {
            val stat = it.getState()
            val textName = vi.findViewById<TextView>(R.id.textViewNameItem)
            val textStat = vi.findViewById<TextView>(R.id.textViewStatusItem)
            val imgStatus = vi.findViewById<ImageView>(R.id.imageViewLoader)

            textName.text = stat.name
            var text = ""
            text += "%d / %d".format(stat.loadedFragments, stat.fragments)
            if (stat.speed > 0) {
                text += "  %s".format(Utils.byteFmt(stat.speed))
                textStat.text = text
            }
            if (stat.size > 0)
                text += "  %s / %s".format(Utils.byteFmt(stat.loadedBytes), Utils.byteFmt(stat.size))
            textStat.text = text

            when {
                it.isLoading() -> {
                    imgStatus.setImageResource(R.drawable.ic_play_circle_outline_black_24dp)
                }
                Manager.inQueue(index) ->
                    imgStatus.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp)
                it.getState().isComplete ->
                    imgStatus.setImageResource(R.drawable.ic_check_black_24dp)
                else ->
                    imgStatus.setImageResource(R.drawable.ic_pause_black_24dp)
            }
            if (stat.error.isNotEmpty())
                textStat.text = stat.error

            val progress = vi.findViewById<ProgressView>(R.id.li_progress)
            progress.setIndexList(index)
        }
        return vi
    }
}