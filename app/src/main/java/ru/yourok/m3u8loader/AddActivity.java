package ru.yourok.m3u8loader;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import ru.yourok.loader.Loader;
import ru.yourok.loader.LoaderHolder;
import ru.yourok.loader.Options;

public class AddActivity extends AppCompatActivity {

    private EditText urlEdit;
    private EditText fileEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        urlEdit = (EditText) findViewById(R.id.editTextUrl);
        fileEdit = (EditText) findViewById(R.id.editTextFileName);
        Intent intent = getIntent();
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
        String Name = fileEdit.getText().toString().trim();
        String Url = urlEdit.getText().toString().trim();

        if (Url.isEmpty()) {
            Toast.makeText(this, R.string.error_url_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        if (Name.isEmpty()) {
            Toast.makeText(this, R.string.error_filename_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < LoaderHolder.getInstance().Size(); i++)
            if (LoaderHolder.getInstance().GetLoader(i).GetName().equals(Name)) {
                Toast.makeText(this, R.string.error_double_name, Toast.LENGTH_SHORT).show();
                return;
            } else if (LoaderHolder.getInstance().GetLoader(i).GetUrl().equals(Url)) {
                Toast.makeText(this, R.string.error_double_url, Toast.LENGTH_SHORT).show();
                return;
            }

        Loader loader = new Loader(this);
        loader.SetUrl(Url);
        loader.SetName(Name);
        LoaderHolder.getInstance().AddLoader(loader);
        finish();
    }

    public void downloadBtnClick(View view) {
        String Name = fileEdit.getText().toString().trim();
        String Url = urlEdit.getText().toString().trim();

        if (Url.isEmpty()) {
            Toast.makeText(this, R.string.error_url_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        if (Name.isEmpty()) {
            Toast.makeText(this, R.string.error_filename_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < LoaderHolder.getInstance().Size(); i++)
            if (LoaderHolder.getInstance().GetLoader(i).GetName().equals(Name)) {
                Toast.makeText(this, R.string.error_double_name, Toast.LENGTH_SHORT).show();
                return;
            } else if (LoaderHolder.getInstance().GetLoader(i).GetUrl().equals(Url)) {
                Toast.makeText(this, R.string.error_double_url, Toast.LENGTH_SHORT).show();
                return;
            }

        Loader loader = new Loader(this);
        loader.SetUrl(Url);
        loader.SetName(Name);
        LoaderHolder.getInstance().AddLoader(loader);
        if (MainActivity.loaderManager != null) {
            MainActivity.loaderManager.Add(LoaderHolder.getInstance().Size() - 1);
            if (!MainActivity.loaderManager.IsLoading())
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.loaderManager.Start();
                    }
                }).start();
        }
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
}
