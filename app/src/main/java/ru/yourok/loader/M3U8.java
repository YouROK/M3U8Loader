package ru.yourok.loader;

import android.content.Context;
import android.provider.Settings;

import go.M3U8Joiner.Joiner;
import go.M3U8Joiner.M3U8Joiner;
import go.M3U8Joiner.Options;
import ru.yourok.m3u8loader.R;

/**
 * Created by yourok on 24.11.16.
 */

public class M3U8 {
    private Joiner joiner;
    private Options opts;
    private Context context;

    public M3U8(Context context) {
        opts = M3U8Joiner.newOptions();
        this.context = context;
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

    public void SetFilename(String filename, String tempDir) {
        opts.setFileName(filename);
        opts.setTempDir(tempDir);
    }

    public boolean IsFinish() {
        if (joiner == null)
            return true;
        return joiner.isFinish();
    }

    public boolean IsLoading() {
        if (joiner == null)
            return false;
        return joiner.isLoading();
    }

    public String GetStatus() {
        if (joiner == null)
            return "";
        switch ((int) joiner.getStage()) {
            case 1:
                return context.getString(R.string.status_load_list);
            case 2:
                if (GetLoadStatus().isEmpty())
                    return context.getString(R.string.status_load_segments);
                else
                    return GetLoadStatus();
            case 3:
                return context.getString(R.string.status_join_files);
            case 4:
                return context.getString(R.string.status_remove_temp);
            case 5:
                return context.getString(R.string.status_finish);
            case -1:
                return context.getString(R.string.error) + " " + joiner.getError();
            default:
                return "";
        }
    }

    public String LoadList() {
        if (joiner == null)
            joiner = M3U8Joiner.newJoiner(opts);
        return joiner.loadList();
    }

    public void Load() {
        if (joiner == null)
            joiner = M3U8Joiner.newJoiner(opts);
        joiner.load();
    }

    public void Stop() {
        if (joiner != null)
            joiner.stop();
    }

    public String GetLoadStatus() {
        if (joiner == null)
            return "";
        int all = (int) joiner.loadedCount();
        int loaded = (int) joiner.loaded();
        if (all == -1)
            return "";
        if (all == 0)
            return String.format(context.getString(R.string.status_loaded) + " %d", loaded);
        else
            return String.format(context.getString(R.string.status_loaded) + " %d / %d %d%%", loaded, all, (loaded * 100) / all);
    }

    public String[] GetListUrls() {
        if (joiner == null)
            return null;
        long count = joiner.getListCount();
        if (count < 1)
            return null;
        String[] listUrls = new String[(int) count];
        for (long i = 0; i < count; i++)
            listUrls[(int) i] = joiner.getListUrl(i);
        return listUrls;
    }

    public void SetListUrl(int i) {
        if (joiner == null)
            return;
        joiner.setListLoad(i);
    }

    public static String RemoveDir(String dir) {
        return M3U8Joiner.removeDir(dir);
    }
}
