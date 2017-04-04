package ru.yourok.loader;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import dwl.*;
import ru.yourok.m3u8loader.R;

/**
 * Created by yourok on 23.03.17.
 */

public class Store {
    static public String SaveConfigDirectory;

    static public void Init(Context context) {
        try {
            if (context.getExternalFilesDir(null) != null)
                SaveConfigDirectory = context.getExternalFilesDir(null).getPath();
            if (SaveConfigDirectory.isEmpty())
                SaveConfigDirectory = Environment.getExternalStorageDirectory().getPath();
            if (!SaveConfigDirectory.isEmpty()) {
                dwl.Manager nativemanager = Dwl.openManager(SaveConfigDirectory);
                Manager.Init(nativemanager);
            }
        } catch (Exception e) {
            SaveConfigDirectory = "";
            e.printStackTrace();
        }
        if (SaveConfigDirectory.isEmpty())
            Toast.makeText(context, R.string.error_find_cfg_save_dir, Toast.LENGTH_SHORT).show();
    }

    static public String getDownloadPath() {
        String dp = Manager.GetSettings().getDownloadPath();
        if (dp.isEmpty()) {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        }
        return dp;
    }

    static public String getTheme(Context context) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            return prefs.getString("Theme", "0");
        } catch (Exception e) {
            return "0";
        }
    }

    static public String getPlayer(Context context) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            return prefs.getString("Player", "0");
        } catch (Exception e) {
            return "0";
        }
    }

    static public void setTheme(Context context, String val) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString("Theme", val).apply();
    }

    static public void setPlayer(Context context, String val) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString("Player", val).apply();
    }

    static public String byteFmt(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
