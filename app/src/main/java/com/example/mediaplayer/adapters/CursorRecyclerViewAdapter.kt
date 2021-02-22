package com.example.mediaplayer.adapters

import android.content.Context
import android.database.Cursor
import android.database.DataSetObserver
import androidx.recyclerview.widget.RecyclerView

abstract class CursorRecyclerViewAdapter<VH : RecyclerView.ViewHolder?> : RecyclerView.Adapter<VH> {
    private var mContext: Context? = null
    private var mCursor: Cursor? = null
    private var mDataValid: Boolean = false
    private var mRowIdColumn: Int = 0
    private var mDataSetObserver: DataSetObserver? = null

    constructor(context: Context?, cursor: Cursor?) {
        mContext = context
        mCursor = cursor
        mDataValid = cursor != null
        mRowIdColumn = if (mDataValid) mCursor!!.getColumnIndex("_id") else -1
        mDataSetObserver = NotifyingDataSetObserver()
        if (mCursor != null) {
            mCursor!!.registerDataSetObserver(mDataSetObserver)
        }
    }

    protected constructor() {}

    fun getCursor(): Cursor? {
        return mCursor
    }

    public override fun getItemCount(): Int {
        if (mDataValid && mCursor != null) {
            return mCursor!!.count
        }
        return 0
    }

    public override fun getItemId(position: Int): Long {
        if (mDataValid && (mCursor != null) && mCursor!!.moveToPosition(position)) {
            return mCursor!!.getLong(mRowIdColumn)
        }
        return 0
    }

    public override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(true)
    }

    abstract fun onBindViewHolder(viewHolder: VH, cursor: Cursor?)
    public override fun onBindViewHolder(viewHolder: VH, position: Int) {
        if (!mDataValid) {
            throw IllegalStateException("this should only be called when the cursor is valid")
        }
        if (!mCursor!!.moveToPosition(position)) {
            throw IllegalStateException("couldn't move cursor to position " + position)
        }
        onBindViewHolder(viewHolder, mCursor)
    }

    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.
     */
    fun changeCursor(cursor: Cursor) {
        swapCursor(cursor)?.close()
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.  Unlike
     * [.changeCursor], the returned old Cursor is *not*
     * closed.
     */
    fun swapCursor(newCursor: Cursor?): Cursor? {
        if (newCursor === mCursor) {
            return null
        }
        val oldCursor: Cursor? = mCursor
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver)
        }
        mCursor = newCursor
        if (mCursor != null) {
            if (mDataSetObserver != null) {
                mCursor!!.registerDataSetObserver(mDataSetObserver)
            }
            mRowIdColumn = newCursor!!.getColumnIndexOrThrow("_id")
            mDataValid = true
            notifyDataSetChanged()
        } else {
            mRowIdColumn = -1
            mDataValid = false
            notifyDataSetChanged()
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
        return oldCursor
    }

    private inner class NotifyingDataSetObserver constructor() : DataSetObserver() {
        public override fun onChanged() {
            super.onChanged()
            mDataValid = true
            notifyDataSetChanged()
        }

        public override fun onInvalidated() {
            super.onInvalidated()
            mDataValid = false
            notifyDataSetChanged()
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
    }
}