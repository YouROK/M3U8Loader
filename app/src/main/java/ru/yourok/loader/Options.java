package ru.yourok.loader;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

/**
 * Created by yourok on 07.12.16.
 */
public class Options {
    private static Options ourInstance = new Options();
    private SharedPreferences prefs;

    public static Options getInstance(Context context) {
        ourInstance.prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return ourInstance;
    }

    private Options() {
    }

    public int GetThreads() {
        return prefs.getInt("Threads", 10);
    }

    public int GetTimeout() {
        return prefs.getInt("Timeout", 60000);
    }

    public String GetTempDir() {
        return prefs.getString("TempDirectory", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/tmp/");
    }

    public String GetOutDir() {
        return prefs.getString("OutDirectory", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
    }

    public boolean IsUseDarkTheme() {
        return prefs.getBoolean("UseDarkTheme", false);
    }

    public void SetThreads(int val) {
        SharedPreferences.Editor ed = prefs.edit();
        ed.putInt("Threads", val);
        ed.apply();
    }

    public void SetTimeout(int val) {
        SharedPreferences.Editor ed = prefs.edit();
        ed.putInt("Timeout", val);
        ed.apply();
    }

    public void SetTempDir(String val) {
        SharedPreferences.Editor ed = prefs.edit();
        ed.putString("TempDirectory", val);
        ed.apply();
    }

    public void SetOutDir(String val) {
        SharedPreferences.Editor ed = prefs.edit();
        ed.putString("OutDirectory", val);
        ed.apply();
    }

    public void SetDarkTheme(boolean val) {
        SharedPreferences.Editor ed = prefs.edit();
        ed.putBoolean("UseDarkTheme", val);
        ed.apply();
    }

    public void SaveList() {
        SharedPreferences.Editor ed = prefs.edit();
        ed.putInt("List_Size", LoaderServiceHandler.SizeLoaders());
        for (int i = 0; i < LoaderServiceHandler.SizeLoaders(); i++) {
            ed.putString("List_Url_" + String.valueOf(i), LoaderServiceHandler.GetLoader(i).GetUrl());
            ed.putString("List_Name_" + String.valueOf(i), LoaderServiceHandler.GetLoader(i).GetName());
        }
        ed.apply();
    }

    public void LoadList() {
        int lSize = prefs.getInt("List_Size", 0);
        for (int i = 0; i < lSize; i++) {
            String url = prefs.getString("List_Url_" + String.valueOf(i), "");
            String name = prefs.getString("List_Name_" + String.valueOf(i), "");
            if (name.isEmpty() || url.isEmpty())
                continue;
            Loader loader = new Loader();
            loader.SetUrl(url);
            loader.SetName(name);
            boolean isEqual = false;
            for (int n = 0; n < LoaderServiceHandler.SizeLoaders(); n++)
                if (LoaderServiceHandler.GetLoader(n).equals(loader))
                    isEqual = true;
            if (!isEqual)
                LoaderServiceHandler.AddLoader(loader);
        }
    }
}
