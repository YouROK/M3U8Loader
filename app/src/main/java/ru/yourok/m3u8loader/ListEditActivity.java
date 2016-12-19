package ru.yourok.m3u8loader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import go.m3u8.Item;
import go.m3u8.List;
import ru.yourok.loader.Loader;
import ru.yourok.loader.LoaderServiceHandler;

public class ListEditActivity extends AppCompatActivity {
    private Loader loader;
    private List list;
    private AdaptorEditList listviewadapter;
    private ArrayList<Integer> path = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_edit);
        final ListView listView = (ListView) findViewById(R.id.loaderEditList);
        listviewadapter = new AdaptorEditList();
        listView.setAdapter(listviewadapter);
        Intent intent = getIntent();
        int id = intent.getIntExtra("LoaderID", -1);
        if (id == -1)
            return;
        loader = LoaderServiceHandler.GetLoader(id);
        loader.Stop();
        list = loader.GetList();
        if (list == null) {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String ret = loader.LoadList();
                    if (!ret.isEmpty()) {
                        Toast.makeText(ListEditActivity.this, ret, Toast.LENGTH_SHORT).show();
                        ListEditActivity.this.finish();
                    } else
                        list = loader.GetList();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            listviewadapter.notifyDataSetChanged();
                        }
                    });
                }
            }).start();
        }
    }

    private void findList() {
        list = loader.GetList();
        listviewadapter.notifyDataSetChanged();
        if (list == null)
            return;
        if (path.size() == 0)
            return;
        for (int i : path)
            list = list.getList(i);
        listviewadapter.notifyDataSetChanged();
    }

    public void upBtnClick(View view) {
        if (path.size() > 0)
            path.remove(path.size() - 1);
        findList();
    }

    public class AdaptorEditList extends BaseAdapter {

        @Override
        public int getCount() {
            if (list == null)
                return 0;
            return (int) (list.listsSize() + list.itemsSize());
        }

        @Override
        public Object getItem(int position) {
            if (position < list.listsSize())
                return list.getList(position);
            else
                return list.getItem(position - list.listsSize());
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup viewGroup) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) ListEditActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.listview_item_loaderlist, null);
            }
            if (position < list.listsSize()) {
                //List
                List subList = list.getList(position);
                ((ImageView) v.findViewById(R.id.imageViewEditItem)).setImageResource(R.drawable.ic_format_list_bulleted_black_24dp);
                ((TextView) v.findViewById(R.id.textViewEditItem)).setText(subList.getName());
                ((CheckBox) v.findViewById(R.id.checkBoxEditItem)).setChecked(subList.isLoadList());
                ((CheckBox) v.findViewById(R.id.checkBoxEditItem)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (list != null && list.getList(position) != null)
                            list.getList(position).setLoadList(b);
                    }
                });
                ((ImageView) v.findViewById(R.id.imageViewEditItem)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        path.add(position);
                        findList();
                        listviewadapter.notifyDataSetChanged();
                    }
                });

            } else {
                //Item
                Item item = list.getItem(position - list.listsSize());
                ((ImageView) v.findViewById(R.id.imageViewEditItem)).setImageResource(R.drawable.ic_play_arrow_black_24dp);
                ((TextView) v.findViewById(R.id.textViewEditItem)).setText(item.getUrl());
                ((CheckBox) v.findViewById(R.id.checkBoxEditItem)).setChecked(item.getIsLoad());
                ((CheckBox) v.findViewById(R.id.checkBoxEditItem)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (list != null && list.getItem(position - list.listsSize()) != null)
                            list.getItem(position - list.listsSize()).setIsLoad(b);
                    }
                });
            }

            return v;
        }
    }
}
