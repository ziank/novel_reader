package com.ziank.novelreader.parsers

import org.jsoup.nodes.Element

/**
 * Created by zhaixianqi on 2017/10/10.
 */

abstract class BaseParser : NovelParser {
    protected fun getTagText(tag: Element?): String {
        return if (null == tag) {
            ""
        } else {
            tag.text()
        }
    }
}
