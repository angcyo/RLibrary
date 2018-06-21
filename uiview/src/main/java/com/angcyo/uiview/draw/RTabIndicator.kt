package com.angcyo.uiview.draw

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator
import com.angcyo.uiview.R
import com.angcyo.uiview.resources.RAnimListener

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/06/21 09:17
 * 修改人员：Robi
 * 修改时间：2018/06/21 09:17
 * 修改备注：
 * Version: 1.0.0
 */
class RTabIndicator(view: View, attributeSet: AttributeSet? = null) : BaseDraw(view, attributeSet) {
    companion object {
        //无样式
        const val INDICATOR_TYPE_NONE = 0
        //底部一根线
        const val INDICATOR_TYPE_BOTTOM_LINE = 1
        //圆角矩形块状
        const val INDICATOR_TYPE_ROUND_RECT_BLOCK = 2
    }

    /**指示器的样式*/
    var indicatorType = INDICATOR_TYPE_BOTTOM_LINE

    /**指示器的颜色*/
    var indicatorColor = -1

    /**如果未指定指示器的宽度, 那么就用对应child的宽度*/
    var indicatorWidth = 0
    var indicatorHeight: Int = (4 * density()).toInt()

    /**偏移距离, 不能用paddingBottom*/
    var indicatorOffsetY: Int = (2 * density()).toInt()
    /**宽度 修正量*/
    var indicatorWidthOffset: Int = (4 * density()).toInt()
    var indicatorHeightOffset: Int = (4 * density()).toInt()

    /**圆角大小*/
    var indicatorRoundSize: Int = (10 * density()).toInt()

    init {
        initAttribute(attributeSet)
    }

    override fun initAttribute(attr: AttributeSet?) {
        val typedArray = obtainStyledAttributes(attr, R.styleable.RTabIndicator)
        indicatorType = typedArray.getInt(R.styleable.RTabIndicator_r_indicator_type, INDICATOR_TYPE_BOTTOM_LINE)
        indicatorColor = baseColor
        indicatorColor = typedArray.getColor(R.styleable.RTabIndicator_r_indicator_color, indicatorColor)
        indicatorWidth = typedArray.getDimensionPixelOffset(R.styleable.RTabIndicator_r_indicator_width, indicatorWidth)
        indicatorHeight = typedArray.getDimensionPixelOffset(R.styleable.RTabIndicator_r_indicator_height, indicatorHeight)
        indicatorOffsetY = typedArray.getDimensionPixelOffset(R.styleable.RTabIndicator_r_indicator_offset_y, indicatorOffsetY)
        indicatorWidthOffset = typedArray.getDimensionPixelOffset(R.styleable.RTabIndicator_r_indicator_offset_width, indicatorWidthOffset)
        indicatorHeightOffset = typedArray.getDimensionPixelOffset(R.styleable.RTabIndicator_r_indicator_offset_height, indicatorHeightOffset)
        indicatorRoundSize = typedArray.getDimensionPixelOffset(R.styleable.RTabIndicator_r_indicator_round_size, indicatorRoundSize)

        typedArray.recycle()
    }


    private var animStartCenterX = -1
    private var animEndCenterX = -1

    private var animStartWidth = -1
    private var animEndWidth = -1

    /**当前指示那个位置*/
    var curIndex = 0
        set(value) {
            if (viewWidth == 0 || viewHeight == 0) {
                field = value
            } else if (field == value) {
                scrollTabLayoutToCenter()
                postInvalidate()
            } else if (pagerPositionOffset == 0f) {

                animStartCenterX = getChildCenter(field)
                animEndCenterX = getChildCenter(value)

                animStartWidth = getIndicatorWidth(field)
                animEndWidth = getIndicatorWidth(value)

                field = value

                if (enableIndicatorAnim) {
                    animatorValueInterpolator = 0f
                    indicatorAnimator.start()
                } else {
                    indicatorAnimator.cancel()
                    scrollTabLayoutToCenter()
                    postInvalidate()
                }
            } else {
                field = value
                scrollTabLayoutToCenter()
                postInvalidate()
            }
        }

    /**ViewPager滚动相关*/
    var pagerPositionOffset = 0f
        set(value) {
            field = value

            if (field > 0f) {
                if (curIndex == pagerPosition) {
                    //view pager 往下一页滚
                    animStartCenterX = getChildCenter(curIndex)
                    animEndCenterX = getChildCenter(curIndex + 1)

                    animStartWidth = getIndicatorWidth(curIndex)
                    animEndWidth = getIndicatorWidth(curIndex + 1)

                    animatorValueInterpolator = value
                    animatorValue = value

                } else {
                    //往上一页滚
                    animStartCenterX = getChildCenter(curIndex)
                    animEndCenterX = getChildCenter(pagerPosition)

                    animStartWidth = getIndicatorWidth(curIndex)
                    animEndWidth = getIndicatorWidth(pagerPosition)

                    animatorValueInterpolator = 1f - value
                    animatorValue = 1f - value
                }
                postInvalidate()
            }
        }
    var pagerPosition = 0

    private val indicatorDrawRect: RectF by lazy {
        RectF()
    }

    private fun getChildCenter(index: Int): Int {
        if (index in 0..(childCount - 1)) {
            val curChildView = getChildAt(index)
            //child横向中心x坐标
            return curChildView.left + curChildView.measuredWidth / 2
        }
        //返回上一次结束的x坐标
        return animEndCenterX
    }

    private fun getIndicatorWidth(index: Int): Int {
        return if (indicatorWidth == 0) {
            if (index in 0..(childCount - 1)) {
                val curChildView = getChildAt(index)
                //child横向中心x坐标
                return curChildView.measuredWidth
            }
            //返回上一次结束的x坐标
            return animEndWidth
        } else {
            indicatorWidth
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (curIndex in 0..(childCount - 1)) {
            //安全的index

            val childView = getChildAt(curIndex)

            //指示器的宽度
            val indicatorDrawWidth = if (animatorValueInterpolator != -1f) {
                (animStartWidth + (animEndWidth - animStartWidth) * animatorValueInterpolator + indicatorWidthOffset).toInt()
            } else {
                getIndicatorWidth(curIndex)
            }

            //child横向中心x坐标
            val childCenter: Int = if (animatorValueInterpolator != -1f) {
                (animStartCenterX + (animEndCenterX - animStartCenterX) * animatorValueInterpolator).toInt()
            } else {
                getChildCenter(curIndex)
            }

            //L.e("RTabIndicator: draw ->$viewWidth $childCenter $indicatorDrawWidth $curIndex $animatorValueInterpolator")

            val left = (childCenter - indicatorDrawWidth / 2 - indicatorWidthOffset / 2).toFloat()
            val right = (childCenter + indicatorDrawWidth / 2 + indicatorWidthOffset / 2).toFloat()

            when (indicatorType) {
                INDICATOR_TYPE_NONE -> {
                }
                INDICATOR_TYPE_BOTTOM_LINE -> {
                    indicatorDrawRect.set(left,
                            (viewHeight - indicatorOffsetY - indicatorHeight).toFloat(),
                            right,
                            (viewHeight - indicatorOffsetY).toFloat())

                    mBasePaint.color = indicatorColor
                    canvas.drawRoundRect(indicatorDrawRect, indicatorRoundSize.toFloat(), indicatorRoundSize.toFloat(), mBasePaint)
                }
                INDICATOR_TYPE_ROUND_RECT_BLOCK -> {
                    indicatorDrawRect.set(left,
                            (childView.top - indicatorHeightOffset / 2).toFloat(),
                            right,
                            (childView.bottom + indicatorHeightOffset / 2).toFloat())

                    mBasePaint.color = indicatorColor
                    canvas.drawRoundRect(indicatorDrawRect, indicatorRoundSize.toFloat(), indicatorRoundSize.toFloat(), mBasePaint)
                }
            }
        }
    }

    /**确保当前的centerX , 在TabLayout 显示区域的中心*/
    private fun scrollTabLayoutToCenter() {
        if (curIndex in 0..(childCount - 1)) {

            //child横向中心x坐标
            val childCenter: Int = if (animatorValueInterpolator != -1f) {
                (animStartCenterX + (animEndCenterX - animStartCenterX) * animatorValue).toInt()
            } else {
                getChildCenter(curIndex)
            }

            val viewCenterX = viewWidth / 2
            if (childCenter > viewCenterX) {
                scrollTo(childCenter - viewCenterX, 0)
            } else {
                scrollTo(0, 0)
            }
        }
    }

    /**激活指示器滚动动画*/
    var enableIndicatorAnim = true
    private var animatorValue = -1f
        set(value) {
            field = value
            if (field != -1f) {
                scrollTabLayoutToCenter()
            }
        }

    /**用此成员变量判断动画开始和结束*/
    private var animatorValueInterpolator = -1f

    private val overshootInterpolator: OvershootInterpolator by lazy { OvershootInterpolator() }

    private val indicatorAnimator by lazy {
        ObjectAnimator.ofFloat(0f, 1f).apply {
            duration = 300
            addUpdateListener {
                animatorValue = it.animatedValue as Float
                animatorValueInterpolator = overshootInterpolator.getInterpolation(animatorValue)
                //L.e("call: $animatorValue -> ")
                postInvalidateOnAnimation()
            }
            addListener(object : RAnimListener() {
                override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                    super.onAnimationEnd(animation, isReverse)
                    animatorValueInterpolator = -1f
                }
            })
        }
    }
}