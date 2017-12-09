package ru.yourok.m3u8loader.activitys.editorActivity

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar
import ru.yourok.dwl.manager.Manager
import ru.yourok.dwl.storage.Storage
import ru.yourok.dwl.utils.Saver
import ru.yourok.dwl.utils.Utils
import ru.yourok.m3u8loader.R


/**
 * Created by yourok on 01.12.17.
 */

class EditorAdaptor(val lists: List<ru.yourok.dwl.list.List>, val context: Context) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, p2: ViewGroup?): View {
        var view: View = convertView ?: (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.editor_list_adaptor, null)
        view.isEnabled = false
        view.setOnClickListener(null)

        val list = lists[position]

        view.findViewById<TextView>(R.id.textViewUrlItem).text = list.url
        view.findViewById<TextView>(R.id.textViewNameItem).text = list.title

        val textViewInfo = view.findViewById<TextView>(R.id.textViewItemsInfo)

        val rangeBar = view.findViewById<CrystalRangeSeekbar>(R.id.rangeBar)
        rangeBar.setMaxValue((list.items.size).toFloat())
        var start = 0
        var isStart = false
        var end = list.items.size
        var isEnd = false
        list.items.forEachIndexed { index, item ->
            if (!isStart && item.isLoad) {
                start = index
                isStart = true
            }
            if (isStart && !isEnd && !item.isLoad) {
                end = index
                isEnd = true
            }
            if (isStart && isEnd)
                return@forEachIndexed
        }

        if (start < 0) start = 0
        if (start > list.items.size) start = list.items.size
        if (end < 0) end = 0
        if (end > list.items.size) end = list.items.size

        rangeBar.setMinStartValue(start.toFloat())
        rangeBar.setMaxStartValue(end.toFloat())
        rangeBar.apply()
        val loader = Manager.findLoader(list)
        rangeBar.setOnRangeSeekbarChangeListener { minValue, maxValue ->
            var startD = 0F
            var endD = 0F
            var betweenD = 0F
            var allD = 0F
            var loadedSize = 0L
            var allSize = 0L
            list.items.forEachIndexed { index, item ->
                item.isLoad = index >= minValue.toInt() && index < maxValue.toInt()
                if (index < minValue.toInt())
                    startD += item.duration
                if (item.isLoad) {
                    betweenD += item.duration
                    loadedSize += item.size
                }
                if (index < maxValue.toInt())
                    endD += item.duration
                allD += item.duration
                allSize += item.size
            }

            var info = "[ $minValue .. $maxValue ]  ${list.items.size}\n" +
                    "[ ${Utils.durationFmt(startD)} .. ${Utils.durationFmt(endD)} ]  ${Utils.durationFmt(betweenD)} / ${Utils.durationFmt(allD)}"

            if (loadedSize > 0 && allSize > 0)
                info += "\n${Utils.byteFmt(loadedSize)} / ${Utils.byteFmt(allSize)}"

            textViewInfo.text = info
            if (loader != null && loader.isComplete())
                loader.clear()
        }

        view.findViewById<TextView>(R.id.buttonClear).setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage(R.string.warn_clean_message)
            builder.setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener { dialogInterface, i ->
                loader?.clear()
                list.items.forEach {
                    it.isComplete = false
                }
                Storage.getDocument(list.filePath)?.delete()
                Saver.saveList(list)
            })
            builder.setNegativeButton(android.R.string.cancel, null)
            builder.create().show()
        }

        return view
    }

    override fun getItem(p0: Int): Any {
        return lists[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return lists.size
    }
}