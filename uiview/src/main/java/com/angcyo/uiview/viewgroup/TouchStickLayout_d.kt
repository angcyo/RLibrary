package com.angcyo.uiview.viewgroup

import android.content.Context
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.OverScroller
import com.angcyo.library.utils.L
import com.angcyo.uiview.recycler.RRecyclerView
import com.angcyo.uiview.utils.UI

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：打造一个支持内嵌滚动, 支持下滑返回, 上滑全屏显示.
 * 创建人员：Robi
 * 创建时间：2017/10/13 14:59
 * 修改人员：Robi
 * 修改时间：2017/10/13 14:59
 * 修改备注：
 * Version: 1.0.0
 */
@Deprecated("写不下去, 还是用 StickLayout2吧")
open class TouchStickLayout_d(context: Context, attributeSet: AttributeSet? = null) : TouchLayout(context, attributeSet) {

    /**布局方式, 采用的是垂直方向的线性布局方式*/
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        //super.onLayout(changed, left, top, right, bottom)
        var top = 0
        for (i in 0 until childCount) {
            val childAt = getChildAt(i)
            childAt.layout(0, top, measuredWidth, top + childAt.measuredHeight)
            top += childAt.measuredHeight
        }
        viewMaxHeight = top
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        super.addView(child, index, params)
        if (childCount > 2) {
            throw IllegalArgumentException("child view not greater than 2")
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }

    override fun onScrollChange(orientation: ORIENTATION, distance: Float) {
        super.onScrollChange(orientation, distance)
//        when (orientation) {
//            ORIENTATION.RIGHT, ORIENTATION.LEFT -> {
//                scrollBy(distance.toInt(), 0)
//            }
//            ORIENTATION.TOP, ORIENTATION.BOTTOM -> {
//                scrollBy(0, distance.toInt())
//            }
//        }
    }

    /*最大允许滚动的高度, 通常等于 child 0 的高度*/
    private val maxScrollTopHeight: Int
        get() {
            return if (childCount > 0) {
                getChildAt(0).measuredHeight
            } else {
                0
            }
        }

    /*顶部还可以滚动多少距离*/
    private val canScrollTopHeight: Int
        get() = Math.max(maxScrollTopHeight - scrollY, 0)

    override fun scrollTo(x: Int, y: Int) {
        if (y > 0) {
            super.scrollTo(x, Math.min(y, maxScrollTopHeight))
        } else if (y < 0) {
            super.scrollTo(x, Math.max(y, 0))
        } else {
            super.scrollTo(x, y)
        }
    }

    private fun consumedScroll(target: View?, dy: Int): Int {
        target?.let {
            if (canScrollTopHeight > 0 || !UI.canChildScrollUp(target)) {
                if (canScrollTopHeight >= dy) {
                    scrollBy(0, dy)
                    return dy
                } else {
                    scrollBy(0, canScrollTopHeight)
                    return canScrollTopHeight
                }
            }
        }
        return 0
    }

    override fun getNestedScrollAxes(): Int {
        L.e("call: getNestedScrollAxes -> ")
        return super.getNestedScrollAxes()
    }

    override fun onNestedFling(target: View?, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        L.e("call: onNestedFling -> $velocityX $velocityY $consumed")
        return super.onNestedFling(target, velocityX, velocityY, consumed)
    }

    override fun onNestedPreScroll(target: View?, dx: Int, dy: Int, consumed: IntArray?) {
        super.onNestedPreScroll(target, dx, dy, consumed)
        //L.e("call: onNestedPreScroll -> $dx $dy $consumed")
        consumed?.let {
            //it[0] = dx / 2
            it[1] = consumedScroll(target, dy)
        }
    }

    override fun onStopNestedScroll(child: View?) {
        super.onStopNestedScroll(child)
        //L.e("call: onStopNestedScroll -> $child")
    }

    override fun onStartNestedScroll(child: View?, target: View?, nestedScrollAxes: Int): Boolean {
        //L.e("call: onStartNestedScroll -> $nestedScrollAxes")
        if (target is RRecyclerView) {
            return true
        }
        return super.onStartNestedScroll(child, target, nestedScrollAxes)
    }

    override fun onNestedScrollAccepted(child: View?, target: View?, axes: Int) {
        super.onNestedScrollAccepted(child, target, axes)
        //L.e("call: onNestedScrollAccepted -> $axes")
    }

    override fun onNestedScroll(target: View?, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
        L.e("call: onNestedScroll -> $dxConsumed $dyConsumed $dxUnconsumed $dyUnconsumed")
    }

    override fun onNestedPreFling(target: View?, velocityX: Float, velocityY: Float): Boolean {
        L.e("call: onNestedPreFling -> $velocityX $velocityY")

        if (target is RRecyclerView) {
            lastRecyclerView = target
            target.setOnFlingEndListener(RRecyclerView.OnFlingEndListener { currVelocity ->
                L.e("call: onNestedPreFling end-> $currVelocity")
                fling(currVelocity)
            })
        }

        if (UI.canChildScrollDown(target) && velocityY > 0) {
            fling(-velocityY)
        }

        return super.onNestedPreFling(target, velocityX, velocityY)
    }

    /*所有child的高度和*/
    private var viewMaxHeight: Int = 0
    /*是否正在fling中*/
    private var isFling: Boolean = false
    private var lastRecyclerView: RRecyclerView? = null
    private var mOverScroller: OverScroller = OverScroller(getContext())
    private var mGestureDetectorCompat: GestureDetectorCompat

    init {
        mGestureDetectorCompat = GestureDetectorCompat(getContext(),
                object : GestureDetector.SimpleOnGestureListener() {

                    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                        if (Math.abs(velocityX) > Math.abs(velocityY)) {
                            return false
                        }

//                        if (isFloat() /*&& velocityY < 0*/) {
//                            return false
//                        }

                        fling(velocityY)
                        return true
                    }
                })
    }


    override fun computeScroll() {
        if (mOverScroller.computeScrollOffset()) {
            val currY = mOverScroller.currY
//            if (currY - maxScrollY >= 0) {
//                if (isFling) {
//                    val recyclerView = mScrollTarget.getRecyclerView()
//                    if (recyclerView != null) {
//                        recyclerView!!.post(Runnable {
//                            //recyclerView.fling(0, Math.max(0, 1000));
//
//
//                            val lastVelocity = getLastVelocity()
//                            recyclerView!!.fling(0, Math.max(0f, Math.abs(lastVelocity)).toInt())
//
//                            //                                int velocityDecay = getChildAt(0).getMeasuredHeight() * 3;//速度衰减值
//                            //                                if (lastVelocity < velocityDecay) {
//                            //                                    recyclerView.fling(0, Math.max(0, (int) Math.abs(lastVelocity)));
//                            //                                } else {
//                            //                                    recyclerView.fling(0, Math.max(0, (int) lastVelocity - velocityDecay));
//                            //                                }
//                        })
//                    }
//                }
//                isFling = false
//            }
            scrollTo(0, currY)
            postInvalidate()
        }
    }

    private fun fling(velocityY: Float) {
        isFling = true
        var maxY = viewMaxHeight
        lastRecyclerView?.let {
            maxY = Math.max(maxY, it.computeVerticalScrollRange())
        }
        mOverScroller.fling(0, scrollY, 0, (-velocityY).toInt(), 0, 0, 0, maxY)
        postInvalidate()
    }

}