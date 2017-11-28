package com.angcyo.uiview.viewgroup

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.calcWidthHeightRatio
import com.angcyo.uiview.utils.ScreenUtil

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/11/28 13:53
 * 修改人员：Robi
 * 修改时间：2017/11/28 13:53
 * 修改备注：
 * Version: 1.0.0
 */
open class RFrameLayout(context: Context, attributeSet: AttributeSet? = null) : FrameLayout(context, attributeSet) {

    /**高度等于宽度*/
    protected var equWidth: Boolean = false
    var widthHeightRatio: String? = null

    private var rBackgroundDrawable: Drawable? = null
    /**
     * 允许的最大高度, 如果为-2px,那么就是屏幕高度的一半, 如果是-3px,那么就是屏幕高度的三分之, 以此内推, 0不处理
     */
    private var maxHeight = -1

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.RFrameLayout)
        equWidth = typedArray.getBoolean(R.styleable.RFrameLayout_r_is_aeq_width, equWidth)
        widthHeightRatio = typedArray.getString(R.styleable.RFrameLayout_r_width_height_ratio)

        rBackgroundDrawable = typedArray.getDrawable(R.styleable.RFrameLayout_r_background)
        maxHeight = typedArray.getDimensionPixelOffset(R.styleable.RFrameLayout_r_max_height, -1)

        typedArray.recycle()

        resetMaxHeight()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (maxHeight > 0) run {
            val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
            super.onMeasure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.AT_MOST))
            val measuredHeight = measuredHeight
            if (measuredHeight > maxHeight) {
                super.onMeasure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(maxHeight, heightMode))
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }

        if (equWidth) {
            //setMeasuredDimension(measuredWidth, measuredWidth)
            super.onMeasure(View.MeasureSpec.makeMeasureSpec(measuredWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(measuredWidth, View.MeasureSpec.EXACTLY))
        } else {
            calcWidthHeightRatio(widthHeightRatio)?.let {
                //setMeasuredDimension(it[0], it[1])
                super.onMeasure(View.MeasureSpec.makeMeasureSpec(it[0], View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(it[1], View.MeasureSpec.EXACTLY))
            }
        }
    }

    override fun draw(canvas: Canvas) {
        rBackgroundDrawable?.let {
            it.bounds = canvas.clipBounds
            it.draw(canvas)
        }
        super.draw(canvas)
    }

    private fun resetMaxHeight() {
        if (maxHeight < -1) {
            val num = Math.abs(maxHeight)
            maxHeight = ScreenUtil.screenHeight / num
        }
    }

    fun setMaxHeight(maxHeight: Int) {
        this.maxHeight = maxHeight
        resetMaxHeight()
        requestLayout()
    }


}