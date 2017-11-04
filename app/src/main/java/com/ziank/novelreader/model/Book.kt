package com.ziank.novelreader.model

import android.database.Cursor
import android.databinding.BindingAdapter
import android.view.View
import android.widget.ImageView

import com.squareup.picasso.Picasso
import com.ziank.novelreader.R
import com.ziank.novelreader.database.CursorHelper
import com.ziank.novelreader.database.DataBaseHelper
import com.ziank.novelreader.manager.BookManager

import java.io.Serializable

/**
 * Created by zhaixianqi on 2017/9/26.
 */

class Book : Serializable {
    var bookCode: String? = null
        private set
    var title: String? = null
        private set
    var author: String? = null
        private set
    var bookUrl: String? = null
        private set
    var updateContent: String? = null
    var bookCoverUrl: String? = null
    var summary: String? = null

    var chapterIndex: Int = 0
    var lineIndex: Int = 0
    var isHasUpdate: Boolean = false

    val bookId: Long
        get() = bookUrl!!.hashCode().toLong()

    constructor(title: String, author: String, bookUrl: String, updateContent: String) {
        bookCode = BookManager.instance.getMd5(bookUrl)
        this.title = title
        this.author = author
        this.bookUrl = bookUrl
        this.updateContent = updateContent
    }

    private constructor() {}

    fun equalsToBook(obj: Any): Boolean {
        if (obj is Book) {
            if (bookId == obj.bookId) {
                return bookUrl!!.equals(obj.bookUrl!!, ignoreCase = true)
            }
        }
        return false
    }

    companion object {

        fun fromCursor(cursor: Cursor): Book {
            val helper = CursorHelper(cursor)
            val book = Book()
            book.title = helper.getString(DataBaseHelper.BookTable
                    .BOOK_TITLE)
            book.bookCode = helper.getString(DataBaseHelper.BookTable
                    .BOOK_CODE)
            book.bookUrl = helper.getString(DataBaseHelper.BookTable
                    .BOOK_URL)
            book.author = helper.getString(DataBaseHelper.BookTable
                    .BOOK_AUTHOR)
            book.bookCoverUrl = helper.getString(DataBaseHelper.BookTable
                    .BOOK_COVER)
            book.updateContent = helper.getString(DataBaseHelper.BookTable
                    .BOOK_UPDATE)
            book.summary = helper.getString(DataBaseHelper.BookTable
                    .BOOK_SUMMARY)
            book.chapterIndex = helper.getInt(DataBaseHelper.BookTable
                    .CHAPTER_INDEX)
            book.lineIndex = helper.getInt(DataBaseHelper.BookTable
                    .LINE_INDEX)
            book.isHasUpdate = helper.getBoolean(DataBaseHelper.BookTable
                    .HAS_UPDATE)
            return book
        }
    }
}
