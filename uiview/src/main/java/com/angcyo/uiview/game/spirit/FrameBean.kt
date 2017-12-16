package com.angcyo.uiview.game.spirit

import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import com.angcyo.uiview.kotlin.getBoundsWith

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：可以在指定位置 循环播放的精灵
 * 创建人员：Robi
 * 创建时间：2017/12/16 12:18
 * 修改人员：Robi
 * 修改时间：2017/12/16 12:18
 * 修改备注：
 * Version: 1.0.0
 */

/**数据bean*/
open class FrameBean(val drawableArray: Array<Drawable> /*需要播放的帧动画*/, val centerPoint: Point /*需要在什么位置播放(中心点)*/) : BaseLayerBean() {
    /**是否循环播放*/
    var loopDrawFrame = true

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
    protected var frameSize = drawableArray.size
    /*当前播放到多少帧*/
    protected var frameIndex = 0

    /*循环的次数*/
    protected var loopCount = 0L

    init {
        frameDrawIntervalTime = 160L
    }

    /*正在绘制的帧*/
    open protected val drawDrawable: Drawable
        get() {
            return drawableArray[frameIndex]
        }

    /**当前绘制的中心点坐标*/
    open fun getDrawPointFun(): Point = drawPoint

    override fun draw(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long, onDrawEnd: (() -> Unit)?) {
        super.draw(canvas, gameStartTime, lastRenderTime, nowRenderTime, onDrawEnd)
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
            if (loopDrawFrame) {
                onLoopFrame()
            } else {
                onFrameEnd(onDrawEnd)
            }
        }
        if (frameIndex < frameSize) {

            canvas.save()
            canvas.translate(getDrawPointFun().x.toFloat(), getDrawPointFun().y.toFloat())
            canvas.rotate(rotateDegrees)
            canvas.scale(scaleX, scaleY)
            drawDrawable.let {
                it.setBounds(-it.intrinsicWidth / 2, -it.intrinsicHeight / 2, it.intrinsicWidth / 2, it.intrinsicHeight / 2)
                it.draw(canvas)
            }
            canvas.restore()
        }
    }

    /**每一帧的bounds*/
    @Deprecated("请用 getDrawPointFun 代替")
    open fun getDrawDrawableBounds(drawable: Drawable): Rect {
        return drawable.getBoundsWith(drawPoint, parentRect)
    }

    /**帧的间隔绘制方法*/
    override fun onFrameOnDrawInterval(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long) {
        super.onFrameOnDrawInterval(canvas, gameStartTime, lastRenderTime, nowRenderTime)
        frameIndex++
    }

    /**再次循环*/
    open fun onLoopFrame() {
        frameIndex = 0
        loopCount++
    }

    /**所有帧播放结束*/
    open fun onFrameEnd(onDrawEnd: (() -> Unit)? = null) {
        onDrawEnd?.invoke()
    }
}