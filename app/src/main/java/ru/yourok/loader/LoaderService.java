package ru.yourok.loader;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class LoaderService extends Service {

    public static final int CMD_START = 1;
    public static final int CMD_STOP = 2;

    public LoaderService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int cmd = 0;
        if (intent != null && intent.hasExtra("command"))
            cmd = intent.getIntExtra("command", 0);
        switch (cmd) {
            case CMD_START: {
                Loader.Start();
                break;
            }
            case CMD_STOP: {
                stopSelf();
                break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static void startServiceLoad() {
        Context context = MyApplication.getContext();
        Intent intent = new Intent(context, LoaderService.class);
        intent.putExtra("command", CMD_START);
        context.startService(intent);
    }

    public static void stopService() {
        Context context = MyApplication.getContext();
        Intent intent = new Intent(context, LoaderService.class);
        intent.putExtra("command", CMD_STOP);
        context.startService(intent);
    }
}
