package com.ziank.novelreader.views.slider

import android.view.MotionEvent

/**
* Created by ziank on 2017/9/27.
* @copyright ziank.2018
*/

interface Slider {
    fun init(slidingLayout: SlidingLayout)
    fun resetFromAdapter(adapter: SlidingAdapter<*>)
    fun onTouchEvent(event: MotionEvent): Boolean
    fun computeScroll()
    fun slideNext()
    fun slidePrevious()


}
