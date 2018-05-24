package com.angcyo.uiview.viewgroup

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import com.angcyo.library.utils.L
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.*

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：模仿QQ侧滑菜单布局
 * 创建人员：Robi
 * 创建时间：2018/04/03 15:19
 * 修改人员：Robi
 * 修改时间：2018/04/03 15:19
 * 修改备注：
 * Version: 1.0.0
 */
class SliderMenuLayout(context: Context, attributeSet: AttributeSet? = null)
    : TouchLayout(context, attributeSet) {

    companion object {
        /*菜单在左边*/
        const val SLIDER_GRAVITY_LEFT = 1
        /*菜单在右边*/
        const val SLIDER_GRAVITY_RIGHT = 2
    }

    private var menuMaxWidthRatio = 0.8f

    /**回调接口*/
    var sliderCallback: SliderCallback? = null

    /**第几个view是菜单*/
    private var menuViewIndex = 0
    /**当开启了enableContentLinkage时, 内容滚动view的index*/
    private var contentViewIndex = 1

    /**激活内容联动, 激活此操作, menuViewIndex最好为1, 否则有界面层级有问题*/
    var enableContentLinkage = true

    /**菜单打开的方向*/
    var menuSliderGravity = SLIDER_GRAVITY_LEFT

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.SliderMenuLayout)
        menuMaxWidthRatio = typedArray.getFloat(R.styleable.SliderMenuLayout_r_menu_max_width, menuMaxWidthRatio)
        menuViewIndex = typedArray.getInt(R.styleable.SliderMenuLayout_r_menu_view_index, menuViewIndex)
        menuSliderGravity = typedArray.getInt(R.styleable.SliderMenuLayout_r_menu_slider_gravity, menuSliderGravity)
        enableContentLinkage = typedArray.getBoolean(R.styleable.SliderMenuLayout_r_enable_content_linkage, enableContentLinkage)
        typedArray.recycle()

        if (menuViewIndex == 0) {
            contentViewIndex = 1
        } else {
            contentViewIndex = 0
            enableContentLinkage = false//自动关闭联动, 因为界面层级有问题
        }

        setWillNotDraw(false)
    }

    private var needInterceptTouchEvent = false
    private var isTouchDown = false
    private var isTouchDownInContentWithMenuOpen = false //菜单打开的状态下, 点击在内容区域
    var isOldMenuOpen = false //事件触发之前,菜单的打开状态

    /*是否激活滑动菜单*/
    private fun canSlider(event: MotionEvent): Boolean {
        if (sliderCallback == null || isOldMenuOpen) {
            return true
        }
        return sliderCallback!!.canSlider(this, event)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val intercept = needInterceptTouchEvent
        super.onInterceptTouchEvent(ev)
        return if (canSlider(ev)) {
            intercept
        } else {
            false
        }
    }

    override fun handleCommonTouchEvent(event: MotionEvent) {
        super.handleCommonTouchEvent(event)
        if (event.isDown()) {
            isOldMenuOpen = isMenuOpen()
        }
        if (canSlider(event)) {

        } else {
            return
        }

        if (needInterceptTouchEvent) {
            parent.requestDisallowInterceptTouchEvent(true)
        }
        if (event.isDown()) {
            isTouchDown = true
            isTouchDownInContentWithMenuOpen = false
            touchDownX = event.x
            touchDownY = event.y
            overScroller.abortAnimation()

            if (isOldMenuOpen) {
                //打开已经打开
                if (event.x >= maxMenuWidth) {
                    //点击在内容区域
                    isTouchDownInContentWithMenuOpen = true
                    needInterceptTouchEvent = true
                }
            } else {
                if (contentLayoutLeft in 1..(maxMenuWidth - 1)) {
                    //当菜单滑动到一半, 突然被终止, 又再次点击时
                    needInterceptTouchEvent = true
                }
            }
        } else if (event.isFinish()) {
            isTouchDown = false
            parent.requestDisallowInterceptTouchEvent(false)

            if (needInterceptTouchEvent) {
                if (isTouchDownInContentWithMenuOpen &&
                        ((touchEventX - touchDownX) == 0f) ||
                        (touchEventY - touchDownY).abs() > (touchEventX - touchDownX).abs()) {
                    if (event.x >= maxMenuWidth) {
                        //在菜单打开的情况下,点击了内容区域, 并且没有触发横向滚动
                        closeMenu()
                    } else {
                        resetLayout()
                    }
                } else {
                    resetLayout()
                }
                isTouchDownInContentWithMenuOpen = false
                needInterceptTouchEvent = false
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        return true
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (childCount < 2) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            var widthSize = MeasureSpec.getSize(widthMeasureSpec)
            val widthMode = MeasureSpec.getMode(widthMeasureSpec)
            var heightSize = MeasureSpec.getSize(heightMeasureSpec)
            val heightMode = MeasureSpec.getMode(heightMeasureSpec)

            if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
                //测量菜单, 和内容的宽度
                for (i in 0 until childCount) {
                    val childAt = getChildAt(i)
                    when (i) {
                        menuViewIndex -> childAt.measure(exactlyMeasure(menuMaxWidthRatio * widthSize), heightMeasureSpec)
                        contentViewIndex -> childAt.measure(exactlyMeasure(widthSize), heightMeasureSpec)
                        else -> measureChildWithMargins(childAt, widthMeasureSpec, 0, heightMeasureSpec, 0)
                    }
                }
                setMeasuredDimension(widthSize, heightSize)
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }
    }

//    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
//        super.addView(child, index, params)
//        if (childCount > 2) {
//            throw IllegalStateException("不支持2个以上的子布局")
//        }
//    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        refreshContentLayout(scrollHorizontalDistance, false)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        sliderCallback?.onSizeChanged(this)
    }


    override fun onScrollChange(orientation: ORIENTATION, distance: Float /*瞬时值*/) {
        super.onScrollChange(orientation, distance)
        //refreshMenuLayout(((secondMotionEvent?.x ?: 0f) - (firstMotionEvent?.x ?: 0f)).toInt())
        if (canSlider(firstMotionEvent!!)) {
            if (isHorizontal(orientation)) {
                if (!needInterceptTouchEvent) {
                    if (distance > 0) {
                        //左滑动
                        if (isMenuClose()) {

                        } else {
                            needInterceptTouchEvent = true
                        }
                    } else {
                        //又滑动
                        if (isMenuOpen()) {

                        } else {
                            needInterceptTouchEvent = true
                        }
                    }
                }

                if (needInterceptTouchEvent) {
                    refreshLayout(distance.toInt())
                }
            }
        }
    }

    override fun onFlingChange(orientation: ORIENTATION, velocity: Float /*瞬时值*/) {
        super.onFlingChange(orientation, velocity)
        //L.e("call: onFlingChange -> $velocity")
        if (canSlider(firstMotionEvent!!)) {
            if (isHorizontal(orientation)) {
                if (velocity < -2000) {
                    //快速向左
                    closeMenu()
                } else if (velocity > 2000) {
                    //快速向右
                    openMenu()
                }
            }
        }
    }

    /**菜单是否完全打开*/
    fun isMenuOpen(): Boolean {
        return scrollHorizontalDistance >= maxMenuWidth
    }

    /**菜单完全关闭*/
    fun isMenuClose(): Boolean {
        return scrollHorizontalDistance <= 0
    }

    /**根据当前打开程度, 决定*/
    fun resetLayout() {
        if (isOldMenuOpen) {
            //菜单已经打开
            if (scrollHorizontalDistance <= maxMenuWidth * 2 / 3) {
                closeMenu()
            } else {
                openMenu()
            }
        } else {
            //菜单未打开
            if (scrollHorizontalDistance >= maxMenuWidth * 1 / 3) {
                openMenu()
            } else {
                closeMenu()
            }
        }
    }

    /**关闭菜单*/
    fun closeMenu() {
        if (scrollHorizontalDistance == 0) {
            if (isOldMenuOpen) {
                sliderCallback?.onMenuSlider(this, 0f, isTouchDown)
            } else {
            }
        } else {
            startScrollTo(scrollHorizontalDistance, 0)
        }
    }

    /**打开菜单*/
    fun openMenu() {
        if (scrollHorizontalDistance == maxMenuWidth) {
            if (isOldMenuOpen) {
            } else {
                sliderCallback?.onMenuSlider(this, 1f, isTouchDown)
            }
        } else {
            startScrollTo(scrollHorizontalDistance, maxMenuWidth)
        }
    }

    /**刷新布局位置*/
    private fun refreshLayout(distanceX: Int /*没次移动的距离*/) {
        //L.e("call: refreshMenuLayout -> $distanceX")
        refreshContentLayout(clampViewPositionHorizontal(scrollHorizontalDistance - distanceX))
    }

    //
    private fun refreshContentLayout(left: Int, notify: Boolean = true) {
        if (childCount >= 2) {
            if (enableContentLinkage) {
                getChildAt(contentViewIndex).apply {
                    layout(left, 0, left + this.measuredWidth, this.measuredHeight)
                }
            }
            scrollHorizontalDistance = left
            refreshMenuLayout()

            if (notify) {
                sliderCallback?.onMenuSlider(this@SliderMenuLayout, left.toFloat() / maxMenuWidth, isTouchDown)
            }
        }
    }

    override fun computeScroll() {
        if (overScroller.computeScrollOffset()) {
            //scrollTo(overScroller.currX, overScroller.currY)
            val currX = overScroller.currX
            if (scrollHorizontalDistance != currX) {
                refreshContentLayout(currX)
            }
            postInvalidate()
        }
    }

    //横向滚动了多少距离
    private var scrollHorizontalDistance = 0

    //当前内容布局的Left坐标
    private val contentLayoutLeft: Int
        get() {
            return if (childCount >= Math.max(menuViewIndex, contentViewIndex)) {
                getChildAt(contentViewIndex).left
            } else {
                0
            }
        }

    //菜单允许展开的最大宽度
    private val maxMenuWidth: Int
        get() {
            return (menuMaxWidthRatio * measuredWidth).toInt()
        }

    //单独更新菜单,营造视差滚动
    private fun refreshMenuLayout() {
        if (enableContentLinkage) {
            //计算出菜单展开的比例
            val fl = contentLayoutLeft.toFloat() / maxMenuWidth
            if (fl >= 0f && childCount > 0) {
                getChildAt(menuViewIndex).apply {
                    //视差开始时的偏移值
                    val menuOffsetStart = -maxMenuWidth / 2
                    val left = menuOffsetStart + (menuOffsetStart.abs() * fl).toInt()
                    layout(left, 0, left + this.measuredWidth, this.measuredHeight)
                }
            }
        } else {
            if (childCount > menuViewIndex) {
                getChildAt(menuViewIndex).apply {
                    val left = -this.measuredWidth + scrollHorizontalDistance
                    L.e("call: menu layout -> left:$left right:${left + this.measuredWidth}")
                    layout(left, 0, left + this.measuredWidth, this.measuredHeight)
                }
            }
        }
    }

    /**约束内容允许滚动的范围*/
    private fun clampViewPositionHorizontal(value: Int): Int {
        val minValue = 0
        val maxValue = maxMenuWidth

        var result = value
        if (value < minValue) {
            result = minValue
        } else if (value > maxValue) {
            result = maxValue
        }
        return result
    }

    private val maskRect: Rect by lazy {
        Rect()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        //绘制内容区域的阴影遮盖
        if (isMenuClose()) {

        } else {
            val layoutLeft = scrollHorizontalDistance
            debugPaint.color = Color.BLACK.tranColor((255 * (layoutLeft.toFloat() / maxMenuWidth) * 0.4f /*限制一下值*/).toInt())
            debugPaint.style = Paint.Style.FILL_AND_STROKE
            maskRect.set(layoutLeft, 0, measuredWidth, measuredHeight)
            canvas.drawRect(maskRect, debugPaint)
        }
    }

    interface SliderCallback {

        /**当前是否可以操作*/
        fun canSlider(menuLayout: SliderMenuLayout, event: MotionEvent): Boolean

        fun onSizeChanged(menuLayout: SliderMenuLayout)

        /**
         * 菜单打开的完成度
         * @param ratio [0-1]
         * */
        fun onMenuSlider(menuLayout: SliderMenuLayout, ratio: Float, isTouchDown: Boolean /*手指是否还在触摸*/)
    }

    open class SimpleSliderCallback : SliderCallback {
        override fun canSlider(menuLayout: SliderMenuLayout, event: MotionEvent): Boolean {
            return true
        }

        override fun onSizeChanged(menuLayout: SliderMenuLayout) {
        }

        override fun onMenuSlider(menuLayout: SliderMenuLayout, ratio: Float, isTouchDown: Boolean) {
        }
    }
}

