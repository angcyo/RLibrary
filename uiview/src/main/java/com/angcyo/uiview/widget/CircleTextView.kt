package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.*
import com.angcyo.uiview.utils.RUtils

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/06/27 16:42
 * 修改人员：Robi
 * 修改时间：2017/06/27 16:42
 * 修改备注：
 * Version: 1.0.0
 */
class CircleTextView(context: Context, attributeSet: AttributeSet? = null) : AppCompatTextView(context, attributeSet) {

    var circleTextList: ArrayList<String>? = null
        set(value) {
            field = value
            postInvalidate()
        }

    /**填充大小*/
    var circlePadding = 4 * density
    var space = 2 * density

    val itemCount: Int
        get() {
            if (circleTextList == null) {
                return 0
            } else {
                return circleTextList!!.size
            }
        }

    val circlePaint: Paint by lazy {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.style = Paint.Style.FILL
        p
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (circleTextList == null) {
            setMeasuredDimension(0, 0)
        } else {
            var textWidth = 0f
            var textHeight = textHeight()
            paint.textSize = textSize
            circleTextList?.map {
                val w = paint.measureText(it)
                textHeight = Math.max(textHeight, w)
                textWidth += (textHeight + 2 * circlePadding)
            }
            setMeasuredDimension((textWidth + paddingLeft + paddingRight + Math.max(0, itemCount - 1) * space).toInt(),
                    Math.max((textHeight + 2 * circlePadding).toInt() + paddingTop + paddingBottom, measuredHeight))
        }
    }

    override fun onDraw(canvas: Canvas) {
        //super.onDraw(canvas)
        var left = paddingLeft
        circlePaint.color = getColor(R.color.base_red_d85940)
        paint.color = currentTextColor
        paint.textSize = textSize
        circleTextList?.map {
            val w = Math.max(paint.measureText(it), textHeight())
            val r = w / 2 + circlePadding

            canvas.drawCircle(left + r, getDrawCenterCy(), r, circlePaint)

            canvas.drawText(it, left.toFloat() + circlePadding, getDrawCenterTextCy(), paint)

            left += (2 * r + space).toInt()
        }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        ensureList()
        if (!text.isNullOrEmpty()) {
            circleTextList?.addAll(RUtils.split(text as String))
        }
    }

    private fun ensureList() {
        if (circleTextList == null) {
            circleTextList = arrayListOf()
        }
        circleTextList!!.clear()
    }
}