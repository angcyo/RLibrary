package com.angcyo.uiview.viewgroup

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.density
import com.angcyo.uiview.kotlin.getColor
import com.angcyo.uiview.skin.SkinHelper
import com.angcyo.uiview.widget.RTextView

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/09/12 17:26
 * 修改人员：Robi
 * 修改时间：2017/09/12 17:26
 * 修改备注：
 * Version: 1.0.0
 */
open class RTextLayout(context: Context, attributeSet: AttributeSet? = null) : FrameLayout(context, attributeSet) {
    lateinit var leftTextView: RTextView
    lateinit var rightTextView: RTextView

    init {
        leftTextView = RTextView(context)
        rightTextView = RTextView(context)

        leftTextView.setTextColor(getColor(R.color.base_text_color))
        rightTextView.setTextColor(getColor(R.color.base_text_color_dark))

        if (isInEditMode) {
            leftTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14f * density)
            rightTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 12f * density)

            leftTextView.text = "左边的文本"
            rightTextView.text = "右边的文本"
        } else {
            leftTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, SkinHelper.getSkin().mainTextSize)
            rightTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, SkinHelper.getSkin().subTextSize)
        }

        addView(leftTextView, LayoutParams(-2, -2).apply {
            this.gravity = (Gravity.CENTER_VERTICAL + Gravity.LEFT)
        })
        addView(rightTextView, LayoutParams(-2, -2).apply {
            this.gravity = (Gravity.CENTER_VERTICAL + Gravity.RIGHT)
        })
    }
}