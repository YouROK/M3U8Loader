package ru.yourok.m3u8loader;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;

import dwl.LoaderInfo;
import ru.yourok.loader.Manager;
import ru.yourok.m3u8loader.utils.ThemeChanger;

public class EditLoaderActivity extends AppCompatActivity {

    private int Index = -1;
    private LoaderInfo info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeChanger.SetTheme(this);
        setContentView(R.layout.activity_edit_loader);

        Intent intent = getIntent();
        Index = intent.getIntExtra("Index", -1);
        if (Index == -1)
            finish();

        info = Manager.GetLoaderInfo(Index);
        if (info == null)
            finish();

        final SeekBar left = (SeekBar) findViewById(R.id.rangeSeekbarLeft);
        final SeekBar right = (SeekBar) findViewById(R.id.rangeSeekbarRight);
        if (left == null || right == null)
            finish();
        left.setMax((int) info.getAll() - 1);
        right.setMax((int) info.getAll() - 1);

        left.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (progress >= right.getProgress() - 1)
                        left.setProgress(right.getProgress() - 1);
                }
                int start = left.getProgress();
                int end = right.getProgress();
                ((TextView) findViewById(R.id.textViewItemsInfo)).setText(start + " ->   <- " + end + " / " + (info.getAll() - 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        right.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (progress <= left.getProgress() + 1)
                        right.setProgress(left.getProgress() + 1);
                }
                int start = left.getProgress();
                int end = right.getProgress();
                ((TextView) findViewById(R.id.textViewItemsInfo)).setText(start + " ->   <- " + end + " / " + (info.getAll() - 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        refresh();
    }

    private void refresh() {
        ((TextView) findViewById(R.id.textViewUrl)).setText(info.getUrl());
        ((TextView) findViewById(R.id.textViewName)).setText(info.getName());
        ((EditText) findViewById(R.id.editTextUseragent)).setText(Manager.GetLoaderUseragent(Index));
        ((EditText) findViewById(R.id.editTextCookies)).setText(Manager.GetLoaderCookies(Index));
        SeekBar left = (SeekBar) findViewById(R.id.rangeSeekbarLeft);
        SeekBar right = (SeekBar) findViewById(R.id.rangeSeekbarRight);
        int start = Manager.GetLoaderRangeFrom(Index);
        int end = Manager.GetLoaderRangeTo(Index);
        left.setProgress(start);
        right.setProgress(end);
        ((TextView) findViewById(R.id.textViewItemsInfo)).setText(start + " ->   <- " + end + " / " + (info.getAll() - 1));
    }

    public void onRefreshClick(View view) {
        refresh();
    }

    public void onClearClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.warn_clean_message);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Manager.Clean(Index);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refresh();
                    }
                });
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }

    public void okBtnClick(View view) {
        final String userAgent = ((EditText) findViewById(R.id.editTextUseragent)).getText().toString();
        final String cookies = ((EditText) findViewById(R.id.editTextCookies)).getText().toString();
        final int start = ((SeekBar) findViewById(R.id.rangeSeekbarLeft)).getProgress();
        final int end = ((SeekBar) findViewById(R.id.rangeSeekbarRight)).getProgress();

        String fn = Manager.GetFileName(Index);
        boolean isExist = new File(fn).exists();

        if (start != Manager.GetLoaderRangeFrom(Index) && isExist) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.range_changed);
            builder.setMessage(R.string.set_range_and_clean);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Manager.SetLoaderUseragent(Index, userAgent);
                    Manager.SetLoaderCookies(Index, cookies);
                    Manager.SetLoaderRange(Index, start, end);
                    Manager.Clean(Index);
                    finish();
                }
            });
            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int oldStart = Manager.GetLoaderRangeFrom(Index);
                    ((SeekBar) findViewById(R.id.rangeSeekbarLeft)).setProgress(oldStart);
                }
            });
            builder.create().show();
        } else {
            Manager.SetLoaderUseragent(Index, userAgent);
            Manager.SetLoaderCookies(Index, cookies);
            Manager.SetLoaderRange(Index, start, end);
            finish();
        }
    }

    public void cancelBtnClick(View view) {
        finish();
    }
}
