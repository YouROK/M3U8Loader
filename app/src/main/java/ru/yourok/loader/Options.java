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

    public static Options getInstance(Context ctx) {
        ourInstance.prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return ourInstance;
    }

    private Options() {
    }

    public int GetThreads() {
        return prefs.getInt("Threads", 10);
    }

    public int GetTimeout() {
        return prefs.getInt("Timeout", 15000);
    }

    public String GetTempDir() {
        return prefs.getString("TempDirectory", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/tmp/");
    }

    public String GetOutDir() {
        return prefs.getString("OutDirectory", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
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

}
