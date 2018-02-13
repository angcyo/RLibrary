package com.angcyo.uiview.container

import android.content.Context
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.angcyo.library.utils.L
import com.angcyo.uiview.kotlin.exactlyMeasure
import com.angcyo.uiview.kotlin.have

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：滑动布局, 上下左右都支持滑动. 可以用来实现侧滑菜单, 侧滑返回等.
 * 创建人员：Robi
 * 创建时间：2017/07/07 16:11
 * 修改人员：Robi
 * 修改时间：2017/07/07 16:11
 * 修改备注：
 * Version: 1.0.0
 */
@Deprecated("实现方式无法满足需求")
open class RSwipeLayout(context: Context, attributeSet: AttributeSet? = null) : ViewGroup(context, attributeSet) {

    companion object {
        const val DRAG_LEFT = ViewDragHelper.EDGE_LEFT
        const val DRAG_RIGHT = ViewDragHelper.EDGE_RIGHT
        const val DRAG_TOP = ViewDragHelper.EDGE_TOP
        const val DRAG_BOTTOM = ViewDragHelper.EDGE_BOTTOM
    }

    private var menuView: View? = null
    private var contentView: View? = null

    private val viewDragCallback = object : ViewDragHelper.Callback() {
        /**希望拖拽的方向*/
        var captureDragDirection: Int = 0

        /**
         * 限制 value 在 min 和 max 之间
         */
        private fun clamp(value: Int, min: Int, max: Int): Int {
            return Math.max(min, Math.min(max, value))
        }

        /**返回想要拖拽的边缘方向*/
        private fun getCaptureDirection(): Int {
            if (mDragHelper.isEdgeTouched(DRAG_RIGHT)) {
                return DRAG_RIGHT
            } else if (mDragHelper.isEdgeTouched(DRAG_TOP)) {
                return DRAG_TOP
            } else if (mDragHelper.isEdgeTouched(DRAG_BOTTOM)) {
                return DRAG_BOTTOM
            } else if (mDragHelper.isEdgeTouched(DRAG_LEFT)) {
                return DRAG_LEFT
            } else {
                return 0
            }
        }

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            if (child == menuView) {
                return false
            }
            captureDragDirection = getCaptureDirection()
            if (dragDirection.have(captureDragDirection)) {
                //如果支持边缘拖拽的方法
                return true
            }
            return false
        }


        override fun onViewDragStateChanged(state: Int) {
            super.onViewDragStateChanged(state)
            L.e("call: onViewDragStateChanged -> $state")
        }

        /**放开手指之后*/
        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)
            L.e("call: onViewReleased -> $xvel $yvel")
            if (captureDragDirection.have(DRAG_RIGHT)) {
                //右边缘滑动
                if (Math.abs(xvel) >= measuredWidth * menuMinOpenRatio) {
                    //打开菜单
                    mDragHelper.settleCapturedViewAt((-measuredWidth * menuMaxOpenRatio).toInt(), 0)
                } else {
                    //关闭菜单
                    closeMenu()
                }
            } else if (captureDragDirection.have(DRAG_TOP)) {
                //顶部缘滑动
                if (Math.abs(yvel) >= measuredHeight * menuMinOpenRatio) {
                    //打开菜单
                    mDragHelper.settleCapturedViewAt(0, (measuredHeight * menuMaxOpenRatio).toInt())
                } else {
                    //关闭菜单
                    closeMenu()
                }
            } else if (captureDragDirection.have(DRAG_BOTTOM)) {
                //底部缘滑动
                if (Math.abs(yvel) >= measuredHeight * menuMinOpenRatio) {
                    //打开菜单
                    mDragHelper.settleCapturedViewAt(0, -(measuredHeight * menuMaxOpenRatio).toInt())
                } else {
                    //关闭菜单
                    closeMenu()
                }
            } else if (captureDragDirection.have(DRAG_LEFT)) {
                //左边缘滑动
                if (Math.abs(xvel) >= measuredWidth * menuMinOpenRatio) {
                    //打开菜单
                    mDragHelper.settleCapturedViewAt((measuredWidth * menuMaxOpenRatio).toInt(), 0)
                } else {
                    //关闭菜单
                    closeMenu()
                }
            } else {
            }
            postInvalidateOnAnimation()
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)
            L.e("call: onViewPositionChanged -> $left $top")
        }

        /**判断方向是否是竖直方向的*/
        private fun isDragVertical(direction: Int): Boolean = direction.have(DRAG_BOTTOM) || direction.have(DRAG_TOP)

        private fun isDragHorizontal(direction: Int): Boolean = direction.have(DRAG_LEFT) || direction.have(DRAG_RIGHT)

        /**允许拖动的范围*/
        override fun getViewVerticalDragRange(child: View): Int {
            if (dragDirection.have(DRAG_BOTTOM) || dragDirection.have(DRAG_TOP)) {
                return (measuredHeight * menuMaxOpenRatio).toInt()
            } else {
                return 0
            }
        }

        override fun getViewHorizontalDragRange(child: View): Int {
            if (dragDirection.have(DRAG_LEFT) || dragDirection.have(DRAG_RIGHT)) {
                return (measuredWidth * menuMaxOpenRatio).toInt()
            } else {
                return 0
            }
        }

        /**修正当前拖拽的位置*/
        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            if (isDragHorizontal(captureDragDirection)) {
                return 0
            }
            if (captureDragDirection.have(DRAG_TOP)) {
                //顶部拖拽
                return clamp(top, 0, (measuredHeight * menuMaxOpenRatio).toInt())
            } else {
                //底部拖拽
                return clamp(top, (-measuredHeight * menuMaxOpenRatio).toInt(), 0)
            }
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            if (isDragVertical(captureDragDirection)) {
                return 0
            }
            if (captureDragDirection.have(DRAG_LEFT)) {
                //左边拖拽
                return clamp(left, 0, (measuredWidth * menuMaxOpenRatio).toInt())
            } else {
                //右边拖拽
                return clamp(left, (-measuredWidth * menuMaxOpenRatio).toInt(), 0)
            }
        }
    }

    private var mDragHelper: ViewDragHelper

    init {
        mDragHelper = ViewDragHelper.create(this, 0.5f, viewDragCallback)
        //mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT)//设置只支持左边缘滑动返回
    }

    /**允许打开的方向*/
    var dragDirection = 0
        set(value) {
            field = value
            mDragHelper.setEdgeTrackingEnabled(field)
        }

    /**菜单允许打开的比例*/
    var menuMaxOpenRatio = 0.8f
    /**菜单滑动多少时, 视为打开菜单*/
    var menuMinOpenRatio = 0.2f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)

        menuView?.let {
            it.measure(exactlyMeasure(widthSize), exactlyMeasure(heightSize))
        }

        contentView?.let {
            it.measure(exactlyMeasure(widthSize), exactlyMeasure(heightSize))
        }

        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        menuView?.let {
            it.layout(0, 0, measuredWidth, measuredHeight)
        }

        contentView?.let {
            it.layout(0, 0, measuredWidth, measuredHeight)
        }
    }

    override fun addView(child: View?, index: Int, params: LayoutParams?) {
        throw NoSuchMethodException("请调用setMenuView() 或者 setContentView")
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        var interceptForDrag = false

        // Fix for pull request #13 and issue #12
        try {
            interceptForDrag = mDragHelper.shouldInterceptTouchEvent(ev)
        } catch (e: Exception) {
            interceptForDrag = false
        }

        return interceptForDrag
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        try {
            mDragHelper.processTouchEvent(event)
        } catch (e: Exception) {
        }
        return true
    }


    override fun computeScroll() {
        super.computeScroll()
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    /**设置菜单*/
    open fun setMenuView(menu: View) {
        menuView = menu
        super.addView(menuView, 0, ViewGroup.LayoutParams(-1, -1))
    }

    /**设置内容*/
    open fun setContentView(content: View) {
        contentView = content
        super.addView(contentView, -1, ViewGroup.LayoutParams(-1, -1))
    }

    /**关闭菜单*/
    open fun closeMenu() {
        mDragHelper.settleCapturedViewAt(0, 0)
        postInvalidateOnAnimation()
    }
}