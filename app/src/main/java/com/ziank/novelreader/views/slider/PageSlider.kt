package com.ziank.novelreader.views.slider

import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.Scroller

/**
 * Created by zhaixianqi on 2017/10/13.
 */

class PageSlider : BaseSlider() {
    private var mScroller: Scroller? = null
    private var mVelocityTracker: VelocityTracker? = null
    private var mVelocityValue = 0
    private var mLimitDistance = 0
    private var mScreenWidth = 0
    private var mTouchResult = BaseSlider.Companion.MOVE_NO_RESULT
    private var mDirection = BaseSlider.Companion.MOVE_NO_RESULT
    private var mMode = BaseSlider.Companion.MODE_NONE
    private var mMoveLastPage: Boolean = false
    private var mMoveFirstPage: Boolean = false
    private var startX = 0

    private var mLeftScrollerView: View? = null
    private var mRightScrollerView: View? = null
    private var mSlidingLayout: SlidingLayout? = null

    private val adapter: SlidingAdapter<List<String>>
        get() = mSlidingLayout!!.adapter as SlidingAdapter<List<String>>

    val topView: View?
        get() = adapter.updatedPreviousView

    val currentShowView: View
        get() = adapter.currentView

    val bottomView: View?
        get() = adapter.updatedNextView

    override fun init(slidingLayout: SlidingLayout) {
        mSlidingLayout = slidingLayout
        mScroller = Scroller(slidingLayout.context)
        mScreenWidth = slidingLayout.context.resources
                .displayMetrics.widthPixels
        mLimitDistance = mScreenWidth / 3
    }

    override fun resetFromAdapter(adapter: SlidingAdapter<*>) {
        val curView = adapter.updatedCurrentView
        mSlidingLayout!!.addView(curView)
        curView.scrollTo(0, 0)

        if (adapter.hasPrevious()) {
            val preView = adapter.updatedPreviousView
            mSlidingLayout!!.addView(preView)
            preView?.scrollTo(mScreenWidth, 0)
        }

        if (adapter.hasNext()) {
            val nextView = adapter.updatedNextView
            mSlidingLayout!!.addView(nextView)
            nextView!!.scrollTo(-mScreenWidth, 0)
        }

        mSlidingLayout!!.slideSeleted(adapter.getCurrent()!!)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        obtainVelocityTracker(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (mScroller!!.isFinished) {
                    startX = event.x.toInt()
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (!mScroller!!.isFinished) {
                    return false
                }
                if (startX == 0) {
                    startX = event.x.toInt()
                }
                val distance = startX - event.x.toInt()
                if (mDirection == BaseSlider.Companion.MOVE_NO_RESULT) {
                    if (distance > 0) {
                        mDirection = BaseSlider.Companion.MOVE_TO_LEFT
                        mMoveLastPage = !adapter.hasNext()
                        mMoveFirstPage = false

                        mSlidingLayout!!.slideScrollStateChanged(BaseSlider.Companion
                                .MOVE_TO_LEFT)

                    } else if (distance < 0) {
                        mDirection = BaseSlider.Companion.MOVE_TO_RIGHT
                        mMoveFirstPage = !adapter.hasPrevious()
                        mMoveLastPage = false

                        mSlidingLayout!!.slideScrollStateChanged(BaseSlider.Companion
                                .MOVE_TO_RIGHT)
                    }
                }
                if (mMode == BaseSlider.Companion.MODE_NONE && (mDirection ==
                        BaseSlider.Companion.MOVE_TO_LEFT || mDirection == BaseSlider.Companion.MOVE_TO_RIGHT)) {
                    mMode = BaseSlider.Companion.MODE_MOVE
                }

                if (mMode == BaseSlider.Companion.MODE_MOVE) {
                    if (mDirection == BaseSlider.Companion.MOVE_TO_LEFT && distance <= 0 || mDirection == BaseSlider.Companion.MOVE_TO_RIGHT && distance >= 0) {
                        mMode = BaseSlider.Companion.MODE_NONE
                    }
                }

                if (mDirection != BaseSlider.Companion.MOVE_NO_RESULT) {
                    if (mDirection == BaseSlider.Companion.MOVE_TO_LEFT) {
                        mLeftScrollerView = currentShowView
                        if (!mMoveLastPage)
                            mRightScrollerView = bottomView
                        else
                            mRightScrollerView = null
                    } else {
                        mRightScrollerView = currentShowView
                        if (!mMoveFirstPage)
                            mLeftScrollerView = topView
                        else
                            mLeftScrollerView = null
                    }
                    if (mMode == BaseSlider.Companion.MODE_MOVE) {
                        mVelocityTracker!!.computeCurrentVelocity(1000, ViewConfiguration
                                .getMaximumFlingVelocity().toFloat())
                        if (mDirection == BaseSlider.Companion.MOVE_TO_LEFT) {
                            if (mMoveLastPage) {
                                mLeftScrollerView!!.scrollTo(distance / 2, 0)
                            } else {
                                mLeftScrollerView!!.scrollTo(distance, 0)
                                mRightScrollerView!!.scrollTo(-mScreenWidth + distance, 0)
                            }
                        } else {
                            if (mMoveFirstPage) {
                                mRightScrollerView!!.scrollTo(distance / 2, 0)
                            } else {
                                mLeftScrollerView!!.scrollTo(mScreenWidth + distance, 0)
                                mRightScrollerView!!.scrollTo(distance, 0)
                            }
                        }
                    } else {
                        var scrollX = 0
                        if (mLeftScrollerView != null) {
                            scrollX = mLeftScrollerView!!.scrollX
                        } else if (mRightScrollerView != null) {
                            scrollX = mRightScrollerView!!.scrollX
                        }
                        if (mDirection == BaseSlider.Companion.MOVE_TO_LEFT && scrollX != 0 && adapter.hasNext()) {
                            mLeftScrollerView!!.scrollTo(0, 0)
                            if (mRightScrollerView != null) mRightScrollerView!!.scrollTo(mScreenWidth, 0)
                        } else if (mDirection == BaseSlider.Companion.MOVE_TO_RIGHT && adapter.hasPrevious() && mScreenWidth != Math.abs(scrollX)) {
                            if (mLeftScrollerView != null)
                                mLeftScrollerView!!.scrollTo(-mScreenWidth, 0)
                            mRightScrollerView!!.scrollTo(0, 0)
                        }

                    }
                }

                invalidate()
            }

            MotionEvent.ACTION_UP -> {

                if (mLeftScrollerView == null && mDirection == BaseSlider.Companion.MOVE_TO_LEFT || mRightScrollerView == null && mDirection == BaseSlider.Companion.MOVE_TO_RIGHT) {
                    return false
                }

                var time = 500

                if (mMoveFirstPage && mRightScrollerView != null) {
                    val rscrollx = mRightScrollerView!!.scrollX
                    mScroller!!.startScroll(rscrollx, 0, -rscrollx, 0, time * Math.abs(rscrollx) / mScreenWidth)
                    mTouchResult = BaseSlider.Companion.MOVE_NO_RESULT
                }

                if (mMoveLastPage && mLeftScrollerView != null) {
                    val lscrollx = mLeftScrollerView!!.scrollX
                    mScroller!!.startScroll(lscrollx, 0, -lscrollx, 0, time * Math.abs(lscrollx) / mScreenWidth)
                    mTouchResult = BaseSlider.Companion.MOVE_NO_RESULT
                }

                if (!mMoveLastPage && !mMoveFirstPage && mLeftScrollerView != null) {
                    val scrollX = mLeftScrollerView!!.scrollX
                    mVelocityValue = mVelocityTracker!!.xVelocity.toInt()
                    // scroll左正，右负(),(startX + dx)的值如果为0，即复位
                    /*
			 * android.widget.Scroller.startScroll( int startX, int startY, int
			 * dx, int dy, int duration )
			 */

                    if (mMode == BaseSlider.Companion.MODE_MOVE && mDirection == BaseSlider.Companion.MOVE_TO_LEFT) {
                        if (scrollX > mLimitDistance || mVelocityValue < -time) {
                            // 手指向左移动，可以翻屏幕
                            mTouchResult = BaseSlider.Companion.MOVE_TO_LEFT
                            if (mVelocityValue < -time) {
                                val tmptime = 1000 * 1000 / Math.abs(mVelocityValue)
                                time = if (tmptime > 500) 500 else tmptime
                            }
                            mScroller!!.startScroll(scrollX, 0, mScreenWidth - scrollX, 0, time)
                        } else {
                            mTouchResult = BaseSlider.Companion.MOVE_NO_RESULT
                            mScroller!!.startScroll(scrollX, 0, -scrollX, 0, time)
                        }
                    } else if (mMode == BaseSlider.Companion.MODE_MOVE && mDirection == BaseSlider.Companion.MOVE_TO_RIGHT) {
                        if (mScreenWidth - scrollX > mLimitDistance || mVelocityValue > time) {
                            // 手指向右移动，可以翻屏幕
                            mTouchResult = BaseSlider.Companion.MOVE_TO_RIGHT
                            if (mVelocityValue > time) {
                                val tmptime = 1000 * 1000 / Math.abs(mVelocityValue)
                                time = if (tmptime > 500) 500 else tmptime
                            }
                            mScroller!!.startScroll(scrollX, 0, -scrollX, 0, time)
                        } else {
                            mTouchResult = BaseSlider.Companion.MOVE_NO_RESULT
                            mScroller!!.startScroll(scrollX, 0, mScreenWidth - scrollX, 0, time)
                        }
                    }
                }
                resetVariables()
                invalidate()
            }
        }

        return true
    }

    override fun computeScroll() {
        if (mScroller!!.computeScrollOffset()) {
            if (mLeftScrollerView != null) {
                mLeftScrollerView!!.scrollTo(mScroller!!.currX, mScroller!!
                        .currY)
            }

            if (mRightScrollerView != null) {
                if (mMoveFirstPage) {
                    mRightScrollerView!!.scrollTo(mScroller!!.currX,
                            mScroller!!.currY)
                } else {
                    mRightScrollerView!!.scrollTo(mScroller!!.currX - mScreenWidth, mScroller!!.currY)
                }
            }

            invalidate()
        } else if (mScroller!!.isFinished) {
            if (mTouchResult != BaseSlider.Companion.MOVE_NO_RESULT) {
                if (mTouchResult == BaseSlider.Companion.MOVE_TO_LEFT) {
                    moveToNext()
                } else {
                    moveToPrevious()
                }
                mTouchResult = BaseSlider.Companion.MOVE_NO_RESULT

                mSlidingLayout!!.slideScrollStateChanged(BaseSlider.Companion
                        .MOVE_NO_RESULT)

                invalidate()
            }
        }
    }

    override fun slideNext() {
        if (!adapter.hasNext() || !mScroller!!.isFinished)
            return

        mLeftScrollerView = currentShowView
        mRightScrollerView = bottomView

        mScroller!!.startScroll(0, 0, mScreenWidth, 0, 500)
        mTouchResult = BaseSlider.Companion.MOVE_TO_LEFT

        mSlidingLayout!!.slideScrollStateChanged(BaseSlider.Companion.MOVE_TO_LEFT)

        invalidate()
    }

    override fun slidePrevious() {
        if (!adapter.hasPrevious() || !mScroller!!.isFinished) {
            return
        }

        mLeftScrollerView = topView
        mRightScrollerView = currentShowView

        mScroller!!.startScroll(mScreenWidth, 0, -mScreenWidth, 0, 500)
        mTouchResult = BaseSlider.Companion.MOVE_TO_RIGHT

        mSlidingLayout!!.slideScrollStateChanged(BaseSlider.Companion.MOVE_TO_RIGHT)

        invalidate()
    }

    private fun invalidate() {
        mSlidingLayout!!.postInvalidate()
    }

    private fun resetVariables() {
        mDirection = BaseSlider.Companion.MOVE_NO_RESULT
        mMode = BaseSlider.Companion.MODE_NONE
        startX = 0
        releaseVelocityTracker()
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

    private fun moveToNext(): Boolean {
        if (!adapter.hasNext()) {
            return false
        }

        val preView = adapter.previousView
        if (preView != null) {
            mSlidingLayout!!.removeView(preView)
        }

        var newNextView = preView

        adapter.moveToNext()

        mSlidingLayout!!.slideSeleted(adapter.getCurrent()!!)

        if (adapter.hasNext()) {
            if (newNextView != null) {
                val updateNextView = adapter.getView(newNextView,
                        adapter.getNext()!!)
                if (updateNextView !== newNextView) {
                    adapter.nextView = updateNextView
                    newNextView = updateNextView
                }
            } else {
                newNextView = adapter.updatedNextView
            }
            newNextView!!.scrollTo(-mScreenWidth, 0)
            mSlidingLayout!!.addView(newNextView)
        }

        return true
    }

    private fun moveToPrevious(): Boolean {
        if (!adapter.hasPrevious()) {
            return false
        }

        val nextView = adapter.nextView
        if (nextView != null) {
            mSlidingLayout!!.removeView(nextView)
        }

        var newPreView = nextView

        adapter.moveToPrevious()

        mSlidingLayout!!.slideSeleted(adapter.getCurrent()!!)

        if (adapter.hasPrevious()) {
            if (newPreView != null) {
                val updatedPreView = adapter.getView(newPreView,
                        adapter.getPrevious()!!)
                if (newPreView !== updatedPreView) {
                    adapter.previousView = updatedPreView
                    newPreView = updatedPreView
                }
            } else {
                newPreView = adapter.updatedPreviousView
            }

            newPreView!!.scrollTo(mScreenWidth, 0)
            mSlidingLayout!!.addView(newPreView)
        }
        return true
    }
}
