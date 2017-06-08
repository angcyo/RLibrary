package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：圆形图片
 * 创建人员：Robi
 * 创建时间：2017/06/08 17:40
 * 修改人员：Robi
 * 修改时间：2017/06/08 17:40
 * 修改备注：
 * Version: 1.0.0
 */
class CircleImageView(context: Context, attributeSet: AttributeSet? = null) : AppCompatImageView(context, attributeSet) {

    val clipPath = Path()

    val paint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }

    override fun onDraw(canvas: Canvas) {
        val size = Math.min(measuredHeight - paddingTop - paddingBottom, measuredWidth - paddingLeft - paddingRight)
        val cx = (paddingLeft + size / 2).toFloat()
        val cy = (paddingTop + size / 2).toFloat()
        val cr = (size / 2).toFloat()

        clipPath.addCircle(cx, cy, cr, Path.Direction.CW)
        canvas.clipPath(clipPath)
        super.onDraw(canvas)

        paint.style = Paint.Style.STROKE
        paint.color = Color.WHITE
        canvas.drawCircle(cx, cy, cr, paint)
    }
}