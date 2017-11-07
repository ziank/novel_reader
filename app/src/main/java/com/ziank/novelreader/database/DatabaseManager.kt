package com.ziank.novelreader.database

import android.content.ContentValues
import android.util.Log

import com.ziank.novelreader.application.NovelApplication
import com.ziank.novelreader.manager.BookManager
import com.ziank.novelreader.model.Book
import java.util.*

/**
 * Created by zhaixianqi on 2017/10/12.
 */

class DatabaseManager {
    private val mDbHelper: DataBaseHelper = DataBaseHelper(NovelApplication
            .instance, BookManager.instance.dbName, null,
            DATABASE_VERSION)

    fun insertOrUpdateBook(book: Book) {
        val contentValues = ContentValues()
        contentValues.put(DataBaseHelper.BookTable.BOOK_CODE, book.bookCode)
        contentValues.put(DataBaseHelper.BookTable.BOOK_TITLE, book.title)
        contentValues.put(DataBaseHelper.BookTable.BOOK_AUTHOR, book
                .author)
        contentValues.put(DataBaseHelper.BookTable.BOOK_URL, book.bookUrl)
        contentValues.put(DataBaseHelper.BookTable.BOOK_COVER, book
                .bookCoverUrl)
        contentValues.put(DataBaseHelper.BookTable.BOOK_UPDATE, book
                .updateContent)
        contentValues.put(DataBaseHelper.BookTable.BOOK_SUMMARY, book
                .summary)
        contentValues.put(DataBaseHelper.BookTable.CHAPTER_INDEX, book
                .chapterIndex)
        contentValues.put(DataBaseHelper.BookTable.READ_POS, book
                .charIndex)
        contentValues.put(DataBaseHelper.BookTable.HAS_UPDATE, book
                .isHasUpdate)
        contentValues.put(DataBaseHelper.BookTable.SORT_TIME,
                System.currentTimeMillis())
        try {
            mDbHelper.writableDatabase.replaceOrThrow(DataBaseHelper
                    .BookTable.TABLE_NAME, null, contentValues)
        } catch (e: Exception) {
            Log.e("DatabaseManager", "insert $contentValues into table " +

                    DataBaseHelper
                            .BookTable.TABLE_NAME
                    + " failed", e)
        }

    }

    fun deleteBook(book: Book) {
        mDbHelper.writableDatabase.delete(DataBaseHelper.BookTable
                .TABLE_NAME, DataBaseHelper.BookTable
                .BOOK_CODE + " = ?", arrayOf(book.bookCode))
    }

    fun updateBookReadPos(book: Book) {
        val contentValues = ContentValues()
        contentValues.put(DataBaseHelper.BookTable.CHAPTER_INDEX, book
                .chapterIndex)
        contentValues.put(DataBaseHelper.BookTable.READ_POS, book
                .charIndex)
        contentValues.put(DataBaseHelper.BookTable.SORT_TIME,
                System.currentTimeMillis())
        mDbHelper.writableDatabase.update(DataBaseHelper.BookTable
                .TABLE_NAME, contentValues, DataBaseHelper.BookTable
                .BOOK_CODE + " = ?", arrayOf(book.bookCode))
    }

    fun updateBookStatus(book: Book) {
        val contentValues = ContentValues()
        contentValues.put(DataBaseHelper.BookTable.HAS_UPDATE,
                book.isHasUpdate)
        contentValues.put(DataBaseHelper.BookTable.BOOK_UPDATE,
                book.updateContent)
        contentValues.put(DataBaseHelper.BookTable.SORT_TIME,
                System.currentTimeMillis())
        mDbHelper.writableDatabase.update(DataBaseHelper.BookTable
                .TABLE_NAME, contentValues, DataBaseHelper.BookTable
                .BOOK_CODE + " = ?", arrayOf(book.bookCode))
    }

    fun fetchAllBooks(): List<Book> {
        val cursor = mDbHelper.readableDatabase.query(DataBaseHelper
                .BookTable.TABLE_NAME, null, null, null, null, null,
                DataBaseHelper.BookTable.SORT_TIME + " DESC", null)
        val result = ArrayList<Book>()
        try {
            while (cursor.moveToNext()) {
                val book = Book.fromCursor(cursor)
                result.add(book)
            }
        } finally {
            cursor.close()
        }
        return result
    }

    companion object {
        var sharedManager = DatabaseManager()
        val DATABASE_VERSION = 1
    }
}
