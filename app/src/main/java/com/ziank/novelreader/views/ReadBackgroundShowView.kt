package com.ziank.novelreader.views

import android.R.attr.bitmap
import android.content.Context
import android.graphics.Canvas
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.ImageView
import android.R.attr.right
import android.graphics.Paint
import android.graphics.RectF
import android.support.v4.view.ViewCompat.getClipBounds
import com.ziank.novelreader.R


/**
 * Created by zhaixianqi on 2017/11/3.
 */
class ReadBackgroundShowView :ImageView {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    var choosed: Boolean = false

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (choosed) {
            val rect = canvas!!.clipBounds

            val paint = Paint()
            paint.color = context.resources.getColor(R.color.colorPrimary)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 9F
            canvas.drawRoundRect(RectF(rect), 15F, 15F, paint)
        }
    }
}