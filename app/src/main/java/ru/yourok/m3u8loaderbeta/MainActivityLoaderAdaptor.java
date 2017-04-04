package ru.yourok.m3u8loaderbeta;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import dwl.LoaderInfo;
import ru.yourok.loader.Manager;
import ru.yourok.loader.Store;

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

        ((TextView) view.findViewById(R.id.textViewName)).setText(String.valueOf(position));
        if (selected == position) {
            view.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_bright));
            view.getBackground().setAlpha(75);
        } else
            view.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));

        LoaderInfo info = Manager.GetLoaderInfo(position);
        if (info != null) {
            try {
                ((TextView) view.findViewById(R.id.textViewName)).setText(info.getName());
                ((TextView) view.findViewById(R.id.textViewUrl)).setText(info.getUrl());
                if (info.getError().isEmpty())
                    view.findViewById(R.id.textViewError).setVisibility(View.GONE);
                else
                    view.findViewById(R.id.textViewError).setVisibility(View.VISIBLE);

                if (info.getSpeed() > 0)
                    ((TextView) view.findViewById(R.id.textViewSpeed)).setText(Store.byteFmt(info.getSpeed(), false)+"/sec");
                else
                    ((TextView) view.findViewById(R.id.textViewSpeed)).setText("");

                ((TextView) view.findViewById(R.id.textViewCount)).setText(info.getCompleted() + "/" + info.getLoadingCount() + " " + Store.byteFmt(info.getLoadedBytes(), false));
                if (info.getLoadingCount() > 0)
                    ((ProgressBar) view.findViewById(R.id.progressBar)).setProgress((int) (info.getCompleted() * 100 / info.getLoadingCount()));
                else
                    ((ProgressBar) view.findViewById(R.id.progressBar)).setProgress(0);

                TextView stView = (TextView) view.findViewById(R.id.textViewStatus);
                switch ((int)info.getStatus()) {
                    case 0: {
                        stView.setText(R.string.status_load_stopped);
                        break;
                    }
                    case 1: {
                        stView.setText(R.string.status_load_loading);
                        break;
                    }
                    case 2: {
                        stView.setText(R.string.status_load_complete);
                        break;
                    }
                    case 3: {
                        stView.setText(R.string.status_load_error);
                        ((TextView) view.findViewById(R.id.textViewError)).setText(info.getError());
                        break;
                    }
                    default:
                        stView.setText(R.string.status_load_unknown);
                        break;
                }
                if (info.getThreads() > 0)
                    stView.setText(stView.getText().toString() + " " + info.getThreads());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return view;
    }
}
