package ru.yourok.m3u8loader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import ru.yourok.loader.Loader;
import ru.yourok.loader.LoaderService;
import ru.yourok.loader.LoaderServiceHandler;
import ru.yourok.loader.Options;

public class AddActivity extends AppCompatActivity {

    private EditText urlEdit;
    private EditText fileEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        LoaderService.startService(this);
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
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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
}
