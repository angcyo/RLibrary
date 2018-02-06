package com.angcyo.uiview.widget

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.debugPaint
import com.angcyo.uiview.resources.RAnimListener

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：抖动动画的图片显示View
 * 创建人员：Robi
 * 创建时间：2017/07/26 11:06
 * 修改人员：Robi
 * 修改时间：2018-2-6 14:52:53
 * 修改备注：
 * Version: 1.0.0
 */
class ShakeImageView(context: Context, attributeSet: AttributeSet? = null) : AppCompatImageView(context, attributeSet) {

    private var rotate = 0F

    private var hotAnim = false //hot 动画阶段

    private var animator: ValueAnimator? = null

    var shakeXRatio = 0.5f
    var shakeYRatio = 0.8f

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.ShakeImageView)
        shakeXRatio = typedArray.getFloat(R.styleable.ShakeImageView_r_shake_x_ratio, shakeXRatio)
        shakeYRatio = typedArray.getFloat(R.styleable.ShakeImageView_r_shake_y_ratio, shakeYRatio)
        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.rotate(rotate, measuredWidth * shakeXRatio, measuredHeight * shakeYRatio)
        super.onDraw(canvas)
        canvas.restore()

        if (isInEditMode) {
            debugPaint.style = Paint.Style.FILL_AND_STROKE
            canvas.drawCircle(measuredWidth * shakeXRatio, measuredHeight * shakeYRatio, 3f, debugPaint)
        }
    }

    private var isAttached = false

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isAttached = true

//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            startAnim()
//        }
        if (visibility == VISIBLE) {
            startAnim()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isAttached = false
        stopAnim()
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (isAttached && visibility == VISIBLE) {
            startAnim()
        } else {
            stopAnim()
        }
    }

    private fun startAnim() {
        if (visibility != View.VISIBLE) {
            return
        }

        if (drawable == null) {
            return
        }

        if (animator == null) {
            val anim = ObjectAnimator.ofFloat(0f, 10f, -10F, 10f, 0f)
            anim.duration = 400
            anim.interpolator = LinearInterpolator()
            anim.addUpdateListener { animation ->
                val value: Float = animation.animatedValue as Float
                if (hotAnim) {
                    rotate = 0f
                } else {
                    rotate = value
                }
                postInvalidateOnAnimation()
            }
            anim.addListener(object : RAnimListener() {
                override fun onAnimationRepeat(animation: Animator?) {
                    super.onAnimationRepeat(animation)
                    hotAnim = !hotAnim
                }
            })
            anim.repeatCount = ObjectAnimator.INFINITE
            anim.repeatMode = ObjectAnimator.RESTART
            animator = anim
        }

        animator?.let {
            if (!it.isStarted) {
                it.start()
            }
        }
    }

    private fun stopAnim() {
        animator?.let {
            it.cancel()
        }
    }
}