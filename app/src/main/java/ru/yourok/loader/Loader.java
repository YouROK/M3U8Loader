package ru.yourok.loader;

import go.m3u8.List;
import go.m3u8.M3U8;
import go.m3u8.M3u8;
import go.m3u8.Options;
import go.m3u8.State;

/**
 * Created by yourok on 24.11.16.
 */

public class Loader {
    private Options opts;
    private M3U8 m3u8;
    private State state;

    private boolean isStoped;

    public Loader() {
        opts = M3u8.newOptions();
    }

    public void SetUrl(String url) {
        opts.setUrl(url);
    }

    public void SetThreads(int threads) {
        opts.setThreads(threads);
    }

    public void SetTimeout(int timeout) {
        opts.setTimeout(timeout);
    }

    public void SetHeader(String key, String val) {
        opts.setHeader(key, val);
    }

    public void SetTempDir(String temp) {
        opts.setTempDir(temp);
    }

    public void SetOutDir(String out) {
        opts.setOutFileDir(out);
    }

    public void SetName(String Name) {
        opts.setName(Name);
    }

    public String GetName() {
        return opts.getName();
    }

    public String GetUrl() {
        return opts.getUrl();
    }

    public boolean IsWorking() {
        if (m3u8 == null)
            return false;
        if (m3u8.isLoading() || m3u8.isJoin())
            return true;
        if (GetState() == null)
            return false;
        switch ((int) state.getStage()) {
            case (int) M3u8.Stage_JoinSegments:
            case (int) M3u8.Stage_LoadingList:
            case (int) M3u8.Stage_LoadingContent:
            case (int) M3u8.Stage_RemoveTemp:
                return true;
        }
        return false;
    }

    public State PollState() {
        if (m3u8 == null)
            return null;
        State st = M3u8.getState(m3u8);
        if (st != null)
            state = st;
        return st;
    }

    public State GetState() {
        return state;
    }

    public String LoadList() {
        try {
            if (m3u8 == null)
                m3u8 = M3u8.newM3U8(opts);
            m3u8.loadList();
            return "";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public List GetList() {
        if (m3u8 == null)
            return null;
        return m3u8.getList();
    }

    public String Load() {
        try {
            isStoped = false;
            if (!isStoped)
                m3u8.load();
            if (!isStoped)
                m3u8.join();
            if (!isStoped)
                m3u8.removeTemp();
            if (!isStoped)
                m3u8.finish();
            isStoped = true;
            return "";
        } catch (Exception e) {
            isStoped = true;
            PollState();
            return e.getMessage();
        }
    }

    public void Stop() {
        isStoped = true;
        if (m3u8 != null)
            m3u8.stop();
    }

    public void RemoveTemp() {
        try {
            if (m3u8 != null)
                m3u8.removeTemp();
        } catch (Exception e) {

        }
    }

    public boolean isStoped() {
        if (m3u8 == null)
            return false;
        if (GetState() == null)
            return false;
        return state.getStage() == M3u8.Stage_Stoped || isStoped;
    }

    public boolean IsFinished() {
        if (m3u8 == null)
            return false;
        if (GetState() == null)
            return false;
        return state.getStage() == M3u8.Stage_Finished;
    }

    public static String RemoveDir(String dir) {
        try {
            M3u8.removeAll(dir);
            return "";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public boolean equals(Loader obj) {
        return this.GetUrl().equals(obj.GetUrl()) && this.GetName().equals(obj.GetName());
    }
}
