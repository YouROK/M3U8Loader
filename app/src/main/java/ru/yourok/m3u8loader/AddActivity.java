package ru.yourok.m3u8loader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import ru.yourok.loader.Loader;
import ru.yourok.loader.LoaderService;
import ru.yourok.loader.LoaderServiceHandler;
import ru.yourok.loader.Options;
import ru.yourok.m3u8loader.utils.ThemeChanger;

public class AddActivity extends AppCompatActivity {

    private EditText urlEdit;
    private EditText fileEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeChanger.SetTheme(this);
        setContentView(R.layout.activity_add);

        LoaderService.startService(this);
        urlEdit = (EditText) findViewById(R.id.editTextUrl);
        fileEdit = (EditText) findViewById(R.id.editTextFileName);
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            for (String key : bundle.keySet())
                if (key.toLowerCase().contains("name") || key.toLowerCase().contains("title")) {
                    Object value = bundle.get(key);
                    String name = cleanFileName(value.toString().trim());
                    if (!name.isEmpty())
                        fileEdit.setText(name);
                }
        }

        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEND) && intent.getStringExtra(Intent.EXTRA_TEXT) != null) {
            urlEdit.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
        }
        if (intent.getData() != null)
            urlEdit.setText(intent.getDataString());

        if (urlEdit.getText().toString().isEmpty())
            urlEdit.requestFocus();
        else
            fileEdit.requestFocus();
    }

    public void addBtnClick(View view) {
        String Name = cleanFileName(fileEdit.getText().toString().trim());
        String Url = urlEdit.getText().toString().trim();
        String err = isFilenameValid(Name);
        if (err != null) {
            Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
            return;
        }

        if (Url.isEmpty()) {
            Toast.makeText(this, R.string.error_url_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        if (Name.isEmpty()) {
            Toast.makeText(this, R.string.error_filename_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < LoaderServiceHandler.SizeLoaders(); i++)
            if (LoaderServiceHandler.GetLoader(i).GetName().equals(Name)) {
                Toast.makeText(this, R.string.error_double_name, Toast.LENGTH_SHORT).show();
                return;
            } else if (LoaderServiceHandler.GetLoader(i).GetUrl().equals(Url)) {
                Toast.makeText(this, R.string.error_double_url, Toast.LENGTH_SHORT).show();
                return;
            }

        Loader loader = new Loader();
        loader.SetUrl(Url);
        loader.SetName(Name);
        LoaderService.registerOnUpdateLoader(null);
        LoaderServiceHandler.AddLoader(loader);
        Options.getInstance(this).SaveList();
        finish();
    }

    public void downloadBtnClick(View view) {
        String Name = cleanFileName(fileEdit.getText().toString().trim());
        String Url = urlEdit.getText().toString().trim();
        String err = isFilenameValid(Name);
        if (err != null) {
            Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
            return;
        }

        if (Url.isEmpty()) {
            Toast.makeText(this, R.string.error_url_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        if (Name.isEmpty()) {
            Toast.makeText(this, R.string.error_filename_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < LoaderServiceHandler.SizeLoaders(); i++)
            if (LoaderServiceHandler.GetLoader(i).GetName().equals(Name)) {
                Toast.makeText(this, R.string.error_double_name, Toast.LENGTH_SHORT).show();
                return;
            } else if (LoaderServiceHandler.GetLoader(i).GetUrl().equals(Url)) {
                Toast.makeText(this, R.string.error_double_url, Toast.LENGTH_SHORT).show();
                return;
            }

        Loader loader = new Loader();
        loader.SetUrl(Url);
        loader.SetName(Name);

        LoaderServiceHandler.AddLoader(loader);
        Options.getInstance(this).SaveList();
        LoaderServiceHandler.AddQueue(LoaderServiceHandler.SizeLoaders() - 1);
        LoaderService.registerOnUpdateLoader(null);
        LoaderService.load(this);
        finish();
    }

    public void cancelBtnClick(View view) {
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("UrlString", urlEdit.getText().toString());
        outState.putString("FileNameString", fileEdit.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        urlEdit.setText(savedInstanceState.getString("UrlString"));
        fileEdit.setText(savedInstanceState.getString("FileNameString"));
    }

    public static String isFilenameValid(String file) {
        File f = new File(file);
        try {
            f.getCanonicalPath();
            return null;
        } catch (IOException e) {
            return e.getLocalizedMessage();
        }
    }

    private static final String ReservedCharsReg = "[|\\\\?*<\\\":>+/']";

    public static String cleanFileName(String file) {
        return file.replaceAll(ReservedCharsReg, "_").replaceAll("_+", "_");
    }
}
