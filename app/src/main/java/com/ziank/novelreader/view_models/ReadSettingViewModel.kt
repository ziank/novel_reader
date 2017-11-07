package com.ziank.novelreader.view_models

import android.databinding.BaseObservable
import android.databinding.Bindable
import com.ziank.novelreader.R
import com.ziank.novelreader.application.NovelApplication
import com.ziank.novelreader.views.slider.SlideMode

/**
 * Created by zhaixianqi on 2017/11/3.
 */
class ReadSettingViewModel(novel: NovelApplication):BaseObservable() {
    var textSize: Int = novel.readTextSize
    val textSizeString: String
        @Bindable
        get() = textSize.toString()


    val backgroundResouceIndex:Int
        @Bindable
        get() = when(backgroundResourceId) {
            R.color.white_color -> 0
            R.drawable.read_bg_0 -> 1
            R.drawable.read_bg_1 -> 2
            R.drawable.read_bg_2 -> 3
            R.drawable.read_bg_3 -> 4
            R.drawable.read_bg_4 -> 5
            else -> 0
        }

    @Bindable
    var slideMode:Int = novel.getSlideMode()

    var backgroundResourceId:Int = novel.backgroundResource


    val isMaximumSize:Boolean
        @Bindable
        get() = textSize >= 30

    val isLeastSize:Boolean
        @Bindable
        get() = textSize <= 12

    var title: String = ""
}