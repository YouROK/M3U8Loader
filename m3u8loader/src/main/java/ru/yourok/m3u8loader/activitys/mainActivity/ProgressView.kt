package ru.yourok.m3u8loader.activitys.mainActivity

import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import ru.yourok.dwl.downloader.LoadState
import ru.yourok.dwl.manager.Manager
import ru.yourok.dwl.settings.Preferences
import ru.yourok.dwl.utils.Utils
import ru.yourok.m3u8loader.R
import kotlin.concurrent.thread


class ProgressView : View {
    private var background: Int = -1
    private var index: Int? = null
    private var delay: Long = 50L
    private val rect: Rect = Rect()
    private val paint = Paint()
    private var isUpdating = false

    private var currentProg = 0
    private var toProg = 0
    private var isMoveProg = false
    private var fragmentWidth: Double = 0.0
    var lastState: String = ""

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun setIndexList(index: Int) {
        if (this.index != index) {
            this.index = index
            fillVars()
        }
        autoUpdate()
    }

    private fun autoUpdate() {
        synchronized(isUpdating) {
            if (isUpdating)
                return

            index?.let {
                Manager.getLoader(it)?.let {
                    val state = it.getState()
                    if (state.toString() != lastState)
                        invalidate()
                    if (state.state != LoadState.ST_LOADING)
                        return
                } ?: return
            } ?: return


            isUpdating = true
        }

        thread {
            while (isUpdating && !(Preferences.get("SimpleProgress", false) as Boolean)) {
                val rect: Rect? = Rect()
                isUpdating = getLocalVisibleRect(rect)
                if (isUpdating) {
                    try {
                        postInvalidate()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        isUpdating = false
                    }
                    index?.let {
                        Manager.getLoader(it)?.let {
                            if (it.getState().state == LoadState.ST_LOADING)
                                Thread.sleep(30)
                            else
                                isUpdating = false
                        }
                    }
                }
            }
            isUpdating = false
        }
    }

    private fun updateProg() {
        if (currentProg == toProg)
            return

        var delta = toProg - currentProg
        if (delta == 0)
            delta = 1
        delay = (500 / delta).toLong()

        synchronized(isMoveProg) {
            if (isMoveProg)
                return
            isMoveProg = true
        }

        thread {
            while (currentProg < toProg) {
                if (delay < 10) {
                    currentProg += 10
                    if (currentProg > toProg)
                        currentProg = toProg
                    Thread.sleep(50)
                } else {
                    currentProg++
                    Thread.sleep(delay)
                }
                if (!isUpdating)
                    postInvalidate()
            }
            isMoveProg = false
        }
    }

    private fun fillVars() {
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        currentProg = 0
        toProg = 0
        fragmentWidth = 1.0
        index?.let { index ->
            val loader = Manager.getLoader(index)
            loader?.let { loader ->
                val state = loader.getState()
                fragmentWidth = ((width.toDouble() / state.loadedItems.size))
                var lastItem = -1
                state.loadedItems.forEachIndexed { index, item ->
                    if (lastItem == -1 && !item.complete) {
                        lastItem = index - 1
                        return@forEachIndexed
                    }
                }
                toProg = (lastItem * fragmentWidth + fragmentWidth + .5).toInt()
                currentProg = toProg
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        val loader = Manager.getLoader(index!!)

        val width = getWidth()
        val height = getHeight()

        if (background == -1)
            background = ContextCompat.getColor(context, R.color.colorPrimaryDark)
        paint.color = background

        rect.right = width
        rect.bottom = height
        rect.left = 0
        rect.top = 0
        canvas!!.drawRect(rect, paint)

        if (index == null)
            return

        val itmColor = ContextCompat.getColor(context, R.color.colorAccent)
        loader?.let {
            val state = loader.getState()

            state.let {
                if (it.fragments == it.loadedFragments) {
                    paint.color = itmColor
                    canvas.drawRect(rect, paint)
                } else {
                    fragmentWidth = ((width.toDouble() / it.loadedItems.size))

                    var endItem = -1
                    var heightBottom = height / 10
                    if (heightBottom == 0)
                        heightBottom = 1

                    it.loadedItems.forEachIndexed { index, item ->
                        if (!item.complete && endItem == -1)
                            endItem = index - 1

                        var prcItem = 0
                        var color = itmColor

                        if (item.complete)
                            prcItem = 255
                        else if (item.size > 0)
                            prcItem = (item.loaded * 255 / item.size).toInt()
                        else if ((item.size == 0L) and (item.loaded > 0))
                            prcItem = 127

                        if (item.error)
                            color = Color.RED

                        val bottom: Int
                        if (prcItem > 0) {
                            val hgh = height - heightBottom
                            bottom = prcItem * hgh / 255
                            paint.setARGB(prcItem, Color.red(color), Color.green(color), Color.blue(color))
                            rect.left = (index * fragmentWidth + .5).toInt()
                            rect.right = (index * fragmentWidth + fragmentWidth + .6).toInt()
                            rect.bottom = bottom
                            rect.top = 0
                            canvas.drawRect(rect, paint)
                        }
                    }
                    if (endItem > 0 || state.isComplete) {
                        paint.color = itmColor
                        rect.left = 0
                        rect.top = 0
                        rect.bottom = height
                        if (state.isComplete) {
                            toProg = width
                            currentProg = toProg
                        } else
                            toProg = (endItem * fragmentWidth + fragmentWidth + .5).toInt()
                        rect.right = currentProg
                        canvas.drawRect(rect, paint)
                        updateProg()
                    }
                }

                var frags = "%d/%d".format(it.loadedFragments, it.fragments)
                if (it.threads > 0)
                    frags = "%-3d: %s".format(it.threads, frags)
                var speed = ""
                var size = ""
                if (it.speed > 0)
                    speed = "  %s/sec ".format(Utils.byteFmt(it.speed))
                if (it.size > 0 && it.isComplete)
                    size = "  %s".format(Utils.byteFmt(it.size))
                else
                    size = "  %s/%s".format(Utils.byteFmt(it.loadedBytes), Utils.byteFmt(it.size))

                paint.typeface = Typeface.DEFAULT
                paint.setARGB(180, 255, 255, 255)
                paint.textSize = height.toFloat() * 0.8F
                paint.setShadowLayer(5.0F, 0.0F, 0.0F, Color.BLACK)

                paint.getTextBounds(frags, 0, frags.length, rect)
                val yPos = (canvas!!.height / 2 - (paint.descent() + paint.ascent()) / 2)
                canvas.drawText(frags, 5.0F, yPos, paint)

                if (!speed.isEmpty()) {
                    paint.getTextBounds(speed, 0, speed.length, rect)
                    val xPos = width.toFloat() / 2 - rect.centerX().toFloat()
                    canvas.drawText(speed, xPos, yPos, paint)
                }
                if (!size.isEmpty()) {
                    paint.getTextBounds(size, 0, size.length, rect)
                    val xPos = width.toFloat() - rect.right.toFloat()
                    canvas.drawText(size, xPos - 5.0F, yPos, paint)
                }

                lastState = it.toString()
            }
        }
    }
}
