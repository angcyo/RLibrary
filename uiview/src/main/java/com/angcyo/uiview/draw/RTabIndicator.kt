package com.angcyo.uiview.draw

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator
import com.angcyo.library.utils.L
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
    init {
        initAttribute(attributeSet)
    }

    override fun initAttribute(attr: AttributeSet?) {

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

    /**如果未指定指示器的宽度, 那么就用对应child的宽度*/
    var indicatorWidth = 0
    var indicatorHeight = 4 * density()

    /**偏移距离, 不能用paddingBottom*/
    var indicatorOffsetY = 2 * density()
    /**宽度 修正量*/
    var indicatorWidthOffset = -4 * density()

    /**圆角大小*/
    var indicatorRoundSize = 10 * density()

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

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (curIndex in 0..(childCount - 1)) {
            //安全的index

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

            indicatorDrawRect.set((childCenter - indicatorDrawWidth / 2).toFloat(),
                    viewHeight - indicatorOffsetY - indicatorHeight,
                    (childCenter + indicatorDrawWidth / 2).toFloat(),
                    viewHeight - indicatorOffsetY)
            canvas.drawRoundRect(indicatorDrawRect, indicatorRoundSize, indicatorRoundSize, mBasePaint)
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