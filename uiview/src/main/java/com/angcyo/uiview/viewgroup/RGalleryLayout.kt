package com.angcyo.uiview.viewgroup

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.OverScroller
import com.angcyo.uiview.R

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

    /**当滚动到最后一个时, 是否增益滚动到第一个*/
    var animatorToFirst = true

//    var galleryAdapter: RGalleryAdapter? = null

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.RGalleryLayout)
        animatorToFirst = typedArray.getBoolean(R.styleable.RGalleryLayout_r_animator_to_first, animatorToFirst)

        typedArray.recycle()
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
        if (isVertical()) {
            overScroller.startScroll(0, scrollY, 0, (measuredHeight * index - scrollY), scrollDuration)
        } else {
            overScroller.startScroll(scrollX, 0, (measuredWidth * index - scrollX), 0, scrollDuration)
        }
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

        val i = if (isVertical()) {
            y / measuredHeight
        } else {
            x / measuredWidth
        }
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
                if (isVertical()) {
                    if (animatorToFirst) {
                        scrollTo(0, -measuredHeight)
                    } else {
                        scrollTo(0, 0)
                    }
                } else {
                    if (animatorToFirst) {
                        scrollTo(-measuredWidth, 0)
                    } else {
                        scrollTo(0, 0)
                    }
                }
            }
            if (isVertical()) {
                overScroller.startScroll(0, scrollY, 0, measuredHeight, scrollDuration)
            } else {
                overScroller.startScroll(scrollX, 0, measuredWidth, 0, scrollDuration)
            }
            postInvalidate()
            startScrollInner(scrollIntervalDuration + scrollDuration)
        }
    }

    /**修正child的滚动位置, 防止滚动到一半, 停下来了*/
    private fun resetScroll() {
        if (isVertical()) {
            val offset = scrollY % measuredHeight
            if (offset.compareTo(0f) != 0) {
                //需要修正
                scrollBy(0, measuredHeight - offset)
            }
        } else {
            val offset = scrollX % measuredWidth
            if (offset.compareTo(0f) != 0) {
                //需要修正
                scrollBy(measuredWidth - offset, 0)
            }
        }
    }

    private val index: Int
        get() = if (isVertical()) {
            Math.ceil(scrollY.toDouble() / measuredHeight).toInt()
        } else {
            Math.ceil(scrollX.toDouble() / measuredWidth).toInt()
        }

    private fun isLast() = index == childCount - 1

    private fun checkCanScroll(): Boolean {
        if (isOnAttachedToWindow &&
                visibility == View.VISIBLE &&
                measuredWidth > 0 &&
                measuredHeight > 0 &&
                childCount > 0
        /*&& galleryAdapter != null*/) {
            return true
        }
        return false
    }

    /**是否是垂直滚动*/
    fun isVertical(): Boolean {
        return orientation == LinearLayout.VERTICAL
    }
}

open class OnGalleryChangeListener {
    open fun onChangeTo(from: Int, to: Int) {

    }
}

//abstract class RGalleryAdapter {
//
//}