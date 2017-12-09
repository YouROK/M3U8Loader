package ru.yourok.dwl.manager

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import ru.yourok.dwl.list.List
import ru.yourok.m3u8loader.App
import ru.yourok.m3u8loader.R

/**
 * Created by yourok on 19.11.17.
 */
object Notifyer {
    var error: String = ""

    fun toastEnd(list: List, complete: Boolean, err: String) {
        error = err
        with(App.getContext()) {
            Handler(Looper.getMainLooper()).post {
                if (complete && err.isEmpty()) {
                    Toast.makeText(this, this.getText(R.string.complete).toString() + ": " + list.title, Toast.LENGTH_SHORT).show()
                } else if (!err.isEmpty()) {
                    Toast.makeText(this, this.getText(R.string.error).toString() + ": " + list.title + ", " + err, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}