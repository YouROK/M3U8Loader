package ru.yourok.m3u8loader.utils;

/**
 * Created by yourok on 11.12.16.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.NotificationCompat;

import dwl.LoaderInfo;
import ru.yourok.loader.Manager;
import ru.yourok.loader.MyApplication;
import ru.yourok.loader.Store;
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

    private static int Index = -1;
    private static boolean isUpdate = false;
    private static Object lock = new Object();

    public static void Update(int index) {
        synchronized (lock) {
            Index = index;
            if (isUpdate)
                return;
            isUpdate = true;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isUpdate) {
                    Context context = MyApplication.getContext();
                    createNotification(context, Index);
                    if (Index == -1) {
                        if (Manager.Length() > 0)
                            createNotification(context, Manager.Length() - 1);
                        else
                            RemoveNotification();
                        break;
                    } else {
                        LoaderInfo info = Manager.GetLoaderInfo(Index);
                        if (info != null && info.getStatus() != Manager.STATUS_LOADING)
                            break;
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                isUpdate = false;
            }
        }).start();
    }

    static void createNotification(Context context, int index) {
        if (context == null || index == -1) {
            return;
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, NOTIFY_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        LoaderInfo info = Manager.GetLoaderInfo(index);
        if (info == null)
            return;

        String status;
        switch ((int) info.getStatus()) {
            case Manager.STATUS_STOPED: {
                String progress = info.getCompleted() + " / " + info.getLoadingCount() + " ";
                status = context.getResources().getString(R.string.status_load_stopped) + " " + progress + Store.byteFmt(info.getLoadedBytes());
                break;
            }
            case Manager.STATUS_LOADING: {
                String speed = "";
                String loaded = Store.byteFmt(info.getLoadedBytes());
                if (info.getSpeed() > 0)
                    speed = Store.byteFmt(info.getSpeed()) + "/sec   ";
                status = context.getResources().getString(R.string.status_load_loading) + " " + info.getThreads() + "   " + speed + loaded;
                break;
            }
            case Manager.STATUS_COMPLETE: {
                status = context.getResources().getString(R.string.status_load_complete) + " " + Store.byteFmt(info.getLoadedBytes());
                break;
            }
            case Manager.STATUS_ERROR: {
                status = context.getResources().getString(R.string.status_load_error) + ": " + info.getError();
                break;
            }
            default:
                status = context.getResources().getString(R.string.status_load_unknown);
                break;
        }

        final android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(info.getName())
                .setContentText(status)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);

        if (info.getLoadingCount() > 0) {
            int percent = (int) (info.getCompleted() * 100 / info.getLoadingCount());
            if (percent > 0)
                builder.setProgress(100, percent, false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            builder.setColor(context.getResources().getColor(R.color.colorPrimaryDark, null));
        else
            builder.setColor(context.getResources().getColor(R.color.colorPrimaryDark));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder.setSmallIcon(R.drawable.ic_file_download_black_24dp);
        else
            builder.setSmallIcon(R.drawable.ic_launcher);

        Notification notification;
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            notification = builder.getNotification(); // API-15 and lower
        else
            notification = builder.build();

        manager.notify(NOTIFY_ID, notification);
    }

    public static void RemoveNotification() {
        Context context = MyApplication.getContext();
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFY_ID);
    }
}