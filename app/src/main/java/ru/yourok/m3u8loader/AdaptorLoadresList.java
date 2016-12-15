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
import ru.yourok.m3u8loader.utils.Status;

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
        ((TextView) view.findViewById(R.id.itemLoaderUrl)).setText(Status.GetUrl(loader));
        ((TextView) view.findViewById(R.id.itemLoaderStatus)).setText(Status.GetStatus(ctx, loader));
        ((ProgressBar) view.findViewById(R.id.itemProgress)).setProgress(Status.GetProgress(loader));

        //Menu
        final int pos = position;
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Loader loader = LoaderHolder.getInstance().GetLoader(pos);
                if (loader == null) {
                    AdaptorLoadresList.this.notifyDataSetChanged();
                    return;
                }
                switch (view.getId()) {
                    case R.id.buttonItemMenuStart:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.loaderManager.Add(pos);
                                if (!MainActivity.loaderManager.IsLoading())
                                    MainActivity.loaderManager.Start();
                            }
                        }).start();
                        break;
                    case R.id.buttonItemMenuStop:
                        MainActivity.loaderManager.Remove(pos);
                        if (loader.IsWorking())
                            loader.Stop();
                        break;
                    case R.id.buttonItemMenuRemove:
                        MainActivity.loaderManager.Stop();
                        loader.RemoveTemp();
                        LoaderHolder.getInstance().Remove(pos);
                        break;
                    case R.id.buttonItemMenuEdit:
                        Intent intent = new Intent(ctx, ListEditActivity.class);
                        intent.putExtra("LoaderID", pos);
                        ctx.startActivity(intent);
                        break;
                }
                notifyDataSetChanged();
            }
        };

        view.findViewById(R.id.buttonItemMenuStart).setOnClickListener(clickListener);
        view.findViewById(R.id.buttonItemMenuStop).setOnClickListener(clickListener);
        view.findViewById(R.id.buttonItemMenuRemove).setOnClickListener(clickListener);
        view.findViewById(R.id.buttonItemMenuEdit).setOnClickListener(clickListener);

        return view;
    }
}
