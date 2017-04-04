package ru.yourok.m3u8loader;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import dwl.Settings;
import ru.yourok.loader.Manager;
import ru.yourok.loader.Store;
import ru.yourok.m3u8loaderbeta.utils.ThemeChanger;

public class SettingsActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 1202;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeChanger.SetTheme(this);
        setContentView(R.layout.activity_settings);

        try {
            PackageInfo pInfo;
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            ((TextView) findViewById(R.id.textViewVersion)).setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.themes_names));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = ((Spinner) findViewById(R.id.spinnerChooseTheme));
        spinner.setAdapter(adapter);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.players_names));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner = ((Spinner) findViewById(R.id.spinnerChoosePlayer));
        spinner.setAdapter(adapter);

        loadSettings();
    }

    private void loadSettings() {
        Settings sets = Manager.GetSettings();
        ((EditText) findViewById(R.id.editTextDirectoryPath)).setText(Store.getDownloadPath());
        ((EditText) findViewById(R.id.editTextThreads)).setText(String.valueOf(sets.getThreads()));
        ((EditText) findViewById(R.id.editTextRepeatError)).setText(String.valueOf(sets.getErrorRepeat()));
        ((EditText) findViewById(R.id.editTextCookies)).setText(sets.getCookies());
        ((EditText) findViewById(R.id.editTextUseragent)).setText(sets.getUseragent());
        ((Spinner) findViewById(R.id.spinnerChooseTheme)).setSelection(Integer.parseInt(Store.getTheme(this)));
        ((Spinner) findViewById(R.id.spinnerChoosePlayer)).setSelection(Integer.parseInt(Store.getPlayer(this)));
    }

    private void saveSettings() {
        String dirout = ((EditText) findViewById(R.id.editTextDirectoryPath)).getText().toString();
        String threads = ((EditText) findViewById(R.id.editTextThreads)).getText().toString();
        String errRepeat = ((EditText) findViewById(R.id.editTextRepeatError)).getText().toString();
        String useragent = ((EditText) findViewById(R.id.editTextUseragent)).getText().toString();
        String cookies = ((EditText) findViewById(R.id.editTextCookies)).getText().toString();
        int theme = ((Spinner) findViewById(R.id.spinnerChooseTheme)).getSelectedItemPosition();
        int player = ((Spinner) findViewById(R.id.spinnerChoosePlayer)).getSelectedItemPosition();
        Manager.SetSettingsDownloadPath(dirout);
        Manager.SetSettingsThreads(Integer.parseInt(threads));
        Manager.SetSettingsErrorRepeat(Integer.parseInt(errRepeat));
        Manager.SetSettingsUseragent(useragent);
        Manager.SetSettingsCookies(cookies);
        Manager.SaveSettings();
        Store.setTheme(this, String.valueOf(theme));
        Store.setPlayer(this, String.valueOf(player));
    }

    public void okBtnClick(View view) {
        String lastTheme = Store.getTheme(this);
        saveSettings();
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        if (!lastTheme.equals(Store.getTheme(this)))
            intent.putExtra("recreate", true);
        else
            intent.putExtra("recreate", false);
        finish();
    }

    public void cancelBtnClick(View view) {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    public void srchBtnClick(View view) {
        Intent intent = new Intent(this, DirectoryListActivity.class);
        startActivityForResult(intent, 1202);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        String name = data.getStringExtra("filename");
        if (name.equals("/")) {
            Toast.makeText(this, getText(R.string.error) + " wrong directory", Toast.LENGTH_SHORT).show();
            return;
        }

        if (requestCode == 1202)
            ((EditText) findViewById(R.id.editTextDirectoryPath)).setText(name);
    }

    public void defOptions(View view) {
        ((EditText) findViewById(R.id.editTextDirectoryPath)).setText(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
        ((EditText) findViewById(R.id.editTextThreads)).setText("30");
        ((EditText) findViewById(R.id.editTextRepeatError)).setText("5");
        ((EditText) findViewById(R.id.editTextUseragent)).setText("DWL/1.0.0 (linux)");
        ((EditText) findViewById(R.id.editTextCookies)).setText("");
        ((Spinner) findViewById(R.id.spinnerChooseTheme)).setSelection(0);
        ((Spinner) findViewById(R.id.spinnerChoosePlayer)).setSelection(0);
    }
}
