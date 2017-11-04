package com.ziank.novelreader.manager

import android.app.LoaderManager
import android.content.Context
import android.content.Loader
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextPaint
import android.util.Base64

import com.ziank.novelreader.application.NovelApplication
import com.ziank.novelreader.database.DatabaseManager
import com.ziank.novelreader.loaders.BookChapterListLoader
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
import java.security.MessageDigest
import java.util.ArrayList
import java.util.Collections
import java.util.function.Predicate
import kotlin.experimental.and

/**
 * Created by zhaixianqi on 2017/9/26.
 */

class BookManager private constructor() {
    var paint: TextPaint? = null

    val novelPath: String
        get() {
            val novelPath = NovelApplication.instance
                    .getExternalFilesDir("novel")!!.absolutePath
            val file = File(novelPath)
            if (file.exists() && file.isDirectory) {
                return novelPath
            } else {
                file.mkdir()
                return novelPath
            }
        }

    val dbName: String
        get() = "novel_reader"

    fun fetchAllBookList(): List<Book>? {
        return DatabaseManager.sharedManager.fetchAllBooks()
    }

    fun addBook(book: Book) {
        DatabaseManager.sharedManager.insertOrUpdateBook(book)
    }

    fun deleteBook(book: Book) {
        DatabaseManager.sharedManager.deleteBook(book)
        var file = File(BookManager.instance.novelPath, String
                .format("%s.json", book.bookCode))
        if (file.exists()) {
            file.delete()
        }
        file = File(novelPath, book.bookCode)
        if (file.exists()) {
            file.delete()
        }
    }

    fun refreshBooklist() {
        val bookList = fetchAllBookList()
        if (null == bookList) {
            EventBus.getDefault().post(NovelEvent(NovelEvent
                    .EventTypeRefreshBookList, null))
            return
        }
        for (book in bookList) {
            downloadChapterList(book)
        }
    }

    fun searchBook(bookname: String) {
        val urlList = NovelParserFactory.instance
                .getSearchBookUrlList(bookname)
        if (urlList == null || urlList.size == 0) {
            return
        }
        for (url in urlList) {
            NetworkManager.sharedManager.getHttpRequest(
                    url,
                    object : NetworkCallback<String> {
                        override fun success(response: String) {
                            val result = NovelParserFactory.instance
                                    .getParser(url)!!.parseBookList(response)
                            //                            result.removeIf(new Predicate<Book>() {
                            //                                @Override
                            //                                public boolean test(Book book) {
                            //                                    return !book.getTitle().contains(bookname);
                            //                                }
                            //                            });
                            val event = NovelEvent(NovelEvent
                                    .EventTypeSearchResult, result)
                            EventBus.getDefault().post(event)
                        }

                        override fun fail(errorMessage: String?) {
                            val event = NovelEvent(NovelEvent
                                    .EventTypeSearchResult, null)
                            EventBus.getDefault().post(event)
                        }
                    })
        }
    }

    fun splitTextWithTextSize(text: String, width: Float): List<String> {
        if (paint == null) {
            return ArrayList()
        }
        val resultLines = ArrayList<String>()
        val lines = text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val count = lines.size
        for (i in 0 until count) {
            val line = lines[i]
            var startPos = 0
            val endPos = line.length
            val widths = floatArrayOf(0f, 0f)
            do {
                val breakPos = paint!!.breakText(line, startPos, endPos,
                        true, width, widths)
                resultLines.add(line.substring(startPos, startPos + breakPos))
                if (startPos + breakPos < endPos) {
                    startPos = startPos + breakPos
                } else {
                    break
                }
            } while (true)
        }

        return resultLines
    }

    fun getMd5(`val`: String): String {
        try {
            val md5 = MessageDigest.getInstance("MD5")
            md5.update(`val`.toByteArray())
            val m = md5.digest()
            return getString(m)
        } catch (e: Exception) {
            e.printStackTrace()
            val bytes = `val`.toByteArray()
            return Base64.encodeToString(bytes, bytes.size)
        }

    }

    private fun getString(bytes: ByteArray): String {
        val stringBuffer = StringBuffer()
        for (i in bytes.indices) {
            val stmp = java.lang.Integer.toHexString(bytes[i].toInt().and(0XFF) )
            stringBuffer.append(stmp)
        }
        return stringBuffer.toString().toUpperCase()
    }

    fun testContent(): String {
        val sb = StringBuffer()
        var count = 500
        while (--count > 0) sb.append(String.format("……%d\n", count))
        return sb.toString()
    }

    fun getChapterContent(book: Book, currentChapter: Chapter): String? {
        return readContentFromDisk(book, currentChapter)
    }

    fun readChapterListFromDisk(book: Book): ArrayList<Chapter>? {
        val file = File(BookManager.instance.novelPath, String
                .format("%s" + ".json", book
                        .bookCode))
        //Read text from file
        val text = StringBuilder()

        try {
            val br = BufferedReader(FileReader(file))
            var line: String? = br.readLine()

            while (line != null) {
                text.append(line!!)
                text.append('\n')
                line = br.readLine()
            }
            br.close()
            val array = JSONArray(text.toString())
            if (array == null || array.length() == 0) {
                return null
            }
            val count = array.length()

            val chapterArrayList = ArrayList<Chapter>()
            for (index in 0 until count) {
                val obj = array.getJSONObject(index)
                val chapter = Chapter.fromJson(obj)
                if (null != chapter) {
                    chapterArrayList.add(chapter)
                }
            }
            return chapterArrayList
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return null
    }

    private fun writeChapterListToDisk(book: Book, chapterList: List<Chapter>?) {
        if (null == chapterList || chapterList.size == 0) {
            return
        }
        val array = JSONArray()
        for (chapter in chapterList) {
            val `object` = chapter.toJson()
            if (null != `object`) {
                array.put(`object`)
            }
        }
        val file = File(BookManager.instance.novelPath, String
                .format("%s" + ".json", book.bookCode))
        val outputStream: FileOutputStream
        try {
            val fOut = FileOutputStream(file)
            val myOutWriter = OutputStreamWriter(fOut)
            myOutWriter.write(array.toString())
            myOutWriter.close()
            fOut.flush()
            fOut.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun downloadChapterContent(book: Book,
                               chapter: Chapter) {
        val urlString = chapter.url
        urlString?.let {
            NetworkManager.sharedManager.getHttpRequest(urlString!!,
                    object : NetworkCallback<String> {
                        override fun success(response: String) {
                            val content = NovelParserFactory.instance
                                    .getParser(book)!!.parseChapterContent(response)
                            if (content !== "") {
                                writeContentToDisk(book, chapter, content)
                                EventBus.getDefault().post(NovelEvent(NovelEvent
                                        .EventTypeFetchChapterContent, chapter))
                            }
                        }

                        override fun fail(errorMessage: String?) {
                            EventBus.getDefault().post(NovelEvent(NovelEvent
                                    .EventTypeFetchChapterContent, null))
                        }
                    })
        }
    }

    private fun writeContentToDisk(book: Book,
                                   chapter: Chapter,
                                   content: String?) {
        if (null == content || content.length == 0) {
            return
        }

        var file = File(novelPath, book.bookCode)
        if (!file.exists() || !file.isDirectory) {
            file.mkdir()
        }

        file = File(file, String.format("%d.txt", chapter.id))
        val outputStream: FileOutputStream
        try {
            val fOut = FileOutputStream(file)
            val myOutWriter = OutputStreamWriter(fOut)
            myOutWriter.write(content)
            myOutWriter.close()
            fOut.flush()
            fOut.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun readContentFromDisk(book: Book,
                                    chapter: Chapter): String? {
        val file = File(novelPath, String.format("%s/%d.txt", book
                .bookCode, chapter.id))
        //Read text from file
        val text = StringBuilder()

        try {
            val br = BufferedReader(FileReader(file))
            var line: String? = br.readLine()

            while (line != null) {
                text.append(line)
                text.append('\n')
                line = br.readLine()
            }
            br.close()

            return text.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    fun updateReadPost(book: Book) {
        DatabaseManager.sharedManager.updateBookReadPos(book)
    }

    fun downloadBook(book: Book, context: Context, loaderManager: LoaderManager) {
        val callbacks = object : LoaderManager.LoaderCallbacks<ArrayList<Chapter>> {

            override fun onCreateLoader(i: Int,
                                        bundle: Bundle?):
                    Loader<ArrayList<Chapter>> {
                return BookChapterListLoader(context, book)
            }

            override fun onLoadFinished(loader: Loader<ArrayList<Chapter>>,
                                        chapters: ArrayList<Chapter>) {
                for (chapter in chapters) {
                    val content = readContentFromDisk(book, chapter)
                    if (null == content || content.length == 0) {
                        downloadChapterContent(book, chapter)
                    } else {
                        EventBus.getDefault().post(NovelEvent(NovelEvent
                                .EventTypeFetchChapterContent, chapter))
                    }
                }
            }

            override fun onLoaderReset(loader: Loader<ArrayList<Chapter>>) {}
        }
        if (null != loaderManager.getLoader<Any>(DOWNLOAD_BOOK)) {
            loaderManager.restartLoader(DOWNLOAD_BOOK, null, callbacks)
        } else {
            loaderManager.restartLoader(DOWNLOAD_BOOK, null, callbacks)
        }
    }

    fun downloadChapterList(book: Book) {
        val urlString = book.bookUrl
        urlString?.let { urlString
            NetworkManager.sharedManager.getHttpRequest(urlString,
                    object : NetworkCallback<String> {
                        override fun success(response: String) {
                            val chapterList = NovelParserFactory
                                    .instance.getParser(book)!!
                                    .parseChapterList(book, response)
                            writeChapterListToDisk(book, chapterList)
                            if (null != chapterList && !chapterList.isEmpty()) {
                                val chapter = chapterList[chapterList
                                        .size - 1]
                                if (!chapter.title.equals(book
                                        .updateContent, ignoreCase = true)) {
                                    book.isHasUpdate = true
                                    book.updateContent = chapter.title
                                    DatabaseManager.sharedManager
                                            .updateBookStatus(book)
                                }
                            }

                            val event = NovelEvent(NovelEvent
                                    .EventTypeFetchChapterList, book)
                            EventBus.getDefault().post(event)
                        }

                        override fun fail(errorMessage: String?) {
                            val event = NovelEvent(NovelEvent
                                    .EventTypeFetchChapterList, book)
                            EventBus.getDefault().post(event)
                        }
                    })
        }

    }

    companion object {
        private val DOWNLOAD_BOOK = Integer.MAX_VALUE
        val instance = BookManager()
    }
}
