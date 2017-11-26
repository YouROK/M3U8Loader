package ru.yourok.m3u8loader.activitys.preferenceActivity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_directory_list.*
import ru.yourok.dwl.settings.Settings
import ru.yourok.dwl.storage.Document
import ru.yourok.dwl.storage.Storage
import ru.yourok.dwl.utils.Utils
import ru.yourok.m3u8loader.R
import ru.yourok.m3u8loader.theme.Theme
import java.io.File
import java.util.*

class DirectoryActivity : AppCompatActivity() {

    private var DirectoryPath: File = File(Settings.downloadPath)
    private var listAdapter: DirsAdapter = DirsAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Theme.set(this)
        setContentView(R.layout.activity_directory_list)

        Storage.getListDirs().forEach {
            if (it.startsWith(DirectoryPath.canonicalPath)) {
                DirectoryPath = File(it)
                return@forEach
            }
        }

        if (!DirectoryPath.exists()) {
            Toast.makeText(this, R.string.error_directory_not_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val listView = findViewById<ListView>(R.id.directoryList)
        listView.adapter = listAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val file = adapterView.getItemAtPosition(i) as File
            val doc = Document.getFile(file.path)
            if ((doc != null && !doc.canWrite()) || !file.canWrite()) {
                Toast.makeText(this, R.string.error_directory_permission, Toast.LENGTH_SHORT).show()
                return@OnItemClickListener
            }
            DirectoryPath = file
            updateViews()
        }
        updateViews()
    }

    fun upBtnClick(view: View) {
        val roots = Storage.getListDirs()
        roots.forEach {
            if (DirectoryPath.canonicalPath == File(it).canonicalPath)
                return
        }
        DirectoryPath = DirectoryPath.parentFile
        updateViews()
    }

    fun homeBtnClick(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setTitle(R.string.selected_folder_label)
            val adapter = HomeDirsAdapter(this)
            dialogBuilder.setAdapter(adapter) { dialog, item ->
                val newDir = adapter.getItem(item) as File?
                if (newDir != null) {
                    DirectoryPath = newDir
                    updateViews()
                }
            }
            val alertDialogObject = dialogBuilder.create()
            alertDialogObject.show()
        }
    }

    fun createDirBtnClick(view: View) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(R.string.new_directory_title)
        alertDialog.setMessage(R.string.new_directory_name_label)

        val input = EditText(this)
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        input.layoutParams = lp
        alertDialog.setView(input)
        alertDialog.setIcon(R.drawable.ic_create_new_folder_black_24dp)

        alertDialog.setPositiveButton(android.R.string.ok) { dialog, which ->
            val name = input.text.toString()
            if (!File(DirectoryPath.absolutePath + "/" + name).mkdir())
                Toast.makeText(this@DirectoryActivity, R.string.error_create_folder, Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            updateViews()
        }
        alertDialog.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
        alertDialog.create().show()
    }

    fun confirmBtnClick(view: View) {
        val intent = Intent()
        intent.putExtra("filename", DirectoryPath.absolutePath)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    fun cancelBtnClick(view: View) {
        finish()
    }

    fun updateViews() {
        txtvSelectedFolder.text = DirectoryPath.absolutePath
        directoryList.invalidateViews()
    }

    internal inner class DirsAdapter(private val context: Context) : BaseAdapter() {
        private var files: Array<File> = arrayOf()

        override fun getCount(): Int {
            files = DirectoryPath.listFiles { pathname -> pathname.isDirectory }
            files.sortWith(Comparator { file1, file2 ->
                var f1 = file1.name.toLowerCase()
                var f2 = file2.name.toLowerCase()
                if (f1.startsWith("."))
                    f1 = f1.substring(1)
                if (f2.startsWith("."))
                    f2 = f2.substring(1)
                f1.compareTo(f2)
            })
            return files.size
        }

        override fun getItem(position: Int): Any? {
            if (position in 0 until files.size)
                return files[position]
            return null
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View = convertView ?: (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(android.R.layout.simple_list_item_1, null)
            val text1 = view.findViewById<TextView>(android.R.id.text1)
            if (position in 0 until files.size)
                text1.text = files[position].name
            return view
        }
    }

    internal inner class HomeDirsAdapter(internal var context: Context) : BaseAdapter() {

        internal var list: List<File> = Storage.getListDirs().map { File(it) }

        override fun getCount(): Int {
            return list.size
        }

        override fun getItem(position: Int): Any? {
            if (position in 0 until list.size)
                return list[position]
            return null
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View = convertView ?: (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(android.R.layout.two_line_list_item, null)

            if (convertView == null) {
                val paddingPixel = 15
                val density = context.resources.displayMetrics.density
                val paddingDp = (paddingPixel * density).toInt()
                view.setPadding(paddingDp, 0, 0, paddingDp)
            }

            if (position in 0 until list.size) {
                (view.findViewById<TextView>(android.R.id.text1))?.text = list[position].absolutePath
                val Space = Utils.byteFmt(list[position].freeSpace) + "/" + Utils.byteFmt(list[position].totalSpace)
                (view.findViewById<TextView>(android.R.id.text2))?.setText(Space)
            }
            return view
        }
    }
}
