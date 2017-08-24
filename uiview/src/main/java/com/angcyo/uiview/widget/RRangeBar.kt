package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.support.v4.view.MotionEventCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.density
import com.angcyo.uiview.kotlin.textHeight
import com.angcyo.uiview.kotlin.textWidth
import com.angcyo.uiview.skin.SkinHelper

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：范围选择bar, (0-100)的比例范围, 自行转换其他数值
 * 创建人员：Robi
 * 创建时间：2017/08/04 08:57
 * 修改人员：Robi
 * 修改时间：2017/08/04 08:57
 * 修改备注：
 * Version: 1.0.0
 */
open class RRangeBar(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet) {

    /**允许拖动的最大值最小值范围*/
    var MAX_VALUE = 100
    var MIN_VALUE = 0
    /**允许最大值与最小值之间的范围*/
    var MIN_RANGE = 0

    /**轨道颜色*/
    var trackColor: Int = Color.parseColor("#999999")
    /**轨道高度*/
    var trackHeight: Int = 2 * density.toInt()
    /**轨道圆角半径*/
    var trackRoundRadius: Int = 2 * density.toInt()

    /**浮子的半径*/
    var thumbRadius: Int = 10 * density.toInt()
    var thumbColor: Int = Color.WHITE
    var thumbOutLineColor: Int = Color.parseColor("#E0E0E0")

    var progressColor: Int = Color.parseColor("#0BD1A0")
    var textColor: Int = Color.parseColor("#0BD1A0")

    var textSize: Float = 12f * density
    var textOffset: Int = 2 * density.toInt()

    var currentMinValue: Int = 0
        set(value) {
            field = ensureValue(value)
            postInvalidate()
        }
    var currentMaxValue: Int = 100
        set(value) {
            field = ensureValue(value)
            postInvalidate()
        }

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.RRangeBar)
        MAX_VALUE = typedArray.getInt(R.styleable.RRangeBar_r_max_value, 100)
        MIN_VALUE = typedArray.getInt(R.styleable.RRangeBar_r_min_value, 0)
        MIN_RANGE = typedArray.getInt(R.styleable.RRangeBar_r_min_range, 0)
        currentMinValue = typedArray.getInt(R.styleable.RRangeBar_r_current_min_value, 0)
        currentMaxValue = typedArray.getInt(R.styleable.RRangeBar_r_current_max_value, 100)
        typedArray.recycle()

        if (!isInEditMode) {
            progressColor = SkinHelper.getSkin().getThemeTranColor(0x80)
            textColor = SkinHelper.getSkin().themeSubColor
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        if (widthMode != MeasureSpec.EXACTLY) {
            widthSize = paddingLeft + paddingRight + (100 * density + 2 * thumbRadius).toInt()
        }

        if (heightMode != MeasureSpec.EXACTLY) {
            paint.textSize = textSize
            heightSize = (paddingTop + paddingBottom + textHeight(paint) + textOffset + Math.max(trackHeight, 2 * thumbRadius)).toInt()
        }

        setMeasuredDimension(widthSize, heightSize)
    }

    private val trackRectF: RectF by lazy {
        RectF()
    }
    private val progressRectF: RectF by lazy {
        RectF()
    }

    private val paint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }

    private val trackTop: Float
        get() {
            paint.textSize = textSize
            if (trackHeight > 2 * thumbRadius) {
                return paddingTop + textHeight(paint) + textOffset
            } else {
                return paddingTop + textHeight(paint) + textOffset + (2 * thumbRadius - trackHeight) / 2
            }
        }

    /**最小浮子的矩形坐标, 用来判断点击事件*/
    private val minValueRectF = RectF()
    private val maxValueRectF = RectF()

    override fun onDraw(canvas: Canvas) {
        //canvas.drawColor(Color.parseColor("#80000000"))

        paint.style = Paint.Style.FILL
        paint.color = trackColor
        paint.textSize = textSize

        //绘制轨道
        trackRectF.set((paddingLeft + thumbRadius).toFloat(), trackTop,
                measuredWidth.toFloat() - paddingRight - thumbRadius, trackTop + trackHeight)
        canvas.drawRoundRect(trackRectF, trackRoundRadius.toFloat(), trackRoundRadius.toFloat(), paint)

        //计算浮子矩形坐标位置
        calcThumbValueRect()

        //绘制进度
        paint.style = Paint.Style.FILL
        paint.color = progressColor
        progressRectF.set(minValueRectF.centerX(), trackRectF.top, maxValueRectF.centerX(), trackRectF.bottom)
        canvas.drawRoundRect(progressRectF, trackRoundRadius.toFloat(), trackRoundRadius.toFloat(), paint)

        //绘制浮子
        drawThumb(canvas, minValueRectF)
        drawThumb(canvas, maxValueRectF)

        //绘制提示文本
        drawText(canvas, minValueRectF, currentMinValue)
        drawText(canvas, maxValueRectF, currentMaxValue)
    }

    private fun drawThumb(canvas: Canvas, rectF: RectF) {
        paint.style = Paint.Style.FILL
        paint.color = thumbColor
        canvas.drawCircle(rectF.centerX(), rectF.centerY(), thumbRadius.toFloat(), paint)

        //绘制浮子外圈
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2 * density
        paint.color = thumbOutLineColor
        canvas.drawCircle(rectF.centerX(), rectF.centerY(), thumbRadius.toFloat() - 1 * density, paint)
    }

    private fun drawText(canvas: Canvas, rectF: RectF, progress: Int) {
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.color = textColor
        paint.strokeWidth = 1f

        val text: String = rangeListener?.getProgressText(progress) ?: "$progress%"
        canvas.drawText(text,
                Math.min(Math.max(0f, rectF.centerX() - textWidth(paint, text) / 2),
                        viewWidth - textWidth(paint, text)),
                paddingTop + textHeight(paint) - paint.descent(),
                paint)
    }

    private val viewWidth: Int
        get() {
            return measuredWidth - paddingLeft - paddingRight
        }

    private fun calcThumbValueRect() {
        val x = (viewWidth - 2 * thumbRadius) * (currentMinValue.toFloat() / 100f) + paddingLeft
        minValueRectF.set(
                x,
                trackTop + trackHeight / 2 - thumbRadius,
                x + 2 * thumbRadius,
                trackTop + trackHeight / 2 + thumbRadius
        )

        val x2 = (viewWidth - 2 * thumbRadius) * (currentMaxValue.toFloat() / 100f) + paddingLeft
        maxValueRectF.set(
                x2,
                trackTop + trackHeight / 2 - thumbRadius,
                x2 + 2 * thumbRadius,
                trackTop + trackHeight / 2 + thumbRadius
        )
    }

    /**手势按在那个点上, 不分最大点和最小点*/
    private var touchValue: Int = -1
    private var notTouchValue: Int = -1

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = MotionEventCompat.getActionMasked(event)
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                if (minValueRectF.contains(event.x, event.y)) {
                    touchValue = currentMinValue
                    notTouchValue = currentMaxValue
                } else if (maxValueRectF.contains(event.x, event.y)) {
                    touchValue = currentMaxValue
                    notTouchValue = currentMinValue
                } else {
                    touchValue = -1
                    notTouchValue = -1
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchValue >= 0) {
                    parent.requestDisallowInterceptTouchEvent(true)

                    //按在了点上
                    touchValue = ((event.x - paddingLeft - thumbRadius) / (viewWidth - 2 * thumbRadius) * 100).toInt()
                    touchValue = ensureValue(touchValue)
                    //L.e("call: onTouchEvent ->$viewWidth ${event.x}  $touchValue")

                    if (Math.abs(touchValue - notTouchValue) >= MIN_RANGE) {
                        currentMinValue = Math.min(touchValue, notTouchValue)
                        currentMaxValue = Math.max(touchValue, notTouchValue)

                        postInvalidate()

                        rangeListener?.onRangeChange(currentMinValue, currentMaxValue)
                    }

                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }

    private fun ensureValue(value: Int): Int {
        return Math.min(Math.max(MIN_VALUE, value), MAX_VALUE)
    }

    var rangeListener: OnRangeListener? = null

    interface OnRangeListener {
        fun onRangeChange(minValue: Int, maxValue: Int)
        fun getProgressText(progress: Int): String
    }
}