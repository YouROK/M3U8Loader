package ru.yourok.m3u8loader;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import ru.yourok.loader.LoaderManager;


public class MainActivity extends AppCompatActivity {
    public static AdaptorLoadresList loadresList;
    private static LoaderManager loaderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (loaderManager == null)
            loaderManager = new LoaderManager(this);
        if (loadresList==null) {
            loadresList = new AdaptorLoadresList(this);
        }else{
            UpdateList();
        }
        ((ListView) findViewById(R.id.listViewLoaders)).setAdapter(loadresList);

        //Check permission
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        UpdateList();
    }

    public void UpdateList() {
        if (loadresList != null)
            if (Looper.getMainLooper().equals(Looper.myLooper())) {
                loadresList.notifyDataSetChanged();
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadresList.notifyDataSetChanged();
                    }
                });
            }
    }

    public void onAddClick(View view) {
        Intent intent = new Intent(this, AddActivity.class);
        startActivity(intent);
    }

    public void onDownloadClick(View view) {
        view.setEnabled(false);
        view.invalidate();
        view.refreshDrawableState();
        new Thread(new Runnable() {
            @Override
            public void run() {
                loaderManager.Start();
            }
        }).start();
        view.setEnabled(true);
    }

    public void onStopClick(View view) {
        loaderManager.Stop();
    }

    public void onSettingsClick(View view) {
        Intent intent = new Intent(this,SettingsActivity.class);
        startActivity(intent);
    }
}
