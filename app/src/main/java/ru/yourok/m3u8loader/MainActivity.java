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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import go.m3u8.M3u8;
import go.m3u8.State;
import ru.yourok.loader.Loader;
import ru.yourok.loader.LoaderHolder;
import ru.yourok.loader.LoaderManager;
import ru.yourok.loader.Options;


public class MainActivity extends AppCompatActivity {
    public static AdaptorLoadresList loadresList;
    public static LoaderManager loaderManager;
    public static boolean isRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (loaderManager == null)
            loaderManager = new LoaderManager(this);
        if (loadresList == null) {
            loadresList = new AdaptorLoadresList(this);
        } else {
            UpdateList();
        }
        ListView listView = ((ListView) findViewById(R.id.listViewLoaders));
        listView.setAdapter(loadresList);
        isRunning = true;
        //Check permission
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        isRunning = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isRunning = false;
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
        if (loaderManager.IsLoading()) {
            Toast.makeText(this, R.string.error_already_loading, Toast.LENGTH_SHORT).show();
        } else {
            loaderManager.Stop();
            for (int i = 0; i < LoaderHolder.getInstance().Size(); i++) {
                State st = LoaderHolder.getInstance().GetLoader(i).GetState();
                if (st == null)
                    loaderManager.Add(i);
                else if (st.getStage() != M3u8.Stage_Finished)
                    loaderManager.Add(i);
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    loaderManager.Start();
                }
            }).start();
        }
        view.setEnabled(true);
    }

    public void onStopClick(View view) {
        loaderManager.Stop();
    }

    public void onSettingsClick(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
