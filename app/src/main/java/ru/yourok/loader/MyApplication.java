package ru.yourok.loader;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import dwl.Settings;

/**
 * Created by yourok on 23.03.17.
 */

public class MyApplication extends Application {

    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        context = getApplicationContext();
        Store.Init(this);
        Settings sets = Manager.GetSettings();
        if (sets != null) {
            String dp = sets.getDownloadPath();
            if (dp.isEmpty()) {
                dp = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                Manager.SetSettingsDownloadPath(dp);
            }
        }
        super.onCreate();
    }
}
