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

import ru.yourok.loader.Loader;
import ru.yourok.loader.LoaderService;
import ru.yourok.loader.LoaderServiceHandler;
import ru.yourok.loader.Options;
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
        int ret = LoaderServiceHandler.SizeLoaders();
        return ret;
    }

    @Override
    public Object getItem(int i) {
        return LoaderServiceHandler.GetLoader(i);
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

        Loader loader = LoaderServiceHandler.GetLoader(position);
        ((TextView) view.findViewById(R.id.itemLoaderName)).setText(loader.GetName());
        ((TextView) view.findViewById(R.id.itemLoaderUrl)).setText(Status.GetUrl(loader));
        ((TextView) view.findViewById(R.id.itemLoaderStatus)).setText(Status.GetStatus(ctx, loader));
        ((ProgressBar) view.findViewById(R.id.itemProgress)).setProgress(Status.GetProgress(loader));

        //Menu
        final int pos = position;
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Loader loader = LoaderServiceHandler.GetLoader(pos);
                if (loader == null) {
                    AdaptorLoadresList.this.notifyDataSetChanged();
                    return;
                }
                switch (view.getId()) {
                    case R.id.buttonItemMenuStart:
                        LoaderServiceHandler.AddQueue(pos);
                        LoaderService.load(ctx);
                        break;
                    case R.id.buttonItemMenuStop:
                        if (loader.IsWorking())
                            loader.Stop();
                        break;
                    case R.id.buttonItemMenuRemove:
                        LoaderService.stop(ctx);
                        loader.RemoveTemp();
                        LoaderServiceHandler.RemoveLoader(pos);
                        Options.getInstance(ctx).SaveList();
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
