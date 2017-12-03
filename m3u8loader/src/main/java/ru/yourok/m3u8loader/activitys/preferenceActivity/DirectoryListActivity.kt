package ru.yourok.m3u8loader.activitys.preferenceActivity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.provider.DocumentFile
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_directory_list.*
import ru.yourok.dwl.storage.RequestStoragePermissionActivity
import ru.yourok.dwl.storage.Storage
import ru.yourok.dwl.utils.Utils
import ru.yourok.m3u8loader.R
import ru.yourok.m3u8loader.theme.Theme
import java.io.File
import java.util.*
import kotlin.concurrent.schedule


class DirectoryActivity : AppCompatActivity() {

    private lateinit var DirectoryPath: File
    private var listAdapter: DirsAdapter = DirsAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Theme.set(this)
        setContentView(R.layout.activity_directory_list)

        var pathFile: File? = File(intent?.data?.toString())
        if (pathFile == null) {
            Toast.makeText(this, getString(R.string.error_directory_not_found) + ": " + intent?.data?.toString(), Toast.LENGTH_SHORT).show()
            if (Storage.getRoots().isNotEmpty())
                pathFile = File(Storage.getPath(Storage.getRoots()[0]))
        }
        if (pathFile == null) {
            finish()
            return
        }
        DirectoryPath = pathFile

        val listView = findViewById<ListView>(R.id.directoryList)
        listView.adapter = listAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val file = adapterView.getItemAtPosition(i) as File
            DirectoryPath = file
            updateViews()
        }
        updateViews()
    }

    fun upBtnClick(view: View) {
        if (DirectoryPath.parentFile == null)
            return
        DirectoryPath = DirectoryPath.parentFile ?: return
        updateViews()
    }

    fun homeBtnClick(view: View) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle(R.string.selected_folder_label)
        val adapter = HomeDirsAdapter(this)
        dialogBuilder.setAdapter(adapter) { dialog, item ->
            val selDir = adapter.getItem(item) as DocumentFile?
            if (selDir != null) {
                DirectoryPath = File(Storage.getPath(selDir))
                updateViews()
            }
        }
        val alertDialogObject = dialogBuilder.create()
        alertDialogObject.show()
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
            val doc = Storage.getDocument(DirectoryPath.canonicalPath)
            if (doc.canWrite()) {
                if (doc.createDirectory(name) != null)
                    DirectoryPath = File(DirectoryPath, name)
            } else
                Toast.makeText(this@DirectoryActivity, R.string.error_create_folder, Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            updateViews()
        }
        alertDialog.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
        alertDialog.create().show()
    }

    fun confirmBtnClick(view: View) {
        DirectoryPath.let {
            if (!it.canWrite()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val doc = Storage.getDocument(DirectoryPath.canonicalPath)
                    if (!doc.canWrite()) {
                        Snackbar.make(findViewById(R.id.directory_activity_layout), R.string.permission_request_access, Snackbar.LENGTH_INDEFINITE)
                                .setAction(R.string.permission_btn) {
                                    startActivity(Intent(this, RequestStoragePermissionActivity::class.java))
                                }
                                .show()
                    }
                } else {
                    Toast.makeText(this, R.string.error_directory_permission, Toast.LENGTH_SHORT).show()
                    return
                }
            }
            val intent = Intent()
            intent.putExtra("filename", it.canonicalPath)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    fun cancelBtnClick(view: View) {
        finish()
    }

    fun updateViews() {
        txtvSelectedFolder.text = DirectoryPath.canonicalPath
        directoryList.invalidateViews()
        if (!DirectoryPath.canWrite()) {
            val doc = Storage.getDocument(DirectoryPath.canonicalPath)
            if (!doc.canWrite()) {
                showStatus(getText(R.string.error_directory_permission).toString())
                return
            }
        }
        showStatus("")
    }

    var countTimer = 0
    fun showStatus(text: String) {
        runOnUiThread {
            if (text.isEmpty()) {
                textViewDirectoryStatus.visibility = View.GONE
                return@runOnUiThread
            }
            textViewDirectoryStatus.visibility = View.VISIBLE
            textViewDirectoryStatus.text = text
            countTimer++
            Timer().schedule(5000) {
                countTimer--
                if (countTimer == 0)
                    runOnUiThread { textViewDirectoryStatus.visibility = View.GONE }
            }
        }
    }

    internal inner class DirsAdapter(private val context: Context) : BaseAdapter() {
        private var files: List<File> = listOf()

        override fun getCount(): Int {
            try {
                files = DirectoryPath.listFiles()?.filter {
                    it.isDirectory()
                } ?: listOf()

                if (files.isEmpty()) {
                    val doc = Storage.getDocument(DirectoryPath.canonicalPath)
                    files = doc.listFiles()?.filter { it.isDirectory }?.map { File(Storage.getPath(it)) } ?: listOf()
                }

                files = files.sortedWith(Comparator { file1, file2 ->
                    var f1 = file1.canonicalPath.toLowerCase()
                    var f2 = file2.canonicalPath.toLowerCase()
                    if (f1.startsWith("."))
                        f1 = f1.substring(1)
                    if (f2.startsWith("."))
                        f2 = f2.substring(1)
                    f1.compareTo(f2)
                })
                return files.size
            } catch (e: Exception) {
                e.printStackTrace()
                return 0
            }
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

        internal var list: List<DocumentFile> = Storage.getRoots().map { it }

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
                (view.findViewById<TextView>(android.R.id.text1))?.text = Storage.getPath(list[position])
                val freeSpace = Storage.getSpace(list[position], false)
                val totalSpace = Storage.getSpace(list[position], true)
                val Space = Utils.byteFmt(freeSpace) + "/" + Utils.byteFmt(totalSpace)
                (view.findViewById<TextView>(android.R.id.text2))?.setText(Space)
            }
            return view
        }
    }
}
