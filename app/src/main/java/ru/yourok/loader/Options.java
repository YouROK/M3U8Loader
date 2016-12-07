package ru.yourok.loader;

import android.content.Context;
import android.content.SharedPreferences;
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
        return prefs.getInt("Timeout", 5000);
    }

    public String GetTempDir() {
        return prefs.getString("TempDirectory", "");
    }

    public String GetOutDir() {
        return prefs.getString("OutDirectory", "");
    }

    public void SetThreads(int val) {
        SharedPreferences.Editor ed = prefs.edit();
        ed.putInt("Threads", 10);
        ed.apply();
    }

    public void SetTimeout(int val) {
        SharedPreferences.Editor ed = prefs.edit();
        ed.putInt("Timeout", 5000);
        ed.apply();
    }

    public void SetTempDir(String val) {
        SharedPreferences.Editor ed = prefs.edit();
        ed.putString("TempDirectory", val);
        ed.apply();
    }

    public void SetOutDir(String val) {
        SharedPreferences.Editor ed = prefs.edit();
        ed.putString("OutDirectory", "");
        ed.apply();
    }

}
