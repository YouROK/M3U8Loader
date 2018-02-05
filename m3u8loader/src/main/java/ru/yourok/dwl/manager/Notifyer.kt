package ru.yourok.dwl.manager

import android.app.Notification
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
    private var notificationBuilder: NotificationCompat.Builder? = null
    private var notificationManager: NotificationManager? = null
    private var context: Context? = null

    fun createNotification(context: Context) {
        this.context = context
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        notificationBuilder = NotificationCompat.Builder(context, "ru.yourok.m3u8loader.channel")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Notifyer.error.isEmpty())
                notificationBuilder?.setSmallIcon(R.drawable.ic_check_black_24dp)
            else
                notificationBuilder?.setSmallIcon(R.drawable.ic_report_problem_black_24dp)
        } else
            notificationBuilder?.setSmallIcon(R.mipmap.ic_launcher)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel("ru.yourok.m3u8loader.channel", "M3U8Loader", NotificationManager.IMPORTANCE_LOW)
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE)
            getManager().createNotificationChannel(mChannel)
        }
    }

    fun updateNotfication(title: String, text: String, progress: Int): Boolean {
        if (notificationBuilder == null || context == null)
            return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Notifyer.error.isEmpty())
                notificationBuilder?.setSmallIcon(android.R.drawable.stat_sys_download)
            else
                notificationBuilder?.setSmallIcon(R.drawable.ic_report_problem_black_24dp)
        }

        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        notificationBuilder!!
                .setContentTitle(title)
                .setContentText(text)
                .setProgress(100, progress, false)
                .setContentIntent(pendingIntent)

        val notificationManager = App.getContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder?.build())
        return true
    }

    fun finishNotification() {
        if (notificationBuilder == null)
            return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Notifyer.error.isEmpty())
                notificationBuilder?.setSmallIcon(R.drawable.ic_check_black_24dp)
            else
                notificationBuilder?.setSmallIcon(R.drawable.ic_report_problem_black_24dp)
        }

        var msg = App.getContext().getString(R.string.loading_complete)
        if (!Notifyer.error.isEmpty())
            msg = App.getContext().getString(R.string.loading_error)

        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        notificationBuilder!!
                .setContentTitle(msg)
                .setContentText(Notifyer.error)
                .setProgress(0, 0, false)
                .setContentIntent(pendingIntent)

        val notificationManager = App.getContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder?.build())
        notificationBuilder = null
    }

    fun addNotify(context: Context, name: String) {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val nBuilder = NotificationCompat.Builder(context, "ru.yourok.m3u8loader.channel1")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            nBuilder?.setSmallIcon(R.drawable.ic_check_black_24dp)
        } else
            nBuilder?.setSmallIcon(R.mipmap.ic_launcher)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel("ru.yourok.m3u8loader.channel1", "M3U8Loader", NotificationManager.IMPORTANCE_LOW)
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE)
            getManager().createNotificationChannel(mChannel)
        }

        nBuilder
                .setContentTitle(context.getString(R.string.added))
                .setContentText(name)
        val notificationManager = App.getContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, nBuilder?.build())
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