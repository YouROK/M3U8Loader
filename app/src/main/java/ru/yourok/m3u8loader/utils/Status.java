package ru.yourok.m3u8loader.utils;

import android.content.Context;

import go.m3u8.M3u8;
import go.m3u8.State;
import ru.yourok.loader.Loader;
import ru.yourok.m3u8loader.R;

/**
 * Created by yourok on 11.12.16.
 */

public class Status {
    static public String GetStatus(Context ctx, Loader loader) {
        if (loader == null || ctx == null)
            return "";
        String status = "";
        State state = loader.GetState();
        int curr = 0;
        int all = 0;
        if (state != null) {
            curr = (int) state.getCurrent();
            all = (int) state.getCount();
            switch ((int) state.getStage()) {
                case (int) M3u8.Stage_Stoped:
                    status = ctx.getString(R.string.status_stoped);
                    break;
                case (int) M3u8.Stage_Error:
                    status = ctx.getString(R.string.error);
                    if (state.getError() != null)
                        status += state.getError().getMessage();
                    else
                        status += "unknown";
                    break;
                case (int) M3u8.Stage_LoadingList:
                    status = ctx.getString(R.string.status_load_list);
                    break;
                case (int) M3u8.Stage_LoadingContent: {
                    if (all == 0)
                        status = String.format(ctx.getString(R.string.status_loaded) + " %d", curr);
                    else
                        status = String.format(ctx.getString(R.string.status_loaded) + " %d / %d", curr, all);
                    break;
                }
                case (int) M3u8.Stage_JoinSegments: {
                    if (all == 0)
                        status = String.format(ctx.getString(R.string.status_joined) + " %d", curr);
                    else
                        status = String.format(ctx.getString(R.string.status_joined) + " %d / %d", curr, all);
                    break;
                }
                case (int) M3u8.Stage_RemoveTemp: {
                    status = ctx.getString(R.string.status_remove_temp);
                    break;
                }
                case (int) M3u8.Stage_Finished: {
                    status = ctx.getString(R.string.status_finish);
                    break;
                }
                default:
                    status = "";
            }
        }
        return status;
    }

    static public String GetUrl(Loader loader) {
        if (loader == null)
            return "";
        State state = loader.GetState();
        if (state != null && state.getText() != null && !state.getText().isEmpty())
            return state.getText();
        return loader.GetUrl();
    }

    static public int GetCount(Loader loader) {
        if (loader == null)
            return 0;
        State state = loader.GetState();
        if (state != null)
            return (int) state.getCount();
        return 0;
    }

    static public int GetCurrent(Loader loader) {
        if (loader == null)
            return 0;
        State state = loader.GetState();
        if (state != null)
            return (int) state.getCurrent();
        return 0;
    }

    static public int GetProgress(Loader loader) {
        if (loader == null)
            return 0;
        int count = GetCount(loader);
        int curr = GetCurrent(loader);
        if (count == 0)
            return 0;
        return ((curr * 100) / count);
    }
}
