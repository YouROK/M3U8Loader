package ru.yourok.m3u8loader.fragments.loaderList

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import ru.yourok.dwl.manager.Manager
import ru.yourok.m3u8loader.R


class ProgressView : View {
    var background: Int = -1

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    private var index: Int? = null

    fun setIndexList(index: Int) {
        this.index = index
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val width = getMeasuredWidth()
        val height = getMeasuredHeight()

        val paint = Paint()
        paint.isAntiAlias = true
        if (background == -1)
            background = ContextCompat.getColor(context, R.color.colorPrimaryDark)
        paint.color = background
        paint.style = Paint.Style.FILL
        canvas!!.drawRect(Rect(0, 0, width, height), paint)

        if (canvas == null && index == null)
            return

        val color = ContextCompat.getColor(context, R.color.colorAccent)
        val loader = Manager.getLoader(index!!)
        loader?.let {
            if (loader.getWorkers().isNotEmpty()) {
                val fragSize = ((width.toDouble() / loader.getWorkers().size.toDouble()))
                loader.getWorkers().let {
                    it.forEachIndexed { index, item ->
                        val paint = Paint()
                        paint.isAntiAlias = true
                        paint.style = Paint.Style.FILL

                        var prcItem = 0
                        item?.let {
                            if (item.first.item.isCompleteLoad)
                                prcItem = 255
                            else if (item.first.item.size > 0)
                                prcItem = (item.second.loadedBytes * 255 / item.first.item.size).toInt()
                        }
                        paint.setARGB(prcItem, Color.red(color), Color.green(color), Color.blue(color))
                        canvas.drawRect(Rect((index * fragSize + .5).toInt(), 0, (index * fragSize + fragSize + .5).toInt(), height), paint)
                    }
                }
            }
        }
        invalidate()
    }
}
