package com.angcyo.uiview.viewgroup

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.angcyo.uiview.R
import com.angcyo.uiview.draw.RDrawLine
import com.angcyo.uiview.kotlin.calcWidthHeightRatio
import com.angcyo.uiview.kotlin.centerX
import com.angcyo.uiview.kotlin.density
import com.angcyo.uiview.kotlin.findView
import com.angcyo.uiview.skin.SkinHelper
import com.angcyo.uiview.utils.ScreenUtil

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/11/28 13:53
 * 修改人员：Robi
 * 修改时间：2017/11/28 13:53
 * 修改备注：
 * Version: 1.0.0
 */
open class RFrameLayout(context: Context, attributeSet: AttributeSet? = null) : FrameLayout(context, attributeSet) {

    /**高度等于宽度*/
    protected var equWidth: Boolean = false
    var widthHeightRatio: String? = null

    private var rBackgroundDrawable: Drawable? = null
    /**
     * 允许的最大高度, 如果为-2px,那么就是屏幕高度的一半, 如果是-3px,那么就是屏幕高度的三分之, 以此内推, 0不处理
     * 如果是负数,就是屏幕的倍数.
     * 如果是正数,就是确确的值
     */
    private var maxHeight = 0

    var showInnerBorder = false
        set(value) {
            field = value
            postInvalidate()
        }

    private val showNoEnableMark = false

    var innerBorderWidth = 0
        set(value) {
            field = value
            postInvalidate()
        }
    var innerBorderOffset = 0
        set(value) {
            field = value
            postInvalidate()
        }
    var innerBorderColor = Color.WHITE
        set(value) {
            field = value
            postInvalidate()
        }

    private val innerBorderPaint: Paint by lazy {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.strokeWidth = innerBorderWidth.toFloat()
        p.strokeJoin = Paint.Join.ROUND
        p.strokeCap = Paint.Cap.ROUND
        p.style = Paint.Style.STROKE
        p.isFilterBitmap = true
        p
    }

    private val mDrawLine: RDrawLine

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.RFrameLayout)
        equWidth = typedArray.getBoolean(R.styleable.RFrameLayout_r_is_aeq_width, equWidth)
        widthHeightRatio = typedArray.getString(R.styleable.RFrameLayout_r_width_height_ratio)

        showInnerBorder = typedArray.getBoolean(R.styleable.RFrameLayout_r_show_inner_border, showInnerBorder)
        if (isInEditMode) {
            innerBorderWidth = typedArray.getDimensionPixelOffset(R.styleable.RFrameLayout_r_inner_border_width, 4)
            innerBorderColor = typedArray.getColor(R.styleable.RFrameLayout_r_inner_border_color, Color.RED)
        } else {
            innerBorderWidth = typedArray.getDimensionPixelOffset(R.styleable.RFrameLayout_r_inner_border_width, (2 * density).toInt())
            innerBorderColor = typedArray.getColor(R.styleable.RFrameLayout_r_inner_border_color, SkinHelper.getSkin().themeSubColor)
        }
        innerBorderOffset = typedArray.getDimensionPixelOffset(R.styleable.RFrameLayout_r_inner_border_offset, innerBorderOffset)

        rBackgroundDrawable = typedArray.getDrawable(R.styleable.RFrameLayout_r_background)
        resetMaxHeight(typedArray.getDimension(R.styleable.RFrameLayout_r_max_height, 0f))
        //maxHeight = typedArray.getDimension(R.styleable.RFrameLayout_r_max_height, maxHeight)

        mDrawLine = RDrawLine(this, attributeSet)

        setWillNotDraw(false)

        typedArray.recycle()

        resetMaxHeight()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (maxHeight > 0) run {
            val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
            super.onMeasure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.AT_MOST))
            val measuredHeight = measuredHeight
            if (measuredHeight > maxHeight) {
                super.onMeasure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(maxHeight, heightMode))
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }

        if (equWidth) {
            //setMeasuredDimension(measuredWidth, measuredWidth)
            super.onMeasure(View.MeasureSpec.makeMeasureSpec(measuredWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(measuredWidth, View.MeasureSpec.EXACTLY))
        } else {
            calcWidthHeightRatio(widthHeightRatio)?.let {
                //setMeasuredDimension(it[0], it[1])
                super.onMeasure(View.MeasureSpec.makeMeasureSpec(it[0], View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(it[1], View.MeasureSpec.EXACTLY))
            }
        }
    }

    override fun draw(canvas: Canvas) {
        rBackgroundDrawable?.let {
            it.bounds = canvas.clipBounds
            it.draw(canvas)
        }
        super.draw(canvas)
        mDrawLine.draw(canvas)
        if (!isEnabled && showNoEnableMark) {
            canvas.drawColor(ContextCompat.getColor(context, R.color.default_base_tran_dark2))
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (showInnerBorder) {
            innerBorderPaint.strokeWidth = innerBorderWidth.toFloat()
            innerBorderPaint.color = innerBorderColor
            val l2 = innerBorderWidth * 1f / 2
            canvas?.drawRect(l2 + innerBorderOffset, l2 + innerBorderOffset,
                    measuredWidth - l2 - innerBorderOffset, measuredHeight - l2 - innerBorderOffset,
                    innerBorderPaint)
        }
    }

    private fun resetMaxHeight() {
        if (maxHeight < 0) {
            val num = Math.abs(maxHeight)
            maxHeight = ScreenUtil.screenHeight * num
        }
    }

    private fun resetMaxHeight(height: Float) {
        if (height < 0) {
            val num = Math.abs(height)
            maxHeight = (ScreenUtil.screenHeight * num).toInt()
        } else {
            maxHeight = height.toInt()
        }
    }

    fun setMaxHeight(maxHeight: Int) {
        this.maxHeight = maxHeight
        resetMaxHeight()
        requestLayout()
    }

    fun setMaxHeight2(height: Float) {
        resetMaxHeight(height)
        requestLayout()
    }

    /*正在触摸的浮动View*/
    private var touchFloatView: View? = null
    private val touchViewLeft: Int get() = touchFloatView!!.x.toInt()
    private val touchViewRight: Int get() = (touchFloatView!!.x + touchFloatView!!.measuredWidth).toInt()
    private val touchViewTop: Int get() = touchFloatView!!.y.toInt()
    private val touchViewBottom: Int get() = (touchFloatView!!.y + touchFloatView!!.measuredHeight).toInt()

    /*用来处理浮动View的手势*/
    private val gestureDetector: GestureDetector by lazy {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                val touchView = findView(e.rawX, e.rawY)
                touchView?.layoutParams?.let {
                    //L.e("call: onDown -> $touchView")
                    if (touchView.layoutParams is LayoutParams) {
                        val param: LayoutParams = touchView.layoutParams as LayoutParams
                        //L.e("call: onDown -> ${param.isFloatView} ${param.isFloatFixRect} ${param.isFloatAutoAdsorb}")
                        if (param.isFloatView) {
                            //floatAnim.cancel()
                            touchFloatView = touchView
                        }
                    }
                }
                return super.onDown(e)
            }

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                //L.e("call: onScroll -> $distanceX $distanceY")
                if (touchFloatView != null) {

                    touchFloatView!!.setTag(R.id.base_r_layout_is_move, true)
                    ViewCompat.offsetLeftAndRight(touchFloatView, -distanceX.toInt())
                    ViewCompat.offsetTopAndBottom(touchFloatView, -distanceY.toInt())

                    checkEdge()
                    return true
                }
                return super.onScroll(e1, e2, distanceX, distanceY)
            }
        }).apply {
            setIsLongpressEnabled(false)
        }
    }

    /*边界检查*/
    private fun checkEdge() {
        touchFloatView?.layoutParams?.let {
            val param: LayoutParams = it as LayoutParams
            if (param.isFloatFixRect) {
                when {
                    touchViewLeft < 0 -> {
                        ViewCompat.offsetLeftAndRight(touchFloatView, -touchViewLeft)
                    }
                    touchViewRight > measuredWidth -> {
                        ViewCompat.offsetLeftAndRight(touchFloatView, -(touchViewRight - measuredWidth))
                    }
                    touchViewTop < 0 -> {
                        ViewCompat.offsetTopAndBottom(touchFloatView, -touchViewTop)
                    }
                    touchViewBottom > measuredHeight -> {
                        ViewCompat.offsetTopAndBottom(touchFloatView, -(touchViewBottom - measuredHeight))
                    }
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        var handleTouch = false

        if (touchFloatView != null) {
            handleTouch = true
            if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
                handleFloatViewOnTouchUp()
                touchFloatView = null
            }
        }

        return if (handleTouch) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(ev) || super.onInterceptTouchEvent(ev)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return LayoutParams(context, attrs)
    }

    val tempRect: Rect by lazy {
        Rect()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {

        /*是否移动过*/
        fun isFloatViewMove(childView: View): Boolean {
            val tag = childView.getTag(R.id.base_r_layout_is_move)
            return (tag != null && tag as Boolean)
        }

        for (i in 0 until childCount) {
            val childAt = getChildAt(i)
            val layoutParams = childAt.layoutParams
            if (layoutParams is LayoutParams) {
                if (layoutParams.isFloatView) {
                    if (isFloatViewMove(childAt)) {
                        childAt.getLocalVisibleRect(tempRect)

                        if (tempRect.width() != 0) {
                            childAt.setTag(R.id.base_r_layout_left, childAt.x)
                            childAt.translationX = 0f
                        } else {
                            childAt.setTag(R.id.base_r_layout_left, null)
                        }
                        if (tempRect.height() != 0) {
                            childAt.setTag(R.id.base_r_layout_top, childAt.y)
                            childAt.translationY = 0f
                        } else {
                            childAt.setTag(R.id.base_r_layout_top, null)
                        }
                    }
                }
            }
        }
        super.onLayout(changed, left, top, right, bottom)
        for (i in 0 until childCount) {
            val childAt = getChildAt(i)
            val layoutParams = childAt.layoutParams
            if (layoutParams is LayoutParams) {
                if (layoutParams.isFloatView) {
                    val tagLeft = childAt.getTag(R.id.base_r_layout_left)
                    val tagTop = childAt.getTag(R.id.base_r_layout_top)

                    val childLeft: Int = if (tagLeft != null) {
                        (tagLeft as Float).toInt()
                    } else {
                        childAt.left
                    }
                    val childTop: Int = if (tagTop != null) {
                        (tagTop as Float).toInt()
                    } else {
                        childAt.top
                    }
                    childAt.layout(childLeft, childTop, childLeft + childAt.measuredWidth, childTop + childAt.measuredHeight)
                }
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun handleFloatViewOnTouchUp() {
        touchFloatView?.layoutParams?.let {
            val param: LayoutParams = it as LayoutParams
            if (param.isFloatAutoAdsorb) {
                //自动吸附
                if (touchFloatView!!.centerX() > measuredWidth / 2) {
                    //自动到右边
                    if (touchViewRight < measuredWidth || param.isFloatFixRect) {
                        //ViewCompat.offsetLeftAndRight(touchFloatView, measuredWidth - touchFloatView!!.right)
                        animStartX = touchViewLeft
                        animEndX = measuredWidth - touchFloatView!!.measuredWidth

                        animStartY = touchViewTop
                        animEndY = touchViewTop
                        //floatAnim.start()
                        touchFloatView!!.animate()
                                .translationXBy((animEndX - animStartX).toFloat())
                                .setDuration(300)
                                .start()
                    }
                } else {
                    //自动到左边
                    if (touchViewLeft > 0 || param.isFloatFixRect) {
                        //ViewCompat.offsetLeftAndRight(touchFloatView, -touchFloatView!!.left)
                        animStartX = touchViewLeft
                        animEndX = 0

                        animStartY = touchViewTop
                        animEndY = touchViewTop
                        //floatAnim.start()

                        touchFloatView!!.animate()
                                .translationXBy((animEndX - animStartX).toFloat())
                                .setDuration(300)
                                .start()
                    }
                }

            }
            checkEdge()
        }
    }

    private var animStartX = 0
    private var animEndX = 0
    private var animStartY = 0
    private var animEndY = 0

    class LayoutParams : FrameLayout.LayoutParams {
        /**是否可以浮动*/
        var isFloatView = false
        /**自动吸附到边缘(左右边缘)*/
        var isFloatAutoAdsorb = false
        var isFloatFixRect = false

        constructor(c: Context, attrs: AttributeSet?) : super(c, attrs) {
            val a = c.obtainStyledAttributes(attrs, R.styleable.RFrameLayout)
            isFloatView = a.getBoolean(R.styleable.RFrameLayout_r_is_float_view, isFloatView)
            isFloatAutoAdsorb = a.getBoolean(R.styleable.RFrameLayout_r_float_auto_adsorb, isFloatAutoAdsorb)
            isFloatFixRect = a.getBoolean(R.styleable.RFrameLayout_r_float_fix_rect, isFloatFixRect)
            a.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height)
        constructor(width: Int, height: Int, gravity: Int) : super(width, height, gravity)
        constructor(source: ViewGroup.LayoutParams?) : super(source)
        constructor(source: MarginLayoutParams?) : super(source)
    }
}