package com.ziank.novelreader.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

/**
* Created by ziank on 2017/10/12.
* @copyright ziank.2018
*/

class DataBaseHelper(context: Context, dbName: String, factory:
SQLiteDatabase.CursorFactory?, version: Int) : SQLiteOpenHelper(context, dbName, factory, version) {

    interface BookTable : BaseColumns {
        companion object {
            const val TABLE_NAME = "book"

            const val BOOK_TITLE = "title"
            const val BOOK_AUTHOR = "author"
            const val BOOK_URL = "book_url"
            const val BOOK_UPDATE = "update_content"
            const val BOOK_COVER = "cover_url"
            const val BOOK_SUMMARY = "summary"
            const val BOOK_CODE = "book_id"
            const val BOOK_SOURCE = "source"

            const val CHAPTER_INDEX = "chapter_index"
            const val CHAPTER_COUNT = "chapter_count"
            const val READ_POS = "read_pos"
            const val HAS_UPDATE = "has_update"
            const val SORT_TIME = "sort_time"
            const val SORT_TIME_DEF = "sort_time LONG DEFAULT 0 NOT NULL"
            const val CHAPTER_COUNT_DEF = "chapter_count INT DEFAULT 0 NOT NULL"

            val CREATE_TABLE_SQL = CREATE_TABLE +
                    TABLE_NAME +
                    " ( " + BOOK_CODE + " VARCHAR(1024) PRIMARY KEY, " +
                    BOOK_TITLE + " TEXT NOT NULL, " +
                    BOOK_AUTHOR + " VARCHAR(128), " +
                    BOOK_URL + " VARCHAR(1024), " +
                    BOOK_UPDATE + " TEXT, " +
                    BOOK_COVER + " VARCHAR(1024), " +
                    BOOK_SUMMARY + " TEXT" + ", " +
                    BOOK_SOURCE + " VARCHAR(128), " +
                    CHAPTER_INDEX + " INT DEFAULT 0 NOT NULL, " +
                    CHAPTER_COUNT + " INT DEFAULT 0 NOT NULL, " +
                    READ_POS + " INT DEFAULT 0 NOT NULL, " +
                    HAS_UPDATE + " BOOLEAN DEFAULT FALSE NOT NULL, " +
                    SORT_TIME + " LONG DEFAULT 0 NOT NULL);"
            val DROP_TABLE_SQL = DROP_TABLE + TABLE_NAME
        }
    }

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        sqLiteDatabase.execSQL(BookTable.CREATE_TABLE_SQL)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, oldVersion: Int,
                           newVersion: Int) {
        if (oldVersion < 2) {
            sqLiteDatabase.execSQL("ALTER TABLE " + BookTable.TABLE_NAME +
                    " ADD " + BookTable.SORT_TIME_DEF)
        }
        if (oldVersion < 4) {
            sqLiteDatabase.execSQL("ALTER TABLE " + BookTable.TABLE_NAME +
                    " ADD " + BookTable.CHAPTER_COUNT_DEF)
        }
    }

    companion object {
        internal const val CREATE_TABLE = "create table if not exists "
        internal const val DROP_TABLE = "drop table if exists "
    }
}
