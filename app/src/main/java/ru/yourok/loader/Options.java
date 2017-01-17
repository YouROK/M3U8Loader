package ru.yourok.loader;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import go.m3u8.M3u8;

/**
 * Created by yourok on 07.12.16.
 */
public class Options {
    private static Options ourInstance = new Options();
    private SharedPreferences prefs;
    private Context context;

    public static Options getInstance(Context context) {
        ourInstance.context = context;
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

    public String GetUseragent() {
        return prefs.getString("Useragent", "DWL/1.0.0 (linux)");
    }

    public int GetTheme() {
        return prefs.getInt("Theme", 1);
    }

    public int GetPlayer() {
        return prefs.getInt("Player", 0);
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

    public void SetUseragent(String val) {
        SharedPreferences.Editor ed = prefs.edit();
        ed.putString("Useragent", val);
        ed.apply();
    }

    public void SetTheme(int val) {
        SharedPreferences.Editor ed = prefs.edit();
        ed.putInt("Theme", val);
        ed.apply();
    }

    public void SetPlayer(int val) {
        SharedPreferences.Editor ed = prefs.edit();
        ed.putInt("Player", val);
        ed.apply();
    }

    public void SaveList() {
        SharedPreferences.Editor ed = prefs.edit();
        for (int i = 0; i < LoaderServiceHandler.SizeLoaders(); i++) {
            Loader loader = LoaderServiceHandler.GetLoader(i);
            if (loader == null)
                continue;
            ed.putString("List_Url_" + String.valueOf(i), loader.GetUrl());
            ed.putString("List_Name_" + String.valueOf(i), loader.GetName());
            ed.putBoolean("List_Finish_" + String.valueOf(i), loader.IsFinished());
        }
        ed.putInt("List_Size", LoaderServiceHandler.SizeLoaders());
        ed.apply();
    }

    public void LoadList() {
        int lSize = prefs.getInt("List_Size", 0);
        for (int i = 0; i < lSize; i++) {
            String url = prefs.getString("List_Url_" + String.valueOf(i), "");
            String name = prefs.getString("List_Name_" + String.valueOf(i), "");
            final boolean finish = prefs.getBoolean("List_Finish_" + String.valueOf(i), false);
            if (name.isEmpty() || url.isEmpty())
                continue;
            final Loader loader = new Loader();
            loader.SetUrl(url);
            loader.SetName(name);
            boolean isEqual = false;
            for (int n = 0; n < LoaderServiceHandler.SizeLoaders(); n++)
                if (LoaderServiceHandler.GetLoader(n) != null && LoaderServiceHandler.GetLoader(n).equals(loader))
                    isEqual = true;
            if (!isEqual) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (finish) {
                            loader.LoadListOpts(context);
                            loader.Finish();
                            loader.PollState();
                        }
                    }
                }).start();
                LoaderServiceHandler.AddLoader(loader);
            }
        }
    }
}
