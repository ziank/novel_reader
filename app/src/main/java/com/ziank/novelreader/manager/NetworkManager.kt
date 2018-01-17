package com.ziank.novelreader.manager

import org.jsoup.Jsoup
import org.jsoup.helper.StringUtil
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import java.io.IOException
import java.nio.charset.Charset

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
* Created by ziank on 2017/9/26.
* @copyright ziank.2018
*/

class NetworkManager private constructor() {
    private val mHttpClient = OkHttpClient.Builder()
            .followRedirects(true).followSslRedirects(true).build()

    init {
        mHttpClient.dispatcher().maxRequestsPerHost = 5
    }

    fun getHttpRequest(urlString: String, networkCallback: NetworkCallback<String>) {
        val request = Request.Builder().url(urlString).addHeader("Content-Type", "text/json;Charset=UTF-8").
                addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel " +
                        "Mac OS X 10_13_2) AppleWebKit/537.36 (KHTML, like " +
                        "Gecko) Chrome/63.0.3239.132 Safari/537.36").build()
        mHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                networkCallback.fail(e.message)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val bytes = response.body()!!.bytes()
                //                String htmlContent = new String(bytes, "GBK");
                var htmlContent = String(bytes, Charset.forName("utf-8"))
                val charset = getCharsetName(htmlContent)
                if (!charset.equals("utf-8", ignoreCase = true) && !charset
                        .equals("utf8", ignoreCase = true)) {
                    htmlContent = String(bytes, Charset.forName(charset))
                }
                networkCallback.success(htmlContent)
            }
        })
    }

    fun getCharsetName(htmlContent: String): String {
        val doc = Jsoup.parse(htmlContent)
        val metas = doc.getElementsByTag("meta")
        var charset = "utf-8"
        for (meta in metas) {
            val metaType = meta.attr("http-equiv")
            if (metaType != null && metaType.equals("Content-Type", ignoreCase = true)) {
                val content = meta.attr("content")
                charset = content.substring(content.indexOf("=") + 1)
            }

            if (meta.attr("charset") != null && meta.attr("charset") !== "") {
                charset = meta.attr("charset")
            }
        }

        return charset
    }

    companion object {
        var sharedManager = NetworkManager()
    }
}
