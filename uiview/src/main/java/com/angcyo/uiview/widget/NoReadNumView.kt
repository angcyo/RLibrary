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
import com.angcyo.uiview.kotlin.minValue
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
            style = Paint.Style.FILL
            strokeWidth = 1f
        }
    }

    var showNoReadNum = true

    var noReadTextSize = 8 * density

    /**未读消息数量, -1表示不显示, 0表示显示一个小红点, 1..99显示数字+小红点, 99+显示特殊*/
    var noReadNum = -1
        set(value) {
            field = value
            if (needMeasure) {
                requestLayout()
            } else {
                postInvalidate()
            }
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

    init {
        if (isInEditMode && tag != null) {
            noReadNum = 99
        }
    }

    protected var needMeasure = false
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        var heightMode = MeasureSpec.getMode(heightMeasureSpec)

        if (widthMode != MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
            needMeasure = true
            var dW = 0
            var dH = 0

            when {
                noReadNum == 0 -> {
                    dW = redDotDrawable.intrinsicWidth
                    dH = redDotDrawable.intrinsicHeight
                }
                noReadNum in 1..99 -> {
                    resetNewMessageDrawable()

                    dW = newMessageDrawable.bounds.width()
                    dH = newMessageDrawable.bounds.height()
                }
                (noReadNum > 99) -> {
                    dW = ninetyNineDrawable.intrinsicWidth
                    dH = ninetyNineDrawable.intrinsicHeight
                }
            }

            val width = dW + paddingLeft + paddingRight
            val height = Math.max(dH.toFloat(), mPaint.textHeight()) + paddingTop + paddingBottom
            setMeasuredDimension(width, height.toInt())
        } else {
            needMeasure = false
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (showNoReadNum) {
            drawNoReadDrawable(canvas)
        }
    }

    protected fun resetNewMessageDrawable() {
        mPaint.textSize = noReadTextSize
        val string = noReadNum.toString()
        val topOffset = 2 * density
        val leftOffset = 2 * topOffset

        val height = (mPaint.textHeight() + topOffset).toInt()
        val width = (mPaint.measureText(string, 0, string.length) + leftOffset).toInt().minValue(height)

        newMessageDrawable.setBounds(0, 0, width, height)
    }

    open fun drawNoReadDrawable(canvas: Canvas) {
        //绘制未读消息
        canvas.save()
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        when {
            noReadNum == 0 -> {
                redDotDrawable.draw(canvas)
            }
            noReadNum in 1..99 -> {
                val string = noReadNum.toString()
                val topOffset = 2 * density

                newMessageDrawable.draw(canvas)
                mPaint.color = Color.WHITE
                val textWidth = mPaint.measureText(string, 0, string.length)
                canvas.drawText(string, newMessageDrawable.bounds.width() / 2 - textWidth / 2,
                        measuredHeight - paddingBottom - mPaint.descent() - topOffset / 2, mPaint)
            }
            (noReadNum > 99) -> {
                ninetyNineDrawable.draw(canvas)
            }
        }
        canvas.restore()
    }

}