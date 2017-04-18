package ru.yourok.m3u8loader;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import dwl.LoaderInfo;
import ru.yourok.loader.Loader;
import ru.yourok.loader.Manager;
import ru.yourok.m3u8loader.utils.ThemeChanger;

public class AddLoaderActivity extends AppCompatActivity {

    private EditText urlEdit;
    private EditText fileEdit;
    private EditText cookieEdit;
    private EditText useragentEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeChanger.SetTheme(this);
        setContentView(R.layout.activity_add_loader);
        findViewById(R.id.buttonCancel).requestFocus();
        findViewById(R.id.layout_settings).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.textViewError)).setText("");

        urlEdit = (EditText) findViewById(R.id.editTextUrl);
        fileEdit = (EditText) findViewById(R.id.editTextFileName);
        cookieEdit = (EditText) findViewById(R.id.editTextCookies);
        useragentEdit = (EditText) findViewById(R.id.editTextUseragent);
        Intent intent = getIntent();

        if (intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            for (String key : bundle.keySet()) {
                if (key.toLowerCase().contains("name") || key.toLowerCase().contains("title")) {
                    Object value = bundle.get(key);
                    if (value != null) {
                        String name = cleanFileName(value.toString().trim());
                        if (!name.isEmpty())
                            fileEdit.setText(name);
                    }
                }
                if (key.toLowerCase().contains("cookie")) {
                    Object value = bundle.get(key);
                    if (value != null)
                        cookieEdit.setText(value.toString().trim());
                }
                if (key.toLowerCase().contains("useragent")) {
                    Object value = bundle.get(key);
                    if (value != null)
                        useragentEdit.setText(value.toString().trim());
                }
            }
        }

        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEND)) {
            if (intent.getStringExtra(Intent.EXTRA_TEXT) != null)
                urlEdit.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
            if (intent.getExtras().get(Intent.EXTRA_STREAM) != null)
                urlEdit.setText(intent.getExtras().get(Intent.EXTRA_STREAM).toString());
        }

        if (intent.getData() != null)
            urlEdit.setText(intent.getDataString());

        if (intent.getExtras() == null && intent.getData() == null) {
            Toast.makeText(this, "Error: not found url", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public static String cleanFileName(String file) {
        final String ReservedCharsReg = "[|\\\\?*<\\\":>+/']";
        String ret = file.replaceAll(ReservedCharsReg, "_").replaceAll("_+", "_");
        ret = ret.trim();
        return ret;
    }

    public void addBtnClick(View view) {
        ((TextView) findViewById(R.id.textViewError)).setText("");
        new Thread(new Runnable() {
            @Override
            public void run() {
                addList(false);
            }
        }).start();
    }

    public void downloadBtnClick(View view) {
        ((TextView) findViewById(R.id.textViewError)).setText("");
        new Thread(new Runnable() {
            @Override
            public void run() {
                addList(true);
            }
        }).start();
    }

    private void toastMsg(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.textViewError)).setText(msg);
                Toast.makeText(AddLoaderActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toastRes(final int msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.textViewError)).setText(msg);
                Toast.makeText(AddLoaderActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void cancelBtnClick(View view) {
        finish();
    }

    private void waitManager(final boolean set) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (set)
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                else
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                findViewById(R.id.buttonAdd).setEnabled(!set);
                findViewById(R.id.buttonDownload).setEnabled(!set);
                findViewById(R.id.buttonCancel).setEnabled(!set);
            }
        });
    }

    private void addList(final boolean load) {
        final String Name = cleanFileName(fileEdit.getText().toString().trim());
        final String Url = urlEdit.getText().toString().trim();
        final String Cookies = cookieEdit.getText().toString().trim();
        final String UserAgent = useragentEdit.getText().toString().trim();
        if (Url.isEmpty()) {
            toastRes(R.string.error_empty_url);
            return;
        }

        if (Name.isEmpty()) {
            toastRes(R.string.error_empty_name);
            return;
        }

        for (int i = 0; i < Manager.Length(); i++) {
            LoaderInfo info = Manager.GetLoaderInfo(i);
            if (info == null) continue;
            String nameLoader = info.getName();
            String urlLoader = info.getUrl();

            if (urlLoader.equals(Url)) {
                toastRes(R.string.error_same_url);
                return;
            }
            if (nameLoader.equals(Name)) {
                changeUrlLoader();
                return;
            }
        }
        waitManager(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int oldLength = Manager.Length();
                String err = Manager.Add(Url, Name, Cookies, UserAgent);
                waitManager(false);
                if (!err.isEmpty()) {
                    toastMsg(AddLoaderActivity.this.getText(R.string.error) + ": " + err);
                    return;
                }

                if (load && oldLength != Manager.Length()) {
                    int start = Manager.Length() - (Manager.Length() - oldLength);
                    for (int i = start; i < Manager.Length(); i++)
                        Loader.Add(i);
                    Loader.Start();
                }
                AddLoaderActivity.this.finish();
            }
        }).start();
    }

    private void changeUrlLoader() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddLoaderActivity.this);
                builder.setTitle(R.string.replace_url);
                builder.setMessage(R.string.replace_url_msg);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String Name = cleanFileName(fileEdit.getText().toString().trim());
                        String Url = urlEdit.getText().toString().trim();
                        String Cookies = cookieEdit.getText().toString().trim();
                        String UserAgent = useragentEdit.getText().toString().trim();
                        final String err = Manager.SetLoaderUrl(Url, Name, Cookies, UserAgent);
                        if (!err.isEmpty()) {
                            toastMsg(AddLoaderActivity.this.getText(R.string.error_change_url) + ": " + err);
                            return;
                        }
                        AddLoaderActivity.this.finish();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.create().show();
            }
        });
    }

    public void onSettingsClick(final View view) {
        view.setEnabled(false);
        if (findViewById(R.id.layout_settings).getVisibility() == View.GONE) {
            RotateAnimation anim = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setDuration(500);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ((ImageButton) findViewById(R.id.buttonSettings)).setImageResource(R.drawable.ic_arrow_drop_up_black_24dp);
                    findViewById(R.id.layout_settings).setVisibility(View.VISIBLE);
                    view.setEnabled(true);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            findViewById(R.id.buttonSettings).startAnimation(anim);
        } else {
            RotateAnimation anim = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setDuration(500);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ((ImageButton) findViewById(R.id.buttonSettings)).setImageResource(R.drawable.ic_arrow_drop_down_black_24dp);
                    findViewById(R.id.layout_settings).setVisibility(View.GONE);
                    view.setEnabled(true);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            findViewById(R.id.buttonSettings).startAnimation(anim);
        }
    }
}
