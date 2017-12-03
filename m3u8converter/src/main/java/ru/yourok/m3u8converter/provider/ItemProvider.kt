package ru.yourok.m3u8converter.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import ru.yourok.m3u8converter.BuildConfig
import ru.yourok.m3u8converter.converter.ConvertItem
import ru.yourok.m3u8converter.converter.Manager


/**
 * Created by yourok on 30.11.17.
 */

class ItemProvider : ContentProvider() {
    companion object {
        val PROVIDER_NAME = BuildConfig.APPLICATION_ID + ".provider.items"
        val CONTENT_URI = Uri.parse("content://$PROVIDER_NAME/items")
        val ITEMS = 1
        val ITEMS_ID = 2
    }

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    init {
        uriMatcher.addURI(PROVIDER_NAME, "items", ITEMS)
        uriMatcher.addURI(PROVIDER_NAME, "items/#", ITEMS_ID)
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        if (uriMatcher.match(uri) != ITEMS)
            throw IllegalArgumentException("Wrong URI: " + uri)

        val cursor = MatrixCursor(arrayOf("name", "path"))
        Manager.execList { list ->
            list.forEach {
                cursor.addRow(arrayOf(it.name, it.path))
            }
        }

        cursor.setNotificationUri(context!!.contentResolver, CONTENT_URI)
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (uriMatcher.match(uri) != ITEMS)
            throw IllegalArgumentException("Wrong URI: " + uri)

        val item = ConvertItem()
        item.name = values?.get("name")?.toString() ?: ""
        item.path = values?.get("path")?.toString() ?: ""

        if (item.name.isEmpty() || item.path.isEmpty())
            throw IllegalArgumentException("Empty value: " + uri)

        val id = Manager.add(item)
        val resultUri = ContentUris.withAppendedId(CONTENT_URI, id.toLong())
        context.contentResolver.notifyChange(resultUri, null)
        return resultUri
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun getType(uri: Uri?): String {
        when (uriMatcher.match(uri)) {
            ITEMS -> return "vnd.android.cursor.dir/vnd.com.${PROVIDER_NAME}"
            ITEMS_ID -> return "vnd.android.cursor.item/vnd.com.${PROVIDER_NAME}"
        }
        return ""
    }
}