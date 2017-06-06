package com.angcyo.uiview.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.angcyo.uiview.R

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/06/05 18:20
 * 修改人员：Robi
 * 修改时间：2017/06/05 18:20
 * 修改备注：
 * Version: 1.0.0
 */
class TouchMoveGroupLayout(context: Context, attributeSet: AttributeSet? = null) : LinearLayout(context, attributeSet) {

    var selectorPosition: Int = 0
    var listener: OnSelectorPositionListener? = null
        set(value) {
            field = value
            value?.onSelectorPosition(getChildAt(selectorPosition) as TouchMoveView, selectorPosition)
        }

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.TouchMoveGroupLayout)
        selectorPosition = typedArray.getInt(R.styleable.TouchMoveGroupLayout_r_default_position, 0)
        typedArray.recycle()
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (child !is TouchMoveView) {
            throw IllegalArgumentException("child need instanceof TouchMoveView.")
        }
        super.addView(child, index, params)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateSelector(getChildAt(selectorPosition) as TouchMoveView)
    }

    /**选中targetView*/
    fun updateSelector(targetView: TouchMoveView) {

        for (i in 0..childCount - 1) {
            //L.e("child at : $i")
            val childAt = getChildAt(i)
            if (childAt is TouchMoveView) {
                val oldState = childAt.mSelected

                childAt.mSelected = childAt == targetView

                val newState = childAt.mSelected
                if (newState && !oldState) {
                    //L.e("selected : $i")
                    listener?.onSelectorPosition(childAt, i)
                } else if (newState && oldState) {
                    listener?.onRepeatSelectorPosition(childAt, i)
                }
            }
        }
    }

    interface OnSelectorPositionListener {
        fun onSelectorPosition(targetView: TouchMoveView, position: Int)
        fun onRepeatSelectorPosition(targetView: TouchMoveView, position: Int)
    }
}
