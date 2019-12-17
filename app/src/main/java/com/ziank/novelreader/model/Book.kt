package com.ziank.novelreader.model

import android.database.Cursor
import com.ziank.novelreader.database.CursorHelper
import com.ziank.novelreader.database.DataBaseHelper
import com.ziank.novelreader.manager.BookManager
import com.ziank.novelreader.parsers.NovelParserFactory
import java.io.Serializable

/**
* Created by ziank on 2017/9/26.
* @copyright ziank.2018
*/

class Book : Serializable {
    var bookCode: String? = null
        private set
    var title: String? = null
        private set
    var author: String? = null
        private set
    var bookUrl: String = ""
        private set
    var updateContent: String? = null
    var bookCoverUrl: String? = null
    var summary: String? = null

    var chapterIndex: Int = 0
    var charIndex: Int = 0
    var isHasUpdate: Boolean = false

    val bookId: Long
        get() = bookUrl.hashCode().toLong()

    val resourceName: String
        get() = NovelParserFactory.instance.getParser(bookUrl)!!.resourceName

    val resourceColor: Int
        get() = NovelParserFactory.instance.getParser(bookUrl)!!.resourceColor

    constructor(title: String, author: String, bookUrl: String, updateContent: String) {
        bookCode = BookManager.instance.getMd5(bookUrl)
        this.title = title
        this.author = author
        this.bookUrl = bookUrl
        this.updateContent = updateContent
        this.bookCoverUrl = ""
        this.summary = ""
    }

    private constructor()

    fun equalsToBook(obj: Any): Boolean {
        if (obj is Book) {
            if (bookId == obj.bookId) {
                return bookUrl.equals(obj.bookUrl, ignoreCase = true)
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
            book.charIndex = helper.getInt(DataBaseHelper.BookTable
                    .READ_POS)
            book.isHasUpdate = helper.getBoolean(DataBaseHelper.BookTable
                    .HAS_UPDATE)
            book.bookUrl = book.bookUrl//.replace("qududu.org", "qududu.net")
            book.bookCoverUrl = book.bookCoverUrl//.replace("qududu.org","qududu.net")
            return book
        }
    }
}
