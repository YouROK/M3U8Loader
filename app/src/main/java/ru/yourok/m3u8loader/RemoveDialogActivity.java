package ru.yourok.m3u8loader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import ru.yourok.loader.Loader;
import ru.yourok.loader.LoaderServiceHandler;
import ru.yourok.loader.Options;

public class RemoveDialogActivity extends AppCompatActivity {

    private int index = -1;
    private Loader loader;
    String[] names;

    public static final int REQUEST_CODE = 1203;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_dialog);

        Intent intent = getIntent();
        if (intent == null) {
            finishCancle();
            return;
        }
        index = intent.getIntExtra("index", -1);
        if (index < 0 || index >= LoaderServiceHandler.SizeLoaders()) {
            finishCancle();
            return;
        }

        loader = LoaderServiceHandler.GetLoader(index);
        if (loader == null) {
            finishCancle();
            return;
        }

        if (loader.GetList() != null) {
            names = loader.GetOutFiles();
            if (names == null || names.length == 0) {
                removeFromList(index);
                finishOK();
                return;
            }
            updateList();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (loader.LoadListOpts(RemoveDialogActivity.this).isEmpty())
                        names = loader.GetOutFiles();
                    if (names == null || names.length == 0) {
                        removeFromList(index);
                        finishOK();
                        return;
                    }
                    updateList();
                }
            }).start();
        }
    }

    private void updateList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loader != null)
                    ((TextView) findViewById(R.id.textViewDelete)).setText(getString(R.string.delete_label) + ": " + loader.GetName());
                findViewById(R.id.progressListLoad).setVisibility(View.GONE);
                String[] namesBase = new String[names.length];
                for (int i = 0; i < names.length; i++)
                    namesBase[i] = new File(names[i]).getName();

                ListView listView = (ListView) findViewById(R.id.listViewRemoveFiles);
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                ArrayAdapter<String> adapter = new ArrayAdapter(RemoveDialogActivity.this, android.R.layout.simple_list_item_multiple_choice, namesBase);
                listView.setAdapter(adapter);
                for (int i = 0; i < listView.getCount(); i++)
                    listView.setItemChecked(i, true);
            }
        });
    }

    public void removeBtnClick(View view) {
        removeFromList(index);
        finishOK();
    }

    public void deleteBtnClick(View view) {
        ListView listView = (ListView) findViewById(R.id.listViewRemoveFiles);
        SparseBooleanArray sbArray = listView.getCheckedItemPositions();
        boolean isRemoveList = true;
        for (int i = 0; i < names.length; i++)
            if (sbArray.valueAt(i))
                new File(names[i]).delete();
            else
                isRemoveList = false;
        if (isRemoveList)
            removeFromList(index);
        finishOK();
    }

    public void cancelBtnClick(View view) {
        finishCancle();
    }

    private void finishOK() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    private void finishCancle() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void removeFromList(int index) {
        final Loader loader = LoaderServiceHandler.GetLoader(index);
        if (loader == null)
            return;
        loader.Stop();
        loader.RemoveTemp();
        loader.RemoveList();
        LoaderServiceHandler.RemoveLoader(index);
        Options.getInstance(this).SaveList();
    }
}
