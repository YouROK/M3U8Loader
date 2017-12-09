package ru.yourok.m3u8loader.activitys.editorActivity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_editor.*
import ru.yourok.dwl.list.List
import ru.yourok.dwl.utils.Saver
import ru.yourok.m3u8loader.R
import ru.yourok.m3u8loader.theme.Theme

/**
 * Created by yourok on 01.12.17.
 */
class EditorActivity : AppCompatActivity() {

    companion object {
        var editorList = mutableListOf<List>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Theme.set(this)
        setContentView(R.layout.activity_editor)
        listViewEditor.adapter = EditorAdaptor(editorList, this)

        buttonOk.setOnClickListener {
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        editorList.forEach {
            Saver.saveList(it)
        }
    }
}