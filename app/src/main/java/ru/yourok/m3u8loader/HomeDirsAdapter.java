package ru.yourok.m3u8loader;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;


/**
 * Created by yourok on 19.12.16.
 */

public class HomeDirsAdapter extends BaseAdapter {

    File[] list;
    Context context;

    public HomeDirsAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            list = context.getExternalFilesDirs(null);
            return list.length + 1;
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (list == null || position < 0 || position >= list.length)
            return null;
        return list[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(android.R.layout.two_line_list_item, null);
            int paddingPixel = 15;
            float density = context.getResources().getDisplayMetrics().density;
            int paddingDp = (int) (paddingPixel * density);
            convertView.setPadding(paddingDp, 0, 0, paddingDp);
        }
        if (list != null) {
            if (position == list.length) {
                ((TextView) convertView.findViewById(android.R.id.text1)).setText(context.getString(R.string.default_directory_change));
                ((TextView) convertView.findViewById(android.R.id.text2)).setText("");
            } else if (list[position] != null) {
                ((TextView) convertView.findViewById(android.R.id.text1)).setText(list[position].getAbsolutePath());
                String Space = humanReadableByteCount(list[position].getFreeSpace(), true) + "/" + humanReadableByteCount(list[position].getTotalSpace(), true);
                ((TextView) convertView.findViewById(android.R.id.text2)).setText(Space);
            }
        }
        return convertView;

    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}