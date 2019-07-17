package com.ziank.novelreader.parsers

import android.util.Log
import com.ziank.novelreader.R
import com.ziank.novelreader.model.Book
import com.ziank.novelreader.model.Chapter

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import java.net.MalformedURLException
import java.net.URL
import java.util.ArrayList

/**
 * Created by ziank on 2018/7/24.
 * @copyright ziank.2018
 */

class BiqulaParser() : BaseParser() {

    override val name: String
        get() = "笔趣拉"

    override fun getSearchBookUrl(bookName: String): String {
        return String.format("https://sou.xanbhx.com/search?siteid=qula&q=%s", bookName)
    }

    override fun getDownloadBookUrl(book: Book): String = book.bookUrl

    override fun parseBookList(htmlContent: String): ArrayList<Book>? {
        val document = Jsoup.parse(htmlContent)

        val bookList = ArrayList<Book>()

        val infoTag = document.getElementById("search-main")
        if (infoTag != null) {
            val tagList = infoTag.select("li")
            var title:String = ""
            var author: String = ""
            var bookUrl: String = ""
            var updateContent: String = ""
            var summary: String = ""
            var coverUrl: String = ""
            if (tagList.size <= 1) {
                return null
            }
            tagList.removeAt(0)
            tagList.forEach { tag ->
                val spanList = tag.select("span")
                spanList.forEach { span ->
                    when(span.className()){
                        "s2" -> {
                            title = getTagText(span)
                            bookUrl = span.getElementsByTag("a").attr("href")
                        }
                        "s3" -> updateContent = getTagText(span)
                        "s4" -> author = getTagText(span)
                    }
                }
                val book = Book(title, author, bookUrl, updateContent)
                book.summary = summary
                book.bookCoverUrl = coverUrl
                book.bookSourceName = name
                bookList.add(book)
            }
        }
        return bookList
    }

    override fun parseChapterList(book: Book, htmlContent: String): ArrayList<Chapter> {
        val document = Jsoup.parse(htmlContent)
        val chapterTagList = document.getElementsByTag("dd")
        val count = chapterTagList.size
        val chapterList = ArrayList<Chapter>()
        val urlList = ArrayList<String>()
        for (i in 0 until count) {
            val tag = chapterTagList[i]
            val chapter = Chapter()
            val titleTag = tag.getElementsByTag("a").first()
            var baseUrl: URL? = null
            try {
                baseUrl = URL(book.bookUrl)
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }

            if (null != titleTag) {
                val title = titleTag.attr("title")
                if (title.isNullOrBlank()) {
                    chapter.title = getTagText(tag)
                } else {
                    chapter.title = title
                }
                val urlString = titleTag.attr("href")
                try {
                    val url = URL(baseUrl, urlString)
                    chapter.url = url.toString()
                    chapter.id = (i + 1).toLong()
                    chapterList.add(chapter)
                    urlList.add(url.toString())
                } catch (exception: MalformedURLException) {
                    exception.printStackTrace()
                }
            }
        }
        val urlCount = chapterList.size
        val newChapterList = ArrayList<Chapter>()
        for (j in 0 until urlCount) {
            urlList.removeAt(0)
            if (urlList.indexOf(chapterList[j].url) == -1) {
                newChapterList.add(chapterList[j])
            }
        }
        return newChapterList
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
