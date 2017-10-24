package com.angcyo.uiview.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import com.angcyo.uiview.kotlin.density
import com.angcyo.uiview.kotlin.viewDrawHeight
import com.angcyo.uiview.kotlin.viewDrawWith
import com.angcyo.uiview.resources.AnimUtil
import com.angcyo.uiview.skin.SkinHelper

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：模仿抖音进度条, 刷刷刷, 闪电般的加载效果
 * 创建人员：Robi
 * 创建时间：2017/09/25 17:06
 * 修改人员：Robi
 * 修改时间：2017/09/25 17:06
 * 修改备注：
 * Version: 1.0.0
 */

class DYProgressBar(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet) {
    init {

    }

    /**进度条颜色*/
    var progressColor = Color.WHITE

    /**保底进度*/
    var startProgress = 0.1f

    var bgLineHeight = 1 * density
    var bgLineColor = SkinHelper.getTranColor(progressColor, 0x80)

    private val drawProgressRect by lazy {
        RectF()
    }

    private val drawBgLineRect by lazy {
        RectF()
    }

    private val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }

    private var drawProgressColor = progressColor

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isInEditMode) {
            canvas.drawColor(Color.RED)
        }

        paint.color = bgLineColor
        val top = paddingTop + viewDrawHeight / 2 - bgLineHeight / 2f
        drawBgLineRect.set(paddingLeft.toFloat(), top, (measuredWidth - paddingRight).toFloat(), top + bgLineHeight)
        canvas.drawRect(drawBgLineRect, paint)

        paint.color = drawProgressColor
        canvas.drawRect(drawProgressRect, paint)

    }

    private fun calcProgressRect(progress: Float) {
        val width = (progress + startProgress) * (viewDrawWith / 2)
        val left = paddingLeft + viewDrawWith / 2 - width
        val right = paddingLeft + viewDrawWith / 2 + width
        drawProgressRect.set(left, paddingTop.toFloat(), right, measuredHeight - paddingBottom.toFloat())
    }

    private var valueAnimator: ValueAnimator? = null

    /**开始进度动画*/
    fun startAnimator() {
        if (valueAnimator == null) {
            valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                interpolator = AccelerateInterpolator()
                duration = 300
                repeatMode = ValueAnimator.RESTART
                repeatCount = ValueAnimator.INFINITE
                addUpdateListener { animation ->
                    val animatedValue: Float = animation.animatedValue as Float
                    drawProgressColor = AnimUtil.evaluateColor(animatedValue, progressColor, bgLineColor /*Color.TRANSPARENT*/)
                    calcProgressRect(animatedValue)
                    postInvalidate()
                }
                start()
            }
        }
    }

    /**停止进度动画*/
    fun stopAnimator() {
        drawProgressColor = progressColor
        valueAnimator?.cancel()
        valueAnimator = null
        drawProgressRect.setEmpty()
        postInvalidate()
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility != View.VISIBLE) {
            stopAnimator()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        //startAnimator()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimator()
    }
}