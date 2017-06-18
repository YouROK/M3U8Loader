package ru.yourok.loader;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.util.ArrayList;

import dwl.LoaderInfo;
import ru.yourok.m3u8loader.R;
import ru.yourok.m3u8loader.utils.Notifications;

/**
 * Created by yourok on 23.03.17.
 */

public class Loader {
    private static ArrayList<Integer> loaderList = new ArrayList<>();

    static private boolean loading;
    static final private Object lock = new Object();

    public static boolean isLoading() {
        return loading;
    }

    static public void Add(int index) {
        synchronized (lock) {
            if (Manager.GetLoaderStatus(index) != Manager.STATUS_COMPLETE) {
                if (loaderList.indexOf(new Integer(index)) == -1)
                    loaderList.add(index);
            }
        }
    }

    static public void Rem(int index) {
        synchronized (lock) {
            for (int i = 0; i < loaderList.size(); i++)
                if (loaderList.get(i) == index) {
                    Manager.Stop(index);
                    loaderList.remove(i);
                    break;
                }
            if (Length() == 0)
                Notifications.RemoveNotification();
        }
    }

    static public void Clear() {
        synchronized (lock) {
            loading = false;
            loaderList.clear();
            for (int i = 0; i < Manager.Length(); i++)
                Manager.Stop(i);
            Notifications.RemoveNotification();
        }
    }

    static public int Length() {
        return loaderList.size();
    }

    static public void Start() {
        synchronized (lock) {
            if (loaderList.size() == 0)
                return;
            if (loading)
                return;
            loading = true;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                int index = -1;
                while (loaderList.size() > 0) {
                    if (!loading)
                        break;
                    synchronized (lock) {
                        index = loaderList.get(0);
                        Manager.Load(index);
                        Notifications.Update(index);
                        loaderList.remove(0);
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Manager.Wait(index);
                    notifyLoader(index);
                    LoaderInfo info = Manager.GetLoaderInfo(index);
                    if (info != null && !info.getError().isEmpty()) {
                        synchronized (lock) {
                            loaderList.clear();
                        }
                        break;
                    }
                }
                loading = false;
                Notifications.Update(index);
            }
        }).start();
    }

    static private void notifyLoader(int index) {
        if (index == -1)
            return;
        Handler handler = new Handler(Looper.getMainLooper());
        final LoaderInfo stat = Manager.GetLoaderInfo(index);
        if (stat != null) {
            if (stat.getStatus() == Manager.STATUS_COMPLETE) {
                String msg = MyApplication.getContext().getString(R.string.status_load_complete) + ": " + stat.getName();
                Notifications.ToastMsg(msg);
            }
            if (stat.getStatus() == Manager.STATUS_ERROR) {
                String msg = MyApplication.getContext().getString(R.string.status_load_error) + ": " + stat.getName() + ", " + stat.getError();
                Notifications.ToastMsg(msg);
            }
        }
    }
}
