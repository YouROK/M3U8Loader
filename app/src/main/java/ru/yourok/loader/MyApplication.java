package ru.yourok.loader;

import android.app.Application;
import android.content.Context;

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
        super.onCreate();
    }
}
