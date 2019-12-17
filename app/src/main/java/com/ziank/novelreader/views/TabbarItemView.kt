package com.ziank.novelreader.views

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import com.ziank.novelreader.R

/**
* Created by ziank on 2017/9/26.
* @copyright ziank.2018
*/

class TabbarItemView : android.support.v7.widget.AppCompatTextView {
    private var mTextId: Int = 0
    private var mDrawableId: Int = 0
    private var mHoverDrawableId: Int = 0


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun initTabs(textId: Int, drawableId: Int, hoverDrawableId: Int) {
        mTextId = textId
        mDrawableId = drawableId
        mHoverDrawableId = hoverDrawableId
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        if (selected) {
            setTextColor(ContextCompat.getColor(context, R.color
                    .main_tab_hover))
            setCompoundDrawablesRelativeWithIntrinsicBounds(null, context.getDrawable(mHoverDrawableId), null, null)
        } else {
            setTextColor(ContextCompat.getColor(context, R.color
                    .main_tab))
            setCompoundDrawablesRelativeWithIntrinsicBounds(null, context.getDrawable(mDrawableId), null, null)
        }
    }
}
