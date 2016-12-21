package ru.yourok.m3u8loader.utils;

/**
 * Created by yourok on 11.12.16.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.NotificationCompat;

import ru.yourok.loader.Loader;
import ru.yourok.loader.LoaderServiceHandler;
import ru.yourok.m3u8loader.MainActivity;
import ru.yourok.m3u8loader.R;

/**
 * Created with IntelliJ IDEA.
 * User: yourok
 * Date: 09.08.12
 * Time: 11:35
 */
public class Notifications {
    private static final int NOTIFY_ID = 0;

    private NotificationManager manager;
    private Context context;

    public Notifications(Context context) {
        this.context = context;
    }

    public void createNotification(int id) {
        if (context == null)
            return;
        if (manager == null)
            manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, NOTIFY_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Loader loader = LoaderServiceHandler.GetLoader(id);
        if (loader == null)
            return;
        int progress = Status.GetProgress(loader);
        String name = loader.GetName();
        String status = Status.GetStatus(context, loader);

        final android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(name)
                .setContentText(status)
                .setContentIntent(contentIntent)
                .setOngoing(true);
        if (progress > 0)
            builder.setProgress(100, progress, false);
        Notification notification = builder.build();
        manager.notify(NOTIFY_ID, notification);
    }

    public void removeNotification() {
        if (manager != null)
            manager.cancel(NOTIFY_ID);
    }
}