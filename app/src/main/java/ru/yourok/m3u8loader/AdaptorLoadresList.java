package ru.yourok.m3u8loader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import ru.yourok.loader.Loader;
import ru.yourok.loader.LoaderServiceHandler;
import ru.yourok.m3u8loader.utils.Status;


/**
 * Created by yourok on 08.12.16.
 */

public class AdaptorLoadresList extends BaseAdapter {
    private int selected;
    private Context context;

    public AdaptorLoadresList(Context ctx) {
        this.context = ctx;
        selected = -1;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setSelected(int val) {
        if (val >= 0 && val < LoaderServiceHandler.SizeLoaders())
            ((MainActivity) context).findViewById(R.id.itemLoaderMenu).setVisibility(View.VISIBLE);
        else
            ((MainActivity) context).findViewById(R.id.itemLoaderMenu).setVisibility(View.GONE);

        selected = val;
    }

    public int getSelected() {
        return selected;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = lInflater.inflate(R.layout.listview_item_loaders, parent, false);
        }

        view.setClickable(true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (position == selected)
                    setSelected(-1);
                else
                    setSelected(position);
                AdaptorLoadresList.this.notifyDataSetChanged();
            }
        });

        Loader loader = LoaderServiceHandler.GetLoader(position);
        if (loader != null) {
            ((TextView) view.findViewById(R.id.itemLoaderName)).setText(loader.GetName());
            ((TextView) view.findViewById(R.id.itemLoaderUrl)).setText(Status.GetUrl(loader));
            ((TextView) view.findViewById(R.id.itemLoaderStatus)).setText(Status.GetStatus(context, loader));
            ((ProgressBar) view.findViewById(R.id.itemProgress)).setProgress(Status.GetProgress(loader));
        }

        if (selected == position) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = context.getTheme();
            theme.resolveAttribute(R.attr.colorItemSelectMenu, typedValue, true);
            view.setBackgroundColor(typedValue.data);
            view.getBackground().setAlpha(100);
        } else
            view.setBackgroundResource(android.R.color.transparent);

        return view;
    }
}
