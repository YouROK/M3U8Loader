package ru.yourok.m3u8loader.activitys.mainActivity

import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import ru.yourok.dwl.manager.Manager
import ru.yourok.dwl.utils.Utils
import ru.yourok.m3u8loader.R
import java.util.*
import kotlin.concurrent.schedule


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
            val state = loader.getState()
            val fragSize = ((width.toDouble() / state.loadedItems.size))
            state.let {
                it.loadedItems.forEachIndexed { index, item ->
                    val paint = Paint()
                    paint.isAntiAlias = true
                    paint.style = Paint.Style.FILL

                    var prcItem = 0
                    item.let {
                        if (item.complete)
                            prcItem = 255
                        else if (item.size > 0)
                            prcItem = (item.loaded * 255 / item.size).toInt()
                    }
                    paint.setARGB(prcItem, Color.red(color), Color.green(color), Color.blue(color))
                    canvas.drawRect(Rect((index * fragSize + .5).toInt(), 0, (index * fragSize + fragSize + .5).toInt(), height), paint)
                }
            }
        }

        val stat = Manager.getLoaderStat(index!!)
        stat?.let {
            var frags = " %d / %d".format(stat.loadedFragments, stat.fragments)
            var speed = ""
            var size = ""
            if (stat.speed > 0)
                speed = "  %s/sec ".format(Utils.byteFmt(stat.speed))
            if (stat.size > 0)
                size = "  %s / %s".format(Utils.byteFmt(stat.loadedBytes), Utils.byteFmt(stat.size))

            val color = Color.LTGRAY

            val paint = Paint()
            paint.isAntiAlias = true
            paint.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
            paint.color = color
            paint.textSize = height.toFloat() * 0.9F

            var rect = Rect()
            paint.getTextBounds(frags, 0, frags.length, rect)
            val yPos = (canvas.height / 2 - (paint.descent() + paint.ascent()) / 2)
            canvas.drawText(frags, 0F, yPos, paint)

            if (!speed.isNullOrEmpty()) {
                paint.getTextBounds(speed, 0, speed.length, rect)
                val xPos = width.toFloat() / 2 - rect.centerX().toFloat()
                canvas.drawText(speed, xPos, yPos, paint)
            }
            if (!size.isNullOrEmpty()) {
                paint.getTextBounds(size, 0, size.length, rect)
                val xPos = width.toFloat() - rect.right.toFloat()
                canvas.drawText(size, xPos - 5.0F, yPos, paint)
            }
        }
        Timer().schedule(50) { postInvalidate() }
    }
}
