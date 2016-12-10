package ru.yourok.m3u8loader;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import ru.yourok.loader.Loader;
import ru.yourok.loader.Options;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        loadSettings();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ((EditText) findViewById(R.id.editTextDirectoryPath)).setText(savedInstanceState.getString("DownloadDir", ""));
        ((EditText) findViewById(R.id.editTextThreads)).setText(savedInstanceState.getString("Threads", ""));
        ((EditText) findViewById(R.id.editTextTimeout)).setText(savedInstanceState.getString("Timeout", ""));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("DownloadDir", ((EditText) findViewById(R.id.editTextDirectoryPath)).getText().toString());
        outState.putString("Threads", ((EditText) findViewById(R.id.editTextThreads)).getText().toString());
        outState.putString("Timeout", ((EditText) findViewById(R.id.editTextTimeout)).getText().toString());
    }

    private void loadSettings() {
        ((EditText) findViewById(R.id.editTextDirectoryPath)).setText(Options.getInstance(this).GetOutDir());
        ((EditText) findViewById(R.id.editTextThreads)).setText(String.valueOf(Options.getInstance(this).GetThreads()));
        ((EditText) findViewById(R.id.editTextTimeout)).setText(String.valueOf(Options.getInstance(this).GetTimeout()));
        ((TextView) findViewById(R.id.textViewTempDir)).setText(Options.getInstance(this).GetTempDir());
    }

    private void saveSettings() {
        String dirout = ((EditText) findViewById(R.id.editTextDirectoryPath)).getText().toString();
        String dirtemp = ((TextView) findViewById(R.id.textViewTempDir)).getText().toString();
        String threads = ((EditText) findViewById(R.id.editTextThreads)).getText().toString();
        String timeout = ((EditText) findViewById(R.id.editTextTimeout)).getText().toString();
        Options.getInstance(this).SetOutDir(dirout);
        Options.getInstance(this).SetTempDir(dirtemp);
        Options.getInstance(this).SetTimeout(Integer.parseInt(timeout));
        Options.getInstance(this).SetThreads(Integer.parseInt(threads));
    }

    public void okBtnClick(View view) {
        saveSettings();
        finish();
    }

    public void cancelBtnClick(View view) {
        finish();
    }

    public void clrTempDir(View view) {
        String tmpdir = ((TextView) findViewById(R.id.textViewTempDir)).getText().toString();
        String ret = Loader.RemoveDir(tmpdir);
        if (ret.isEmpty()) ret = getText(android.R.string.ok).toString();
        Toast.makeText(this, ret, Toast.LENGTH_SHORT).show();
    }

    public void srchBtnClick(View view) {
        Intent intent = new Intent(this, DirectoryChooserActivity.class);
        startActivityForResult(intent, 1202);
    }

    public void findTempDir(View view) {
        if (Build.VERSION.SDK_INT < 19) {
            Intent intent = new Intent(this, DirectoryChooserActivity.class);
            startActivityForResult(intent, 1203);
        }else {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            final FileDirsAdapter adapter = new FileDirsAdapter();
            dialogBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    ((TextView) findViewById(R.id.textViewTempDir)).setText(((File) adapter.getItem(item)).getAbsolutePath());
                }
            });
            AlertDialog alertDialogObject = dialogBuilder.create();
            alertDialogObject.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        String name = data.getStringExtra("filename");
        if (name.equals("/")) {
            Toast.makeText(this, getText(R.string.error) + " wrong directory", Toast.LENGTH_SHORT).show();
            return;
        }

        if (requestCode == 1202) {
            ((EditText) findViewById(R.id.editTextDirectoryPath)).setText(name);
        }else if(requestCode == 1203){
            ((TextView) findViewById(R.id.textViewTempDir)).setText(name);
        }
    }

    public class FileDirsAdapter extends BaseAdapter {

        File[] list;

        @Override
        public int getCount() {
            list = getExternalFilesDirs(null);
            return list.length;
        }

        @Override
        public Object getItem(int position) {

            return list[position];
        }

        @Override
        public long getItemId(int position) {

            return position;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(android.R.layout.two_line_list_item, null);
            }

            ((TextView) convertView.findViewById(android.R.id.text1)).setText(list[position].getAbsolutePath());
            String Space = humanReadableByteCount(list[position].getFreeSpace(), true) + "/" + humanReadableByteCount(list[position].getTotalSpace(), true);
            ((TextView) convertView.findViewById(android.R.id.text2)).setText(Space);

            return convertView;

        }

    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
