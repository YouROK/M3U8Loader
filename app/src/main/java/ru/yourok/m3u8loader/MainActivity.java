package ru.yourok.m3u8loader;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;

import dwl.LoaderInfo;
import ru.yourok.loader.Loader;
import ru.yourok.loader.Manager;
import ru.yourok.loader.Store;
import ru.yourok.m3u8loader.utils.*;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    private ListView loadersList;
    private boolean isUpdateList;

    private final Object lock = new Object();
    private static long lastViewDonate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeChanger.SetTheme(this);
        setContentView(R.layout.activity_main);

        loadersList = (ListView) findViewById(R.id.listViewLoaders);
        final MainActivityLoaderAdaptor adapter = new MainActivityLoaderAdaptor(this);
        loadersList.setAdapter(adapter);
        loadersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapter.getSelected() == i) {
                    adapter.setSelected(-1);
                } else {
                    adapter.setSelected(i);
                }
                updateMenu();
                adapter.notifyDataSetChanged();
            }
        });
        requestPermissionWithRationale();
    }

    @Override
    protected void onPause() {
        isUpdateList = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
        showDonate();
    }

    public void requestPermissionWithRationale() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(findViewById(R.id.main_layout), R.string.permission_storage_msg, Snackbar.LENGTH_INDEFINITE)
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

    private void updateMenu() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(300);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (lock) {
                            int sel = ((MainActivityLoaderAdaptor) loadersList.getAdapter()).getSelected();
                            if (sel == -1)
                                findViewById(R.id.itemLoaderMenu).setVisibility(View.GONE);
                            else {
                                if (isPlayButton(sel))
                                    ((ImageButton) findViewById(R.id.buttonItemMenuStart)).setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                                else
                                    ((ImageButton) findViewById(R.id.buttonItemMenuStart)).setImageResource(R.drawable.ic_file_download_black_24dp);
                                findViewById(R.id.itemLoaderMenu).setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
            }
        }).start();
    }

    private boolean isPlayButton(int sel) {
        LoaderInfo info = Manager.GetLoaderInfo(sel);
        if (info == null)
            return false;
        if (info.getStatus() == Manager.STATUS_COMPLETE)
            return true;
        if (Manager.GetSettings() == null)
            return false;
        if (info.getStatus() == Manager.STATUS_LOADING && Manager.GetSettings().getDynamicSize() && (int) info.getLoadedDuration() > Manager.GetSettings().getThreads())
            return true;
        return false;
    }

    private void updateList() {
        updateMenu();
        synchronized (lock) {
            if (isUpdateList)
                return;
            isUpdateList = true;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (; isUpdateList; ) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ((MainActivityLoaderAdaptor) loadersList.getAdapter()).notifyDataSetChanged();
                                MainActivityLoaderAdaptor adaptorList = ((MainActivityLoaderAdaptor) loadersList.getAdapter());
                                if (Manager.Length() > 0 && adaptorList.getSelected() >= Manager.Length())
                                    adaptorList.setSelected(Manager.Length() - 1);
                                if (Manager.Length() == 0)
                                    adaptorList.setSelected(-1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    try {
                        if (Loader.isLoading())
                            Thread.sleep(200);
                        else {
                            for (int i = 0; i < 30; i++) {
                                Thread.sleep(100);
                                if (!isUpdateList || Loader.isLoading())
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void onAddClick(View view) {
        view.setEnabled(false);
        Intent intent = new Intent(this, AddLoaderActivity.class);
        intent.setData(Uri.parse(""));
        startActivity(intent);
        view.setEnabled(true);
    }

    public void onDownloadAllClick(View view) {
        view.setEnabled(false);
        for (int i = 0; i < Manager.Length(); i++) {
            if (Manager.GetLoaderStatus(i) != Manager.STATUS_COMPLETE)
                Loader.Add(i);
        }
        Loader.Start();
        view.setEnabled(true);
        updateMenu();
    }

    public void onStopAllClick(View view) {
        view.setEnabled(false);
        Loader.Clear();
        view.setEnabled(true);
        updateMenu();
    }

    public void onClearListClick(View view) {
        if (Manager.Length() == 0)
            return;
        boolean isFiles = false;
        for (int i = 0; i < Manager.Length(); i++)
            if (new File(Manager.GetFileName(i)).exists()) {
                isFiles = true;
                break;
            }
        view.setEnabled(false);
        if (!isFiles) {
            Loader.Clear();
            while (Manager.Length() > 0)
                Manager.Remove(0);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.delete_label) + "?");
            builder.setPositiveButton(R.string.delete_with_files, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Loader.Clear();
                    while (Manager.Length() > 0) {
                        String outFileName = Manager.GetFileName(0);
                        String subsFileName = Manager.GetSubtitlesFileName(0);
                        Manager.Remove(0);
                        if (!outFileName.isEmpty())
                            new File(outFileName).delete();
                        if (!subsFileName.isEmpty())
                            new File(subsFileName).delete();
                    }
                    updateMenu();
                }
            });
            builder.setNegativeButton(R.string.remove_from_list, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Loader.Clear();
                    while (Manager.Length() > 0)
                        Manager.Remove(0);
                    updateMenu();
                }
            });
            builder.setNeutralButton(" ", null);
            builder.create().show();
        }
        view.setEnabled(true);
        updateMenu();
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
    }

    public void onLoadItemClick(View view) {
        int sel = ((MainActivityLoaderAdaptor) loadersList.getAdapter()).getSelected();
        if (sel != -1) {
            if (isPlayButton(sel)) {
                String fn = Manager.GetFileName(sel);
                LoaderInfo info = Manager.GetLoaderInfo(sel);
                if (info == null)
                    return;
                String title = info.getName();
                if (!new File(fn).exists()) {
                    Toast.makeText(this, getText(R.string.error_file_notexist) + ": " + fn, Toast.LENGTH_SHORT).show();
                    return;
                }
                PlayIntent.start(this, fn, title);
                return;
            }

            if (Manager.GetLoaderStatus(sel) != Manager.STATUS_LOADING) {
                view.setEnabled(false);
                Loader.Add(sel);
                Loader.Start();
                view.setEnabled(true);
                updateMenu();
            }
        }
    }

    public void onStopItemClick(View view) {
        int sel = ((MainActivityLoaderAdaptor) loadersList.getAdapter()).getSelected();
        if (sel != -1) {
            view.setEnabled(false);
            Loader.Rem(sel);
            Manager.Stop(sel);
            view.setEnabled(true);
        }
        updateMenu();
    }

    public void onRemoveItemClick(View view) {
        view.setEnabled(false);
        final int sel = ((MainActivityLoaderAdaptor) loadersList.getAdapter()).getSelected();
        if (sel != -1) {
            final String outFileName = Manager.GetFileName(sel);
            final String subsFileName = Manager.GetSubtitlesFileName(sel);
            if (!new File(outFileName).exists() && !new File(subsFileName).exists()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Loader.Rem(sel);
                        Manager.Remove(sel);
                        updateMenu();
                    }
                }).start();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.delete_label) + "?");
                builder.setPositiveButton(R.string.delete_with_files, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Loader.Rem(sel);
                        Manager.Remove(sel);
                        if (!outFileName.isEmpty())
                            new File(outFileName).delete();
                        if (!subsFileName.isEmpty())
                            new File(subsFileName).delete();
                        updateMenu();
                    }
                });
                builder.setNegativeButton(R.string.remove_from_list, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Loader.Rem(sel);
                        Manager.Remove(sel);
                        updateMenu();
                    }
                });
                builder.setNeutralButton(" ", null);
                builder.create().show();
            }
        }
        view.setEnabled(true);
    }

    public void onEditItemClick(View view) {
        int sel = ((MainActivityLoaderAdaptor) loadersList.getAdapter()).getSelected();
        if (sel != -1) {
            onStopAllClick(view);
            Intent intent = new Intent(this, EditLoaderActivity.class);
            intent.putExtra("Index", sel);
            startActivity(intent);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (loadersList == null)
            return super.onKeyUp(keyCode, event);

        MainActivityLoaderAdaptor adapter = ((MainActivityLoaderAdaptor) loadersList.getAdapter());
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.ACTION_UP:
                adapter.setSelected(adapter.getSelected() - 1);
                if (adapter.getSelected() < 0)
                    adapter.setSelected(adapter.getCount() - 1);
                updateMenu();
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.ACTION_DOWN:
                adapter.setSelected(adapter.getSelected() + 1);
                if (adapter.getSelected() >= adapter.getCount())
                    adapter.setSelected(0);
                updateMenu();
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT: {
                View view = findViewById(R.id.buttonSettings);
                if (view.isFocused()) {
                    findViewById(R.id.buttonItemMenuStart).requestFocus();
                    return true;
                }
                view = findViewById(R.id.buttonItemMenuEdit);
                if (view.isFocused()) {
                    findViewById(R.id.buttonAdd).requestFocus();
                    return true;
                }
                break;
            }
            case KeyEvent.KEYCODE_DPAD_LEFT: {
                View view = findViewById(R.id.buttonAdd);
                if (view.isFocused()) {
                    findViewById(R.id.buttonItemMenuEdit).requestFocus();
                    return true;
                }
                view = findViewById(R.id.buttonItemMenuStart);
                if (view.isFocused()) {
                    findViewById(R.id.buttonSettings).requestFocus();
                    return true;
                }
                break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showDonate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (MainActivity.this.getText(R.string.error).toString().equals("Ошибка"))
                    return;

                if (System.currentTimeMillis() - lastViewDonate < 3 * 1000)
                    return;
                lastViewDonate = System.currentTimeMillis();

                final long oneweek = 518400000l;

                long last = Store.getLastDonationView(MainActivity.this);
                if (last == -1)
                    return;
                if (System.currentTimeMillis() - last > oneweek) {//раз в неделю
                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(findViewById(R.id.main_layout), getText(R.string.donation) + "?", Snackbar.LENGTH_INDEFINITE)
                                    .setAction(android.R.string.ok, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Store.setLastDonationView(MainActivity.this, System.currentTimeMillis());
                                            Intent intent = new Intent(MainActivity.this, DonationActivity.class);
                                            startActivity(intent);
                                        }
                                    })
                                    .show();
                        }
                    }, 2000);
                }
            }
        }).start();
    }
}
