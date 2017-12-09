package ru.yourok.m3u8loader.activitys.mainActivity

import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import ru.yourok.dwl.downloader.LoadState
import ru.yourok.dwl.manager.Manager
import ru.yourok.dwl.utils.Utils
import ru.yourok.m3u8loader.R
import java.util.*
import kotlin.concurrent.schedule


class ProgressView : View {
    private var background: Int = -1
    private val timer: Timer = Timer()
    private var index: Int? = null
    private var times: Long = 0L
    private var delay: Long = 50L
    private val rect: Rect = Rect()

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    fun setIndexList(index: Int) {
        this.index = index
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val width = getMeasuredWidth()
        val height = getHeight()

        val paint = Paint()
        paint.isAntiAlias = true
        if (background == -1)
            background = ContextCompat.getColor(context, R.color.colorPrimaryDark)
        paint.color = background
        paint.style = Paint.Style.FILL
        rect.right = width
        rect.bottom = height
        rect.left = 0
        rect.top = 0
        canvas!!.drawRect(rect, paint)

        if (canvas == null && index == null)
            return

        val itmColor = ContextCompat.getColor(context, R.color.colorAccent)
        val loader = Manager.getLoader(index!!)
        loader?.let {
            val state = loader.getState()
            val fragSize = ((width.toDouble() / state.loadedItems.size))
            state.let {
                if (state.state != LoadState.ST_LOADING)
                    delay = 500L
                val paintItem = Paint()
                paintItem.isAntiAlias = true
                paintItem.style = Paint.Style.FILL

                var endItem = 0
                var heightBottom = height / 10
                if (heightBottom == 0)
                    heightBottom = 1

                it.loadedItems.forEachIndexed { index, item ->
                    var prcItem = 0
                    var color = itmColor
                    item.let {
                        if (item.complete)
                            prcItem = 255
                        else if (item.size > 0)
                            prcItem = (item.loaded * 255 / item.size).toInt()

                        if (endItem == 0 && !item.complete)
                            endItem = index - 1
                        if (item.error)
                            color = Color.RED
                    }
                    val bottom: Int
                    if (prcItem > 0) {
                        val hgh = height - heightBottom
                        bottom = prcItem * hgh / 255
                        paintItem.setARGB(prcItem, Color.red(color), Color.green(color), Color.blue(color))
                        rect.left = (index * fragSize + .5).toInt()
                        rect.right = (index * fragSize + fragSize + .5).toInt()
                        rect.bottom = bottom
                        rect.top = 0
                        canvas.drawRect(rect, paintItem)
                    }
                }
                if (endItem > 0 || state.isComplete) {
                    paintItem.color = itmColor
                    rect.left = 0
                    rect.top = height - heightBottom
                    if (state.isComplete)
                        rect.right = width
                    else
                        rect.right = (endItem * fragSize + fragSize + .5).toInt()
                    rect.bottom = height
                    canvas.drawRect(rect, paintItem)
                }
            }
        }

        val stat = Manager.getLoaderStat(index!!)
        stat?.let {
            var frags = "%d/%d".format(stat.loadedFragments, stat.fragments)
            if (stat.threads > 0)
                frags = "%-3d: %s".format(stat.threads, frags)
            var speed = ""
            var size = ""
            if (stat.speed > 0)
                speed = "  %s/sec ".format(Utils.byteFmt(stat.speed))
            if (stat.size > 0)
                size = "  %s/%s".format(Utils.byteFmt(stat.loadedBytes), Utils.byteFmt(stat.size))

            val colorText = Color.CYAN

            val paintText = Paint()
            paintText.isAntiAlias = true
            paintText.typeface = Typeface.DEFAULT
            paintText.color = colorText
            paintText.textSize = height.toFloat() * 0.8F
            paintText.setShadowLayer(5.0F, 0.0F, 0.0F, Color.BLACK)

            paintText.getTextBounds(frags, 0, frags.length, rect)
            val yPos = (canvas.height / 2 - (paintText.descent() + paintText.ascent()) / 2)
            canvas.drawText(frags, 5.0F, yPos, paintText)

            if (!speed.isNullOrEmpty()) {
                paintText.getTextBounds(speed, 0, speed.length, rect)
                val xPos = width.toFloat() / 2 - rect.centerX().toFloat()
                canvas.drawText(speed, xPos, yPos, paintText)
            }
            if (!size.isNullOrEmpty()) {
                paintText.getTextBounds(size, 0, size.length, rect)
                val xPos = width.toFloat() - rect.right.toFloat()
                canvas.drawText(size, xPos - 5.0F, yPos, paintText)
            }
        }

        times = System.currentTimeMillis()

        timer.schedule(delay) {
            postInvalidate()
            val delta = System.currentTimeMillis() - times

            if (delta == delay)
                delay = 30

            if (delta < delay && delay > 30)
                delay--

            if (delta > delay && delay < 2000)
                delay++
        }
    }
}
