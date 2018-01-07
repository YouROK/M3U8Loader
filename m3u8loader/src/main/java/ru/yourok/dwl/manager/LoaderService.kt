package ru.yourok.dwl.manager

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import ru.yourok.dwl.utils.Utils
import ru.yourok.m3u8loader.App
import ru.yourok.m3u8loader.R
import ru.yourok.m3u8loader.activitys.mainActivity.MainActivity
import kotlin.concurrent.thread


/**
 * Created by yourok on 19.11.17.
 */
class LoaderService : Service() {
    override fun onBind(p0: Intent?): IBinder? = null

    private var isUpdates = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.hasExtra("exit"))
            stopSelf()
        startUpdate()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        isUpdates = false
        sendNotificationComplete()
        super.onDestroy()
    }

    private fun startUpdate() {
        synchronized(isUpdates) {
            if (isUpdates)
                return
            isUpdates = true
        }
        thread {
            while (isUpdates) {
                sendNotification()
                Thread.sleep(100)
            }
        }
    }

    var notificationBuilder: NotificationCompat.Builder? = null
    var notificationManager: NotificationManager? = null

    private fun getManager(): NotificationManager {
        notificationManager?.let {
            return it
        }
        notificationManager = App.getContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager!!
    }

    private fun createNotfication(title: String, text: String): NotificationCompat.Builder? {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        notificationBuilder = NotificationCompat.Builder(this, "ru.yourok.m3u8loader.channel")
                .setContentTitle(title)
                .setContentText(text)
                .setProgress(0, 0, false)
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
        return notificationBuilder
    }

    private fun sendNotificationComplete() {
        var msg = getString(R.string.loading_complete)
        if (!Notifyer.error.isEmpty())
            msg = getString(R.string.loading_error)

        if (notificationBuilder == null)
            createNotfication(msg, Notifyer.error)
        else
            notificationBuilder
                    ?.setContentTitle(msg)
                    ?.setContentText(Notifyer.error)
                    ?.setProgress(0, 0, false)

        val notificationManager = App.getContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder?.build())
        Manager.saveLists()
    }

    private fun sendNotification() {
        val index = Manager.getCurrentLoader()
        if (index == -1)
            return
        val state = Manager.getLoaderStat(index) ?: return


        var percent = 0
        if (state.size > 0)
            percent = (state.loadedFragments * 100 / state.fragments)

        val status = "%d/%d %s/sec %d%%".format(state.loadedFragments, state.fragments, Utils.byteFmt(state.speed), percent)

        if (notificationBuilder == null)
            createNotfication(state.name, status)
                    ?.setProgress(100, percent, false)
        else
            notificationBuilder
                    ?.setContentTitle(state.name)
                    ?.setContentText(status)
                    ?.setProgress(100, percent, false)

        val notificationManager = App.getContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder?.build())
    }

    companion object {
        fun start() {
            val intent = Intent(App.getContext(), LoaderService::class.java)
            App.getContext().startService(intent)
        }

        fun stop() {
            val intent = Intent(App.getContext(), LoaderService::class.java)
            intent.putExtra("exit", true)
            App.getContext().startService(intent)
        }
    }
}