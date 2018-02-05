package ru.yourok.m3u8loader.activitys

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
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

        val edit = findViewById<TextView>(R.id.editTextReport)
        edit.setText(intent.getStringExtra("report"))
        edit.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("report", edit.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Copy to clipboard", Toast.LENGTH_SHORT).show()
        }
        val btnReport = findViewById<Button>(R.id.buttonSendReport)
        btnReport.setOnClickListener {
            ACR.sendErrorMail(this, edit.text.toString())
            finish()
        }
        Toast.makeText(this, R.string.crash_app, Toast.LENGTH_LONG).show()
    }
}