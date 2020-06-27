package com.example.capture;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyProvider extends ContentProvider {
    private static final String myURI ="content://com.example.provider.capture/items";
    public static final Uri CONTENT_URI = Uri.parse(myURI);

    @Override
    public boolean onCreate() {
        // TODO: Construct the underlying database.
        return true;
    }

// Create the constants used to differentiate between the different
// URI requests.
    private static final int ALLROWS = 1;
    private static final int SINGLE_ROW = 2;
    private static UriMatcher uriMatcher;
// Populate the UriMatcher object, where a URI ending in ‘items’ will
// correspond to a request for all items, and ‘items/[rowID]’
// represents a single row.
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("com.example.provider.capture", "items", ALLROWS);
        uriMatcher.addURI("com.example.provider.capture", "items/#", SINGLE_ROW);
    }


    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        // If this is a row query, limit the result set to the passed in row.
        switch (uriMatcher.match(uri)) {
            case SINGLE_ROW :
                // TODO: Modify selection based on row id, where:
                // rowNumber = uri.getPathSegments().get(1));
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ALLROWS: return "vnd.example.cursor.dir/myprovidercontent";
            case SINGLE_ROW: return "vnd.example.cursor.item/myprovidercontent";
            default: throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        long rowID = 1;//[ ... Add a new item ... ]

        // Return a URI to the newly added item.
        if (rowID > 0) {
            return ContentUris.withAppendedId(CONTENT_URI, rowID);
        }
        throw new SQLException("Failed to add new item into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case ALLROWS:
            case SINGLE_ROW:
            default:
                throw new IllegalArgumentException("Unsupported URI:" + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case ALLROWS:
            case SINGLE_ROW:
            default: throw new IllegalArgumentException("Unsupported URI:" + uri);
        }

    }
}
