package ru.yourok.m3u8loader;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import dwl.LoaderInfo;
import ru.yourok.loader.Loader;
import ru.yourok.loader.Manager;
import ru.yourok.m3u8loader.utils.ThemeChanger;

public class AddLoaderActivity extends AppCompatActivity {

    private EditText urlEdit;
    private EditText fileEdit;

    private String cookies = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeChanger.SetTheme(this);
        setContentView(R.layout.activity_add_loader);
        findViewById(R.id.buttonCancel).requestFocus();

        urlEdit = (EditText) findViewById(R.id.editTextUrl);
        fileEdit = (EditText) findViewById(R.id.editTextFileName);
        Intent intent = getIntent();

        if (intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            for (String key : bundle.keySet()) {
                if (key.toLowerCase().contains("name") || key.toLowerCase().contains("title")) {
                    Object value = bundle.get(key);
                    if (value != null) {
                        String name = cleanFileName(value.toString().trim());
                        if (!name.isEmpty())
                            fileEdit.setText(name);
                    }
                }
                if (key.toLowerCase().contains("cookie")) {
                    Object value = bundle.get(key);
                    if (value != null)
                        cookies = value.toString().trim();
                }
            }
        }

        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEND)) {
            if (intent.getStringExtra(Intent.EXTRA_TEXT) != null)
                urlEdit.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
            if (intent.getExtras().get(Intent.EXTRA_STREAM) != null)
                urlEdit.setText(intent.getExtras().get(Intent.EXTRA_STREAM).toString());
        }

        if (intent.getData() != null)
            urlEdit.setText(intent.getDataString());

        if (intent.getExtras() == null && intent.getData() == null) {
            Toast.makeText(this, "Error: not found url", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public static String cleanFileName(String file) {
        final String ReservedCharsReg = "[|\\\\?*<\\\":>+/']";
        String ret = file.replaceAll(ReservedCharsReg, "_").replaceAll("_+", "_");
        ret = ret.trim();
        return ret;
    }

    public void addBtnClick(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                addList(false);
            }
        }).start();
    }

    public void downloadBtnClick(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                addList(true);
            }
        }).start();
    }

    private void toastMsg(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AddLoaderActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toastRes(final int msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AddLoaderActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void cancelBtnClick(View view) {
        finish();
    }

    private void addList(boolean load) {
        String Name = cleanFileName(fileEdit.getText().toString().trim());
        String Url = urlEdit.getText().toString().trim();
        if (Url.isEmpty()) {
            toastRes(R.string.error_empty_url);
            return;
        }

        if (Name.isEmpty()) {
            toastRes(R.string.error_empty_name);
            return;
        }

        for (int i = 0; i < Manager.Length(); i++) {
            LoaderInfo info = Manager.GetLoaderInfo(i);
            String nameLoader = info.getName();
            String urlLoader = info.getUrl();

            if (urlLoader.equals(Url)) {
                toastRes(R.string.error_same_url);
                return;
            }
            if (nameLoader.equals(Name)) {
                changeUrlLoader();
                return;
            }
        }
        int oldLength = Manager.Length();
        String err = Manager.Add(Url, Name);
        if (!err.isEmpty()) {
            toastMsg(AddLoaderActivity.this.getText(R.string.error) + ": " + err);
            return;
        }

        if (load && oldLength != Manager.Length()) {
            int start = Manager.Length() - oldLength;
            for (int i = start; i < Manager.Length(); i++)
                Loader.Add(i);
            Loader.Start();
        }
        finish();
    }

    private void changeUrlLoader() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddLoaderActivity.this);
                builder.setTitle(R.string.replace_url);
                builder.setMessage(R.string.replace_url_msg);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String Name = cleanFileName(fileEdit.getText().toString().trim());
                        String Url = urlEdit.getText().toString().trim();
                        final String err = Manager.SetLoaderUrl(Url, Name);
                        if (!err.isEmpty()) {
                            toastMsg(AddLoaderActivity.this.getText(R.string.error_change_url) + ": " + err);
                            return;
                        }
                        AddLoaderActivity.this.finish();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.create().show();
            }
        });
    }
}
