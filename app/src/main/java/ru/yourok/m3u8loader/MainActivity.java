package ru.yourok.m3u8loader;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import ru.yourok.loader.M3U8;

public class MainActivity extends AppCompatActivity {
    UpdateTimeTask updateTimeTask;
    String editTextUrl;
    String editTextFileName;
    static public M3U8 m3u8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Check permission
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }
        Intent intent = getIntent();
        if (intent.getData() != null) {
            ((EditText) findViewById(R.id.editTextUrl)).setText(intent.getData().toString());
            if (intent.getStringExtra("filename") != null)
                ((EditText) findViewById(R.id.editTextFileName)).setText(intent.getStringExtra("filename"));
        }
    }

    @Override
    protected void onPause() {
        editTextUrl = ((EditText) findViewById(R.id.editTextUrl)).getText().toString();
        editTextFileName = ((EditText) findViewById(R.id.editTextFileName)).getText().toString();
        stopTimer();
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (editTextFileName != null)
            ((EditText) findViewById(R.id.editTextUrl)).setText(editTextUrl);
        if (editTextFileName != null)
            ((EditText) findViewById(R.id.editTextFileName)).setText(editTextFileName);
        startTimer();
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        stopTimer();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        startTimer();
    }

    public void settingsBtnClick(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void loadBtnClick(View view) {
        if (m3u8 != null && m3u8.IsLoading()) {
            Toast.makeText(this, R.string.error_already_loading, Toast.LENGTH_SHORT).show();
            return;
        } else {
            m3u8 = new M3U8(this);
        }
        enableStartButton(false);
        Thread th = new Thread(new Runnable() {
            public void run() {
                showStatus(R.string.status_load_list);
                EditText editUrl = (EditText) findViewById(R.id.editTextUrl);
                String url = editUrl.getText().toString().trim();
                EditText editName = (EditText) findViewById(R.id.editTextFileName);
                String name = editName.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(MainActivity.this, R.string.error_filename_empty, Toast.LENGTH_SHORT).show();
                    enableStartButton(true);
                    return;
                }
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                String threadsStr = prefs.getString("Threads", "10");
                String timeoutStr = prefs.getString("Timeout", "5000");
                String downloadPath = prefs.getString("DownloadDir", "");

                if (downloadPath.isEmpty()) {
                    Toast.makeText(MainActivity.this, getText(R.string.error_download_dir), Toast.LENGTH_SHORT).show();
                    enableStartButton(true);
                    return;
                }

                int timeout = 15000;
                int threads = 10;
                if (!timeoutStr.isEmpty())
                    timeout = Integer.parseInt(timeoutStr, 10);
                if (!threadsStr.isEmpty())
                    threads = Integer.parseInt(threadsStr, 10);

                m3u8.SetUrl(url);
                m3u8.SetFilename(downloadPath + "/" + name, downloadPath + "/tmp/" + name);
                m3u8.SetThreads(threads);
                m3u8.SetTimeout(timeout);
                startTimer();
                if (m3u8.LoadList().isEmpty()) {
                    String[] urls = m3u8.GetListUrls();
                    if (urls == null || urls.length == 0)
                        m3u8.Load();
                    else {
                        Intent intent = new Intent(MainActivity.this, ChangeListActivity.class);
                        startActivityForResult(intent, 1202);
                    }
                }
                enableStartButton(true);
            }
        });
        th.start();
    }

    public void stopBtnClick(View view) {
        if (m3u8 != null) {
            m3u8.Stop();
            stopTimer();
            Toast.makeText(this, R.string.status_stoped, Toast.LENGTH_SHORT).show();
        }
    }

    public void urlCleanBtnClick(View view) {
        ((EditText) findViewById(R.id.editTextUrl)).setText("");
        ((EditText) findViewById(R.id.editTextFileName)).setText("");
    }

    private void startTimer() {
        if (m3u8 != null && updateTimeTask == null) {
            Timer statusTimer = new Timer();
            updateTimeTask = new UpdateTimeTask(statusTimer);
            statusTimer.schedule(updateTimeTask, 0, 200);
        }
    }

    private void enableStartButton(final boolean val) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View btn = findViewById(R.id.buttonStart);
                btn.setEnabled(val);
                btn.invalidate();
                btn.refreshDrawableState();
            }
        });
    }

    private void stopTimer() {
        if (updateTimeTask != null) {
            updateTimeTask.timer.cancel();
            updateTimeTask.timer.purge();
            updateTimeTask = null;
        }
    }

    private void showStatus(final String st) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textViewStatus = ((TextView) findViewById(R.id.textViewStatus));
                textViewStatus.setText(st);
                textViewStatus.invalidate();
                textViewStatus.refreshDrawableState();
            }
        });
    }

    private void showStatus(int st) {
        showStatus(getString(st));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        if (requestCode == 1202) {
            boolean[] checkeds = data.getBooleanArrayExtra("UrlsChecked");

            m3u8.SetListUrl(-1);
            boolean isSelect = false;
            for (int i = 0; i < checkeds.length; i++)
                if (checkeds[i]) {
                    m3u8.SetListUrl(i);
                    isSelect = true;
                }
            if (isSelect)
                m3u8.Load();
            else
                m3u8.Stop();
        }
    }

    class UpdateTimeTask extends TimerTask {
        private Timer timer;

        UpdateTimeTask(Timer timer) {
            this.timer = timer;
        }

        public void run() {
            if (m3u8 != null) {
                showStatus(m3u8.GetStatus());
            } else {
                timer.cancel();
                timer.purge();
                updateTimeTask = null;
            }
        }
    }
}
