package com.angcyo.uiview.accessibility.shape

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.text.TextPaint
import com.angcyo.uiview.accessibility.CanvasLayout

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/01/25 15:09
 * 修改人员：Robi
 * 修改时间：2018/01/25 15:09
 * 修改备注：
 * Version: 1.0.0
 */
open class RectShape : TextShape() {
    val drawRect = Rect()
    var drawRectColor = Color.WHITE
    override fun onDraw(canvasLayout: CanvasLayout, canvas: Canvas, paint: TextPaint) {
        paint.color = drawRectColor
        canvas.drawRect(drawRect, paint)
        canvasLayout.resetPaint()
        super.onDraw(canvasLayout, canvas, paint)
    }
}