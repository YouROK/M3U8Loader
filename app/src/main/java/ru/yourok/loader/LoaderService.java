package ru.yourok.loader;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import go.m3u8.M3u8;
import go.m3u8.State;
import ru.yourok.m3u8loader.utils.Notifications;

import static ru.yourok.loader.LoaderServiceHandler.loadersQueue;

/**
 * Created by yourok on 15.12.16.
 */

public class LoaderService extends Service {
    private Notifications notifications;
    private static LoaderService instance;
    private static LoaderServiceCallbackUpdate loaderServiceCallback;

    public static final int CMD_NONE = 0;
    public static final int CMD_START = 1;
    public static final int CMD_STOP = 2;

    public interface LoaderServiceCallbackUpdate {
        void onUpdateLoader(int id);
    }

    public LoaderService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Options.getInstance(this).LoadList();
        notifications = new Notifications(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int cmd = 0;
        if (intent != null && intent.hasExtra("command"))
            cmd = intent.getIntExtra("command", 0);
        switch (cmd) {
            case CMD_NONE:
                break;
            case CMD_START: {
                Start();
                break;
            }
            case CMD_STOP: {
                Stop();
                break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (instance != null)
            synchronized (instance) {
                instance = null;
            }
        Options.getInstance(this).SaveList();
        notifications.removeNotification();
        super.onDestroy();
    }

    public static void registerOnUpdateLoader(LoaderServiceCallbackUpdate onUpdate) {
        loaderServiceCallback = onUpdate;
        if (instance != null && instance.id != -1)
            instance.checkState();
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, LoaderService.class);
        context.startService(intent);
    }

    public static void load(Context context) {
        Intent intent = new Intent(context, LoaderService.class);
        intent.putExtra("command", CMD_START);
        context.startService(intent);
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, LoaderService.class);
        intent.putExtra("command", CMD_STOP);
        context.startService(intent);
    }

////////////////
/// Wroking funcs

    private Boolean isLoading = false;
    private Boolean isChecked = false;
    private int id = -1;

    public void Start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (isLoading) {
                    if (isLoading)
                        return;
                    isLoading = true;
                }
                while (LoaderServiceHandler.SizeQueue() > 0) {
                    id = LoaderServiceHandler.PollQueue();
                    if (!isLoading || id == -1) break;
                    Loader loader = LoaderServiceHandler.GetLoader(id);
                    if (loader == null)
                        continue;
                    if (loader.GetState() != null && loader.GetState().getStage() == M3u8.Stage_Finished)
                        continue;
                    checkState();
                    load(loader);
                }

                for (int i = 0; i < LoaderServiceHandler.SizeLoaders(); i++)
                    if (LoaderServiceHandler.GetLoader(id) != null)
                        LoaderServiceHandler.GetLoader(i).PollState();
                if (loaderServiceCallback != null)
                    loaderServiceCallback.onUpdateLoader(0);

                synchronized (isLoading) {
                    isLoading = false;
                }
            }
        }).start();
    }

    public void Stop() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadersQueue.clear();
                synchronized (isLoading) {
                    if (!isLoading) return;
                    isLoading = false;
                }
                for (int i = 0; i < LoaderServiceHandler.SizeLoaders(); i++) {
                    LoaderServiceHandler.GetLoader(i).PollState();
                    State state = LoaderServiceHandler.GetLoader(i).GetState();
                    if (state != null && state.getStage() == M3u8.Stage_LoadingContent)
                        LoaderServiceHandler.GetLoader(i).Stop();
                }
                updateNotif();
            }
        }).start();
    }

    private void load(Loader loader) {
        String ret = "";
        if (loader.GetList() == null)
            ret = loader.LoadListOpts(this);
        if (ret.isEmpty())
            loader.Load();
    }

    void updateNotif() {
        if (id != -1) {
            if (loaderServiceCallback != null)
                loaderServiceCallback.onUpdateLoader(id);
            notifications.createNotification(id);
        }
    }

    private void checkState() {
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (isChecked) {
                    if (isChecked)
                        return;
                    isChecked = true;
                }

                int countNil = 0;
                long time = System.currentTimeMillis();
                int timeout = Options.getInstance(LoaderService.this).GetTimeout() / 1000;
                while (true) {
                    Loader loader = LoaderServiceHandler.GetLoader(id);
                    if (loader == null)
                        break;
                    while (loader.PollState() == null) ;

                    if (loader.PollState() == null) {
                        if (System.currentTimeMillis() - time > 1000) {
                            countNil++;
                            time = System.currentTimeMillis();
                        }
                    } else
                        countNil = 0;

                    updateNotif();

                    if (countNil > timeout + 60)
                        break;
                }

                synchronized (isChecked) {
                    isChecked = false;
                }
                updateNotif();
            }
        });
        th.start();
    }
}
