package com.ziank.novelreader.loaders

import android.content.AsyncTaskLoader
import android.content.Context
import com.ziank.novelreader.manager.BookManager
import com.ziank.novelreader.model.Book
import com.ziank.novelreader.model.Chapter
import java.util.*

/**
* Created by ziank on 2017/10/23.
* @copyright ziank.2018
*/

class BookChapterListLoader(context: Context, private val mBook: Book) : AsyncTaskLoader<ArrayList<Chapter>>(context) {

    override fun onStartLoading() {
        forceLoad()
    }

    override fun loadInBackground(): ArrayList<Chapter>? {
        val chapterList = BookManager.instance
                .readChapterListFromDisk(mBook)
        if (null != chapterList && chapterList.size > 0) {
            return chapterList
        }
        BookManager.instance.downloadChapterList(mBook)
        return null
    }
}
