package ru.yourok.m3u8loader;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;

import go.m3u8.M3u8;
import go.m3u8.State;
import ru.yourok.loader.Loader;
import ru.yourok.loader.LoaderService;
import ru.yourok.loader.LoaderServiceHandler;
import ru.yourok.loader.Options;
import ru.yourok.m3u8loader.utils.PlayIntent;
import ru.yourok.m3u8loader.utils.ThemeChanger;


public class MainActivity extends AppCompatActivity implements LoaderService.LoaderServiceCallbackUpdate {
    public static AdaptorLoadersList loadersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeChanger.SetTheme(this);
        setContentView(R.layout.activity_main);

        LoaderService.registerOnUpdateLoader(this);
        LoaderService.startService(this);
        if (loadersList == null) {
            loadersList = new AdaptorLoadersList(this);
        } else {
            loadersList.setContext(this);
            UpdateList();
        }
        ListView listView = ((ListView) findViewById(R.id.listViewLoaders));
        listView.setAdapter(loadersList);
        if (loadersList.getSelected() == -1)
            findViewById(R.id.itemLoaderMenu).setVisibility(View.GONE);

        setMenuClickListener();
        requestPermissionWithRationale();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LoaderService.registerOnUpdateLoader(this);
        LoaderService.startService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoaderService.registerOnUpdateLoader(this);
        LoaderService.startService(this);
    }

    @Override
    protected void onStop() {
        LoaderService.registerOnUpdateLoader(null);
        super.onStop();
    }

    @Override
    protected void onPause() {
        LoaderService.registerOnUpdateLoader(null);
        super.onPause();
    }

    public void UpdateList() {
        if (loadersList != null)
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadersList.notifyDataSetChanged();
                    int sel = loadersList.getSelected();
                    if (sel < 0 || sel >= loadersList.getCount())
                        findViewById(R.id.itemLoaderMenu).setVisibility(View.GONE);
                }
            });
    }

    public void onAddClick(View view) {
        Intent intent = new Intent(this, AddActivity.class);
        final int sel = loadersList.getSelected();
        final Loader loader = LoaderServiceHandler.GetLoader(sel);
        if (loader != null) {
            intent.setData(Uri.parse(loader.GetUrl()));
            intent.putExtra("name", loader.GetName());
        } else
            intent.setData(Uri.parse(""));
        startActivity(intent);
    }

    public void onDownloadClick(View view) {
        view.setEnabled(false);
        view.invalidate();
        view.refreshDrawableState();

        LoaderServiceHandler.loadersQueue.clear();
        for (int i = 0; i < LoaderServiceHandler.SizeLoaders(); i++) {
            Loader loader = LoaderServiceHandler.GetLoader(i);
            if (loader == null)
                continue;
            State st = loader.GetState();
            if (st == null || st.getStage() != M3u8.Stage_Finished)
                LoaderServiceHandler.AddQueue(i);
        }
        LoaderService.load(this);

        view.setEnabled(true);
    }

    public void onStopClick(View view) {
        LoaderService.stop(this);
    }

    public void onClearListClick(View view) {
        if (LoaderServiceHandler.SizeLoaders() == 0)
            return;
        LoaderService.stop(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.delete_label) + "?");
        builder.setPositiveButton(R.string.delete_with_files, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                while (LoaderServiceHandler.SizeLoaders() > 0) {
                    Loader loader = LoaderServiceHandler.GetLoader(0);
                    String[] files = loader.GetOutFiles();
                    if (files != null)
                        for (String f : files)
                            new File(f).delete();
                    loader.Stop();
                    loader.RemoveTemp();
                    loader.RemoveList();
                    LoaderServiceHandler.RemoveLoader(0);
                    Options.getInstance(MainActivity.this).SaveList();
                }
                UpdateList();
            }
        });
        builder.setNegativeButton(R.string.remove_from_list, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                while (LoaderServiceHandler.SizeLoaders() > 0) {
                    Loader loader = LoaderServiceHandler.GetLoader(0);
                    if (loader == null)
                        continue;
                    loader.Stop();
                    loader.RemoveTemp();
                    loader.RemoveList();
                    LoaderServiceHandler.RemoveLoader(0);
                    Options.getInstance(MainActivity.this).SaveList();
                }
                UpdateList();
            }
        });
        builder.create().show();
    }

    public void onSettingsClick(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, SettingsActivity.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SettingsActivity.REQUEST_CODE && resultCode == RESULT_OK) {
            if (data.getBooleanExtra("recreate", false)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.this.recreate();
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
        if (requestCode == RemoveDialogActivity.REQUEST_CODE && resultCode == RESULT_OK) {
            if (loadersList.getSelected() == 0 && LoaderServiceHandler.SizeLoaders() > 0)
                loadersList.setSelected(loadersList.getSelected());
            else if (loadersList.getSelected() >= 0)
                loadersList.setSelected(loadersList.getSelected() - 1);
            loadersList.notifyDataSetChanged();
        }
    }

    @Override
    public void onUpdateLoader() {
        UpdateList();
    }

    public void requestPermissionWithRationale() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(findViewById(R.id.main_layout), R.string.permission_msg, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.permission_btn, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void setMenuClickListener() {
        //Menu
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final int sel = loadersList.getSelected();
                final Loader loader = LoaderServiceHandler.GetLoader(sel);
                if (loader == null) {
                    loadersList.setSelected(-1);
                    return;
                }
                if (sel == -1)
                    return;
                switch (view.getId()) {
                    case R.id.buttonItemMenuStart: {
                        if (loader.GetList() == null) {
                            view.setEnabled(false);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    loader.LoadListOpts(MainActivity.this);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            view.setEnabled(true);
                                        }
                                    });
                                    if (loader.GetOutFiles() != null)
                                        openFile(loader);
                                    else {
                                        if (loadersList.getSelected() != -1)
                                            LoaderServiceHandler.AddQueue(loadersList.getSelected());
                                        LoaderService.load(MainActivity.this);
                                    }
                                }
                            }).start();
                            LoaderService.startService(MainActivity.this);
                        } else {
                            if (loader.GetOutFiles() != null)
                                openFile(loader);
                            else {
                                LoaderServiceHandler.AddQueue(sel);
                                LoaderService.load(MainActivity.this);
                            }
                        }
                        break;
                    }
                    case R.id.buttonItemMenuStop: {
                        if (loader.IsWorking())
                            loader.Stop();
                        break;
                    }
                    case R.id.buttonItemMenuRemove: {
                        LoaderService.stop(MainActivity.this);
                        Intent intent = new Intent(MainActivity.this, RemoveDialogActivity.class);
                        intent.putExtra("index", sel);
                        MainActivity.this.startActivityForResult(intent, RemoveDialogActivity.REQUEST_CODE);
                        break;
                    }
                    case R.id.buttonItemMenuEdit: {
                        Intent intent = new Intent(MainActivity.this, ListEditActivity.class);
                        intent.putExtra("LoaderID", sel);
                        MainActivity.this.startActivity(intent);
                        break;
                    }
                }
                UpdateList();
            }
        };

        findViewById(R.id.buttonItemMenuStart).setOnClickListener(clickListener);
        findViewById(R.id.buttonItemMenuStop).setOnClickListener(clickListener);
        findViewById(R.id.buttonItemMenuRemove).setOnClickListener(clickListener);
        findViewById(R.id.buttonItemMenuEdit).setOnClickListener(clickListener);
    }


    private void openFile(final Loader loader) {
        if (loader == null)
            return;
        final String[] names = loader.GetOutFiles();
        if (names == null || names.length == 0)
            return;
        if (names.length == 1) {
            PlayIntent.start(this, names[0], loader.GetName());
        } else {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
            dialogBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    PlayIntent.start(MainActivity.this, names[item], loader.GetName());
                }
            });
            AlertDialog alertDialogObject = dialogBuilder.create();
            alertDialogObject.show();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (loadersList == null)
            return super.onKeyUp(keyCode, event);
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.ACTION_UP:
                if (loadersList.getSelected() > 0) {
                    loadersList.setSelected(loadersList.getSelected() - 1);
                    findViewById(R.id.topLoaderMenu).requestFocus();
                }
                UpdateList();
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.ACTION_DOWN:
                if (loadersList.getSelected() < loadersList.getCount() - 1) {
                    loadersList.setSelected(loadersList.getSelected() + 1);
                    findViewById(R.id.itemLoaderMenu).requestFocus();
                }
                UpdateList();
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }
}
