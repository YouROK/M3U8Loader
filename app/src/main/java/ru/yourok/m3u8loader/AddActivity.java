package ru.yourok.m3u8loader;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AddActivity extends AppCompatActivity {

    private EditText urlEdit;
    private EditText fileEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        urlEdit = (EditText) findViewById(R.id.editTextUrl);
        fileEdit = (EditText) findViewById(R.id.editTextFileName);
        fileEdit.requestFocus();
        Intent intent = getIntent();
        if (intent.getAction().equals(Intent.ACTION_SEND) && intent.getStringExtra(Intent.EXTRA_TEXT) != null)
            urlEdit.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
        if (intent.getData() != null)
            urlEdit.setText(intent.getDataString());
    }

    public void okBtnClick(View view) {
        if (urlEdit.getText().toString().isEmpty()) {
            Toast.makeText(this, R.string.error_url_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        if (fileEdit.getText().toString().isEmpty()) {
            Toast.makeText(this, R.string.error_filename_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent startIntent = new Intent(this, MainActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startIntent.setData(Uri.parse(urlEdit.getText().toString()));
        startIntent.putExtra("filename", fileEdit.getText().toString());
        this.startActivity(startIntent);

        finish();
    }

    public void cancelBtnClick(View view) {
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("UrlString", urlEdit.getText().toString());
        outState.putString("FileNameString", fileEdit.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        urlEdit.setText(savedInstanceState.getString("UrlString"));
        fileEdit.setText(savedInstanceState.getString("FileNameString"));
    }
}
