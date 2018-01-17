package com.ziank.novelreader.parsers

import org.jsoup.nodes.Element

/**
* Created by ziank on 2017/10/10.
* @copyright ziank.2018
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
