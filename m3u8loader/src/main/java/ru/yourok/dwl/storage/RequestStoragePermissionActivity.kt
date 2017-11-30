package ru.yourok.dwl.storage

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import ru.yourok.dwl.settings.Preferences


/**
 * Created by yourok on 25.11.17.
 */
class RequestStoragePermissionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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