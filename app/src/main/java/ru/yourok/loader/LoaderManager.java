package ru.yourok.loader;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import go.m3u8.M3u8;
import ru.yourok.m3u8loader.MainActivity;
import ru.yourok.m3u8loader.R;

/**
 * Created by yourok on 07.12.16.
 */

public class LoaderManager {

    private boolean isState, isStoped;
    private MainActivity mainActivity;
    private Loader currentLoader;

    public LoaderManager() {
        this.mainActivity = null;
    }

    public LoaderManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void Start() {
        for (int i = 0; i < LoaderHolder.getInstance().Size(); i++)
            if (LoaderHolder.getInstance().GetLoader(i).IsWorking()) {
                Toast.makeText(mainActivity, R.string.error_already_loading, Toast.LENGTH_SHORT).show();
                return;
            }
        isStoped = false;
        checkState();
        for (int i = 0; i < LoaderHolder.getInstance().Size(); i++) {
            currentLoader = LoaderHolder.getInstance().GetLoader(i);
            if (currentLoader.IsFinished())
                continue;
            if (isStoped)
                break;
            String ret = "";
            if (currentLoader.GetList() == null)
                ret = currentLoader.LoadList();
            if (ret.isEmpty())
                currentLoader.Load();
        }
        mainActivity.UpdateList();
    }

    public void Start(int id) {
        Loader loader = LoaderHolder.getInstance().GetLoader(id);
        if (loader.IsWorking()) {
            Toast.makeText(mainActivity, R.string.error_already_loading, Toast.LENGTH_SHORT).show();
            return;
        }
        if (loader.IsFinished())
            return;
        checkState();
        String ret = "";
        if (loader.GetList() == null)
            ret = loader.LoadList();
        if (ret.isEmpty())
            loader.Load();
        mainActivity.UpdateList();
    }

    public void Stop() {
        isStoped = true;
        for (int i = 0; i < LoaderHolder.getInstance().Size(); i++)
            if (LoaderHolder.getInstance().GetLoader(i).IsWorking())
                LoaderHolder.getInstance().GetLoader(i).Stop();
        mainActivity.UpdateList();
    }

    private void checkState() {
        if (isState)
            return;
        isState = true;
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (mainActivity != null)
                        mainActivity.UpdateList();
                    else break;
                    boolean isEnd = true;

                    for (int i = 0; i < LoaderHolder.getInstance().Size(); i++)
                        if (LoaderHolder.getInstance().GetLoader(i).IsWorking()) {
                            isEnd = false;
                            break;
                        }
                    if (isEnd || isStoped)
                        break;
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                isState = false;
            }
        });
        th.start();
    }
}
