package com.angcyo.uiview.widget.group

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.Region
import android.util.AttributeSet
import android.view.ViewGroup
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.density
import com.angcyo.uiview.kotlin.exactlyMeasure

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/01/15 13:39
 * 修改人员：Robi
 * 修改时间：2018/01/15 13:39
 * 修改备注：
 * Version: 1.0.0
 */
class HumpTabLayout(context: Context, attributeSet: AttributeSet? = null) : ViewGroup(context, attributeSet) {

    /**凸起的高度*/
    var humpHeight = 20 * density.toInt()

    init {
        val a = context.obtainStyledAttributes(attributeSet, R.styleable.HumpTabLayout)
        humpHeight = a.getDimensionPixelOffset(R.styleable.HumpTabLayout_r_hump_height, humpHeight)
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val size = if (childCount > 0) {
            wSize / childCount
        } else {
            0
        }

        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)
        var childHeight = 0
        for (i in 0 until childCount) {
            val childAt = getChildAt(i)
            val params = childAt.layoutParams
            if (params is LayoutParams) {
                if (params.isHump) {
                    childAt.measure(exactlyMeasure(size), exactlyMeasure(size + humpHeight))
                } else {
                    childAt.measure(exactlyMeasure(size), heightMeasureSpec)
                    childHeight = Math.max(childHeight, childAt.measuredHeight)
                }
            }
        }


        if (hMode != MeasureSpec.EXACTLY) {
            setMeasuredDimension(wSize, childHeight)
        } else {
            setMeasuredDimension(wSize, hSize)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var left: Int
        val size = if (childCount > 0) {
            measuredWidth / childCount
        } else {
            0
        }
        for (i in 0 until childCount) {
            val childAt = getChildAt(i)
            val params = childAt.layoutParams
            if (params is LayoutParams) {
                left = size / 2 + size * i - childAt.measuredWidth / 2
                if (params.isHump) {
                    childAt.layout(left, -humpHeight, left + childAt.measuredWidth, measuredHeight)
                } else {
                    childAt.layout(left, 0, left + childAt.measuredWidth, measuredHeight)
                }
            }
        }
    }

    val tempRect: Rect by lazy {
        Rect()
    }

    override fun draw(canvas: Canvas) {
        canvas.getClipBounds(tempRect)
        tempRect.set(tempRect.left, tempRect.top - humpHeight, tempRect.right, tempRect.bottom)
        canvas.clipRect(tempRect, Region.Op.REPLACE)
        super.draw(canvas)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return LayoutParams(context, attrs)
    }

    class LayoutParams : ViewGroup.LayoutParams {
        var isHump = false

        constructor(c: Context, attrs: AttributeSet?) : super(c, attrs) {
            val a = c.obtainStyledAttributes(attrs, R.styleable.HumpTabLayout)
            isHump = a.getBoolean(R.styleable.HumpTabLayout_r_is_hump, isHump)
            a.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height)
        constructor(source: ViewGroup.LayoutParams?) : super(source)
    }

}