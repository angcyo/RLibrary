package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/12/13 09:03
 * 修改人员：Robi
 * 修改时间：2017/12/13 09:03
 * 修改备注：
 * Version: 1.0.0
 */
class RainAnimView(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet) {
    var rainList: List<RainBean>? = null

    var listener: OnTapUpListener? = null

    /*多点控制*/
    private val pointList = SparseArray<PointF>()

    private val gestureDetector: GestureDetector by lazy {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                checkListener(e.x.toInt(), e.y.toInt())
                return true
            }
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        rainList?.let {
            for (bean in it) {
                bean.draw(canvas)
            }
        }
    }

    private val deviation = 10

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (rainList?.isEmpty() == false) {
            //gestureDetector.onTouchEvent(event)
            val actionIndex = event.actionIndex
            val id = event.getPointerId(actionIndex)

            val eventX = event.getX(actionIndex)
            val eventY = event.getY(actionIndex)

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    pointList.put(id, PointF(eventX, eventY))
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    val p = pointList.get(id)
                    if (Math.abs(p.x - eventX) < deviation &&
                            Math.abs(p.y - eventY) < deviation) {
                        checkListener(p.x.toInt(), p.y.toInt())
                        //L.i("check", "${p.eventX} ${p.y} -> true")
                    }
                    pointList.remove(id)
                }
            }

            return true
        }
        return super.onTouchEvent(event)
    }

    private fun checkListener(x: Int, y: Int) {
        if (listener != null) {
            rainList?.let {
                for (i in it.size - 1 downTo 0) {
                    val rainBean = it[i]
                    //L.w("check", "${rainBean.getRect()} $x $y ${rainBean.isIn(x, y)}")
                    if (rainBean.isIn(x, y)) {
                        listener?.onTaoUp(rainBean)
                        return@let
                    }
                }
            }
        }
    }

    var isOnAttachedToWindow = false

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isOnAttachedToWindow = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isOnAttachedToWindow = false
    }

    interface OnTapUpListener {
        fun onTaoUp(bean: RainBean)
    }
}

class RainBean {
    /**坐标位置*/
    private val rect = Rect()
    /**资源*/
    var rainDrawable: Drawable? = null

    /**Y轴每次移动的步长 dp单位 可以单独控制某一个的下降速度*/
    var stepY = 2 //px

    fun setRect(x: Int, y: Int, w: Int, h: Int) {
        rect.set(x, y, x + w, y + h)
    }

    fun offset(dx: Int, dy: Int) {
        rect.offset(dx, dy)
    }

    fun getTop(): Int {
        return rect.top
    }

    fun isIn(x: Int, y: Int) = rect.contains(x, y)

    fun getRect() = rect

    fun draw(canvas: Canvas) {
        rainDrawable?.let {
            it.bounds = rect
            it.draw(canvas)
        }
    }
}