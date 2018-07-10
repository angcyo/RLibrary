package com.angcyo.uiview.draw

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.getDrawCenterTextCx
import com.angcyo.uiview.kotlin.getDrawCenterTextCy
import com.angcyo.uiview.kotlin.maxValue
import com.angcyo.uiview.skin.SkinHelper
import com.angcyo.uiview.utils.ScreenUtil.density
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/07/10 16:51
 * 修改人员：Robi
 * 修改时间：2018/07/10 16:51
 * 修改备注：
 * Version: 1.0.0
 */
class RDrawProgress(view: View, attributeSet: AttributeSet? = null) : BaseDraw(view, attributeSet) {

    companion object {
        /**圆形进度*/
        val PROGRESS_TYPE_CIRCLE = 1
        /**矩形进度*/
        val PROGRESS_TYPE_RECT = 2
        /**圆角矩形进度*/
        val PROGRESS_TYPE_ROUND = 3
    }

    var progressBarType = PROGRESS_TYPE_RECT
        set(value) {
            field = value
            if (field > 3 || isIncertitudeProgress) {
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

    var drawProgressText = false

    var isIncertitudeProgress = false

    var mProgressTextSize = 12 * density()

    var mProgressTextColor = Color.WHITE

    init {
        initAttribute(attributeSet)
    }

    override fun initAttribute(attr: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attr, R.styleable.RDrawProgress)
        progressBarType = typedArray.getInteger(R.styleable.RDrawProgress_r_progress_bar_type, progressBarType)
        progressBgColor = typedArray.getColor(R.styleable.RDrawProgress_r_progress_bg_color, progressBgColor)
        progressColor = typedArray.getColor(R.styleable.RDrawProgress_r_progress_color, progressColor)
        mProgressTextColor = typedArray.getColor(R.styleable.RDrawProgress_r_progress_text_color, mProgressTextColor)
        mProgressTextSize = typedArray.getDimensionPixelOffset(R.styleable.RDrawProgress_r_progress_text_size, mProgressTextSize.toInt()).toFloat()
        progressSecondColor = typedArray.getColor(R.styleable.RDrawProgress_r_second_progress_color, progressSecondColor)
        maxProgress = typedArray.getInteger(R.styleable.RDrawProgress_r_max_progress, maxProgress)
        curProgress = typedArray.getInteger(R.styleable.RDrawProgress_r_cur_progress, curProgress)
        secondProgress = typedArray.getInteger(R.styleable.RDrawProgress_r_second_progress, secondProgress)
        progressWidth = typedArray.getDimensionPixelOffset(R.styleable.RDrawProgress_r_progress_width, progressWidth.toInt()).toFloat()
        progressRoundRadius = typedArray.getDimensionPixelOffset(R.styleable.RDrawProgress_r_progress_round_radius, progressRoundRadius.toInt()).toFloat()
        drawBgOnNoProgress = typedArray.getBoolean(R.styleable.RDrawProgress_r_draw_bg_on_no_progress, drawBgOnNoProgress)
        isIncertitudeProgress = typedArray.getBoolean(R.styleable.RDrawProgress_r_incertitude_progress, isIncertitudeProgress)
        drawProgressText = typedArray.getBoolean(R.styleable.RDrawProgress_r_draw_progress_text, drawProgressText)

        typedArray.recycle()
    }

    //视图允许的绘制区域
    val viewDrawRect by lazy {
        Rect()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        viewDrawRect.set(paddingLeft, paddingTop, viewWidth - paddingRight, viewHeight - paddingBottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.translate(viewDrawRect.left.toFloat(), viewDrawRect.top.toFloat())
        when (progressBarType) {
            PROGRESS_TYPE_CIRCLE -> drawCircle(canvas, isIncertitudeProgress)
            PROGRESS_TYPE_RECT -> drawRect(canvas, isIncertitudeProgress)
            PROGRESS_TYPE_ROUND -> drawRound(canvas, isIncertitudeProgress)
        }
        drawProgressText(canvas)
        canvas.restore()
    }

    val tempRectF by lazy {
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

        mBasePaint.style = Paint.Style.STROKE

        //进度背景
        if (drawBgOnNoProgress || curProgress > 0) {
            mBasePaint.color = progressBgColor
            mBasePaint.strokeWidth = progressWidth
            canvas.drawArc(tempRectF, 0f, 360f, false, mBasePaint)
        }

        //第二进度
        if (!isIncertitude) {
            mBasePaint.color = progressSecondColor
            mBasePaint.strokeWidth = inset
            canvas.drawArc(tempRectF, circleProgressStartAngle, p(secondProgress), false, mBasePaint)
        }

        //当前进度
        if (isIncertitude) {
            mBasePaint.strokeWidth = inset
            if (curProgress <= 0) {
                //默认 颜色渐变动画
                mBasePaint.color = incertitudeRectDrawColor
                canvas.drawArc(tempRectF, incertitudeStartAngle, 360f, false, mBasePaint)
            } else {
                //旋转动画
                mBasePaint.color = progressSecondColor
                canvas.drawArc(tempRectF, incertitudeStartAngle, 90f, false, mBasePaint)
            }
        } else {
            mBasePaint.color = progressColor
            mBasePaint.strokeWidth = inset
            canvas.drawArc(tempRectF, circleProgressStartAngle, p(curProgress), false, mBasePaint)
        }

    }

    private var incertitudeRectDrawColor = progressColor
    private val incertitudeRect by lazy {
        RectF()
    }

    private fun drawRect(canvas: Canvas, isIncertitude: Boolean) {
        fun p(progress: Int) = tempRectF.set(viewDrawRect.left.toFloat(), viewDrawRect.top.toFloat(),
                progress * 1f / maxProgress * viewDrawRect.width(), viewDrawRect.height().toFloat())

        tempRectF.set(viewDrawRect.left.toFloat(), viewDrawRect.top.toFloat(),
                viewDrawRect.width().toFloat(), viewDrawRect.height().toFloat())
        mBasePaint.style = Paint.Style.FILL

        //进度背景
        if (drawBgOnNoProgress || curProgress > 0) {
            mBasePaint.color = progressBgColor
            canvas.drawRect(tempRectF, mBasePaint)
        }

        //第二进度
        if (!isIncertitude) {
            mBasePaint.color = progressSecondColor
            p(secondProgress)
            canvas.drawRect(tempRectF, mBasePaint)
        }

        //当前进度
        if (isIncertitude) {
            if (curProgress <= 0) {
                //颜色渐变
                mBasePaint.color = incertitudeRectDrawColor
                p(maxProgress)
                canvas.drawRect(tempRectF, mBasePaint)
            } else {
                //平移动画
                mBasePaint.color = progressSecondColor
                canvas.drawRect(incertitudeRect, mBasePaint)
            }
        } else {
            mBasePaint.color = progressColor
            p(curProgress)
            canvas.drawRect(tempRectF, mBasePaint)
        }
    }

    private fun drawRound(canvas: Canvas, isIncertitude: Boolean) {
        fun p(progress: Int) = tempRectF.set(viewDrawRect.left.toFloat(), viewDrawRect.top.toFloat(),
                progress * 1f / maxProgress * viewDrawRect.width(), viewDrawRect.height().toFloat())

        tempRectF.set(viewDrawRect.left.toFloat(), viewDrawRect.top.toFloat(),
                viewDrawRect.width().toFloat(), viewDrawRect.height().toFloat())
        mBasePaint.style = Paint.Style.FILL

        //进度背景
        if (drawBgOnNoProgress || curProgress > 0) {
            mBasePaint.color = progressBgColor
            canvas.drawRoundRect(tempRectF, progressRoundRadius, progressRoundRadius, mBasePaint)
        }

        //第二进度
        if (!isIncertitude) {
            mBasePaint.color = progressSecondColor
            p(secondProgress)
            canvas.drawRoundRect(tempRectF, progressRoundRadius, progressRoundRadius, mBasePaint)
        }

        //当前进度
        if (isIncertitude) {
            if (curProgress <= 0) {
                //颜色渐变
                mBasePaint.color = incertitudeRectDrawColor
                p(maxProgress)
                canvas.drawRoundRect(tempRectF, progressRoundRadius, progressRoundRadius, mBasePaint)
            } else {
                //平移动画
                mBasePaint.color = progressSecondColor
                canvas.drawRoundRect(incertitudeRect, progressRoundRadius, progressRoundRadius, mBasePaint)
            }
        } else {
            mBasePaint.color = progressColor
            p(curProgress)
            canvas.drawRoundRect(tempRectF, progressRoundRadius, progressRoundRadius, mBasePaint)
        }
    }

    private fun drawProgressText(canvas: Canvas) {
        if (drawProgressText) {
            //绘制进度文本
            mBasePaint.style = Paint.Style.FILL_AND_STROKE
            mBasePaint.textSize = mProgressTextSize
            mBasePaint.color = mProgressTextColor
            mBasePaint.strokeWidth = 1f
            val text = "$curProgress%"
            canvas.drawText(text,
                    mView.getDrawCenterTextCx(mBasePaint, text),
                    mView.getDrawCenterTextCy(mBasePaint),
                    mBasePaint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mColorAnimator?.cancel()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isIncertitudeProgress) {
            startIncertitudeAnimator()
        }
    }

    private var mColorAnimator: ValueAnimator? = null
    private fun startIncertitudeAnimator() {
        if (mColorAnimator == null) {
            mColorAnimator = ValueAnimator.ofObject(ArgbEvaluator(),
                    progressColor,
                    SkinHelper.getTranColor(progressColor, 0x10)).apply {
                interpolator = LinearInterpolator()
                addUpdateListener { animation ->
                    incertitudeRectDrawColor = animation.animatedValue as Int//之后就可以得到动画的颜色了.
                    incertitudeStartAngle++

                    val left = (-viewDrawRect.width() / 2 + viewDrawRect.left + (viewDrawRect.width() * 3 / 2 * animation.animatedFraction)).maxValue(viewDrawRect.right.toFloat())
                    val right = (left + viewDrawRect.width() / 2).maxValue(viewDrawRect.right.toFloat())
                    incertitudeRect.set(left, paddingTop.toFloat(), right, viewDrawRect.height().toFloat())

                    postInvalidate()
                }
                duration = 1000
                repeatCount = ValueAnimator.INFINITE
                repeatMode = if (progressBarType != PROGRESS_TYPE_CIRCLE && curProgress > 0) ValueAnimator.RESTART else ValueAnimator.REVERSE
            }
        }
        if (mColorAnimator?.isStarted != true) {
            mColorAnimator?.start()
        }
    }

    /**可以用动画的形式, 进行动画*/
    fun setCurProgress(progress: Int, anim: Boolean, duration: Long = 700) {
        if (anim) {
            ValueAnimator.ofInt(curProgress, progress).apply {
                this.duration = duration
                interpolator = LinearInterpolator()
                addUpdateListener {
                    curProgress = it.animatedValue as Int
                }
                start()
            }
        } else {
            curProgress = progress
        }
    }
}

class RefreshProperty<T>(var value: T) : ReadWriteProperty<BaseDraw, T> {
    override fun getValue(thisRef: BaseDraw, property: KProperty<*>): T = value

    override fun setValue(thisRef: BaseDraw, property: KProperty<*>, value: T) {
        this.value = value
        thisRef.postInvalidate()
    }
}