package com.ziank.novelreader.parsers

import com.ziank.novelreader.manager.BookSuggestType
import com.ziank.novelreader.model.Book
import com.ziank.novelreader.model.Chapter
import org.jsoup.Jsoup
import java.net.MalformedURLException
import java.net.URL
import java.util.ArrayList

/**
 * Created by zhaixianqi on 2017/11/6.
 */
class QidianParser {
    fun getSuggestUrl(suggestType: BookSuggestType):String {
        return when (suggestType) {
            BookSuggestType.SuggestTypeDefault -> "https://www.qidian.com/rank/collect?style=1"
            BookSuggestType.SuggestTypeRankWeek -> "https://www.qidian.com/rank/click?style=1&dateType=1"
            BookSuggestType.SuggestTypeRankMonth -> "https://www.qidian.com/rank/click?style=1&dateType=2"
            BookSuggestType.SuggestTypeRankAll -> "https://www.qidian.com/rank/click?style=1&dateType=3"
            BookSuggestType.SuggestTypeAdviseWeek -> "https://www.qidian.com/rank/recom?style=1&dateType=1"
            BookSuggestType.SuggestTypeAdviseMonth -> "https://www.qidian" +
                    ".com/rank/recom?style=1&dateType=2"
            BookSuggestType.SuggestTypeAdviseAll -> "https://www.qidian" +
                    ".com/rank/recom?style=1&dateType=3"
            else -> "https://www.qidian.com/rank/collect?style=1"
        }
    }

    fun parseBookList(htmlContent:String, baseUrlStr: String) :
            ArrayList<Book> {
        val document = Jsoup.parse(htmlContent)
        val bodyTag = document.getElementById("rank-view-list")
        val bookTags = bodyTag.getElementsByTag("li")
        val baseUrl: URL? = URL(baseUrlStr)
        if (baseUrl == null || null == bookTags || bookTags.size == 0) {
            return ArrayList<Book>()
        }

        val books = ArrayList<Book>()

        for (element in bookTags) {
            val coverTag = element.getElementsByTag("img").first()
            var coverUrl: String = ""
            if (null != coverTag) {
                coverUrl = coverTag.attr("data-src")
                if (null == coverUrl || coverUrl.isEmpty()) {
                    coverUrl = coverTag.attr("src")
                }
                coverUrl = URL(baseUrl, coverUrl).toString()
            }


            var bookUrl = ""
            var bookTitle = ""
            try {
                val titleTag = element.select("div.book-mid-info").first()
                        .getElementsByTag("a").first()

                if (null != titleTag) {
                    bookUrl = titleTag.attr("href")
                    bookUrl = URL(baseUrl, bookUrl).toString()
                    bookTitle = titleTag.text()
                }
            } catch (e:Exception) {
                e.printStackTrace()
                continue
            }

            val summaryTag = element.select("p.intro").first()
            val summary = summaryTag.text()

            var author = ""
            try {
                val authorTag = element.select("p.author").first()
                val nameTag = authorTag.select("a.name").first()
                author = nameTag.text()
            } catch (e:Exception) {
                e.printStackTrace()
                continue
            }

            val book = Book(bookTitle, author, bookUrl, "")
            book.summary = summary
            book.bookCoverUrl = coverUrl
            books.add(book)
        }
        return books
    }
}