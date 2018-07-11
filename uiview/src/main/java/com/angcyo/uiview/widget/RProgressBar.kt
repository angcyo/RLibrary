package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.angcyo.uiview.R
import com.angcyo.uiview.draw.RDrawProgress
import com.angcyo.uiview.kotlin.density
import com.angcyo.uiview.kotlin.exactlyMeasure

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：支持圆形, 矩形, 不明确的进度, (特殊支持分段进度)
 * 创建人员：Robi
 * 创建时间：2017/09/21 09:05
 * 修改人员：Robi
 * 修改时间：2017/09/21 09:05
 * 修改备注：
 * Version: 1.0.0
 */
open class RProgressBar(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet) {

    var rDrawProgress: RDrawProgress = RDrawProgress(this, attributeSet)
    var equWidth = false

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.RProgressBar)
        equWidth = typedArray.getBoolean(R.styleable.RProgressBar_r_equ_width, equWidth)
        typedArray.recycle()
    }

    var maxProgress: Int = 0
        get() = rDrawProgress.maxProgress
        set(value) {
            field = value
            rDrawProgress.maxProgress = value
        }

    /**当前进度*/
    var curProgress: Int = 0
        get() = rDrawProgress.curProgress
        set(value) {
            field = value
            rDrawProgress.curProgress = value
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        var heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)

        if (equWidth) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec)
        } else {
            if (widthMode != MeasureSpec.EXACTLY) {
                widthSize = if (rDrawProgress.progressBarType != RDrawProgress.PROGRESS_TYPE_CIRCLE) {
                    (100 * density).toInt()
                } else {
                    (30 * density).toInt()
                } + paddingLeft + paddingRight
            }

            if (heightMode != MeasureSpec.EXACTLY) {
                heightSize = if (rDrawProgress.progressBarType != RDrawProgress.PROGRESS_TYPE_CIRCLE) {
                    (10 * density).toInt()
                } else {
                    (30 * density).toInt()
                } + paddingTop + paddingBottom
            }

            super.onMeasure(exactlyMeasure(widthSize), exactlyMeasure(heightSize))
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        rDrawProgress.onLayout(changed, left, top, right, bottom)
    }

    /**项目特殊定制功能, 分段进度*/
    private val stepPointList = mutableListOf<Int>()
    var stepPointWidth = 2 * density
    var stepPointColor = Color.WHITE

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
//        if (isInEditMode) {
//            canvas.drawColor(Color.BLACK)
//            val tag = tag
//            if (tag != null) {
//                progressBarType = tag.toString().toInt()
//            }
//        }

        rDrawProgress.onDraw(canvas)

        canvas.save()
        canvas.translate(rDrawProgress.viewDrawRect.left.toFloat(), rDrawProgress.viewDrawRect.top.toFloat())
        when (rDrawProgress.progressBarType) {
            RDrawProgress.PROGRESS_TYPE_RECT -> {
                stepPointList.map { point ->
                    val px = point * 1f / rDrawProgress.maxProgress * rDrawProgress.viewDrawRect.width()
                    rDrawProgress.tempRectF.set(px - stepPointWidth / 2,
                            0f, px + stepPointWidth / 2, rDrawProgress.viewDrawRect.height().toFloat())
                    rDrawProgress.mBasePaint.style = Paint.Style.FILL
                    rDrawProgress.mBasePaint.color = stepPointColor
                    canvas.drawRect(rDrawProgress.tempRectF, rDrawProgress.mBasePaint)
                }
            }
        }
        canvas.restore()
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        rDrawProgress.onDetachedFromWindow()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        rDrawProgress.onAttachedToWindow()
    }

    fun addStepPoint(vararg point: Int /*可变参数*/) {
        for (p in point) {
            if (stepPointList.contains(p)) {
            } else {
                stepPointList.add(p)
            }
        }
        postInvalidate()
    }

    fun clearStepPoint() {
        stepPointList.clear()
        postInvalidate()
    }

    fun getLastStepPoint(): Int {
        if (stepPointList.isEmpty()) {
            return 0
        }
        return stepPointList.last()
    }

    fun getPointList() = stepPointList

    fun removeLastStepPoint(): Boolean {
        if (!stepPointList.isEmpty()) {
            stepPointList.removeAt(stepPointList.size - 1)
            rDrawProgress.curProgress = getLastStepPoint()
            return true
        }
        return false
    }
}