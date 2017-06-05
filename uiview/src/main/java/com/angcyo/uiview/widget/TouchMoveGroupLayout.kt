package com.angcyo.uiview.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

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
class TouchMoveGroupLayout(context: Context, attributeSet: AttributeSet? = null) : FrameLayout(context, attributeSet) {
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        (0..childCount - 1)
                .map { getChildAt(it) }
                .filterIsInstance<TouchMoveView>()
                .forEach { it.onTouchEvent(event) }
        return true
    }
}