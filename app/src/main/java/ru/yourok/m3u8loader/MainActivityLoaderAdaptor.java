package ru.yourok.m3u8loader;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import dwl.LoaderInfo;
import ru.yourok.loader.Manager;
import ru.yourok.m3u8loader.utils.ThemeChanger;

/**
 * Created by yourok on 23.03.17.
 */

public class MainActivityLoaderAdaptor extends BaseAdapter {

    private int selected;
    private Context context;

    public MainActivityLoaderAdaptor(Context ctx) {
        this.context = ctx;
        selected = -1;
    }

    @Override
    public int getCount() {
        return Manager.Length();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void setSelected(int i) {
        selected = i;
    }

    public int getSelected() {
        return selected;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = lInflater.inflate(R.layout.activity_main_loader_adaptorlist_item, parent, false);
        }

        ((TextView) view.findViewById(R.id.textViewNameItem)).setText(String.valueOf(position));
        if (selected == position) {
            view.setBackgroundColor(Color.parseColor("#cccccc"));
            view.getBackground().setAlpha(128);
        } else
            view.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));

        LoaderInfo info = Manager.GetLoaderInfo(position);
        if (info != null) {
            try {
                ((TextView) view.findViewById(R.id.textViewNameItem)).setText(info.getName());
                TextView stView = (TextView) view.findViewById(R.id.textViewStatusItem);
                ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBarItem);
                if (info.getLoadingCount() > 0 && info.getCompleted() > 0) {
                    progressBar.getProgressDrawable().setColorFilter(ThemeChanger.getProgressBarColor(context), PorterDuff.Mode.SRC_IN);
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress((int) (info.getCompleted() * 100 / info.getLoadingCount()));
                } else
                    progressBar.setVisibility(View.GONE);
                switch ((int) info.getStatus()) {
                    case 0: {
                        stView.setText(R.string.status_load_stopped);
                        break;
                    }
                    case 1: {
                        stView.setText(context.getText(R.string.status_load_loading));
                        break;
                    }
                    case 2: {
                        stView.setText(R.string.status_load_complete);
                        break;
                    }
                    case 3: {
                        stView.setText(context.getText(R.string.status_load_error) + ": " + info.getError());
                        break;
                    }
                    default:
                        stView.setText(R.string.status_load_unknown);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return view;
    }
}
