package com.angcyo.uiview.game.layer

import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import com.angcyo.uiview.kotlin.getBoundsWith

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：用来控制从一个点, 移动到另一个点的Layer
 * 创建人员：Robi
 * 创建时间：2017/12/15 15:34
 * 修改人员：Robi
 * 修改时间：2017/12/15 15:34
 * 修改备注：
 * Version: 1.0.0
 */
open class BaseMoveLayer : BaseFrameLayer() {

}

class MoveBean(drawables: Array<Drawable>,
               val startPoint: Point /*开始的点*/,
               endPoint: Point /*结束的点*/) : FrameBean(drawables, startPoint) {

    val drawRect = Rect()

    private var startDrawTime = 0L

    /**起点移动到终点所需要的时间*/
    var maxMoveTime: Int = 1000

    init {
        loop = true
        drawDrawable.let {
            drawRect.set(it.getBoundsWith(centerPoint, parentRect))
        }
    }

    override fun draw(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long, onDrawEnd: () -> Unit) {
        val nowTime = System.currentTimeMillis()

        if (startDrawTime > 0L && nowTime - startDrawTime > maxMoveTime) {
            onDrawEnd.invoke()
        } else {
            if (startDrawTime == 0L) {
                startDrawTime = nowTime
            }

            val time = nowTime - startDrawTime
            drawPoint.set(x(time.toInt()).toInt(), y(time.toInt()).toInt())

            super.draw(canvas, gameStartTime, lastRenderTime, nowRenderTime, onDrawEnd)
        }
    }

    private val aX: Float = (endPoint.x - startPoint.x).toFloat() / (maxMoveTime * maxMoveTime)
    private val aY: Float = (endPoint.y - startPoint.y).toFloat() / (maxMoveTime * maxMoveTime)

    private fun x(t: Int) = startPoint.x + aX * t * t
    private fun y(t: Int) = startPoint.y + aY * t * t
}