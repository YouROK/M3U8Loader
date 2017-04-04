package ru.yourok.m3u8loaderbeta;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import ru.yourok.loader.Store;
import ru.yourok.m3u8loaderbeta.utils.ThemeChanger;

public class DirectoryListActivity extends AppCompatActivity {

    private File DirectoryPath;
    private MyAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeChanger.SetTheme(this);
        setContentView(R.layout.activity_directory_list);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            findViewById(R.id.btnNavHome).setVisibility(View.GONE);

        String dir = Store.getDownloadPath();
        if (!dir.isEmpty())
            DirectoryPath = new File(dir);
        if (DirectoryPath == null || !DirectoryPath.exists()) {
            DirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!DirectoryPath.exists())
                DirectoryPath = Environment.getExternalStorageDirectory();
            if (!DirectoryPath.exists())
                DirectoryPath = new File("/sdcard");
            if (!DirectoryPath.exists())
                DirectoryPath = new File("/storage");
            if (!DirectoryPath.exists())
                DirectoryPath = new File("/");
        }

        final ListView listView = (ListView) findViewById(R.id.directoryList);
        listAdapter = new MyAdapter(this);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                File file = (File) adapterView.getItemAtPosition(i);
                if (file != null) {
                    if (!file.canWrite())
                        Toast.makeText(DirectoryListActivity.this, getText(R.string.error_directory_permission), Toast.LENGTH_SHORT).show();
                    DirectoryPath = file;
                    updateViews();
                }
            }
        });
        updateViews();
    }

    public void upBtnClick(View view) {
        if (DirectoryPath != null) {
            if (DirectoryPath.getParentFile() != null) {
                DirectoryPath = DirectoryPath.getParentFile();
                updateViews();
            }
            if (!DirectoryPath.canWrite())
                Toast.makeText(this, getText(R.string.error_directory_permission), Toast.LENGTH_SHORT).show();
        }
    }

    public void homeBtnClick(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle(R.string.selected_folder_label);
            final HomeDirsAdapter adapter = new HomeDirsAdapter(this);
            dialogBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    File newDir = (File) adapter.getItem(item);
                    if (newDir != null) {
                        DirectoryPath = newDir;
                        updateViews();
                    }
                }
            });
            AlertDialog alertDialogObject = dialogBuilder.create();
            alertDialogObject.show();
        }
    }

    public void createDirBtnClick(View view) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.new_directory_title);
        alertDialog.setMessage(R.string.new_directory_name_label);

        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(R.drawable.ic_create_new_folder_black_24dp);

        alertDialog.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String name = input.getText().toString();
                        if (!new File(DirectoryPath.getAbsolutePath() + "/" + name).mkdir())
                            Toast.makeText(DirectoryListActivity.this, R.string.error_create_folder, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        updateViews();
                    }
                });
        alertDialog.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.create().show();
    }

    public void confirmBtnClick(View view) {
        if (DirectoryPath == null)
            return;
        if (!DirectoryPath.canWrite()) {
            Toast.makeText(DirectoryListActivity.this, getText(R.string.error_directory_permission), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("filename", DirectoryPath.getAbsolutePath());
        setResult(RESULT_OK, intent);
        finish();
    }

    public void cancelBtnClick(View view) {
        finish();
    }

    public void updateViews() {
        if (DirectoryPath != null)
            ((TextView) findViewById(R.id.txtvSelectedFolder)).setText(DirectoryPath.getAbsolutePath());
        ((ListView) findViewById(R.id.directoryList)).invalidateViews();
    }

    class MyAdapter extends BaseAdapter {

        private Context context;
        private File[] files;

        MyAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            if (DirectoryPath != null) {
                files = DirectoryPath.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isDirectory();
                    }
                });
                if (files != null) {
                    Arrays.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File file1, File file2) {
                            String f1 = file1.getName().toLowerCase();
                            String f2 = file2.getName().toLowerCase();
                            if (f1.startsWith(".") && !f2.startsWith("."))
                                return 1;
                            if (!f1.startsWith(".") && f2.startsWith("."))
                                return -1;
                            return f1.compareTo(f2);
                        }
                    });
                    return files.length;
                }
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (position >= 0 && position < files.length)
                return files[position];
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_1, null);
            }

            TextView text1 = (TextView) v.findViewById(android.R.id.text1);
            if (position >= 0 && position < files.length)
                text1.setText(files[position].getName());
            return v;
        }
    }


    public class HomeDirsAdapter extends BaseAdapter {

        ArrayList<File> list;
        Context context;

        public HomeDirsAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            list = new ArrayList<>();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                File[] flist = context.getExternalFilesDirs(null);
                if (flist != null)
                    list.addAll(Arrays.asList(flist));
            }
            File dload = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (dload != null)
                list.add(dload);
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            if (list == null || position < 0 || position >= list.size())
                return null;
            return list.get(position);
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
                ((TextView) convertView.findViewById(android.R.id.text1)).setText(list.get(position).getAbsolutePath());
                String Space = Store.byteFmt(list.get(position).getFreeSpace(), true) + "/" + Store.byteFmt(list.get(position).getTotalSpace(), true);
                ((TextView) convertView.findViewById(android.R.id.text2)).setText(Space);
            }
            return convertView;

        }
    }
}
