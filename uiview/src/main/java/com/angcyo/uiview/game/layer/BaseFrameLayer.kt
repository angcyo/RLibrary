package com.angcyo.uiview.game.layer

import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import com.angcyo.uiview.kotlin.getBoundsWith

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：用来播放帧动画的Layer
 * 创建人员：Robi
 * 创建时间：2017/12/15 14:46
 * 修改人员：Robi
 * 修改时间：2017/12/15 14:46
 * 修改备注：
 * Version: 1.0.0
 */
open class BaseFrameLayer : BaseLayer() {

    private val frameList = mutableListOf<BaseLayerBean>()

    init {
        //drawIntervalTime = 100
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun draw(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long) {
        super.draw(canvas, gameStartTime, lastRenderTime, nowRenderTime)
        val deleteList = mutableListOf<BaseLayerBean>()
        for (frame in frameList) {
            frame.parentRect.set(layerRect)
            frame.draw(canvas, gameStartTime, lastRenderTime, nowRenderTime) {
                deleteList.add(frame)
            }
        }
        frameList.removeAll(deleteList)
    }

    override fun onDraw(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long) {
        super.onDraw(canvas, gameStartTime, lastRenderTime, nowRenderTime)
        for (frame in frameList) {
            frame.onDraw(canvas, gameStartTime, lastRenderTime, nowRenderTime)
        }
    }

    fun addFrameBean(frameBean: BaseLayerBean) {
        frameList.add(frameBean)
    }
}

open class BaseLayerBean {

    /**每一帧绘制间隔时间*/
    var frameDrawIntervalTime = 0L
    protected var lastFrameDrawTime = 0L

    /**Layer的显示区域范围*/
    var parentRect = Rect()

    open fun draw(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long, onDrawEnd: () -> Unit) {
    }

    open fun onDraw(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long) {
        if (nowRenderTime - lastFrameDrawTime > frameDrawIntervalTime) {
            lastFrameDrawTime = nowRenderTime
            onFrameDrawInterval(canvas, gameStartTime, lastRenderTime, nowRenderTime)
        }
    }

    open fun onFrameDrawInterval(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long) {

    }
}

open class FrameBgBean(val bgDrawable: Drawable) : BaseLayerBean() {
    override fun draw(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long, onDrawEnd: () -> Unit) {
        super.draw(canvas, gameStartTime, lastRenderTime, nowRenderTime, onDrawEnd)
        bgDrawable.let {
            it.bounds = parentRect
            it.draw(canvas)
        }
    }
}

/**数据bean*/
open class FrameBean(val drawableArray: Array<Drawable> /*需要播放的帧动画*/, val centerPoint: Point /*需要在什么位置播放(中心点)*/) : BaseLayerBean() {
    /**是否循环播放*/
    var loop = true

    /**X轴的旋转角度*/
    var rotateDegrees = 0f

    /**缩放比率*/
    var scaleX = 1f
    var scaleY = 1f

    /**首次渲染延时的时间*/
    var delayDrawTime = 0L

    /*用来控制延迟draw的变量*/
    private var firstDrawTime = 0L

    protected var drawPoint = Point(centerPoint)

    /*总共多少帧*/
    private val frameSize = drawableArray.size
    /*当前播放到多少帧*/
    private var frameIndex = 0

    init {
        frameDrawIntervalTime = 160L
    }

    /*正在绘制的帧*/
    protected val drawDrawable: Drawable
        get() {
            return drawableArray[frameIndex]
        }

    override fun draw(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long, onDrawEnd: () -> Unit) {
        if (delayDrawTime > 0) {
            if (firstDrawTime == 0L) {
                firstDrawTime = nowRenderTime
                return
            } else if ((nowRenderTime - firstDrawTime) < delayDrawTime) {
                return
            }
        }

        if (frameIndex >= frameSize) {
            //播放结束
            if (loop) {
                frameIndex = 0
            } else {
                onDrawEnd.invoke()
            }
        }
        if (frameIndex < frameSize) {

            canvas.save()
            canvas.translate(drawPoint.x.toFloat(), drawPoint.y.toFloat())
            canvas.rotate(rotateDegrees)
            canvas.scale(scaleX, scaleY)
            drawDrawable.let {
                it.setBounds(-it.intrinsicWidth / 2, -it.intrinsicHeight / 2, it.intrinsicWidth / 2, it.intrinsicHeight / 2)
                it.draw(canvas)
            }
            canvas.restore()
        }
    }

    open fun getDrawDrawableBounds(drawable: Drawable): Rect {
        return drawable.getBoundsWith(drawPoint, parentRect)
    }

    override fun onFrameDrawInterval(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long) {
        super.onFrameDrawInterval(canvas, gameStartTime, lastRenderTime, nowRenderTime)
        frameIndex++
    }
}