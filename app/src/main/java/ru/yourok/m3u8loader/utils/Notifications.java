package ru.yourok.m3u8loader.utils;

/**
 * Created by yourok on 11.12.16.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import ru.yourok.loader.Loader;
import ru.yourok.loader.LoaderHolder;
import ru.yourok.m3u8loader.MainActivity;
import ru.yourok.m3u8loader.R;

/**
 * Created with IntelliJ IDEA.
 * User: yourok
 * Date: 09.08.12
 * Time: 11:35
 */
public class Notifications {
    private NotificationManager manager;
    private Context context;
    private int currentId;

    public Notifications(Context context) {
        currentId = -1;
        this.context = context;
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void createNotification(int id) {
        currentId = id;
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Loader loader = LoaderHolder.getInstance().GetLoader(id);
        int progress = Status.GetProgress(loader);
        String name = loader.GetName();
        String status = Status.GetStatus(context, loader);

        android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.m3u8)
                .setContentTitle(name)
                .setContentText(status)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);
        if (progress > 0)
            builder.setProgress(100, progress, false);

        manager.notify(0, builder.build());
    }

    public void update() {
        if (currentId != -1)
            createNotification(currentId);
    }

    public void removeNotification(int id) {
        manager.cancel(0);
        currentId = -1;
    }
}