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
//        mParserList.add(BiqulaParser())
        mParserList.add(DingdianParser())
//        mParserList.add(Biquge11Parser())
        mParserList.add(BiqugeInfoParser())
        mParserList.add(BiquwoParser())
    }

    fun getParser(book: Book): NovelParser? {
        for (parser in mParserList) {
            val identifier = parser.name.toLowerCase()
            if (book.bookSourceName == identifier) {
                return parser
            }
        }
        return null
    }

    fun getParser(host: String): NovelParser? {
        for (parser in mParserList) {
            val searchUrl = parser.getSearchBookUrl("").toLowerCase()
            if (searchUrl.toLowerCase().contains(host)) {
                return parser
            }
        }

        return null
    }

    fun getParserList(): List<NovelParser> {
        return mParserList
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
