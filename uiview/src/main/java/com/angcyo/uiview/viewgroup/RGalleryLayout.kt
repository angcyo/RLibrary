package com.angcyo.uiview.viewgroup

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.OverScroller

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：自动滚动的画廊Layout
 * 创建人员：Robi
 * 创建时间：2018/01/04 10:41
 * 修改人员：Robi
 * 修改时间：2018/01/04 10:41
 * 修改备注：
 * Version: 1.0.0
 */
class RGalleryLayout(context: Context, attributeSet: AttributeSet? = null) : LinearLayout(context, attributeSet) {

    /**当前显示child的索引*/
    var curShowIndex = -1

//    var galleryAdapter: RGalleryAdapter? = null

    init {
        orientation = LinearLayout.HORIZONTAL
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //of kotlin
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            for (i in 0 until childCount) {
                val childAt = getChildAt(i)
                childAt.measure(widthMeasureSpec, heightMeasureSpec)
            }
            setMeasuredDimension(widthSize, heightSize)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }
//
//    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
////        for (i in 0 until childCount) {
////            val childAt = getChildAt(i)
////            childAt.layout(0, 0, childAt.measuredWidth, childAt.measuredHeight)
////        }
//    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        startScroll()
    }

    private var isOnAttachedToWindow = false
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isOnAttachedToWindow = true
        startScroll()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isOnAttachedToWindow = false
        stopScroll()
    }

    private val overScroller: OverScroller by lazy {
        OverScroller(context, DecelerateInterpolator())
    }

    /**滚动持续时间*/
    var scrollDuration = 1000
    /**滚动间隔时间*/
    var scrollIntervalDuration = 2000L

    override fun computeScroll() {
        if (overScroller.computeScrollOffset()) {
            scrollTo(overScroller.currX, overScroller.currY)
            postInvalidate()
        }
    }

    fun startScroll() {
        startScrollInner(scrollIntervalDuration)
    }

    fun stopScroll() {
        removeCallbacks(scrollRunnable)
        resetScroll()
    }

    fun scrollToIndex(index: Int /*滚动到第几个child 0开始的索引*/) {
        overScroller.startScroll(scrollX, 0, (measuredWidth * index - scrollX), 0, scrollDuration)
        postInvalidate()
    }

    private fun startScrollInner(interval: Long) {
        if (checkCanScroll()) {
            stopScroll()
            postDelayed(scrollRunnable, interval)
        }
    }

    var onGalleryChangeListener: OnGalleryChangeListener? = null
    override fun scrollTo(x: Int, y: Int) {
        super.scrollTo(x, y)
        val i = x / measuredWidth
        val oldIndex = curShowIndex
        if (oldIndex != i) {
            curShowIndex = i
            onGalleryChangeListener?.onChangeTo(oldIndex, i)
        }
        //L.e("call: scrollTo ->${x / measuredWidth}  ${x % measuredWidth}")
    }

    private val scrollRunnable = Runnable {
        if (checkCanScroll()) {
            if (isLast()) {
                scrollTo(-measuredWidth, 0)
            }
            overScroller.startScroll(scrollX, 0, measuredWidth, 0, scrollDuration)
            postInvalidate()
            startScrollInner(scrollIntervalDuration + scrollDuration)
        }
    }

    /**修正child的滚动位置, 防止滚动到一半, 停下来了*/
    private fun resetScroll() {
        val offset = scrollX % measuredWidth
        if (offset.compareTo(0f) != 0) {
            //需要修正
            scrollBy(measuredWidth - offset, 0)
        }
    }

    private val index: Int
        get() = Math.ceil(scrollX.toDouble() / measuredWidth).toInt()

    private fun isLast() = index == childCount - 1

    private fun checkCanScroll(): Boolean {
        if (isOnAttachedToWindow &&
                visibility == View.VISIBLE &&
                measuredWidth > 0 &&
                measuredHeight > 0
        /*&& galleryAdapter != null*/) {
            return true
        }
        return false
    }
}

open class OnGalleryChangeListener {
    open fun onChangeTo(from: Int, to: Int) {

    }
}

//abstract class RGalleryAdapter {
//
//}