package com.angcyo.uiview.widget.helper

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.view.animation.LinearInterpolator
import com.angcyo.uiview.resources.AnimUtil
import com.angcyo.uiview.resources.RAnimListener
import com.angcyo.uiview.utils.ScreenUtil

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/10/23 16:17
 * 修改人员：Robi
 * 修改时间：2017/10/23 16:17
 * 修改备注：
 * Version: 1.0.0
 */
class RippleHelper {

    /**波纹的颜色*/
    var rippleColor = Color.RED //SkinHelper.getSkin().themeSubColor

    /**波纹的作用对象*/
    var targetView: View? = null

    /**波纹的数量*/
    var rippleCount = 30

    /**波纹之间的间隙*/
    var rippleSpace = 30 * ScreenUtil.density

    /**波纹开始的半径*/
    var rippleStartR = 80 * ScreenUtil.density

    /**波纹结束的半径*/
    var rippleEndR = 200 * ScreenUtil.density

    /**波纹没次扩散的步长*/
    var rippleStep = 2 * ScreenUtil.density

    private val ripples = mutableListOf<Ripple>()

    private val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
    }

    fun startRipple(view: View) {
        targetView = view
        view.parent.requestDisallowInterceptTouchEvent(true)
        resetRipples()
        startAnim()
        view.postInvalidate()
    }

    private fun resetRipples() {
        ripples.clear()
        targetView?.let { view ->
            (0 until rippleCount).map { i ->
                ripples.add(Ripple().apply {
                    currentColor = rippleColor
                    startX = (view.measuredWidth / 2).toFloat()
                    startY = (view.measuredHeight / 2).toFloat()
                    startR = rippleStartR - i * rippleSpace
                    currentR = startR
                    offset = startR
                })
            }
        }
    }

    fun stopRipple() {
        if (animator.isStarted) {
            animator.cancel()
            targetView?.postInvalidateOnAnimation()
            targetView?.parent?.requestDisallowInterceptTouchEvent(false)
        }
    }

    fun onDraw(canvas: Canvas) {
        if (animator.isRunning) {
            targetView?.let {
                ripples.map {
                    if (it.currentR >= rippleStartR) {
                        paint.color = it.currentColor
                        canvas.drawCircle(it.startX, it.startY, it.currentR, paint)
                    }
                }
            }
        }
    }

    /*循环绘制的控制动画*/
    private val animator by lazy {
        ValueAnimator.ofFloat(0f, 1f).apply {
            interpolator = LinearInterpolator()
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            duration = 3000
            addListener(object : RAnimListener() {
                override fun onAnimationFinish(animation: Animator?, cancel: Boolean) {
                    super.onAnimationFinish(animation, cancel)
                }

                override fun onAnimationRepeat(animation: Animator?) {
                    super.onAnimationRepeat(animation)
                    ripples.map {
                        it.offset = it.currentR
                        if (it.offset >= rippleEndR) {
                            it.offset = it.startR
                        }
                    }
                }
            })
            addUpdateListener { anim ->
                onAnimationUpdate(anim)
            }
        }
    }

    private fun startAnim() {
        if (animator.isStarted) {
        } else {
            animator.start()
        }
    }

    private fun onAnimationUpdate(animation: ValueAnimator) {

        ripples.mapIndexed { _, ripple ->
            ripple.currentR = ripple.offset + animation.animatedFraction * (rippleEndR - rippleStartR)
            if (ripple.currentR >= rippleEndR) {
                ripple.currentR = ripple.startR
                ripple.offset = ripple.startR
                ripple.currentColor = rippleColor
            } else {
                if (ripple.currentR >= rippleStartR) {
                    ripple.currentColor = AnimUtil.evaluateColor(ripple.currentR / rippleEndR, rippleColor, Color.TRANSPARENT)
                }
            }
        }
        targetView?.postInvalidateOnAnimation()
    }

    private inner class Ripple {
        /*波纹开始的x坐标*/
        var startX = 0f
        var startY = 0f
        /*波纹当前的半径*/
        var currentR = 0f
        /*波纹开始的坐标, 用来重置状态*/
        var startR = 0f
        /*波纹当前的颜色*/
        var currentColor = 0
        /*上一次执行动画结束后的偏移*/
        var offset = 0f
    }
}