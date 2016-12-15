package ru.yourok.loader;

import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import go.m3u8.M3u8;
import go.m3u8.State;
import ru.yourok.m3u8loader.MainActivity;
import ru.yourok.m3u8loader.R;
import ru.yourok.m3u8loader.utils.Notifications;

/**
 * Created by yourok on 07.12.16.
 */

public class LoaderManager {

    private Boolean isLoading;
    private MainActivity mainActivity;
    private Notifications notifications;
    private LinkedBlockingDeque<Integer> queueLoaders;

    public LoaderManager() {
        this.mainActivity = null;
        this.notifications = null;
        this.queueLoaders = new LinkedBlockingDeque<>();
        this.isLoading = new Boolean(false);
    }

    public LoaderManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.notifications = new Notifications(mainActivity);
        this.queueLoaders = new LinkedBlockingDeque<>();
        this.isLoading = new Boolean(false);
    }

    public void Add(int id) {
        if (queueLoaders.contains(id)) return;
        queueLoaders.add(id);
    }

    public void Remove(int id) {
        queueLoaders.remove(id);
    }

    public void Start() {
        synchronized (isLoading) {
            if (isLoading) {
                return;
            }
            isLoading = true;
        }

        for (int id : queueLoaders) {
            if (!isLoading)
                break;
            Loader loader = LoaderHolder.getInstance().GetLoader(id);
            if (loader == null)
                continue;
            sendNotif(id);
            checkState(id);
            load(loader);
        }

        synchronized (isLoading) {
            isLoading = false;
        }
    }

    public void Stop() {
        queueLoaders.clear();
        synchronized (isLoading) {
            if (!isLoading) return;
            isLoading = false;
        }
        for (int i = 0; i < LoaderHolder.getInstance().Size(); i++)
            LoaderHolder.getInstance().GetLoader(i).Stop();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < LoaderHolder.getInstance().Size(); i++)
                    LoaderHolder.getInstance().GetLoader(i).PollState();
                updateNotif();
            }
        }).start();
    }

    public boolean IsLoading() {
        return isLoading;
    }

    private void load(Loader loader) {
        loader.SetThreads(Options.getInstance(mainActivity).GetThreads());
        loader.SetTimeout(Options.getInstance(mainActivity).GetTimeout());
        loader.SetTempDir(Options.getInstance(mainActivity).GetTempDir());
        loader.SetOutDir(Options.getInstance(mainActivity).GetOutDir());
        String ret = "";
        if (loader.GetList() == null)
            ret = loader.LoadList();
        if (ret.isEmpty())
            loader.Load();
        updateNotif();
    }

    void updateNotif() {
        if (notifications != null)
            notifications.update();
        if (mainActivity != null)
            mainActivity.UpdateList();
    }

    void sendNotif(int id) {
        if (notifications != null)
            notifications.createNotification(id);
        if (mainActivity != null)
            mainActivity.UpdateList();
    }

    private void checkState(final int id) {
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                int countNil = 0;
                while (true) {
                    Loader loader = LoaderHolder.getInstance().GetLoader(id);
                    if (loader == null)
                        return;
                    if (loader.PollState() == null)
                        countNil++;
                    else
                        countNil = 0;
                    updateNotif();

                    if (!isLoading || mainActivity == null || countNil > 30)
                        break;
                }
            }
        });
        th.start();
    }
}
