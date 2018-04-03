package com.angcyo.uiview.viewgroup

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.exactlyMeasure

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：模仿QQ侧滑菜单布局
 * 创建人员：Robi
 * 创建时间：2018/04/03 15:19
 * 修改人员：Robi
 * 修改时间：2018/04/03 15:19
 * 修改备注：
 * Version: 1.0.0
 */
class SliderMenuLayout(context: Context, attributeSet: AttributeSet? = null)
    : TouchLayout(context, attributeSet) {

    private var menuMaxWidthRatio = 0.8f

    /**回调接口*/
    var sliderCallback: SliderCallback? = null

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.SliderMenuLayout)
        menuMaxWidthRatio = typedArray.getFloat(R.styleable.SliderMenuLayout_r_menu_max_width, menuMaxWidthRatio)
        typedArray.recycle()
    }


    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return super.onInterceptTouchEvent(ev)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (childCount < 2) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            var widthSize = MeasureSpec.getSize(widthMeasureSpec)
            val widthMode = MeasureSpec.getMode(widthMeasureSpec)
            var heightSize = MeasureSpec.getSize(heightMeasureSpec)
            val heightMode = MeasureSpec.getMode(heightMeasureSpec)

            if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
                //测量菜单, 和内容的宽度
                getChildAt(0).measure(exactlyMeasure(menuMaxWidthRatio * widthSize), heightMeasureSpec)
                getChildAt(1).measure(exactlyMeasure(widthSize), heightMeasureSpec)

                setMeasuredDimension(widthSize, heightSize)
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        super.addView(child, index, params)
        if (childCount > 2) {
            throw IllegalStateException("不支持2个以上的子布局")
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        sliderCallback?.onSizeChanged(this)
    }


    override fun onScrollChange(orientation: ORIENTATION, distance: Float /*瞬时值*/) {
        super.onScrollChange(orientation, distance)
        refreshMenuLayout(((secondMotionEvent?.x ?: 0f) - (firstMotionEvent?.x ?: 0f)).toInt())
    }

    override fun onFlingChange(orientation: ORIENTATION, velocity: Float /*瞬时值*/) {
        super.onFlingChange(orientation, velocity)
    }

    /**刷新布局位置*/
    fun refreshMenuLayout(moveToX: Int /*需要将内容布局移动x的坐标*/) {
        if (childCount == 2) {
            getChildAt(1).apply {
                layout(moveToX, 0, moveToX + this.measuredWidth, this.measuredHeight)
            }
        }
    }

    interface SliderCallback {

        /**当前是否可以操作*/
        fun canSlider(): Boolean

        fun onSizeChanged(menuLayout: SliderMenuLayout)
    }
}