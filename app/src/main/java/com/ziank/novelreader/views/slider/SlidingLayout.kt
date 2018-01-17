package com.ziank.novelreader.views.slider

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.os.ParcelableCompat
import android.support.v4.os.ParcelableCompatCreatorCallbacks
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup

/**
* Created by ziank on 2017/9/27.
* @copyright ziank.2018
*/

typealias OnTapListener = (MotionEvent)->Unit

class SlidingLayout : ViewGroup {

    private var mDownMotionX: Int = 0
    private var mDownMotionY: Int = 0
    private var mDownMotionTime: Long = 0
    private var mOnTapListener: OnTapListener? = null
    private var mSlider: Slider? = null
    var adapter: SlidingAdapter<*>? = null
        set(adapter) {
            field = adapter
            this.adapter!!.setSlidingLayout(this)
            if (mRestoredAdapterState != null) {
                this.adapter!!.restoreState()
                mRestoredAdapterState = null
                mRestoredClassLoader = null
            }

            resetFromAdapter()

            postInvalidate()
        }
    private var mRestoredAdapterState: Parcelable? = null
    private var mRestoredClassLoader: ClassLoader? = null

    private var mSlideChangeListener: OnSlideChangeListener? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {}

    fun setSlider(slider: Slider) {
        mSlider = slider
        slider.init(this)
        resetFromAdapter()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownMotionX = event.x.toInt()
                mDownMotionY = event.y.toInt()
                mDownMotionTime = System.currentTimeMillis()
            }

            MotionEvent.ACTION_UP -> computeTapMotion(event)
        }
        return mSlider!!.onTouchEvent(event) || super.onTouchEvent(event)
    }

    fun setOnTapListener(onTapListener: OnTapListener) {
        mOnTapListener = onTapListener
    }

    private fun computeTapMotion(event: MotionEvent) {
        if (mOnTapListener == null) {
            return
        }

        val xDiff = Math.abs(event.x - mDownMotionX).toInt()
        val yDiff = Math.abs(event.y - mDownMotionY).toInt()
        val timeDiff = System.currentTimeMillis() - mDownMotionTime

        if (xDiff < 5 && yDiff < 5 && timeDiff < 200) {
            mOnTapListener!!(event)
        }
    }

    override fun computeScroll() {
        super.computeScroll()
        mSlider!!.computeScroll()
    }

    fun slideNext() {
        mSlider!!.slideNext()
    }

    fun slidePrevious() {
        mSlider!!.slidePrevious()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val height = child.measuredHeight
            val width = child.measuredWidth
            child.layout(0, 0, width, height)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        val height = View.MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)

        for (i in 0 until childCount) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    class SavedState : View.BaseSavedState {
        internal var mAdapterState: Parcelable? = null
        internal lateinit var mLoader: ClassLoader

        constructor(source: Parcelable) : super(source) {}

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeParcelable(mAdapterState, flags)
        }

        override fun toString(): String {
            return "BaseSlidingLayout.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + "}"
        }

        internal constructor(`in`: Parcel, loader: ClassLoader?) : super(`in`) {
            var classLoader = loader
            if (classLoader == null) {
                classLoader = javaClass.classLoader
            }
            mAdapterState = `in`.readParcelable(classLoader)
            mLoader = classLoader!!
        }

        companion object {

            val CREATOR: Parcelable.Creator<SavedState> = ParcelableCompat.newCreator(object : ParcelableCompatCreatorCallbacks<SavedState> {
                override fun createFromParcel(`in`: Parcel, loader: ClassLoader): SavedState {
                    return SavedState(`in`, loader)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            })
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        if (adapter != null) {
            ss.mAdapterState = adapter!!.saveState()
        }
        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        if (adapter != null) {
            adapter!!.restoreState()
        } else {
            mRestoredAdapterState = state.mAdapterState
            mRestoredClassLoader = state.mLoader
        }
    }

    fun resetFromAdapter() {
        removeAllViews()
        if (mSlider != null && adapter != null) {
            mSlider!!.resetFromAdapter(adapter!!)
        }
    }

    fun setOnSlideChangeListener(sliderChangeListener: OnSlideChangeListener) {
        mSlideChangeListener = sliderChangeListener
    }

    interface OnSlideChangeListener {
        fun onSlideScrollStateChanged(touchResult: Int)
        fun onSliderSelected(obj: Any)
    }

    fun slideScrollStateChanged(moveDirection: Int) {
        if (mSlideChangeListener != null) {
            mSlideChangeListener!!.onSlideScrollStateChanged(moveDirection)
        }
    }

    fun slideSeleted(obj: Any) {
        if (mSlideChangeListener != null) {
            mSlideChangeListener!!.onSliderSelected(obj)
        }
    }
}
