package ru.yourok.m3u8loader.activitys.mainActivity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import ru.yourok.converter.ConverterHelper
import ru.yourok.dwl.downloader.LoadState
import ru.yourok.dwl.manager.Manager
import ru.yourok.dwl.settings.Preferences
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
        val vi: View = convertView
                ?: (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.loader_list_adaptor, null)
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
            if (ConverterHelper.isConvert(it.list)) {
                imgStatus.setImageResource(R.drawable.ic_convert_black)
            }

            if (state.isPlayed)
                vi.findViewById<View>(R.id.imageViewPlayed).visibility = View.VISIBLE
            else
                vi.findViewById<View>(R.id.imageViewPlayed).visibility = View.GONE

            val err = state.error
            if (!err.isEmpty())
                vi.findViewById<TextView>(R.id.textViewError).setText(err)
            else
                vi.findViewById<TextView>(R.id.textViewError).setText("")


            val progress_f = vi.findViewById<ProgressView>(R.id.li_progress)
            val progress_s = vi.findViewById<View>(R.id.simpleProgress)

            if (Preferences.get("SimpleProgress", false) as Boolean) {
                progress_f.visibility = View.GONE
                progress_s.visibility = View.VISIBLE

                var frags = "%d/%d".format(state.loadedFragments, state.fragments)
                if (state.threads > 0)
                    frags = "%-3d: %s".format(state.threads, frags)
                var speed = ""
                var size = ""
                if (state.speed > 0)
                    speed = "  %s/sec ".format(Utils.byteFmt(state.speed))
                if (state.size > 0 && state.isComplete)
                    size = "  %s".format(Utils.byteFmt(state.size))
                else
                    size = "  %s/%s".format(Utils.byteFmt(state.loadedBytes), Utils.byteFmt(state.size))

                vi.findViewById<ProgressBar>(R.id.li_s_progress).progress = state.loadedFragments * 100 / state.fragments
                vi.findViewById<TextView>(R.id.textViewFragmentsStat).text = frags
                vi.findViewById<TextView>(R.id.textViewSpeedStat).text = speed
                vi.findViewById<TextView>(R.id.textViewSizeStat).text = size

            } else {
                progress_f.visibility = View.VISIBLE
                progress_s.visibility = View.GONE
                progress_f.setIndexList(index)
            }
        }
        return vi
    }
}