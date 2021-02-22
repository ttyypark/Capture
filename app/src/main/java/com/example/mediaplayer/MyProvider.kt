package com.example.mediaplayer

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.net.Uri

class MyProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        // TODO: Construct the underlying database.
        return true
    }

    companion object {
        private val myURI: String = "content://com.example.provider.capture/items"
        val CONTENT_URI: Uri = Uri.parse(myURI)

        // Create the constants used to differentiate between the different
        // URI requests.
        private val ALLROWS: Int = 1
        private val SINGLE_ROW: Int = 2
        private var uriMatcher: UriMatcher? = null

        // Populate the UriMatcher object, where a URI ending in ‘items’ will
        // correspond to a request for all items, and ‘items/[rowID]’
        // represents a single row.
        init {
            uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
            uriMatcher!!.addURI("com.example.provider.capture", "items", ALLROWS)
            uriMatcher!!.addURI("com.example.provider.capture", "items/#", SINGLE_ROW)
        }
    }

    public override fun query(uri: Uri, projection: Array<String>?,
                              selection: String?, selectionArgs: Array<String>?,
                              sortOrder: String?): Cursor? {
        // If this is a row query, limit the result set to the passed in row.
        when (uriMatcher!!.match(uri)) {
            SINGLE_ROW -> {
            }
        }
        return null
    }

    public override fun getType(uri: Uri): String? {
        when (uriMatcher!!.match(uri)) {
            ALLROWS -> return "vnd.example.cursor.dir/myprovidercontent"
            SINGLE_ROW -> return "vnd.example.cursor.item/myprovidercontent"
            else -> throw IllegalArgumentException("Unsupported URI: " + uri)
        }
    }

    public override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val rowID: Long = 1 //[ ... Add a new item ... ]

        // Return a URI to the newly added item.
        if (rowID > 0) {
            return ContentUris.withAppendedId(CONTENT_URI, rowID)
        }
        throw SQLException("Failed to add new item into " + uri)
    }

    public override fun delete(uri: Uri, selection: String?,
                               selectionArgs: Array<String>?): Int {
        when (uriMatcher!!.match(uri)) {
            ALLROWS, SINGLE_ROW -> throw IllegalArgumentException("Unsupported URI:" + uri)
            else -> throw IllegalArgumentException("Unsupported URI:" + uri)
        }
    }

    public override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        when (uriMatcher!!.match(uri)) {
            ALLROWS, SINGLE_ROW -> throw IllegalArgumentException("Unsupported URI:" + uri)
            else -> throw IllegalArgumentException("Unsupported URI:" + uri)
        }
    }
}