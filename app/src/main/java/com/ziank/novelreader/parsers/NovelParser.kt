package com.ziank.novelreader.parsers

import com.ziank.novelreader.model.Book
import com.ziank.novelreader.model.Chapter

import java.util.ArrayList

/**
 * Created by zhaixianqi on 2017/10/9.
 */

interface NovelParser {
    val hostIdentifier: String
    fun getSearchBookUrl(bookName: String): String
    fun getDownloadBookUrl(book: Book): String
    fun parseBookList(htmlContent: String): ArrayList<Book>?
    fun parseChapterList(book: Book, htmlContent: String): ArrayList<Chapter>

    fun parseChapterContent(htmlContent: String): String
}
