package com.angcyo.uiview.widget.group

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.density

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：把子View堆起来显示 叠起来显示
 * 创建人员：Robi
 * 创建时间：2017/12/27 13:37
 * 修改人员：Robi
 * 修改时间：2017/12/27 13:37
 * 修改备注：
 * Version: 1.0.0
 */
class PileFrameLayout(context: Context, attributeSet: AttributeSet? = null) : FrameLayout(context, attributeSet) {

    /**堆起来的宽度*/
    var pileWidth: Int = (10 * density).toInt()

    init {
        val a = context.obtainStyledAttributes(attributeSet, R.styleable.PileFrameLayout)
        pileWidth = a.getDimensionPixelOffset(R.styleable.PileFrameLayout_r_pile_width, pileWidth)
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (childCount > 1) {
            val width = (0 until childCount)
                    .map { getChildAt(it) }
                    .sumBy { it.measuredWidth }
            setMeasuredDimension(width - (childCount - 1) * pileWidth + paddingLeft + paddingRight, measuredHeight)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        for (i in 0 until childCount) {
            val childAt = getChildAt(i)
            val r = paddingLeft + childAt.measuredWidth * i
            val l = if (i > 0) {
                r - i * pileWidth
            } else {
                paddingLeft
            }
            childAt.layout(l, paddingTop, l + childAt.measuredWidth, paddingTop + childAt.measuredHeight)
        }
    }
}