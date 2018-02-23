package com.angcyo.uiview.viewgroup

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.abs
import com.angcyo.uiview.kotlin.density
import com.angcyo.uiview.kotlin.random

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：一个中心View, 其他View在周边浮动旋转
 * 创建人员：Robi
 * 创建时间：2018/02/23 10:44
 * 修改人员：Robi
 * 修改时间：2018/02/23 10:44
 * 修改备注：
 * Version: 1.0.0
 */
class DriftLayout(context: Context, attributeSet: AttributeSet? = null) : FrameLayout(context, attributeSet) {

    var enableDrift = true
    var enableShake = true

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.DriftLayout)
        enableDrift = typedArray.getBoolean(R.styleable.DriftLayout_r_enable_drift, enableDrift)
        enableShake = typedArray.getBoolean(R.styleable.DriftLayout_r_enable_shake, enableShake)
        typedArray.recycle()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        updateLayout()
    }

    private fun updateLayout() {
        driftCenterView?.let {
            //修正布局坐标
            for (i in 0 until childCount) {
                val childAt = getChildAt(i)
                val layoutParams = childAt.layoutParams
                if (layoutParams is LayoutParams) {
                    if (childAt != it) {

                        //围绕的中心点坐标
                        val cx = it.left + it.measuredWidth / 2
                        val cy = it.top + it.measuredHeight / 2

                        //计算出的布局点的坐标
                        val r = Math.max(childAt.measuredWidth, childAt.measuredHeight) / 2 + layoutParams.driftROffset //距离中心点的半径
                        val x: Int = (cx + Math.cos(Math.toRadians(layoutParams.animDriftAngle.toDouble())) * r + layoutParams.animDriftXOffset * density).toInt()
                        val y: Int = (cy + Math.sin(Math.toRadians(layoutParams.animDriftAngle.toDouble())) * r + layoutParams.animDriftYOffset * density).toInt()

                        childAt.layout(x - childAt.measuredWidth / 2, y - childAt.measuredHeight / 2,
                                x + childAt.measuredWidth / 2, y + childAt.measuredHeight / 2)
                    }
                }
            }
        }
    }

    private var isOnAttachedToWindow = false

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isOnAttachedToWindow = true
        checkStartDrift()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isOnAttachedToWindow = false
        checkStartDrift()
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        checkStartDrift()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        checkStartDrift()
    }

    private fun checkStartDrift() {
        //检查是否需要浮动
        if (isOnAttachedToWindow && visibility == View.VISIBLE &&
                measuredWidth > 0 && measuredHeight > 0 &&
                !driftAnimator.isStarted) {
            driftAnimator.start()
        } else {
            driftAnimator.cancel()
        }
    }

    /**动画*/
    private val driftAnimator: ValueAnimator by lazy {
        ObjectAnimator.ofInt(0, 360).apply {
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            duration = 1000
            addUpdateListener {
                driftCenterView?.let {
                    //修正布局坐标
                    for (i in 0 until childCount) {
                        val childAt = getChildAt(i)
                        val layoutParams = childAt.layoutParams
                        if (layoutParams is LayoutParams) {
                            if (childAt != it) {
                                if (enableDrift) {
                                    layoutParams.animDriftAngle += layoutParams.animDriftAngleStep
                                }
                                if (enableShake) {
                                    if (random.nextBoolean()) {
                                        layoutParams.animDriftXOffset += layoutParams.animDriftXOffsetStep
                                    } else {
                                        layoutParams.animDriftYOffset += layoutParams.animDriftYOffsetStep
                                    }
                                }
                            }
                        }
                    }
                    updateLayout()
                    postInvalidateOnAnimation()
                }
            }
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams?): ViewGroup.LayoutParams {
        return LayoutParams(lp)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    //居中定位的View
    private val driftCenterView: View?
        get() {
            var view: View? = null
            for (i in 0 until childCount) {
                val childAt = getChildAt(i)
                val layoutParams = childAt.layoutParams
                if (layoutParams is LayoutParams) {
                    if (layoutParams.isDriftCenterView) {
                        view = childAt
                        break
                    }
                }
            }
            return view
        }

    class LayoutParams : FrameLayout.LayoutParams {
        var isDriftCenterView = false
        var driftAngle = 0
        var driftROffset = 0

        //每帧旋转的角度
        var animDriftAngleStep = 0.1f

        var animDriftXOffsetMax = 20
        //每帧x轴抖动的距离
        var animDriftXOffsetStep = 0.15f
        var animDriftXOffset = 0f //px 自动转换为dp
            set(value) {
                field = value
                if (value > animDriftXOffsetMax) {
                    animDriftXOffsetStep = -animDriftXOffsetStep.abs()
                } else if (value < -animDriftXOffsetMax) {
                    animDriftXOffsetStep = animDriftXOffsetStep.abs()
                }
            }

        var animDriftYOffsetMax = animDriftXOffsetMax
        var animDriftYOffsetStep = animDriftXOffsetStep
        var animDriftYOffset = 0f
            set(value) {
                field = value
                if (value > animDriftYOffsetMax) {
                    animDriftYOffsetStep = -animDriftYOffsetStep.abs()
                } else if (value < -animDriftYOffsetMax) {
                    animDriftYOffsetStep = animDriftYOffsetStep.abs()
                }
            }


        //动画执行中的角度保存变量
        var animDriftAngle = 0f
            set(value) {
                field = if (value > 360) {
                    0f
                } else {
                    value
                }
            }

        constructor(c: Context, attrs: AttributeSet?) : super(c, attrs) {
            val a = c.obtainStyledAttributes(attrs, R.styleable.DriftLayout)
            isDriftCenterView = a.getBoolean(R.styleable.DriftLayout_r_is_drift_center, isDriftCenterView)
            driftAngle = a.getInt(R.styleable.DriftLayout_r_drift_angle, driftAngle)
            animDriftAngleStep = a.getFloat(R.styleable.DriftLayout_r_drift_angle_step, animDriftAngleStep)
            animDriftAngle = driftAngle.toFloat()
            driftROffset = a.getDimensionPixelOffset(R.styleable.DriftLayout_r_drift_r_offset, driftROffset)
            a.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height)
        constructor(width: Int, height: Int, gravity: Int) : super(width, height, gravity)
        constructor(source: ViewGroup.LayoutParams?) : super(source)
        constructor(source: MarginLayoutParams?) : super(source)
    }
}