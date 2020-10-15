package com.ziank.novelreader.views

import android.content.Context
import android.text.Layout
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.TextView

/**
* Created by ziank on 2017/9/27.
* @copyright ziank.2018
*/

class ReadView : android.support.v7.widget.AppCompatTextView {

    /**
     * 获取当前页总字数
     */
    val charNum: Int
        get() {
            val layout = layout ?: return 0
            return layout.getLineEnd(lineNum)
        }

    val charCount:Int
        get() {
            return charNum - lineNum
        }

    /**
     * 获取当前页总行数
     */
    val lineNum: Int
        get() {
            val layout = layout ?: return 0
            val topOfLastLine = height - paddingTop - paddingBottom
            return layout.getLineForVertical(topOfLastLine)
        }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    // 构造函数略...

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        resize()
    }

    /**
     * 去除当前页无法显示的字
     * @return 去掉的字数
     */
    fun resize(): Int {
        val oldContent = text
        val newContent = oldContent.subSequence(0, charNum)
        text = newContent
        return oldContent.length - newContent.length
    }

    override fun getLineCount(): Int {
        val linenum = lineNum
        return if (text.isNullOrEmpty()) 0 else linenum + 1
    }
}
