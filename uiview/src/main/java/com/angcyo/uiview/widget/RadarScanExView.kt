package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.density
import com.angcyo.uiview.kotlin.getColor
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
class RadarScanExView(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet),
        Choreographer.FrameCallback {
    override fun doFrame(frameTimeNanos: Long) {
        sweepAngle++

        mChoreographer.postFrameCallback(this)
    }

    /**田字格线之间的距离*/
    var lineSpace = 10 * density

    private val lineCirclePath: Path by lazy {
        Path()
    }
    private val drawLinePath: Path by lazy {
        Path()
    }
    private val linePaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }

    val paint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }

    var circleWidth = 2 * density

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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = Math.min(measuredHeight, measuredWidth)
        setMeasuredDimension(size, size)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mChoreographer.removeFrameCallback(this)
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == VISIBLE) {
            mChoreographer.removeFrameCallback(this)
            mChoreographer.postFrameCallback(this)
        } else {
            mChoreographer.removeFrameCallback(this)
        }
    }

    override fun onDraw(canvas: Canvas) {
        //有效的绘制直径
        val size = Math.min(measuredWidth, measuredHeight)

        val centerX = measuredWidth / 2
        val centerY = measuredHeight / 2

        var circleRadius = size / 2f

        //绘制田字格
        canvas.save()
        lineCirclePath.reset()
        lineCirclePath.addCircle(centerX.toFloat(), centerY.toFloat(), circleRadius, Path.Direction.CW)
        canvas.clipPath(lineCirclePath)
        linePaint.color = getColor(R.color.default_base_line)
        linePaint.strokeWidth = 1 * density
        linePaint.style = Paint.Style.FILL_AND_STROKE
        val intervals = 1f * density
        linePaint.pathEffect = DashPathEffect(floatArrayOf(intervals, intervals), 0f)

        var lineTop = lineSpace
        while (lineTop < measuredHeight) {
            drawLinePath.reset()
            drawLinePath.moveTo(0.toFloat(), lineTop)
            drawLinePath.lineTo(measuredWidth.toFloat(), lineTop)

            canvas.drawPath(drawLinePath, linePaint)
            lineTop += lineSpace
        }

        var lineLeft = lineSpace
        while (lineLeft < measuredWidth) {
            drawLinePath.reset()
            drawLinePath.moveTo(lineLeft, 0.toFloat())
            drawLinePath.lineTo(lineLeft, measuredHeight.toFloat())

            canvas.drawPath(drawLinePath, linePaint)
            lineLeft += lineSpace
        }
        canvas.restore()

        //绘制扫描弧度
        canvas.save()
        val shaderColor: Int
        val shaderStartColor: Int
        val lineColor: Int

        if (isInEditMode) {
            paint.color = SkinHelper.getTranColor(Color.RED, 0x40)
            lineColor = Color.GREEN
            shaderStartColor = SkinHelper.getTranColor(Color.RED, 0x40)
            shaderColor = SkinHelper.getTranColor(Color.RED, 0xFF)
        } else {
            paint.color = SkinHelper.getTranColor(SkinHelper.getSkin().themeSubColor, 0x40)
            lineColor = SkinHelper.getSkin().themeSubColor
            shaderStartColor = SkinHelper.getTranColor(SkinHelper.getSkin().themeSubColor, 0x40)
            shaderColor = SkinHelper.getTranColor(SkinHelper.getSkin().themeSubColor, 0xAA) //SkinHelper.getSkin().themeSubColor// SkinHelper.getTranColor(SkinHelper.getSkin().themeSubColor, 0xFF)
        }

        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeWidth = circleWidth //圈的厚度

        val arcOffset = circleWidth / 2
        scanDrawRectF.set(centerX - circleRadius + arcOffset, centerY - circleRadius + arcOffset,
                centerX + circleRadius - arcOffset, centerY + circleRadius - arcOffset)

        //雷达区域扫描arc
        paint.shader = object : SweepGradient((measuredWidth / 2).toFloat(), (measuredWidth / 2).toFloat(),
                intArrayOf(Color.TRANSPARENT, shaderStartColor, shaderColor), floatArrayOf(0f, 0.6f, 1f)) {
        }
        if (!isInEditMode) {
            canvas.concat(scanMatrix)
        }
        canvas.drawArc(scanDrawRectF, 0f, 360f /*(36 * 6).toFloat()*/, true, paint)

        //外圈轮廓渐变arc
        paint.style = Paint.Style.STROKE
        paint.shader = object : SweepGradient((measuredWidth / 2).toFloat(), (measuredWidth / 2).toFloat(),
                intArrayOf(Color.TRANSPARENT, shaderStartColor, shaderColor), floatArrayOf(0f, 0.1f, 1f)) {
        }
        canvas.drawArc(scanDrawRectF, 0f, 360f /*(36 * 6).toFloat()*/, false, paint)

        //开始扫描线
        paint.color = lineColor
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeWidth = 2 * circleWidth
//        drawLinePath.reset()
//        drawLinePath.moveTo(0.toFloat(), lineTop)
//        drawLinePath.lineTo(measuredWidth.toFloat(), lineTop)

        canvas.drawLine(centerX.toFloat(), centerY.toFloat(), measuredWidth.toFloat(), centerY.toFloat(), paint)
        canvas.restore()

//        //绘制圆圈
//        canvas.save()
//        canvas.translate((measuredWidth / 2).toFloat(), (measuredHeight / 2).toFloat())
//        paint.shader = object : LinearGradient(0f, 0f, (measuredWidth / 2).toFloat(), (measuredWidth / 2).toFloat(),
//                intArrayOf(Color.TRANSPARENT, Color.RED), floatArrayOf(0f, 1f), TileMode.CLAMP) {
//        }
//        paint.style = Paint.Style.FILL_AND_STROKE
//        paint.strokeWidth = circleWidth //圈的厚度
//        canvas.drawCircle(0f, 0f, circleRadius - circleWidth / 2, paint)
//
//        canvas.restore()
    }
}