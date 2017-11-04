package com.ziank.novelreader.parsers

import com.ziank.novelreader.model.Book

import java.util.ArrayList

/**
 * Created by zhaixianqi on 2017/10/18.
 */

class NovelParserFactory private constructor() {
    private val mParserList: MutableList<NovelParser>

    init {
        mParserList = ArrayList()
        mParserList.add(QududuParser())
    }

    fun getParser(book: Book): NovelParser? {
        for (parser in mParserList) {
            val identifier = parser.hostIdentifier.toLowerCase()
            if (book.bookUrl!!.toLowerCase().contains(identifier)) {
                return parser
            }
        }
        return null
    }

    fun getParser(url: String): NovelParser? {
        for (parser in mParserList) {
            val identifier = parser.hostIdentifier.toLowerCase()
            if (url.toLowerCase().contains(identifier)) {
                return parser
            }
        }
        return null
    }

    fun getSearchBookUrlList(bookName: String): List<String> {
        val urlList = ArrayList<String>()
        for (parser in mParserList) {
            val searchUrl = parser.getSearchBookUrl(bookName)
            if (null != searchUrl && searchUrl.length > 0) {
                urlList.add(searchUrl)
            }
        }
        return urlList
    }

    companion object {
        private var sParserFactory: NovelParserFactory? = null

        val instance: NovelParserFactory
            get() = synchronized(NovelParserFactory::class.java) {
                if (sParserFactory == null) {
                    sParserFactory = NovelParserFactory()
                }
                return sParserFactory!!
            }
    }
}
