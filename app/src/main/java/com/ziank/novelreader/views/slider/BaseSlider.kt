package com.ziank.novelreader.views.slider

/**
* Created by ziank on 2017/9/27.
* @copyright ziank.2018
*/

abstract class BaseSlider : Slider {
    companion object {
        /** 手指移动的方向  */
        val MOVE_NO_RESULT = 0
        val MOVE_TO_LEFT = 1  // Move to next
        val MOVE_TO_RIGHT = 2 // Move to previous

        /** 触摸的模式  */
        internal val MODE_NONE = 0
        internal val MODE_MOVE = 1
    }
}
