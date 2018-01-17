package com.ziank.novelreader.database

/**
* Created by ziank on 2017/10/12.
* @copyright ziank.2018
*/

import android.database.Cursor

class CursorHelper(cursor: Cursor) {

    private var mCursor: Cursor

    init {
        this.mCursor = cursor
    }

    fun getString(columnName: String): String {
        return mCursor.getString(mCursor.getColumnIndexOrThrow(columnName))
    }

    fun getInt(columnName: String): Int {
        return mCursor.getInt(mCursor.getColumnIndexOrThrow(columnName))
    }

    fun getLong(columnName: String): Long {
        return mCursor.getLong(mCursor.getColumnIndexOrThrow(columnName))
    }

    fun getBoolean(columnName: String): Boolean {
        return mCursor.getInt(mCursor.getColumnIndexOrThrow(columnName)) != 0
    }

    fun getFloat(columnName: String): Float {
        return mCursor.getFloat(mCursor.getColumnIndexOrThrow(columnName))
    }

    fun moveToNext(): Boolean {
        return mCursor.moveToNext()
    }
}