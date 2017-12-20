package com.angcyo.uiview.widget.group

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.OverScroller
import com.angcyo.uiview.kotlin.isTouchFinish
import com.angcyo.uiview.utils.ScreenUtil
import com.angcyo.uiview.viewgroup.TouchLayout

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：模仿抖音, 滑动选择 长按拍摄, 单击拍摄
 * 创建人员：Robi
 * 创建时间：2017/12/20 13:44
 * 修改人员：Robi
 * 修改时间：2017/12/20 13:44
 * 修改备注：
 * Version: 1.0.0
 */
class SlideSelectLayout(context: Context, attributeSet: AttributeSet? = null) : LinearLayout(context, attributeSet) {

    /**当前选择的index*/
    var curSelectIndex = 0

    var onSlideSelectChangeListener: OnSlideSelectChangeListener? = null

    init {
        orientation = LinearLayout.HORIZONTAL
        setPadding(paddingLeft, paddingTop, paddingRight, (paddingBottom + 6 * ScreenUtil.density()).toInt()) /*底部预留 绘制指示器的距离*/
        setWillNotDraw(false)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        for (i in 0 until childCount) {
            getChildAt(i).setOnClickListener {
                selectIndex(i)
            }
        }

        selectIndex(curSelectIndex)
    }

    /**选中某个Item*/
    fun selectIndex(index: Int, anim: Boolean = true) {
        if (index >= childCount) {
            return
        }
        val oldIndex = curSelectIndex

        curSelectIndex = index
        val childAt = getChildAt(index)
        val centerX = measuredWidth / 2 //中点坐标
        val childX = childAt.left + childAt.measuredWidth / 2 //item的中心

        if (anim) {
            startScroll(childX - centerX - scrollX, 0)
        } else {
            scrollTo(childX - centerX, 0)
        }

        for (i in 0 until childCount) {
            getChildAt(i).isSelected = index == i
        }

        //L.e("call: selectIndex -> oldIndex:$oldIndex $curSelectIndex")
        if (oldIndex != curSelectIndex) {
            onSlideSelectChangeListener?.onSlideSelectChange(oldIndex, curSelectIndex)
        }
    }

//    private val time = System.currentTimeMillis()
//    private var time2 = System.currentTimeMillis()
//    private val time3: Long
//        get() = System.currentTimeMillis()

    private val indicatorWidth: Int = (4 * ScreenUtil.density()).toInt()
    private val indicatorHeight: Int = (4 * ScreenUtil.density()).toInt()
    private val indicatorColor: Int = Color.WHITE
    private val paint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL_AND_STROKE
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

//        L.e("call: onDraw -> $time     $time2   $time3")

        //在中心位置绘制指示器
        canvas.save()
        val m2 = measuredWidth / 2
        val i2 = indicatorWidth / 2
        paint.color = indicatorColor
        canvas.drawRect((m2 - i2).toFloat() + scrollX,
                (measuredHeight - indicatorHeight).toFloat(),
                (m2 + i2).toFloat() + scrollX,
                measuredHeight.toFloat(),
                paint)
        canvas.restore()
    }

    private val overScroller = OverScroller(context)
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            //L.e("call: onFling -> \n$e1 \n$e2 \n$velocityX $velocityY")

            val absX = Math.abs(velocityX)
            val absY = Math.abs(velocityY)

            if (absX > TouchLayout.flingVelocitySlop || absY > TouchLayout.flingVelocitySlop) {
                if (absY > absX) {
                    //竖直方向的Fling操作
                    //onFlingChange(if (velocityY > 0) TouchLayout.ORIENTATION.BOTTOM else TouchLayout.ORIENTATION.TOP, velocityY)
                } else if (absX > absY) {
                    //水平方向的Fling操作
                    //onFlingChange(if (velocityX > 0) TouchLayout.ORIENTATION.RIGHT else TouchLayout.ORIENTATION.LEFT, velocityX)
                }
            }

            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            //L.e("call: onScroll -> \n$e1 \n$e2 \n$distanceX $distanceY")

            val absX = Math.abs(distanceX)
            val absY = Math.abs(distanceY)

            if (absX > TouchLayout.scrollDistanceSlop || absY > TouchLayout.scrollDistanceSlop) {
                if (absY > absX) {
                    //竖直方向的Scroll操作
                    onScrollChange(if (distanceY > 0) TouchLayout.ORIENTATION.TOP else TouchLayout.ORIENTATION.BOTTOM, distanceY)
                } else if (absX > absY) {
                    //水平方向的Scroll操作
                    onScrollChange(if (distanceX > 0) TouchLayout.ORIENTATION.LEFT else TouchLayout.ORIENTATION.RIGHT, distanceX)
                }
            }

            return true
        }

    })

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        if (isTouchFinish(event)) {
            val ix = scrollX + measuredWidth / 2 //中点目标位置
            var index = curSelectIndex
            var dx = measuredWidth

            for (i in 0 until childCount) {
                val childAt = getChildAt(i)
                val cx = childAt.left + childAt.measuredWidth / 2
                val abs = Math.abs(cx - ix)
                if (abs < dx) {
                    //计算离中心点最近的child
                    dx = abs
                    index = i
                }
            }
            selectIndex(index)
            //L.e("call: onTouchEvent -> ")
        }
        return true
    }

    override fun computeScroll() {
        if (overScroller.computeScrollOffset()) {
            scrollTo(overScroller.currX, overScroller.currY)
            postInvalidate()
        }
    }

    /**Scroll操作的处理方法*/
    fun onScrollChange(orientation: TouchLayout.ORIENTATION, distance: Float) {
        if (orientation == TouchLayout.ORIENTATION.LEFT || orientation == TouchLayout.ORIENTATION.RIGHT) {
            scrollBy(distance.toInt(), 0)
        }
    }

    fun startScroll(dx: Int, dy: Int) {
        overScroller.startScroll(scrollX, scrollY, dx, dy, 300)
        postInvalidate()
    }

    interface OnSlideSelectChangeListener {
        fun onSlideSelectChange(from: Int, to: Int)
    }
}