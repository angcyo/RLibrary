package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.View

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：雷达扫描效果
 * 创建人员：Robi
 * 创建时间：2017/06/08 11:46
 * 修改人员：Robi
 * 修改时间：2017/06/08 11:46
 * 修改备注：
 * Version: 1.0.0
 */
class RadarScanView(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet) {
    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.RED)
    }
}