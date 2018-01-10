package com.angcyo.uiview.game.spirit

import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import com.angcyo.uiview.helper.BezierHelper
import com.angcyo.uiview.kotlin.scale

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/01/10 19:23
 * 修改人员：Robi
 * 修改时间：2018/01/10 19:23
 * 修改备注：
 * Version: 1.0.0
 */
open class TouchSpiritBean(drawableArray: Array<Drawable>) : FrameBean(drawableArray, Point()) {
    /**坐标位置*/
    private val rect = Rect()

    /**Y轴每次移动的步长 dp单位 可以单独控制某一个的下降速度*/
    var stepY = 4 //px

    var bezierHelper: BezierHelper? = null

    /**使用贝塞尔曲线*/
    var useBezier = true

    /**随机step*/
    var randomStep = true

    /*开始的坐标, 计算出来的值, 无需手动赋值*/
    var startX = 0
    var startY = 0

    /*数据更新的次数, 自动计算*/
    var updateIndex = 0

    fun setRect(x: Int, y: Int, w: Int, h: Int) {
        rect.set(x, y, x + w, y + h)
        rect.scale(scaleX, scaleY)
    }

    fun offset(dx: Int, dy: Int) {
        rect.offset(dx, dy)
    }

    fun getTop(): Int {
        return rect.top
    }

    fun getBottom(): Int {
        return rect.bottom
    }

    fun isIn(x: Int, y: Int) = rect.contains(x, y)

    fun getRect() = rect

    init {
        loopDrawFrame = true
        frameDrawIntervalTime = 100
    }

    override fun getDrawDrawableBounds(drawable: Drawable): Rect {
        return rect
    }

    override fun getDrawPointFun(): Point {
        return Point(rect.centerX(), rect.centerY())
    }

    /**之前的帧率, 是在BaseLayer的帧率下, 计算的帧率. 现在废弃, 直接在*/
    override fun onFrameOnDrawInterval(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long) {
        //super.onFrameDrawInterval(canvas, gameStartTime, lastRenderTime, nowRenderTime)
    }

    override fun onFrameDrawInterval(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long) {
        super.onFrameDrawInterval(canvas, gameStartTime, lastRenderTime, nowRenderTime)
        frameIndex++
    }

    fun width() = if (drawableArray.isEmpty()) {
        0
    } else {
        val drawable = drawableArray[0]
        drawable.intrinsicWidth
    }

    fun height() = if (drawableArray.isEmpty()) {
        0
    } else {
        val drawable = drawableArray[0]
        drawable.intrinsicHeight
    }
}
