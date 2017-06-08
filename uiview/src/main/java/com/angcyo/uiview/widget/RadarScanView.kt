package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import com.angcyo.uiview.kotlin.density
import com.angcyo.uiview.skin.SkinHelper

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
class RadarScanView(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet),
        Choreographer.FrameCallback {
    override fun doFrame(frameTimeNanos: Long) {
        sweepAngle++

        mChoreographer.postFrameCallback(this)
    }

    val paint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }

    /**圈圈之间的距离*/
    var circleSpace = 10 * density

    var circleWidth = 1 * density

    var scanDrawRectF = RectF()

    val scanMatrix = Matrix()

    var sweepAngle = 0f
        set(value) {
            field = value % 360
            scanMatrix.reset()
            scanMatrix.postRotate(field, (measuredWidth / 2).toFloat(), (measuredHeight / 2).toFloat())
            postInvalidate()
        }

    val mChoreographer: Choreographer by lazy {
        Choreographer.getInstance()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mChoreographer.postFrameCallback(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mChoreographer.removeFrameCallback(this)
    }

    override fun onDraw(canvas: Canvas) {
        val minRadius = 10 * density

        //有效的绘制直径
        val size = Math.min(measuredWidth, measuredHeight)

        if (isInEditMode) {
            paint.color = SkinHelper.getTranColor(Color.RED, 0x40)
        } else {
            paint.color = SkinHelper.getTranColor(SkinHelper.getSkin().themeSubColor, 0x40)
        }

        //绘制圆圈
        canvas.save()
        canvas.translate((measuredWidth / 2).toFloat(), (measuredHeight / 2).toFloat())
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = circleWidth //圈的厚度
        var circleCount = 0
        var circleRadius = circleCount * circleSpace + minRadius
        while (circleRadius <= size / 2) {
            canvas.drawCircle(0f, 0f, circleRadius - circleWidth, paint)
            circleCount++
            if (circleCount == 1 || circleCount == 2) {
                canvas.drawCircle(0f, 0f, circleRadius - circleWidth + circleCount * 2 * density, paint)
            }
            circleRadius = circleCount * circleSpace + minRadius
        }

        //绘制几个无规则的圆
        canvas.drawCircle(0f, 0f, 4 * density, paint)

        canvas.restore()

        //绘制扫描弧度
        canvas.save()
        circleRadius = (circleCount - 1) * circleSpace + minRadius
        paint.style = Paint.Style.FILL
        val arcOffset = circleWidth / 2
        scanDrawRectF.set(measuredWidth / 2 - circleRadius + arcOffset, measuredHeight / 2 - circleRadius + arcOffset,
                measuredWidth / 2 + circleRadius - arcOffset, measuredHeight / 2 + circleRadius - arcOffset)

        val shaderColor: Int
        val shaderStartColor: Int
        if (isInEditMode) {
            shaderStartColor = SkinHelper.getTranColor(Color.RED, 0x40)
            shaderColor = SkinHelper.getTranColor(Color.RED, 0xFF)
        } else {
            shaderStartColor = SkinHelper.getTranColor(SkinHelper.getSkin().themeSubColor, 0x40)
            shaderColor = SkinHelper.getSkin().themeSubColor// SkinHelper.getTranColor(SkinHelper.getSkin().themeSubColor, 0xFF)
        }

        paint.shader = object : SweepGradient((measuredWidth / 2).toFloat(), (measuredWidth / 2).toFloat(),
                intArrayOf(shaderStartColor, shaderColor, shaderColor), floatArrayOf(0f, 0.3f, 1f)) {
        }
        if (!isInEditMode) {
            canvas.concat(scanMatrix)
        }
        canvas.drawArc(scanDrawRectF, 0f, (36 * 6).toFloat(), true, paint)
        canvas.restore()

    }
}