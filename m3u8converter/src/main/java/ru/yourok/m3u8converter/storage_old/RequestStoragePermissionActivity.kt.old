package ru.yourok.m3u8converter.storage

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import ru.yourok.m3u8converter.utils.Preferences
import java.io.File

/**
 * Created by yourok on 28.11.17.
 */
class RequestStoragePermissionActivity : AppCompatActivity() {

    companion object {
        fun needRequest(file: String): Boolean {
            return (!File(file).canWrite() && Preferences.get(Preferences.DocumentRootUri, "") as String == "")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if ((Preferences.get(Preferences.DocumentRootUri, "") as String).isNotEmpty()) {
            finish()
            return
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), 555)
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != 555 || resultCode != Activity.RESULT_OK || data == null) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        val treeUri = data.data
        grantUriPermission(packageName, treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            contentResolver.takePersistableUriPermission(treeUri!!, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        Preferences.set(Preferences.DocumentRootUri, treeUri.toString())
        setResult(Activity.RESULT_OK)
        finish()
    }
}
