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

    interface DatabaseSchema {
        val createSql: String
        val tableName: String
    }

    interface BookTable : DatabaseSchema, BaseColumns {
        companion object {
            val TABLE_NAME = "book"

            val BOOK_TITLE = "title"
            val BOOK_AUTHOR = "author"
            val BOOK_URL = "book_url"
            val BOOK_UPDATE = "update_content"
            val BOOK_COVER = "cover_url"
            val BOOK_SUMMARY = "summary"
            val BOOK_CODE = "book_id"

            val CHAPTER_INDEX = "chapter_index"
            val READ_POS = "read_pos"
            val HAS_UPDATE = "has_update"
            val SORT_TIME = "sort_time"
            val SORT_TIME_DEF = "sort_time LONG DEFAULT 0 NOT NULL"

            val CREATE_TABLE_SQL = CREATE_TABLE +
                    TABLE_NAME +
                    " ( " + BOOK_CODE + " VARCHAR(1024) PRIMARY KEY, " +
                    BOOK_TITLE + " TEXT NOT NULL, " +
                    BOOK_AUTHOR + " VARCHAR(128), " +
                    BOOK_URL + " VARCHAR(1024), " +
                    BOOK_UPDATE + " TEXT, " +
                    BOOK_COVER + " VARCHAR(1024), " +
                    BOOK_SUMMARY + " TEXT" + ", " +
                    CHAPTER_INDEX + " INT DEFAULT 0 NOT NULL, " +
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
//        if (oldVersion < 2) {
//            sqLiteDatabase.execSQL("ALTER TABLE " + BookTable.TABLE_NAME +
//                    " ADD " + BookTable.SORT_TIME_DEF)
//        }
    }

    companion object {
        internal val CREATE_TABLE = "create table if not exists "
        internal val DROP_TABLE = "drop table if exists "
    }
}
