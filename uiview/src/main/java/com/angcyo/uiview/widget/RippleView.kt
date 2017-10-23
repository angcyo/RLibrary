package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.angcyo.uiview.widget.helper.RippleHelper

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/10/23 16:13
 * 修改人员：Robi
 * 修改时间：2017/10/23 16:13
 * 修改备注：
 * Version: 1.0.0
 */
class RippleView(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet) {

    val rippleHelper by lazy {
        RippleHelper()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        rippleHelper.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                rippleHelper.startRipple(this)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                rippleHelper.stopRipple()
            }
        }
        return true
    }
}