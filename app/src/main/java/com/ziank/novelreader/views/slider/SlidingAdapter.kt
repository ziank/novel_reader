package com.ziank.novelreader.views.slider

import android.os.Bundle
import android.os.Parcelable
import android.view.View

/**
 * Created by zhaixianqi on 2017/9/27.
 */

abstract class SlidingAdapter<T> {
    private var mViews: Array<View?> = arrayOfNulls(3)
    private var mCurrentViewIndex: Int = 0
    private var mSlidingLayout: SlidingLayout? = null

    val updatedCurrentView: View
        get() {
            var curView: View? = mViews[mCurrentViewIndex]
            if (curView == null) {
                curView = getView(null, getCurrent())
                mViews[mCurrentViewIndex] = curView
            } else {
                val updateView = getView(curView, getCurrent())
                if (curView != updateView) {
                    curView = updateView
                    mViews[mCurrentViewIndex] = updateView
                }
            }
            return curView
        }

    var currentView: View
        get() {
            var curView: View? = mViews[mCurrentViewIndex]
            if (curView == null) {
                curView = getView(null, getCurrent())
                mViews[mCurrentViewIndex] = curView
            }
            return curView
        }
        set(view) = setView(mCurrentViewIndex, view)

    val updatedNextView: View?
        get() {
            var nextView: View? = getView(mCurrentViewIndex + 1)
            val hasNext = hasNext()
            if (nextView == null && hasNext) {
                nextView = getView(null, getNext())
                setView(mCurrentViewIndex + 1, nextView)
            } else if (hasNext) {
                val updatedView = getView(nextView, getNext())
                if (updatedView != nextView) {
                    nextView = updatedView
                    setView(mCurrentViewIndex + 1, nextView)
                }
            }
            return nextView
        }

    var nextView: View?
        get() {
            var nextView: View? = getView(mCurrentViewIndex + 1)
            if (nextView == null && hasNext()) {
                nextView = getView(null, getNext())
                setView(mCurrentViewIndex + 1, nextView)
            }
            return nextView
        }
        set(view) = setView(mCurrentViewIndex + 1, view!!)

    val updatedPreviousView: View?
        get() {
            var prevView: View? = getView(mCurrentViewIndex - 1)
            val hasprev = hasPrevious()
            if (prevView == null && hasprev) {
                prevView = getView(null, getPrevious())
                setView(mCurrentViewIndex - 1, prevView)
            } else if (hasprev) {
                val updatedView = getView(prevView, getPrevious())
                if (updatedView != prevView) {
                    prevView = updatedView
                    setView(mCurrentViewIndex - 1, prevView)
                }
            }
            return prevView
        }

    var previousView: View?
        get() {
            var prevView: View? = getView(mCurrentViewIndex - 1)
            if (prevView == null && hasPrevious()) {
                prevView = getView(null, getPrevious())
                setView(mCurrentViewIndex - 1, prevView)
            }
            return prevView
        }
        set(view) = setView(mCurrentViewIndex - 1, view!!)

    abstract fun getCurrent(): T

    abstract fun getNext(): T

    abstract fun getPrevious(): T

    init {
        mCurrentViewIndex = 0
    }

    fun setSlidingLayout(slidingLayout: SlidingLayout) {
        mSlidingLayout = slidingLayout
    }

    fun getView(index: Int): View? {
        return mViews[(index + 3) % 3]
    }

    fun setView(index: Int, view: View) {
        mViews[(index + 3) % 3] = view
    }

    fun moveToNext() {
        computeNext()

        mCurrentViewIndex = (mCurrentViewIndex + 1) % 3
    }

    fun moveToPrevious() {
        computePrevious()
        mCurrentViewIndex = (mCurrentViewIndex + 2) % 3
    }


    abstract open fun getView(contentView: View?, t: T): View

    abstract operator fun hasNext(): Boolean

    abstract fun hasPrevious(): Boolean

    protected abstract fun computeNext()

    protected abstract fun computePrevious()

    fun saveState(): Bundle? {
        return null
    }

    fun restoreState(parcelable: Parcelable, classLoader: ClassLoader) {
        mCurrentViewIndex = 0
        mViews[0] = null
        mViews[1] = null
        mViews[2] = null
    }

    fun notifyDataSetChanged() {
        if (mSlidingLayout != null) {
            mSlidingLayout!!.resetFromAdapter()
            mSlidingLayout!!.postInvalidate()
        }
    }
}
