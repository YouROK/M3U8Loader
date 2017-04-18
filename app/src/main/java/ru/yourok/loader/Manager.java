package ru.yourok.loader;


import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dwl.Dwl;
import dwl.LoaderInfo;
import dwl.Settings;
//import dwl.Options;


/**
 * Created by yourok on 21.03.17.
 */

public class Manager {
    static final public String TAG = "Manager";
    static private dwl.Manager manager;

    static final public int STATUS_STOPED = 0;
    static final public int STATUS_LOADING = 1;
    static final public int STATUS_COMPLETE = 2;
    static final public int STATUS_ERROR = 3;

    static public void Init(dwl.Manager manager) {
        Manager.manager = manager;
    }

    static public String Add(String url, String name, String cookies, String useragent) {
        try {
            if (manager != null)
                manager.add(url, name, cookies, useragent);
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    static public void Remove(int i) {
        if (manager != null)
            manager.rem(i);
    }

    static public void Clean(int i) {
        if (manager != null)
            manager.clean(i);
    }

    static public int Length() {
        if (manager == null)
            return 0;
        return (int) manager.len();
    }

    static public void Load(int i) {
        if (manager != null)
            manager.load(i);
    }

    static public void Stop(int i) {
        if (manager != null)
            manager.stop(i);
    }

    static public void Wait(int i) {
        if (manager != null)
            manager.waitLoader(i);
    }

    static public String SetLoaderUrl(String url, String name, String cookies, String useragent) {
        if (manager != null)
            try {
                manager.setLoaderUrl(url, name, cookies, useragent);
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
        return "";
    }

    static public void SetLoaderUseragent(int index, String val) {
        if (manager != null)
            manager.setLoaderUserAgent(index, val);
    }

    static public void SetLoaderCookies(int index, String val) {
        if (manager != null)
            manager.setLoaderCookies(index, val);
    }

    static public void SetLoaderRange(int index, int from, int to) {
        if (manager != null)
            manager.setLoaderRange(index, from, to);
    }

    static public String GetLoaderUseragent(int index) {
        if (manager != null)
            return manager.getLoaderUserAgent(index);
        return "";
    }

    static public String GetLoaderCookies(int index) {
        if (manager != null)
            return manager.getLoaderCookies(index);
        return "";
    }

    static public int GetLoaderRangeFrom(int index) {
        if (manager != null)
            return (int) manager.getLoaderRangeFrom(index);
        return 0;
    }

    static public int GetLoaderRangeTo(int index) {
        if (manager != null)
            return (int) manager.getLoaderRangeTo(index);
        return 0;
    }

    static public int GetLoaderStatus(int index) {
        if (manager != null) {
            LoaderInfo info = manager.getLoaderInfo(index);
            if (info != null) {
                return (int) info.getStatus();
            }
        }
        return -1;
    }

    static public LoaderInfo GetLoaderInfo(int index) {
        if (manager != null)
            return manager.getLoaderInfo(index);
        return null;
    }

    static public String GetFileName(int i) {
        return manager.getLoaderFileName(i);
    }

    ////Settings

    static public Settings GetSettings() {
        return manager.getSettings();
    }

    static public String SaveSettings() {
        if (manager != null)
            return manager.saveSettings();
        return "";
    }

    static public void SetSettingsDownloadPath(String val) {
        if (manager != null)
            manager.setSettingsDownloadPath(val);
    }

    static public void SetSettingsUseragent(String val) {
        if (manager != null)
            manager.setSettingsUseragent(val);
    }

    static public void SetSettingsCookies(String val) {
        if (manager != null)
            manager.setSettingsCookies(val);
    }

    static public void SetSettingsThreads(int val) {
        if (manager != null)
            manager.setSettingsThreads(val);
    }

    static public void SetSettingsErrorRepeat(int val) {
        if (manager != null)
            manager.setSettingsErrorRepeat(val);
    }
}
