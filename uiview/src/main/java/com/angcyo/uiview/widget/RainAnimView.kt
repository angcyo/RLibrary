package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
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

    private val gestureDetector: GestureDetector by lazy {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                if (listener != null) {
                    rainList?.let {
                        for (i in it.size - 1 downTo 0) {
                            val rainBean = it[i]
                            if (rainBean.isIn(e.x.toInt(), e.y.toInt())) {
                                listener?.onTaoUp(rainBean)
                                return@let
                            }
                        }
                    }
                }
                return true
            }

            override fun onDown(e: MotionEvent?): Boolean {
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (rainList?.isEmpty() == false) {
            return gestureDetector.onTouchEvent(event)
        }
        return super.onTouchEvent(event)
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

    /**Y轴每次移动的步长 dp单位*/
    var stepY = 2 //dp

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

    fun draw(canvas: Canvas) {
        rainDrawable?.let {
            it.bounds = rect
            it.draw(canvas)
        }
    }
}