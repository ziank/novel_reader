package com.ziank.novelreader.parsers

import com.ziank.novelreader.model.Book

import java.util.ArrayList

/**
* Created by ziank on 2017/10/18.
* @copyright ziank.2018
*/

class NovelParserFactory private constructor() {
    private val mParserList: MutableList<NovelParser>

    init {
        mParserList = ArrayList()
        mParserList.add(QududuParser())
        mParserList.add(SanjianggeParser())
    }

    fun getParser(book: Book): NovelParser? {
        for (parser in mParserList) {
            val identifier = parser.hostIdentifier.toLowerCase()
            if (book.bookUrl.toLowerCase().contains(identifier)) {
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
        if (url.toLowerCase().contains("7818637081234473025")) {
            for (parser in mParserList) {
                if (parser is SanjianggeParser) {
                    return parser
                }
            }
        }

        return null
    }

    fun getSearchBookUrlList(bookName: String): List<String> = mParserList
            .map { it.getSearchBookUrl(bookName) }
            .filter { it.isNotEmpty() }

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
