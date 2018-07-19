package com.ziank.novelreader.model

/**
* Created by ziank on 2017/10/19.
* @copyright ziank.2018
*/

class NovelEvent(var eventType: Int,
                 var eventData: Any?) {
    companion object {
        val EventTypeFavoriteChanged = 0
        val EventTypeLastUpdateChanged = EventTypeFavoriteChanged + 1
        val EventTypeSearchResult = EventTypeLastUpdateChanged + 1
        val EventTypeFetchChapterList = EventTypeSearchResult + 1
        val EventTypeFetchChapterContent = EventTypeFetchChapterList + 1
        val EventTypeRefreshBookList = EventTypeFetchChapterContent + 1
        val EventTypeChangeReadBg: Int = EventTypeRefreshBookList + 1
        val EventTypeChangeSlideMode: Int = EventTypeChangeReadBg + 1
        val EventTypeChangeFontSize: Int = EventTypeChangeSlideMode + 1
        val EventTypeSuggestResult: Int = EventTypeChangeFontSize + 1
        val EventtypeChangeFontTypeface: Int = EventTypeSuggestResult + 1
    }
}
