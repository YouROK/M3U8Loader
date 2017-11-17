package ru.yourok.m3u8loader

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import ru.yourok.m3u8loader.theme.Theme

class AddListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Theme.set(this)
        setContentView(R.layout.activity_add_list)

    }

    fun addBtnClick(view: View) {
    }

    fun downloadBtnClick(view: View) {
    }

    fun cancelBtnClick(view: View) {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    fun cleanFileName(file: String): String {
        val ReservedCharsReg = "[|\\\\?*<\\\":>+/']"
        var ret = file.replace(ReservedCharsReg.toRegex(), "_").replace("_+".toRegex(), "_")
        ret = ret.trim { it <= ' ' }
        return ret
    }
}