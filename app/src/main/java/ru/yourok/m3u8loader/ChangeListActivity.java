package ru.yourok.m3u8loader;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ChangeListActivity extends AppCompatActivity {

    boolean[] ListChecks;
    ArrayList<UrlItem> urlList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_list);
        displayListView();
    }

    public void confirmBtnClick(View view) {
        Intent intent = new Intent();
        for (int i = 0; i < ListChecks.length; i++)
            ListChecks[i] = urlList.get(i).isSelected();
        intent.putExtra("UrlsChecked", ListChecks);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void allBtnClick(View view) {
        Intent intent = new Intent();
        for (int i = 0; i < ListChecks.length; i++)
            ListChecks[i] = true;
        intent.putExtra("UrlsChecked", ListChecks);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void cancelBtnClick(View view) {
//        MainActivity.m3u8.Stop();
        finish();
    }

    private void displayListView() {
        String[] urls = null;//MainActivity.m3u8.GetListUrls();
        ListChecks = new boolean[urls.length];
        urlList = new ArrayList<>();
        for (String u : urls) {
            UrlItem item = new UrlItem(u, false);
            urlList.add(item);
        }

        MyCustomAdapter dataAdapter = new MyCustomAdapter(this,
                R.layout.url_list_item, urlList);
        final ListView listView = (ListView) findViewById(R.id.urlListView);

        listView.setAdapter(dataAdapter);
    }

    public class UrlItem {

        String url = null;
        boolean selected = false;

        public UrlItem(String url, boolean selected) {
            super();
            this.url = url;
            this.selected = selected;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

    }

    private class MyCustomAdapter extends ArrayAdapter<UrlItem> {

        private ArrayList<UrlItem> countryList;

        public MyCustomAdapter(Context context, int textViewResourceId,
                               ArrayList<UrlItem> countryList) {
            super(context, textViewResourceId, countryList);
            this.countryList = new ArrayList<>();
            this.countryList.addAll(countryList);
        }

        private class ViewHolder {
            CheckBox name;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.url_list_item, null);

                holder = new ViewHolder();
                holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
                convertView.setTag(holder);

                holder.name.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        UrlItem urlItem = (UrlItem) cb.getTag();
                        urlItem.setSelected(cb.isChecked());
                    }
                });
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            UrlItem urlItem = countryList.get(position);
            holder.name.setText(urlItem.getUrl());
            holder.name.setChecked(urlItem.isSelected());
            holder.name.setTag(urlItem);

            return convertView;

        }
    }
}
