package com.ziank.novelreader.loaders

import android.content.AsyncTaskLoader
import android.content.Context

import com.ziank.novelreader.manager.BookManager
import com.ziank.novelreader.manager.NetworkCallback
import com.ziank.novelreader.manager.NetworkManager
import com.ziank.novelreader.model.Book
import com.ziank.novelreader.model.Chapter
import com.ziank.novelreader.model.NovelEvent
import com.ziank.novelreader.parsers.NovelParserFactory

import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.ArrayList

/**
 * Created by zhaixianqi on 2017/10/23.
 */

class BookChapterListLoader(context: Context, private val mBook: Book) : AsyncTaskLoader<ArrayList<Chapter>>(context) {

    override fun onStartLoading() {
        forceLoad()
    }

    override fun loadInBackground(): ArrayList<Chapter>? {
        val chapterList = BookManager.instance
                .readChapterListFromDisk(mBook)
        if (null != chapterList && chapterList!!.size > 0) {
            return chapterList
        }
        BookManager.instance.downloadChapterList(mBook)
        return null
    }
}
