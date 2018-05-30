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
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            context.getSystemService<NotificationManager>(NotificationManager::class.java)!!.createNotificationChannel(channel)
        }

        val builder: NotificationCompat.Builder
        builder = NotificationCompat.Builder(context)

        builder.setContentTitle(title)
        builder.setContentText(text)
        builder.setChannelId(channelId)
        builder.setAutoCancel(true)

        var typeIcon = 0

        when (progress) {
            in 0..100 -> {
                builder.setProgress(100, progress, false)
                typeIcon = 1
            }
            -1 -> {
                builder.setProgress(100, 0, true)
                typeIcon = 2
            }
            else -> {
                typeIcon = 0
                builder.setProgress(0, 0, false)
            }
        }

        if (error.isNotEmpty())
            typeIcon = 3

        builder.setSmallIcon(createIcon(typeIcon))

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

    private fun createIcon(type: Int): Int {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            if (Notifyer.error.isEmpty())
//                return R.drawable.ic_check_black_24dp
//            else
//                return R.drawable.ic_report_problem_black_24dp
//        } else {
        if (type == 1)
            return android.R.drawable.stat_sys_download
        if (type == 2)
            return android.R.drawable.stat_notify_sync_noanim
        if (type == 3)
            return android.R.drawable.stat_notify_error
        return R.mipmap.ic_launcher
//        }
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