package ru.yourok.loader;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import go.m3u8.M3u8;
import go.m3u8.State;
import ru.yourok.m3u8loader.R;
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
        void onUpdateLoader();
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
                checkState();
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
        if (instance != null)
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
    private Loader loader;

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
                    int id = LoaderServiceHandler.PollQueue();
                    if (!isLoading || id == -1) break;
                    loader = LoaderServiceHandler.GetLoader(id);
                    if (loader == null)
                        continue;
                    if (loader.GetState() != null && loader.GetState().getStage() == M3u8.Stage_Finished)
                        continue;
                    checkState();
                    load(loader);
                }

                for (int i = 0; i < LoaderServiceHandler.SizeLoaders(); i++)
                    LoaderServiceHandler.GetLoader(i).PollState();
                if (loaderServiceCallback != null)
                    loaderServiceCallback.onUpdateLoader();

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
                    if (state != null && (state.getStage() == M3u8.Stage_LoadingContent ||
                            state.getStage() == M3u8.Stage_JoinSegments))
                        LoaderServiceHandler.GetLoader(i).Stop();
                }
                updateNotif();
            }
        }).start();
    }

    private void load(final Loader loader) {
        String ret = "";
        if (loader == null)
            return;
        if (loader.GetList() == null)
            ret = loader.LoadListOpts(this);
        if (ret.isEmpty())
            ret = loader.Load();
        Handler handler = new Handler(Looper.getMainLooper());
        final String finalRet = ret;
        if (isLoading)
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (finalRet.isEmpty()) {
                        if (loader.IsFinished()) {
                            Toast.makeText(LoaderService.this, getString(R.string.status_finish) + ": " + loader.GetName(), Toast.LENGTH_SHORT).show();
                            Options.getInstance(LoaderService.this).SaveList();
                        }
                    } else {
                        Toast.makeText(LoaderService.this, getString(R.string.error) + finalRet, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        if (!ret.isEmpty()) {
            isLoading = false;
            loader.PollState();
            updateNotif();
        }
    }

    void updateNotif() {
        if (loaderServiceCallback != null)
            loaderServiceCallback.onUpdateLoader();
        notifications.createNotification(loader);
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
                if (timeout == 0)
                    timeout = 30;
                updateNotif();
                while (true) {
                    if (loader == null) {
                        try {
                            Thread.sleep(200);
                            countNil++;
                            if (countNil > 150)//wait 30 sec
                                break;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    } else {
                        State state = pollState();
                        if (state == null && System.currentTimeMillis() - time > 1000) {
                            countNil++;
                            time = System.currentTimeMillis();
                        } else if (state != null)
                            countNil = 0;

                        while (state != null) {
                            state = pollState();
                            if (state != null && System.currentTimeMillis() - time > 1000) {
                                time = System.currentTimeMillis();
                                updateNotif();
                            }
                        }

                        updateNotif();
                        if (countNil > timeout || LoaderServiceHandler.SizeLoaders() == 0)
                            break;
                    }
                }
                updateNotif();

                synchronized (isChecked) {
                    isChecked = false;
                }
                updateNotif();
            }
        });
        th.start();
    }

    private State pollState() {
        if (loader != null)
            return loader.PollState();
        return null;
    }
}
