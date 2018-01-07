package ru.yourok.m3u8loader.activitys

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import ru.yourok.m3u8loader.ACR
import ru.yourok.m3u8loader.R

/**
 * Created by yourok on 07.01.18.
 */
class ACRActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acr)
        if (!intent.hasExtra("report")) {
            finish()
            return
        }

        val edit = findViewById<EditText>(R.id.editTextReport)
        edit.setText(intent.getStringExtra("report"))
        val btnReport = findViewById<Button>(R.id.buttonSendReport)
        btnReport.setOnClickListener {
            ACR.sendErrorMail(this, edit.text.toString())
            finish()
        }
        Toast.makeText(this, R.string.crash_app, Toast.LENGTH_LONG).show()
    }
}