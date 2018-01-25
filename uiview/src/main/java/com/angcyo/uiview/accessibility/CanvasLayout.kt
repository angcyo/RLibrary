package com.angcyo.uiview.accessibility

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import android.widget.FrameLayout
import com.angcyo.uiview.R
import com.angcyo.uiview.accessibility.shape.BaseShape
import com.angcyo.uiview.kotlin.density
import com.angcyo.uiview.kotlin.getColor

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：自定义画布, 想画啥, 画啥
 * 创建人员：Robi
 * 创建时间：2018/01/25 14:52
 * 修改人员：Robi
 * 修改时间：2018/01/25 14:52
 * 修改备注：
 * Version: 1.0.0
 */
open class CanvasLayout(context: Context, attributeSet: AttributeSet? = null) : FrameLayout(context, attributeSet) {
    val shapeList = mutableListOf<BaseShape>()

    fun clear() {
        shapeList.clear()
        postInvalidate()
    }

    fun addShape(shape: BaseShape) {
        shapeList.add(shape)
        postInvalidate()
    }

    fun addShapeList(list: List<BaseShape>) {
        shapeList.clear()
        shapeList.addAll(list)
        postInvalidate()
    }

    val paint: TextPaint by lazy {
        TextPaint()
    }

    fun resetPaint() {
        paint.flags = Paint.ANTI_ALIAS_FLAG
        paint.textSize = 12 * density
        paint.textAlign = Paint.Align.LEFT //文本在点的左边开始绘制, 系统默认
        if (isInEditMode) {
            paint.color = Color.BLACK
        } else {
            paint.color = getColor(R.color.base_text_color)
        }
        paint.strokeWidth = 1 * density
        paint.style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (i in shapeList.indices) {
            resetPaint()
            canvas.save()
            shapeList[i].onDraw(this, canvas, paint)
            canvas.restore()
        }
    }
}