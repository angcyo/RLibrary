package com.angcyo.uiview.viewgroup

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.exactlyMeasure
import com.angcyo.uiview.kotlin.have

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：允许一个子View撑满剩余空间, 另一个子view可以固定大小, 也可以自动缩放到最小大小
 * 创建人员：Robi
 * 创建时间：2018/05/17 17:55
 * 修改人员：Robi
 * 修改时间：2018/05/17 17:55
 * 修改备注：
 * Version: 1.0.0
 */
open class FillLayout : ViewGroup {
    var reverseLayout = false

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FillLayout)
        reverseLayout = typedArray.getBoolean(R.styleable.FillLayout_r_reverse_fill_layout, reverseLayout)
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        var childMaxWidth = 0
        var childMaxHeight = 0
        var otherChildWidth = 0 //去掉fillView之后子View的宽度
        var fillView: View? = null
        var fillViewWidth = 0

        var fillMinWidthView: View? = null
        var fillMinWidth = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val params: LayoutParams = child.layoutParams as LayoutParams

            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)

            childMaxWidth += child.measuredWidth + params.widthMargin()

            if (params.isFillView) {
                fillView = child
                fillViewWidth = child.measuredWidth + params.widthMargin()
            } else {
                if (params.fillMinWidth > 0) {
                    fillMinWidth = params.fillMinWidth + params.widthMargin()
                    fillMinWidthView = child
                }
                otherChildWidth += child.measuredWidth + params.widthMargin()
            }

            childMaxHeight = Math.max(childMaxHeight, child.measuredHeight + params.heightMargin())
        }

        if (widthMode != MeasureSpec.EXACTLY) {
            widthSize = childMaxWidth + paddingLeft + paddingRight
        }

        if (fillMinWidth > 0) {
            otherChildWidth = fillMinWidth
        }

        fun resetHeightSize(size: Int) {
            if (size + paddingTop + paddingBottom > heightSize) {
                if (heightMode != MeasureSpec.EXACTLY) {
                    heightSize = size + paddingTop + paddingBottom
                }
            }
        }

        fun childHeightMeasure(params: LayoutParams): Int {
            return if (params.height > -1) {
                exactlyMeasure(params.height)
            } else if (params.height == -1) {
                exactlyMeasure(heightSize - paddingTop - paddingBottom)
            } else {
                MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.UNSPECIFIED)
            }
        }

        resetHeightSize(childMaxHeight)

        fillView?.let {
            val params: LayoutParams = it.layoutParams as LayoutParams
            if (params.width == -2 && fillViewWidth + fillMinWidth < widthSize) {

            } else if (childMaxWidth > widthSize || params.isFillSpace) {

                //子View的宽度 超范围了
                it.measure(exactlyMeasure(widthSize - otherChildWidth - paddingLeft - paddingRight), childHeightMeasure(params))

                fillViewWidth = it.measuredWidth + params.widthMargin()
                resetHeightSize(it.measuredHeight)
            }
        }

        fillMinWidthView?.let {
            val params: LayoutParams = it.layoutParams as LayoutParams
            if (params.width == -1 && fillViewWidth + fillMinWidth < widthSize) {
                it.measure(exactlyMeasure(widthSize - fillViewWidth - paddingLeft - paddingRight), childHeightMeasure(params))
                resetHeightSize(it.measuredHeight)
            } else if (childMaxWidth > widthSize) {
                it.measure(exactlyMeasure(params.fillMinWidth), childHeightMeasure(params))
                resetHeightSize(it.measuredHeight)
            } else {
                if (params.isFillSpace) {
                    it.measure(exactlyMeasure(widthSize - fillViewWidth - paddingLeft - paddingRight), childHeightMeasure(params))
                    resetHeightSize(it.measuredHeight)
                }
            }
        }

        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        refreshLayout()
    }

    /**
     * 从右边向左边布局
     */
    fun refreshLayout() {
        //目前只支持横向 反向布局
//        var rightOffset = paddingLeft//getPaddingRight();
//        val topOffset = paddingTop

        var startHOffset = paddingLeft
        var startVOffset = paddingTop

        for (i in 0 until childCount) {
            val childAt = getChildAt(i)
            if (childAt.visibility != View.VISIBLE) {
                continue
            }

            if (childAt is LinearLayout) {
                childAt.gravity = if (reverseLayout) {
                    Gravity.END
                } else {
                    Gravity.START
                }

            } else if (childAt is FrameLayout) {
                for (j in 0 until childAt.childCount) {
                    val subChildView = childAt.getChildAt(j)
                    val layoutParams = subChildView.layoutParams as FrameLayout.LayoutParams
                    layoutParams.gravity = if (reverseLayout) {
                        Gravity.END
                    } else {
                        Gravity.START
                    }

                    subChildView.layoutParams = layoutParams
                }
            }
            val lp = childAt.layoutParams as LayoutParams

            val viewWidth = childAt.measuredWidth
            val viewHeight = childAt.measuredHeight

            var top = startVOffset + lp.topMargin

            val left = if (reverseLayout) {
                measuredWidth - startHOffset - lp.leftMargin - viewWidth
            } else {
                startHOffset + lp.leftMargin
            }

            if (lp.gravity.have(Gravity.BOTTOM)) {
                top = measuredHeight - paddingBottom - lp.bottomMargin - viewHeight
            }

            childAt.layout(left, top, left + viewWidth, top + viewHeight)

            startHOffset += viewWidth + lp.leftMargin
        }
    }


    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        super.addView(child, index, params)
        //最多支持2个子View
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(super.generateDefaultLayoutParams())
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return LayoutParams(context, attrs)
    }

    open class LayoutParams : LinearLayout.LayoutParams {
        /**如果为true, 那么这个View的宽度, 在超出限制之后, 会重新测量*/
        var isFillView = false
        /**当layout的空间有多时, 是否占满*/
        var isFillSpace = false

        /**设置这个值之后(>0), view一开始会占满layout的剩余空间, 当isFillView为true的view重新测量的时候, 这个View允许的最小宽度*/
        var fillMinWidth = 0

        constructor(width: Int, height: Int) : super(width, height)
        constructor(p: ViewGroup.LayoutParams?) : super(p)

        constructor(c: Context, attrs: AttributeSet?) : super(c, attrs) {
            val typedArray = c.obtainStyledAttributes(attrs, R.styleable.FillLayout_Layout)
            isFillView = typedArray.getBoolean(R.styleable.FillLayout_Layout_r_fill_view, isFillView)
            isFillSpace = typedArray.getBoolean(R.styleable.FillLayout_Layout_r_fill_space, isFillSpace)
            fillMinWidth = typedArray.getDimensionPixelOffset(R.styleable.FillLayout_Layout_r_fill_min_width, fillMinWidth)
            typedArray.recycle()
        }

        fun widthMargin(): Int {
            return leftMargin + rightMargin
        }

        fun heightMargin(): Int {
            return topMargin + bottomMargin
        }
    }
}