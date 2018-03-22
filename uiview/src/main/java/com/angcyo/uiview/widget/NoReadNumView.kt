package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.textHeight

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/03/22 08:39
 * 修改人员：Robi
 * 修改时间：2018/03/22 08:39
 * 修改备注：
 * Version: 1.0.0
 */
open class NoReadNumView(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet) {

    val density: Float by lazy { resources.displayMetrics.density }

    val mPaint: TextPaint by lazy {
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 1 * density
        }
    }

    var showNoReadNum = true

    /**未读消息数量, -1表示不显示, 0表示显示一个小红点, 1..99显示数字+小红点, 99+显示特殊*/
    var noReadNum = -1
        set(value) {
            field = value
            postInvalidate()
        }

    /**小红点*/
    val redDotDrawable: Drawable by lazy {
        val drawable = ContextCompat.getDrawable(context, R.drawable.skin_tips_dot_png)!!
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable
    }
    /**.9格式的小红点*/
    val newMessageDrawable: Drawable by lazy {
        val drawable = ContextCompat.getDrawable(context, R.drawable.skin_tips_newmessage_9_png)!!
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable
    }
    /**99+*/
    val ninetyNineDrawable: Drawable by lazy {
        val drawable = ContextCompat.getDrawable(context, R.drawable.skin_tips_newmessage_ninetynine_png)!!
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        var heightMode = MeasureSpec.getMode(heightMeasureSpec)

        if (widthMode != MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
            val width = ninetyNineDrawable.intrinsicWidth + paddingLeft + paddingRight
            val height = Math.max(ninetyNineDrawable.intrinsicHeight.toFloat(), mPaint.textHeight()) + paddingTop + paddingBottom
            setMeasuredDimension(width * 2, (height * 2).toInt())
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }


    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (showNoReadNum) {
            drawNoReadDrawable(canvas)
        }
    }

    fun drawNoReadDrawable(canvas: Canvas) {
        //绘制未读消息
        canvas.save()
        canvas.translate((measuredWidth / 2).toFloat(), paddingTop + 4 * density)//移动到中间位置
        when {
            noReadNum == 0 -> {
                redDotDrawable.draw(canvas)
            }
            noReadNum in 1..99 -> {
                mPaint.textSize = 9 * density
                val string = noReadNum.toString()
                val paddingTop = 2 * density
                val paddingLeft = 2 * paddingTop
                newMessageDrawable.setBounds(0, 0,
                        (mPaint.measureText(string, 0, string.length) + 2 * paddingLeft).toInt(),
                        (mPaint.descent() - mPaint.ascent() + 2 * paddingTop).toInt())
                newMessageDrawable.draw(canvas)
                mPaint.color = Color.WHITE
                canvas.drawText(string, paddingLeft, paddingTop - mPaint.ascent(), mPaint)
            }
            (noReadNum > 99) -> {
                ninetyNineDrawable.draw(canvas)
            }
        }
        canvas.restore()
    }

}