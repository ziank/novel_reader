package com.ziank.novelreader.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import com.ziank.novelreader.R


/**
* Created by ziank on 2017/11/3.
* @copyright ziank.2018
*/
class ReadBackgroundShowView :AppCompatImageView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var chosen: Boolean = false
    private val paint: Paint = Paint()
    private val rect:Rect = Rect()
    private val mRectF:RectF = RectF()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (chosen) {
            canvas!!.getClipBounds(rect)
            paint.color = ContextCompat.getColor(context, R.color.colorPrimary)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 9F
            mRectF.set(rect.left.toFloat(), rect.top.toFloat(), rect.right.toFloat(), rect.bottom.toFloat())
            canvas.drawRoundRect(mRectF, 15F, 15F, paint)
        }
    }
}