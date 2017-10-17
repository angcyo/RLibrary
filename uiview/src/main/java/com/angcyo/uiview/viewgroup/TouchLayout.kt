package com.angcyo.uiview.viewgroup

import android.content.Context
import android.support.annotation.CallSuper
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：支持 四方向手势
 * 创建人员：Robi
 * 创建时间：2017/10/13 14:59
 * 修改人员：Robi
 * 修改时间：2017/10/13 14:59
 * 修改备注：
 * Version: 1.0.0
 */
open class TouchLayout(context: Context, attributeSet: AttributeSet? = null) : FrameLayout(context, attributeSet) {

    companion object {
        /**当滚动距离大于多少时, 视为滚动了*/
        const val scrollDistanceSlop = 0
        /**当Fling速度大于多少时, 视为Fling*/
        const val flingVelocitySlop = 0
    }

    /**4个方向*/
    enum class ORIENTATION {
        LEFT, RIGHT, TOP, BOTTOM
    }

    /*用来检测手指滑动方向*/
    protected val orientationGestureDetector = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            //L.e("call: onFling -> \n$e1 \n$e2 \n$velocityX $velocityY")

            val absX = Math.abs(velocityX)
            val absY = Math.abs(velocityY)

            if (absX > flingVelocitySlop || absY > flingVelocitySlop) {
                if (absY > absX) {
                    //竖直方向的Fling操作
                    onFlingChange(if (velocityY > 0) ORIENTATION.BOTTOM else ORIENTATION.TOP, velocityY)
                } else if (absX > absY) {
                    //水平方向的Fling操作
                    onFlingChange(if (velocityX > 0) ORIENTATION.RIGHT else ORIENTATION.LEFT, velocityX)
                }
            }

            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            //L.e("call: onScroll -> \n$e1 \n$e2 \n$distanceX $distanceY")

            val absX = Math.abs(distanceX)
            val absY = Math.abs(distanceY)

            if (absX > scrollDistanceSlop || absY > scrollDistanceSlop) {
                if (absY > absX) {
                    //竖直方向的Scroll操作
                    onScrollChange(if (distanceY > 0) ORIENTATION.TOP else ORIENTATION.BOTTOM, distanceY)
                } else if (absX > absY) {
                    //水平方向的Scroll操作
                    onScrollChange(if (distanceX > 0) ORIENTATION.LEFT else ORIENTATION.RIGHT, distanceX)
                }
            }

            return true
        }

    })

    @CallSuper
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        //orientationGestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    @CallSuper
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        orientationGestureDetector.onTouchEvent(ev)
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        //orientationGestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    fun isVertical(orientation: ORIENTATION) = orientation == ORIENTATION.TOP || orientation == ORIENTATION.BOTTOM
    fun isHorizontal(orientation: ORIENTATION) = orientation == ORIENTATION.LEFT || orientation == ORIENTATION.RIGHT

    /**Fling操作的处理方法*/
    open fun onFlingChange(orientation: ORIENTATION, velocity: Float) {
        //L.e("call: onFlingChange -> $orientation $velocity")
    }

    /**Scroll操作的处理方法*/
    open fun onScrollChange(orientation: ORIENTATION, distance: Float) {
        //L.e("call: onScrollChange -> $orientation $distance")
    }
}