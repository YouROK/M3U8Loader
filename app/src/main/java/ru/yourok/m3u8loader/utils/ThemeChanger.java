package ru.yourok.m3u8loader.utils;

import android.app.Activity;

import ru.yourok.loader.Store;
import ru.yourok.m3u8loader.R;

/**
 * Created by yourok on 29.03.17.
 */

public class ThemeChanger {
    public static void SetTheme(Activity activity) {
        switch (Store.getTheme(activity)) {
            case "0":
                SetDarkTheme(activity);
                return;
            case "1":
                SetLightTheme(activity);
                return;
        }
    }

    public static void SetDarkTheme(Activity activity) {
        activity.setTheme(R.style.AppThemeDark);
    }

    public static void SetLightTheme(Activity activity) {
        activity.setTheme(R.style.AppTheme);
    }
}
