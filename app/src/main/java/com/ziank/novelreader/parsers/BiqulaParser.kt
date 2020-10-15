package com.ziank.novelreader.parsers

import android.util.Log
import com.ziank.novelreader.R
import com.ziank.novelreader.model.Book
import com.ziank.novelreader.model.Chapter
import org.jsoup.Jsoup
import java.net.MalformedURLException
import java.net.URL
import java.util.ArrayList

class BiqulaParser: BaseParser() {
    override val resourceName: String
        get() = "笔趣啦"

    override val hostIdentifier: String
        get() = "qu.la"

    override val resourceColor: Int
        get() = R.color.biqula_icon

    override fun getSearchBookUrl(bookName: String): String {
        return String.format("https://sou.xanbhx.com/search?siteid=qula&q=%s", bookName)
    }

    override fun getDownloadBookUrl(book: Book): String = book.bookUrl

    override fun parseBookList(htmlContent: String): ArrayList<Book>? {
        Log.e("----", htmlContent)
        val document = Jsoup.parse(htmlContent)
        val novelListTag = document.getElementsByClass("search-list").first()
        if (novelListTag == null) {
            return null
        }
        val bookTags = novelListTag.getElementsByTag("li")
        if (null == bookTags || bookTags.size <= 1) {
            return null
        }
        bookTags.removeAt(0)

        val books = ArrayList<Book>()

        for (element in bookTags) {
            val spanList = element.getElementsByTag("span")
            val titleTag = spanList[1]
            val title = getTagText(titleTag)
            var bookUrl = ""
            if (null != titleTag) {
                bookUrl = titleTag.getElementsByTag("a").attr("href")
            }

            val updateTag = spanList[2]
            val updateContent = getTagText(updateTag)
            val authorTag = spanList[3]
            val author = getTagText(authorTag)

            val book = Book(title, author, bookUrl, updateContent)
            books.add(book)
        }
        return books
    }

    override fun parseChapterList(book: Book, htmlContent: String): ArrayList<Chapter> {
        val document = Jsoup.parse(htmlContent)
        val chapterListTag = document.getElementById("list")
        val chapterTagList = chapterListTag.select("dd")

        val count = chapterTagList.size
        val chapterList = ArrayList<Chapter>()
        var baseUrl: URL? = null
        try {
            baseUrl = URL(book.bookUrl)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }

        for (i in 0 until count) {
            val tag = chapterTagList[i]
            val chapter = Chapter()
            val titleTag = tag.getElementsByTag("a").first()
            if (null != titleTag) {
                val title = getTagText(titleTag)
                if (title.isNotEmpty()) {
                    chapter.title = title
                } else {
                    chapter.title = titleTag.text()
                }
                val urlString = titleTag.attr("href")
                try {
                    val url = URL(baseUrl, urlString)
                    chapter.url = url.toString()
                } catch (exception: MalformedURLException) {
                    exception.printStackTrace()
                }

                chapter.id = (i + 1).toLong()
                chapterList.add(chapter)
            }
        }
        while (chapterList.size > 0) {
            val chapter = chapterList.first()
            if (chapterList.filter { el -> el.url == chapter.url }.size > 1) {
                chapterList.removeAt(0)
            } else {
                break
            }
        }
        return chapterList
    }

    override fun parseChapterContent(htmlContent: String): String {
        val document = Jsoup.parse(htmlContent)
        val body = document.getElementById("content") ?: return ""
        body.select("br").append("\\n")
        body.select("p").prepend("\\n")
        var content = body.text()
        content = content.replace("\\\\n".toRegex(), "\n").replace("\r".toRegex(), "").replace("\n\\s+\n".toRegex(), "\n").replace("\n\n+".toRegex(), "\n")
        return content
    }
}