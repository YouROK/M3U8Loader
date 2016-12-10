package ru.yourok.m3u8loader;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import go.m3u8.M3u8;
import go.m3u8.State;
import ru.yourok.loader.Loader;
import ru.yourok.loader.LoaderHolder;

import go.m3u8.M3U8;
import ru.yourok.loader.LoaderManager;

/**
 * Created by yourok on 08.12.16.
 */

public class AdaptorLoadresList extends BaseAdapter {

    Context ctx;

    public AdaptorLoadresList(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public int getCount() {
        int ret = LoaderHolder.getInstance().Size();
        return ret;
    }

    @Override
    public Object getItem(int i) {
        return LoaderHolder.getInstance().GetLoader(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = lInflater.inflate(R.layout.listview_item_loaders, parent, false);
        }

        final View finalView = view;
        ((ImageButton) view.findViewById(R.id.buttonOpenItemMenu)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = finalView.findViewById(R.id.itemLoaderMenu);
                if (view.getVisibility() == View.GONE)
                    view.findViewById(R.id.itemLoaderMenu).setVisibility(View.VISIBLE);
                else
                    view.findViewById(R.id.itemLoaderMenu).setVisibility(View.GONE);
            }
        });

        Loader loader = LoaderHolder.getInstance().GetLoader(position);
        ((TextView) view.findViewById(R.id.itemLoaderName)).setText(loader.GetName());
        String url = loader.GetUrl();
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
        if (state != null && state.getText() != null && !state.getText().isEmpty())
            url = state.getText();
        ((TextView) view.findViewById(R.id.itemLoaderUrl)).setText(url);
        ((TextView) view.findViewById(R.id.itemLoaderStatus)).setText(status);
        if (all > 0)
            ((ProgressBar) view.findViewById(R.id.itemProgress)).setProgress((curr * 100) / all);
        else
            ((ProgressBar) view.findViewById(R.id.itemProgress)).setProgress(0);

        //Menu
        final int pos = position;
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Loader loader = LoaderHolder.getInstance().GetLoader(pos);
                switch (view.getId()) {
                    case R.id.buttonItemMenuStart:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.loaderManager.Start(pos);
                            }
                        }).start();
                        break;
                    case R.id.buttonItemMenuStop:
                        if (loader.IsWorking())
                            loader.Stop();
                        break;
                    case R.id.buttonItemMenuRemove:
                        loader.Stop();
                        loader.RemoveTemp();
                        LoaderHolder.getInstance().Remove(pos);
                        notifyDataSetChanged();
                        break;
                    case R.id.buttonItemMenuEdit:
                        Intent intent = new Intent(ctx, ListEditActivity.class);
                        intent.putExtra("LoaderID", pos);
                        ctx.startActivity(intent);
                        break;
                }
            }
        };

        view.findViewById(R.id.buttonItemMenuStart).setOnClickListener(clickListener);
        view.findViewById(R.id.buttonItemMenuStop).setOnClickListener(clickListener);
        view.findViewById(R.id.buttonItemMenuRemove).setOnClickListener(clickListener);
        view.findViewById(R.id.buttonItemMenuEdit).setOnClickListener(clickListener);

        return view;
    }
}
