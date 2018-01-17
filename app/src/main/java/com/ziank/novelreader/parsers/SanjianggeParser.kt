package com.ziank.novelreader.parsers

import com.ziank.novelreader.R
import com.ziank.novelreader.model.Book
import com.ziank.novelreader.model.Chapter
import org.jsoup.Jsoup
import java.net.MalformedURLException
import java.net.URL
import java.util.ArrayList

/**
 * Created by zhaixianqi on 2018/1/16.
 * @copyright NetEase.2017
 */
class SanjianggeParser: BaseParser() {

    override val resourceId: Int
        get() = R.drawable.icon_sanjiangge

    override val hostIdentifier: String
        get() = "sanjiangge.com"

    override fun getSearchBookUrl(bookName: String): String {
        return String.format("http://zhannei.baidu" +
                ".com/cse/search?s=7818637081234473025&entry=1&q=%s" +
                "&isNeedCheckDomain=1&jump=1", bookName)
    }

    override fun getDownloadBookUrl(book: Book): String {
        return book.bookUrl
    }

    override fun parseBookList(htmlContent: String): ArrayList<Book>? {
        val document = Jsoup.parse(htmlContent)
        val bookTags = document.select("div.result-game-item-detail")
        if (null == bookTags || bookTags.size == 0) {
            return null
        }

        val books = ArrayList<Book>()

        for (element in bookTags) {
            val coverTag = element.parent().getElementsByTag("img").first()
            var coverUrl: String? = ""
            if (null != coverUrl) {
                coverUrl = coverTag.attr("src")
            }

            val titleTag = element.getElementsByTag("a").first()
            val title = getTagText(titleTag)
            var bookUrl = ""
            if (null != titleTag) {
                bookUrl = titleTag.attr("href")//.replace("qududu.org",
                //"qududu.net")
            }

            val summaryTag = element.select("p.result-game-item-desc")
                    .first()
            val summary = getTagText(summaryTag)

            val infoTags = element.select("p.result-game-item-info-tag")
            var updateContent = ""
            var author = ""
            if (infoTags.size >= 4) {
                updateContent = getTagText(infoTags[3].getElementsByTag("a").first())
                author = getTagText(infoTags.first().getElementsByTag("span").last())
                //                String updateDate =
                //                        infoTags.get(2).getElementsByTag("span").last().text();
            }


            val book = Book(title, author, bookUrl, updateContent)
            book.summary = summary
            book.bookCoverUrl = coverUrl//.replace("qududu.org",
            //"qududu.net")
            books.add(book)
        }
        return books
    }

    override fun parseChapterList(book: Book, htmlContent: String): ArrayList<Chapter> {
        val document = Jsoup.parse(htmlContent)
        val chapterTagList = document.getElementsByTag("dd")
        val count = chapterTagList.size
        val chapterList = ArrayList<Chapter>()
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
                if (null != title && title.length > 0) {
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