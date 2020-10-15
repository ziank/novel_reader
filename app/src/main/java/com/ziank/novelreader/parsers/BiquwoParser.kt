package com.ziank.novelreader.parsers

import com.ziank.novelreader.R
import com.ziank.novelreader.model.Book
import com.ziank.novelreader.model.Chapter
import org.jsoup.Jsoup
import java.net.MalformedURLException
import java.net.URL

class BiquwoParser: BaseParser() {
    override val resourceName: String
        get() = "笔趣窝"

    override val hostIdentifier: String
        get() = "biquwo."

    override val resourceColor: Int
        get() = R.color.biquwo_icon

    override fun getSearchBookUrl(bookName: String): String {
        return String.format("https://biquwo.com/searchbook.php?keyword=%s", bookName)
    }

    override fun getDownloadBookUrl(book: Book): String = book.bookUrl

    override fun parseBookList(htmlContent: String): ArrayList<Book>? {
        val document = Jsoup.parse(htmlContent)
        val novelListTag = document.getElementsByClass("novelslist2").first()
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
            val titleTag = element.getElementsByClass("s2").first()
            val title = getTagText(titleTag)
            var bookUrl = ""
            if (null != titleTag) {
                bookUrl = titleTag.getElementsByTag("a").attr("href")
                bookUrl = URL(URL("https://biquwo.com/"), bookUrl).toString()
            }

            val updateTag = element.getElementsByClass("s3").first()
            val updateContent = getTagText(updateTag)
            val authorTag = element.getElementsByClass("s4").first()
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