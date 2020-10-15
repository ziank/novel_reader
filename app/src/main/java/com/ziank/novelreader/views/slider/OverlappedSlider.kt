package com.ziank.novelreader.views.slider

import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.Scroller

/**
* Created by ziank on 2017/9/27.
* @copyright ziank.2018
*/

class OverlappedSlider : BaseSlider() {
    private var mScroller: Scroller? = null
    private var mVelocityTracker: VelocityTracker? = null
    private var mVelocityValue = 0

    private var mLimitDistance = 0
    private var mScreenWidth = 0

    private var mTouchResult = BaseSlider.MOVE_NO_RESULT

    private var mDirection = BaseSlider.MOVE_NO_RESULT

    private var mMode = BaseSlider.MODE_NONE

    private var mScrollerView: View? = null

    private var mStartX = 0
    private var mSlidingLayout: SlidingLayout? = null

    private val adapter: SlidingAdapter<List<String>>
        get() = mSlidingLayout!!.adapter as SlidingAdapter<List<String>>

    val previousView: View?
        get() = adapter.updatedPreviousView

    val currentShowView: View
        get() = adapter.currentView

    override fun init(slidingLayout: SlidingLayout) {
        mSlidingLayout = slidingLayout
        mScroller = Scroller(slidingLayout.context)
        mScreenWidth = slidingLayout.context.resources.displayMetrics.widthPixels
        mLimitDistance = mScreenWidth / 3
    }

    override fun resetFromAdapter(adapter: SlidingAdapter<*>) {
        mSlidingLayout!!.addView(adapter.updatedCurrentView)

        if (adapter.hasNext()) {
            val nextView = adapter.updatedNextView
            mSlidingLayout!!.addView(nextView, 0)
            nextView?.scrollTo(0, 0)
        }

        if (adapter.hasPrevious()) {
            val prevView = adapter.updatedPreviousView
            mSlidingLayout!!.addView(prevView)
            prevView?.scrollTo(mScreenWidth, 0)
        }

        mSlidingLayout!!.slideSeleted(adapter.getCurrent()!!)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        obtainVelocityTracker(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (mScroller!!.isFinished) {
                    mStartX = event.x.toInt()
                }
            }

            MotionEvent.ACTION_MOVE -> run {
                if (!mScroller!!.isFinished) {
                    return@run
                }
                if (mStartX == 0) {
                    mStartX = event.x.toInt()
                }
                val distance = (mStartX - event.x).toInt()
                if (mDirection == BaseSlider.Companion.MOVE_NO_RESULT) {
                    if (adapter.hasNext() && distance > 0) {
                        mDirection = BaseSlider.Companion.MOVE_TO_LEFT
                    } else if (adapter.hasPrevious() && distance < 0) {
                        mDirection = BaseSlider.Companion.MOVE_TO_RIGHT
                    }
                }

                if (mMode == BaseSlider.MODE_NONE && (mDirection ==
                        BaseSlider.MOVE_TO_LEFT && adapter.hasNext() || mDirection == BaseSlider.MOVE_TO_RIGHT && adapter.hasPrevious())) {
                    mMode = BaseSlider.MODE_MOVE
                }
                if (mMode == BaseSlider.MODE_MOVE) {
                    if (mDirection == BaseSlider.Companion.MOVE_TO_LEFT && distance <= 0 || mDirection == BaseSlider.Companion.MOVE_TO_RIGHT && distance >= 0) {
                        mMode = BaseSlider.MODE_NONE
                    }
                }
                if (mDirection != BaseSlider.Companion.MOVE_NO_RESULT) {
                    if (mDirection == BaseSlider.Companion.MOVE_TO_LEFT) {
                        mScrollerView = currentShowView
                        val nextView = adapter.updatedNextView
                        if (nextView!!.parent != null) {
                            mSlidingLayout!!.removeView(nextView)
                        }
                        mSlidingLayout!!.addView(nextView, 0)
                    } else {
                        mScrollerView = previousView
                    }

                    if (mMode == BaseSlider.MODE_MOVE) {
                        mVelocityTracker!!.computeCurrentVelocity(1000,
                                ViewConfiguration.get(mSlidingLayout!!.context)
                                        .scaledMaximumFlingVelocity.toFloat())
                        if (mDirection == BaseSlider.Companion.MOVE_TO_LEFT) {
                            mScrollerView!!.scrollTo(distance, 0)
                        } else {
                            mScrollerView!!.scrollTo(mScreenWidth + distance, 0)
                        }
                    } else {
                        val scrollX = mScrollerView!!.scrollX
                        if (mDirection == BaseSlider.Companion.MOVE_TO_LEFT && scrollX != 0 && adapter.hasNext()) {
                            mScrollerView!!.scrollTo(0, 0)
                        } else if (mDirection == BaseSlider.Companion.MOVE_TO_RIGHT && adapter.hasPrevious() && mScreenWidth != Math.abs(scrollX)) {
                            mScrollerView!!.scrollTo(mScreenWidth, 0)
                        }
                    }
                }

                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                if (mScrollerView == null) {
                    return false
                }

                val scrollX = mScrollerView!!.scrollX
                mVelocityValue = mVelocityTracker!!.xVelocity.toInt()

                var time = 500

                if (mMode == BaseSlider.Companion.MODE_MOVE && mDirection == BaseSlider.Companion.MOVE_TO_LEFT) {
                    if (scrollX > mLimitDistance || mVelocityValue < -mScreenWidth / 2) {
                        mTouchResult = BaseSlider.Companion.MOVE_TO_LEFT
                        if (mVelocityValue < -mScreenWidth / 2) {
                            time = 200
                        }
                        mScroller!!.startScroll(scrollX, 0, mScreenWidth - scrollX, 0, time)
                    } else {
                        mTouchResult = BaseSlider.Companion.MOVE_NO_RESULT
                        mScroller!!.startScroll(scrollX, 0, -scrollX, 0, time)
                    }
                } else if (mMode == BaseSlider.Companion.MODE_MOVE && mDirection == BaseSlider.Companion.MOVE_TO_RIGHT) {
                    if (mScreenWidth - scrollX > mLimitDistance || mVelocityValue > mScreenWidth / 2) {
                        mTouchResult = BaseSlider.Companion.MOVE_TO_RIGHT
                        if (mVelocityValue > mScreenWidth / 2) {
                            time = 200
                        }
                        mScroller!!.startScroll(scrollX, 0, -scrollX, 0, time)
                    } else {
                        mScroller!!.startScroll(scrollX, 0, mScreenWidth - scrollX, 0, time)
                    }
                }
                resetVariables()
                invalidate()
            }
        }
        return true
    }

    private fun resetVariables() {
        mDirection = BaseSlider.Companion.MOVE_NO_RESULT
        mMode = BaseSlider.Companion.MODE_NONE
        mStartX = 0
        releaseVelocityTracker()
    }

    override fun computeScroll() {
        if (mScroller!!.computeScrollOffset()) {
            mScrollerView!!.scrollTo(mScroller!!.currX, mScroller!!.currY)
            invalidate()
        } else if (mScroller!!.isFinished && mTouchResult != BaseSlider.Companion.MOVE_NO_RESULT) {
            if (mTouchResult == BaseSlider.Companion.MOVE_TO_LEFT) {
                moveToNext()
            } else {
                moveToPrevious()
            }
            mTouchResult = BaseSlider.Companion.MOVE_NO_RESULT
            invalidate()
        }
    }

    override fun slideNext() {
        if (!adapter.hasNext() || !mScroller!!.isFinished) {
            return
        }

        val nextView = adapter.updatedNextView
        if (nextView!!.parent != null) {
            mSlidingLayout!!.removeView(nextView)
        }
        mSlidingLayout!!.addView(nextView, 0)

        mScrollerView = currentShowView
        mScroller!!.startScroll(0, 0, mScreenWidth, 0, 500)
        mTouchResult = BaseSlider.Companion.MOVE_TO_LEFT

        invalidate()
    }

    override fun slidePrevious() {
        if (!adapter.hasPrevious() || !mScroller!!.isFinished)
            return

        mScrollerView = previousView

        mScroller!!.startScroll(mScreenWidth, 0, -mScreenWidth, 0, 500)
        mTouchResult = BaseSlider.Companion.MOVE_TO_RIGHT

        mSlidingLayout!!.slideScrollStateChanged(BaseSlider.Companion.MOVE_TO_RIGHT)

        invalidate()
    }

    fun moveToNext(): Boolean {
        if (!adapter.hasNext()) {
            return false
        }

        val prevView = adapter.previousView
        if (prevView != null) {
            mSlidingLayout!!.removeView(prevView)
        }
        var newNextView = prevView

        adapter.moveToNext()
        mSlidingLayout!!.slideSeleted(adapter.getCurrent())

        if (adapter.hasNext()) {
            if (newNextView != null) {
                val updateNextView = adapter.getView(newNextView, adapter
                        .getNext())
                if (updateNextView !== newNextView) {
                    adapter.nextView = updateNextView
                    newNextView = updateNextView
                }
            } else {
                newNextView = adapter.nextView
            }

            if (newNextView!!.parent != null) {
                mSlidingLayout!!.removeView(newNextView)
            }
            mSlidingLayout!!.addView(newNextView, 0)
            newNextView.scrollTo(0, 0)
        }
        return true
    }

    fun moveToPrevious(): Boolean {
        if (!adapter.hasPrevious()) {
            return false
        }

        val nextView = adapter.nextView
        if (nextView != null) {
            mSlidingLayout!!.removeView(nextView)
        }

        var newPrevView = nextView
        adapter.moveToPrevious()

        mSlidingLayout!!.slideSeleted(adapter.getCurrent())
        if (adapter.hasPrevious()) {
            if (newPrevView != null) {
                val updatedPrevView = adapter.getView(newPrevView, adapter
                        .getPrevious())
                if (newPrevView !== updatedPrevView) {
                    adapter.previousView = updatedPrevView
                    newPrevView = updatedPrevView
                }
            } else {
                newPrevView = adapter.previousView
            }
            if (newPrevView!!.parent != null) {
                mSlidingLayout!!.removeView(newPrevView)
            }
            mSlidingLayout!!.addView(newPrevView)
            newPrevView.scrollTo(mScreenWidth, 0)
        }
        return true
    }

    private fun invalidate() {
        mSlidingLayout!!.postInvalidate()
    }

    private fun obtainVelocityTracker(event: MotionEvent) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(event)
    }

    private fun releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker!!.recycle()
            mVelocityTracker = null
        }
    }
}
