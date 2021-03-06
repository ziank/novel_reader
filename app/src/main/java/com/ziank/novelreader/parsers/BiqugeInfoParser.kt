package com.ziank.novelreader.parsers

import com.ziank.novelreader.model.Book
import com.ziank.novelreader.model.Chapter
import org.jsoup.Jsoup
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder
import java.util.ArrayList

class BiqugeInfoParser : BaseParser() {
    override val name: String
        get() = "笔趣阁"

    override fun getSearchBookUrl(bookName: String): String {
        val bookName = URLEncoder.encode(bookName, "UTF8")
        return String.format("https://www.biquge.info/modules/article/search.php?searchkey=%s", bookName)
    }

    override fun getDownloadBookUrl(book: Book): String = book.bookUrl

    override fun parseBookList(htmlContent: String): ArrayList<Book>? {
        val document = Jsoup.parse(htmlContent)

        val bookList = ArrayList<Book>()
        var baseUrl: URL? = null
        try {
            baseUrl = URL(getSearchBookUrl(""))
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }

        val infoTag = document.getElementsByTag("tbody").first()
        if (infoTag != null) {
            val tagList = infoTag.select("tr")
            var title:String = ""
            var author: String = ""
            var bookUrl: String = ""
            var updateContent: String = ""
            val summary: String = ""
            val coverUrl: String = ""
            if (tagList.size <= 1) {
                return null
            }
            tagList.removeAt(0)
            tagList.forEach { tag ->
                val spanList = tag.select("td")
                spanList.forEachIndexed { index, span ->
                    when(index){
                        0 -> {
                            title = getTagText(span)
                            bookUrl = span.getElementsByTag("a").attr("href")
                        }
                        1 -> updateContent = getTagText(span)
                        2 -> author = getTagText(span)
                    }
                }
                bookUrl = URL(baseUrl, bookUrl).toString()
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
