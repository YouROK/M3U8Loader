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
import android.widget.Toast;

import go.m3u8.M3u8;
import go.m3u8.State;
import ru.yourok.loader.LoaderService;
import ru.yourok.loader.LoaderServiceHandler;
import ru.yourok.loader.Options;


public class MainActivity extends AppCompatActivity implements LoaderService.LoaderServiceCallbackUpdate {
    public static AdaptorLoadresList loadresList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LoaderService.registerOnUpdateLoader(this);
        LoaderService.startService(this);
        if (loadresList == null) {
            loadresList = new AdaptorLoadresList(this);
        } else {
            UpdateList();
        }
        ListView listView = ((ListView) findViewById(R.id.listViewLoaders));
        listView.setAdapter(loadresList);
        //Check permission
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LoaderService.registerOnUpdateLoader(this);
    }

    @Override
    protected void onStop() {
        LoaderService.registerOnUpdateLoader(null);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoaderService.registerOnUpdateLoader(this);
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

        LoaderServiceHandler.loadersQueue.clear();
        for (int i = 0; i < LoaderServiceHandler.SizeLoaders(); i++) {
            State st = LoaderServiceHandler.GetLoader(i).GetState();
            if (st == null)
                LoaderServiceHandler.AddQueue(i);
            else if (st.getStage() != M3u8.Stage_Finished)
                LoaderServiceHandler.AddQueue(i);
        }
        LoaderService.load(this);

        view.setEnabled(true);
    }

    public void onStopClick(View view) {
        LoaderService.stop(this);
    }

    public void onSettingsClick(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onUpdateLoader(int id) {
        if (id == -1)
            return;
        UpdateList();
    }
}
