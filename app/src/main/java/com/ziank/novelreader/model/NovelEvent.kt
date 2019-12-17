package com.ziank.novelreader.model

/**
* Created by ziank on 2017/10/19.
* @copyright ziank.2018
*/

class NovelEvent(var eventType: Int,
                 var eventData: Any?) {
    companion object {
        const val EventTypeFavoriteChanged = 0
        const val EventTypeLastUpdateChanged = EventTypeFavoriteChanged + 1
        const val EventTypeSearchResult = EventTypeLastUpdateChanged + 1
        const val EventTypeFetchChapterList = EventTypeSearchResult + 1
        const val EventTypeFetchChapterContent = EventTypeFetchChapterList + 1
        const val EventTypeRefreshBookList = EventTypeFetchChapterContent + 1
        const val EventTypeChangeReadBg: Int = EventTypeRefreshBookList + 1
        const val EventTypeChangeSlideMode: Int = EventTypeChangeReadBg + 1
        const val EventTypeChangeFontSize: Int = EventTypeChangeSlideMode + 1
        const val EventTypeSuggestResult: Int = EventTypeChangeFontSize + 1
        const val EventtypeChangeFontTypeface: Int = EventTypeSuggestResult + 1
    }
}
