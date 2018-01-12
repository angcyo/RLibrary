package com.angcyo.uiview.game.spirit

import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import com.angcyo.uiview.helper.BezierHelper
import com.angcyo.uiview.kotlin.scale
import com.angcyo.uiview.kotlin.scaleTo

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
    /**坐标位置, 更新此坐标, 可以移动精灵绘制的位置*/
    private val spiritDrawRect = Rect()
    /**未缩放变化的坐标位置*/
    private val spiritRect = Rect()

    /**Y轴每次移动的步长 dp单位 可以单独控制某一个的下降速度*/
    var stepY = 4 //px

    var bezierHelper: BezierHelper? = null

    /**使用贝塞尔曲线*/
    var useBezier = true

    /**随机step*/
    var randomStep = true

    /*开始的坐标, 计算出来的值, 无需手动赋值*/
    var startX = 0
        set(value) {
            field = value
            drawPoint = Point(startX, startY)
        }
    var startY = 0
        set(value) {
            field = value
            drawPoint = Point(startX, startY)
        }

    /*数据更新的次数, 自动计算*/
    var updateIndex = 0

    init {
        loopDrawFrame = true
        frameDrawIntervalTime = 100
    }

    open fun setRect(x: Int, y: Int, w: Int, h: Int) {
        spiritRect.set(x - w / 2, y - h / 2, x + w / 2, y + h / 2)
        spiritRect.scaleTo(spiritDrawRect, scaleX, scaleY)
    }

    open fun offset(dx: Int, dy: Int) {
        spiritRect.offset(dx, dy)
        spiritRect.scaleTo(spiritDrawRect, scaleX, scaleY)
    }

    open fun getDrawTop(): Int {
        return spiritDrawRect.top
    }

    open fun getDrawBottom(): Int {
        return spiritDrawRect.bottom
    }

    open fun isIn(x: Int, y: Int) = spiritDrawRect.contains(x, y)

    open fun getSpiritDrawRect() = spiritDrawRect
    open fun getSpiritRect() = spiritRect

    /**获取Drawable缩放后的Bounds*/
    fun getDrawableScaleBounds(): Rect = Rect().apply {
        set(0, 0, this@TouchSpiritBean.width(), this@TouchSpiritBean.height())
        scale(scaleX, scaleY)
    }

    override fun getDrawDrawableBounds(drawable: Drawable): Rect {
        return spiritDrawRect
    }

    override fun getDrawPointFun(): Point {
        val bounds = getDrawDrawableBounds(drawableArray[0])
        return Point(bounds.centerX(), bounds.centerY())
    }

    /**之前的帧率, 是在BaseLayer的帧率下, 计算的帧率. 现在废弃, 直接在*/
    override fun onFrameOnDrawInterval(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long) {
        //super.onFrameDrawInterval(canvas, gameStartTime, lastRenderTime, nowRenderTime)
    }

    override fun onFrameDrawInterval(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long) {
        super.onFrameDrawInterval(canvas, gameStartTime, lastRenderTime, nowRenderTime)
        frameIndex++
    }

    open fun width() = if (drawableArray.isEmpty()) {
        0
    } else {
        val drawable = drawableArray[0]
        drawable.intrinsicWidth * density.toInt()
    }

    open fun height() = if (drawableArray.isEmpty()) {
        0
    } else {
        val drawable = drawableArray[0]
        drawable.intrinsicHeight * density.toInt()
    }

    /**用来更新精灵的参数, 返回true, 表示完全控制精灵*/
    open fun onUpdateSpiritList() = false
}
