package com.angcyo.uiview.design

import android.content.Context
import android.support.v4.view.GestureDetectorCompat
import android.support.v4.view.MotionEventCompat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.OverScroller

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：固定顶部的Layout
 * 创建人员：Robi
 * 创建时间：2017/09/04 17:58
 * 修改人员：Robi
 * 修改时间：2017/09/04 17:58
 * 修改备注：
 * Version: 1.0.0
 */
class StickTopLayout(context: Context, attributeSet: AttributeSet? = null) : FrameLayout(context, attributeSet) {

    /**顶部是否处于可见状态*/
    private var isTopStick = true

    private var handTouch = true

    private var isFirst = true

    private val mOverScroller: OverScroller by lazy {
        OverScroller(context)
    }

    private val mGestureDetectorCompat: GestureDetectorCompat by lazy {
        GestureDetectorCompat(getContext(), object : GestureDetector.SimpleOnGestureListener() {

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                //L.e("call: onScroll -> $distanceY")
                if (isFirst) {
                    isFirst = false
                    if (distanceY < 0) {
                        handTouch = false
                        return false
                    }
                }

                if (canScroll) {
                    if (scrollY == 0 && (distanceY < 0 || distanceY < distanceX)) {
                        return false
                    } else {
                        //top 打开状态,并且是向上滚动 拦截事件
                        scrollBy(0, distanceY.toInt())
                        return true
                    }
                }
                return super.onScroll(e1, e2, distanceX, distanceY)
            }

            override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                //L.e("call: onFling -> $velocityY")
                if (isTopStick && velocityY < -3000 && velocityY < 0 && velocityY < velocityX) {
                    closeTop()
                    return true
                }
                return false
            }
        })
    }

    /**关闭Top*/
    fun closeTop() {
        if (scrollY == topViewHeight()) {
            return
        }
        mOverScroller.startScroll(0, scrollY, 0, topViewHeight() - scrollY, 300)
        postInvalidate()
    }

    /**打开Top*/
    fun openTop() {
        if (scrollY == 0) {
            return
        }
        mOverScroller.startScroll(0, scrollY, 0, -scrollY, 300)
        postInvalidate()
    }

    override fun scrollTo(x: Int, y: Int) {
        super.scrollTo(x, Math.min(topViewHeight(), Math.max(y, 0)))
        //L.e("call: scrollTo -> $y")
        isTopStick = scrollY != topViewHeight()

        onTopScrollListener?.onTopScroll(isTopStick, y, topViewHeight())
    }

    override fun computeScroll() {
        if (mOverScroller.computeScrollOffset()) {
            val currY = mOverScroller.currY
            scrollTo(0, currY)
            postInvalidate()
        }
    }


    /**顶部View*/
    private var topView: View? = null
    private var contentView: View? = null

    private fun topViewHeight(): Int {
        return if (topView == null) {
            0
        } else {
            topView!!.measuredHeight
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        //super.onLayout(changed, left, top, right, bottom)
        if (topView == null) {
            contentView?.let {
                it.layout(0, 0, measuredWidth, it.measuredHeight)
            }
        } else {
            topView!!.layout(0, 0, measuredWidth, topView!!.measuredHeight)

            contentView?.let {
                it.layout(0, topView!!.measuredHeight, measuredWidth, topView!!.measuredHeight + it.measuredHeight)
            }
        }
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        super.addView(child, index, params)
        if (childCount > 1) {
            contentView = getChildAt(1)
        }
        if (childCount > 0) {
            topView = getChildAt(0)
        }
    }

    private var canScroll = false

    /**事件分发*/
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (MotionEventCompat.getActionMasked(ev)) {
            MotionEvent.ACTION_DOWN -> {
                handTouch = true
                isFirst = true
                mOverScroller.abortAnimation()
                canScroll = canScroll()
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                //L.e("call: dispatchTouchEvent -> --------------------- ${MotionEventCompat.getActionMasked(ev)}")
                if (topViewHeight() != 0) {
                    if (scrollY > topViewHeight() * 1f / 3f) {
                        //如果滚动的距离超过三分一
                        closeTop()
                    } else if (isTopStick) {
                        openTop()
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        mGestureDetectorCompat.onTouchEvent(event)
        return true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (handTouch) {
            return mGestureDetectorCompat.onTouchEvent(ev)
        } else {
            return false
        }
    }

    var onTopScrollListener: OnTopScrollListener? = null

    /**是否需要处理滚动事件*/
    private fun canScroll(): Boolean {
        if (topView == null) {
            return false
        }

        if (scrollY == topViewHeight()) {
            return false
        }

        return true
    }

    interface OnTopScrollListener {
        fun onTopScroll(isStickTop: Boolean, y: Int, max: Int)
    }
}