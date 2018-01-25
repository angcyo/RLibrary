package com.angcyo.uiview.accessibility.shape

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import com.angcyo.uiview.accessibility.CanvasLayout
import com.angcyo.uiview.kotlin.textHeight
import com.angcyo.uiview.utils.ScreenUtil

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/01/25 15:03
 * 修改人员：Robi
 * 修改时间：2018/01/25 15:03
 * 修改备注：
 * Version: 1.0.0
 */
open class TextShape : BaseShape() {

    /**文本左上角的绘制坐标, y轴会自动+上文本的高度*/
    var textDrawX = 0f
    var textDrawY = 0f

    /**需要绘制的文本*/
    var drawText = ""
    var drawTextSize = 9 * ScreenUtil.density()

    var drawTextColor = Color.WHITE

    override fun onDraw(canvasLayout: CanvasLayout, canvas: Canvas, paint: TextPaint) {
        paint.strokeWidth = 1f
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.color = drawTextColor
        paint.textSize = drawTextSize
        canvas.drawText(drawText, textDrawX, textDrawY + paint.textHeight(), paint)
    }

}