package com.angcyo.uiview.widget

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.density
import com.angcyo.uiview.kotlin.maxValue
import com.angcyo.uiview.skin.SkinHelper

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
    companion object {
        /**圆形进度*/
        val PROGRESS_TYPE_CIRCLE = 1
        /**矩形进度*/
        val PROGRESS_TYPE_RECT = 2
        /**圆角矩形进度*/
        val PROGRESS_TYPE_ROUND = 3
        /**不明确的进度*/
        val PROGRESS_TYPE_CIRCLE_INCERTITUDE = 4
        val PROGRESS_TYPE_RECT_INCERTITUDE = 5
        val PROGRESS_TYPE_ROUND_INCERTITUDE = 6
    }

    var progressBarType = PROGRESS_TYPE_RECT
        set(value) {
            field = value
            if (field > 3) {
                startIncertitudeAnimator()
            }
            postInvalidate()
        }

    /**进度条背景颜色*/
    var progressBgColor by RefreshProperty(Color.parseColor("#80000000"))
    /**进度条颜色*/
    var progressColor by RefreshProperty(Color.WHITE)
    /**第二进度*/
    var progressSecondColor by RefreshProperty(Color.GRAY)

    /**最大进度*/
    var maxProgress by RefreshProperty(100)
    /**当前进度*/
    var curProgress by RefreshProperty(0)
    /**第二进度*/
    var secondProgress by RefreshProperty(0)

    /**圆角进度条的圆角半径*/
    var progressRoundRadius by RefreshProperty(6 * density)
    /**圆形进度条的宽度*/
    var progressWidth by RefreshProperty(3 * density)

    /**圆形进度条, 开始绘制的角度*/
    var circleProgressStartAngle = -90f

    /**没有进度的时候, 是否绘制背景*/
    var drawBgOnNoProgress = true

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.RProgressBar)
        progressBarType = typedArray.getInteger(R.styleable.RProgressBar_r_progress_bar_type, progressBarType)
        progressBgColor = typedArray.getColor(R.styleable.RProgressBar_r_progress_bg_color, progressBgColor)
        progressColor = typedArray.getColor(R.styleable.RProgressBar_r_progress_color, progressColor)
        progressSecondColor = typedArray.getColor(R.styleable.RProgressBar_r_second_progress_color, progressSecondColor)
        maxProgress = typedArray.getInteger(R.styleable.RProgressBar_r_max_progress, maxProgress)
        curProgress = typedArray.getInteger(R.styleable.RProgressBar_r_cur_progress, curProgress)
        secondProgress = typedArray.getInteger(R.styleable.RProgressBar_r_second_progress, secondProgress)
        progressWidth = typedArray.getDimensionPixelOffset(R.styleable.RProgressBar_r_progress_width, progressWidth.toInt()).toFloat()
        progressRoundRadius = typedArray.getDimensionPixelOffset(R.styleable.RProgressBar_r_progress_round_radius, progressRoundRadius.toInt()).toFloat()
        drawBgOnNoProgress = typedArray.getBoolean(R.styleable.RProgressBar_r_draw_bg_on_no_progress, drawBgOnNoProgress)

        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    //视图允许的绘制区域
    private val viewDrawRect by lazy {
        Rect()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        viewDrawRect.set(paddingLeft, paddingTop, measuredWidth - paddingRight, measuredHeight - paddingBottom)
    }

    //画笔, 自定义view必备
    private val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }

    /**项目特殊定制功能, 分段进度*/
    private val stepPointList = mutableListOf<Int>()
    var stepPointWidth = 2 * density
    var stepPointColor = Color.WHITE

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isInEditMode) {
            canvas.drawColor(Color.BLACK)
            val tag = tag
            if (tag != null) {
                progressBarType = tag.toString().toInt()
            }
        }

        canvas.save()
        canvas.translate(viewDrawRect.left.toFloat(), viewDrawRect.top.toFloat())
        when (progressBarType) {
            PROGRESS_TYPE_CIRCLE -> drawCircle(canvas, false)
            PROGRESS_TYPE_RECT -> {
                drawRect(canvas, false)
                stepPointList.map { point ->
                    val px = point * 1f / maxProgress * viewDrawRect.width()
                    tempRectF.set(px - stepPointWidth / 2, 0f, px + stepPointWidth / 2, viewDrawRect.height().toFloat())
                    paint.style = Paint.Style.FILL
                    paint.color = stepPointColor
                    canvas.drawRect(tempRectF, paint)
                }
            }
            PROGRESS_TYPE_ROUND -> drawRound(canvas, false)
            PROGRESS_TYPE_CIRCLE_INCERTITUDE -> drawCircle(canvas, true)
            PROGRESS_TYPE_RECT_INCERTITUDE -> drawRect(canvas, true)
            PROGRESS_TYPE_ROUND_INCERTITUDE -> drawRound(canvas, true)
        }
        canvas.restore()
    }

    private val tempRectF by lazy {
        RectF()
    }

    //圆形进度, 不明确时, 开始的角度, 用来做无限旋转的动画
    private var incertitudeStartAngle = circleProgressStartAngle

    private fun drawCircle(canvas: Canvas, isIncertitude: Boolean) {
        fun p(progress: Int): Float = progress * 1f / maxProgress * 360f

        val size = viewDrawRect.width().maxValue(viewDrawRect.height())
        tempRectF.set(0F, 0F, size.toFloat(), size.toFloat())
        val inset = progressWidth + 1.1f
        tempRectF.inset(inset / 2, inset / 2)

        paint.style = Paint.Style.STROKE

        //进度背景
        if (drawBgOnNoProgress || curProgress > 0) {
            paint.color = progressBgColor
            paint.strokeWidth = progressWidth
            canvas.drawArc(tempRectF, 0f, 360f, false, paint)
        }

        //第二进度
        if (!isIncertitude) {
            paint.color = progressSecondColor
            paint.strokeWidth = inset
            canvas.drawArc(tempRectF, circleProgressStartAngle, p(secondProgress), false, paint)
        }

        //当前进度
        if (isIncertitude) {
            paint.color = progressSecondColor
            paint.strokeWidth = inset
            canvas.drawArc(tempRectF, incertitudeStartAngle, 90f, false, paint)
        } else {
            paint.color = progressColor
            paint.strokeWidth = inset
            canvas.drawArc(tempRectF, circleProgressStartAngle, p(curProgress), false, paint)
        }

    }

    private var incertitudeRectDrawColor = progressColor
    private fun drawRect(canvas: Canvas, isIncertitude: Boolean) {
        fun p(progress: Int) = tempRectF.set(0f, 0f,
                progress * 1f / maxProgress * viewDrawRect.width(), viewDrawRect.height().toFloat())

        tempRectF.set(0f, 0f, viewDrawRect.width().toFloat(), viewDrawRect.height().toFloat())
        paint.style = Paint.Style.FILL

        //进度背景
        if (drawBgOnNoProgress || curProgress > 0) {
            paint.color = progressBgColor
            canvas.drawRect(tempRectF, paint)
        }

        //第二进度
        if (!isIncertitude) {
            paint.color = progressSecondColor
            p(secondProgress)
            canvas.drawRect(tempRectF, paint)
        }

        //当前进度
        if (isIncertitude) {
            paint.color = incertitudeRectDrawColor
            p(maxProgress)
        } else {
            paint.color = progressColor
            p(curProgress)
        }
        canvas.drawRect(tempRectF, paint)
    }

    private fun drawRound(canvas: Canvas, isIncertitude: Boolean) {
        fun p(progress: Int) = tempRectF.set(0f, 0f,
                progress * 1f / maxProgress * viewDrawRect.width(), viewDrawRect.height().toFloat())

        tempRectF.set(0f, 0f, viewDrawRect.width().toFloat(), viewDrawRect.height().toFloat())
        paint.style = Paint.Style.FILL

        //进度背景
        if (drawBgOnNoProgress || curProgress > 0) {
            paint.color = progressBgColor
            canvas.drawRoundRect(tempRectF, progressRoundRadius, progressRoundRadius, paint)
        }

        //第二进度
        if (!isIncertitude) {
            paint.color = progressSecondColor
            p(secondProgress)
            canvas.drawRoundRect(tempRectF, progressRoundRadius, progressRoundRadius, paint)
        }

        //当前进度
        if (isIncertitude) {
            paint.color = incertitudeRectDrawColor
            p(maxProgress)
        } else {
            paint.color = progressColor
            p(curProgress)
        }
        canvas.drawRoundRect(tempRectF, progressRoundRadius, progressRoundRadius, paint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mColorAnimator?.cancel()
    }

    private var mColorAnimator: ValueAnimator? = null
    private fun startIncertitudeAnimator() {
        if (mColorAnimator == null) {
            mColorAnimator = ValueAnimator.ofObject(ArgbEvaluator(),
                    progressColor,
                    SkinHelper.getTranColor(progressColor, 0x10)).apply {
                interpolator = LinearInterpolator()
                addUpdateListener({ animation ->
                    incertitudeRectDrawColor = animation.animatedValue as Int//之后就可以得到动画的颜色了.
                    incertitudeStartAngle++
                    postInvalidate()
                })
                duration = 1000
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
            }
        }
        mColorAnimator?.start()
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

    fun removeLastStepPoint(): Boolean {
        if (!stepPointList.isEmpty()) {
            stepPointList.removeAt(stepPointList.size - 1)
            curProgress = getLastStepPoint()
            return true
        }
        return false
    }
}