package ru.yourok.dwl.manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import ru.yourok.dwl.list.List
import ru.yourok.m3u8loader.App
import ru.yourok.m3u8loader.R
import ru.yourok.m3u8loader.activitys.mainActivity.MainActivity

/**
 * Created by yourok on 19.11.17.
 */
object Notifyer {
    private var notificationManager: NotificationManager? = null

    val TYPE_NOTIFYLOAD = 0
    val TYPE_NOTIFYCONVERT = 1

    fun sendNotification(context: Context, type: Int, title: String, text: String, progress: Int) {
        val channelId = getChannelId(type)
        val channelName = getChannelName(type)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            context.getSystemService<NotificationManager>(NotificationManager::class.java)!!.createNotificationChannel(channel)
        }

        val builder: NotificationCompat.Builder
        builder = NotificationCompat.Builder(context)

        builder.setSmallIcon(createIcon())

        builder.setContentTitle(title)
        builder.setContentText(text)
        builder.setChannelId(channelId)
        builder.setAutoCancel(true)

        when (progress) {
            in 0..100 -> builder.setProgress(100, progress, false)
            -1 -> builder.setProgress(100, 0, true)
            else -> builder.setProgress(0, 0, false)
        }

        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        builder.setContentIntent(pendingIntent)

        getManager().notify(type, builder.build())
    }

    fun sendNotification(context: Context, type: Int, title: String) {
        sendNotification(context, type, title, "", -2)
    }

    fun sendNotification(context: Context, type: Int, title: String, text: String) {
        sendNotification(context, type, title, text, -2)
    }

    private fun getChannelId(type: Int): String {
        when (type) {
            TYPE_NOTIFYLOAD -> return "ru.yourok.m3u8loader.load"
            TYPE_NOTIFYCONVERT -> return "ru.yourok.m3u8loader.convert"
            else -> return "ru.yourok.m3u8loader"
        }
    }

    private fun getChannelName(type: Int): String {
        when (type) {
            TYPE_NOTIFYLOAD -> return "Loading"
            TYPE_NOTIFYCONVERT -> return "Converting"
            else -> return "M3U8 Loader"
        }
    }

    private fun createIcon(): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Notifyer.error.isEmpty())
                return R.drawable.ic_check_black_24dp
            else
                return R.drawable.ic_report_problem_black_24dp
        } else
            return R.mipmap.ic_launcher
    }

    private fun getManager(): NotificationManager {
        notificationManager?.let {
            return it
        }
        notificationManager = App.getContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager!!
    }

    var error: String = ""

    fun toastEnd(list: List, complete: Boolean, err: String) {
        error = err
        with(App.getContext()) {
            Handler(Looper.getMainLooper()).post {
                if (complete && err.isEmpty()) {
                    Toast.makeText(this, this.getText(R.string.complete).toString() + ": " + list.title, Toast.LENGTH_SHORT).show()
                } else if (!err.isEmpty()) {
                    Toast.makeText(this, this.getText(R.string.error).toString() + ": " + list.title + ", " + err, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}