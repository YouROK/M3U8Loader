package ru.yourok.m3u8loader.utils;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;

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

    public static int getProgressBarColor(Context context) {
        switch (Store.getTheme(context)) {
            case "0":
                return ResourcesCompat.getColor(context.getResources(), R.color.button_color_light, null);
            default:
                return ResourcesCompat.getColor(context.getResources(), R.color.button_color_dark, null);
        }
    }
}
