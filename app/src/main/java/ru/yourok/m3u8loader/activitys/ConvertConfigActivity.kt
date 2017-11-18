package ru.yourok.m3u8loader.activitys

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_convert_config.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import ru.yourok.m3u8loader.R


class ConvertConfigActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_convert_config)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            textViewCPUInfo.text = Build.SUPPORTED_ABIS.joinToString()
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            textViewCPUInfo.setText(Build.CPU_ABI + " " + Build.CPU_ABI)

        buttonOk.onClick { finish() }

        val spList = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, resources.getStringArray(R.array.ffmpeg_list))
        spinnerFFMpeg.setAdapter(spList)
        buttonDownloadFFMpeg.onClick {
            load(spinnerFFMpeg.selectedItemPosition)
        }
    }
    private fun load(index:Int){
        
    }
}
