package com.angcyo.uiview.widget.group

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.exactlyMeasure

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/12/26 17:26
 * 修改人员：Robi
 * 修改时间：2017/12/26 17:26
 * 修改备注：
 * Version: 1.0.0
 */
class GuideFrameLayout(context: Context, attributeSet: AttributeSet? = null) : FrameLayout(context, attributeSet) {

    companion object {
        val LEFT = 1
        val TOP = 2
        val RIGHT = 3
        val BOTTOM = 4
        val CENTER = 5
    }

    /*锚点坐标列表*/
    private var anchorList = mutableListOf<Rect>()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //修正大小
        for (i in 0 until childCount) {
            val childAt = getChildAt(i)
            val layoutParams = childAt.layoutParams
            if (layoutParams is LayoutParams) {
                if (layoutParams.anchorIndex >= 0 && layoutParams.anchorIndex < anchorList.size) {
                    //需要对齐锚点
                    val anchorRect = anchorList[layoutParams.anchorIndex]
                    val offsetX = layoutParams.offsetX
                    val offsetY = layoutParams.offsetY

                    when (layoutParams.guideGravity) {
                        CENTER -> {
                            val w = anchorRect.width() + 2 * offsetX
                            val h = anchorRect.height() + 2 * offsetY
                            childAt.measure(exactlyMeasure(w), exactlyMeasure(h))
                        }
                    }
                }
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        //修正坐标
        for (i in 0 until childCount) {
            val childAt = getChildAt(i)
            val layoutParams = childAt.layoutParams
            if (layoutParams is LayoutParams) {
                if (layoutParams.anchorIndex >= 0 && layoutParams.anchorIndex < anchorList.size) {
                    //需要对齐锚点
                    val anchorRect = anchorList[layoutParams.anchorIndex]
                    val offsetX = layoutParams.offsetX
                    val offsetY = layoutParams.offsetY

                    when (layoutParams.guideGravity) {
                        LEFT -> {
                            val l = anchorRect.left - offsetX - childAt.measuredWidth
                            childAt.layout(l, childAt.top + offsetY, l + childAt.measuredWidth, childAt.bottom + offsetY)
                        }
                        TOP -> {
                            val t = anchorRect.top - offsetY - childAt.measuredHeight
                            childAt.layout(childAt.left + offsetX, t, childAt.right + offsetX, t + childAt.measuredHeight)
                        }
                        RIGHT -> {
                            val l = anchorRect.right + offsetX
                            childAt.layout(l, childAt.top + offsetY, l + childAt.measuredWidth, childAt.bottom + offsetY)
                        }
                        BOTTOM -> {
                            val t = anchorRect.bottom + offsetY
                            childAt.layout(childAt.left + offsetX, t, childAt.right + offsetX, t + childAt.measuredHeight)
                        }
                        CENTER -> {
                            val l = anchorRect.centerX() - childAt.measuredWidth / 2
                            val t = anchorRect.centerY() - childAt.measuredHeight / 2
                            childAt.layout(l - offsetX, t - offsetY, l + childAt.measuredWidth + offsetX, t + childAt.measuredHeight + offsetY)
                        }
                        else -> {

                        }
                    }
                }
            }
        }
    }

    fun addAnchorList(anchors: List<Rect>) {
        anchorList.clear()
        anchorList.addAll(anchors)
    }

    fun addAnchor(anchor: Rect) {
        anchorList.add(anchor)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams?): ViewGroup.LayoutParams {
        return LayoutParams(lp)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    class LayoutParams : FrameLayout.LayoutParams {
        var anchorIndex = -1
        var guideGravity = 0
        var offsetX = 0
        var offsetY = 0

        constructor(c: Context, attrs: AttributeSet?) : super(c, attrs) {
            val a = c.obtainStyledAttributes(attrs, R.styleable.GuideFrameLayout)
            anchorIndex = a.getInt(R.styleable.GuideFrameLayout_r_guide_show_in_anchor, anchorIndex)
            guideGravity = a.getInt(R.styleable.GuideFrameLayout_r_guide_gravity, guideGravity)
            offsetX = a.getDimensionPixelOffset(R.styleable.GuideFrameLayout_r_guide_offset_x, offsetX)
            offsetY = a.getDimensionPixelOffset(R.styleable.GuideFrameLayout_r_guide_offset_y, offsetY)
            a.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height)
        constructor(width: Int, height: Int, gravity: Int) : super(width, height, gravity)
        constructor(source: ViewGroup.LayoutParams?) : super(source)
        constructor(source: MarginLayoutParams?) : super(source)
        constructor(source: FrameLayout.LayoutParams?) : super(source)
    }
}