package com.angcyo.uiview.viewgroup

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.HorizontalScrollView

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：支持点击事件的 HorizontalScrollView
 * 创建人员：Robi
 * 创建时间：2017/10/25 13:51
 * 修改人员：Robi
 * 修改时间：2017/10/25 13:51
 * 修改备注：
 * Version: 1.0.0
 */
class RHorizontalScrollView(context: Context, attributeSet: AttributeSet? = null) : HorizontalScrollView(context, attributeSet) {
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            performClick()
            return super.onSingleTapUp(e)
        }

        override fun onDown(e: MotionEvent): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawableHotspotChanged(e.x, e.y)
            }
            isPressed = true
            return super.onDown(e)
        }
    })

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        when (ev.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> isPressed = false
        }
        return super.onTouchEvent(ev)
    }
}