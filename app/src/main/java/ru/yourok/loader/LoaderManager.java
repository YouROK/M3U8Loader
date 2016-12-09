package ru.yourok.loader;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;

import ru.yourok.m3u8loader.MainActivity;
import ru.yourok.m3u8loader.R;

/**
 * Created by yourok on 07.12.16.
 */

public class LoaderManager {

    private boolean isLoading;
    private MainActivity mainActivity;
    private Loader currentLoader;

    public LoaderManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        isLoading = false;
    }

    public void Start() {
        if (isLoading)
            return;
        isLoading = true;
        checkState();
        for (int i = 0; i < LoaderHolder.getInstance().Size(); i++) {
            if (!isLoading)
                break;
            currentLoader = LoaderHolder.getInstance().GetLoader(i);
            if (currentLoader.IsFinished())
                continue;
            if (currentLoader.LoadList().isEmpty())
                currentLoader.Load();
        }
        isLoading = false;
    }

    public void Stop() {
        isLoading = false;
        for (int i = 0; i < LoaderHolder.getInstance().Size(); i++)
            LoaderHolder.getInstance().GetLoader(i).Stop();
        mainActivity.UpdateList();
    }

    private void checkState() {
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (mainActivity != null)
                        mainActivity.UpdateList();
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        th.start();
    }


}
