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
class BaseMoveLayer : BaseFrameLayer() {

}

class MoveBean(val drawable: Drawable,
               val startPoint: Point /*开始的点*/,
               val endPoint: Point /*结束的点*/) : FrameBean(arrayOf(drawable), startPoint) {

    val drawRect = Rect()

    private var drawPoint = Point(startPoint)

    private var startDrawTime = 0L

    init {
        loop = true
        drawable.let {
            drawRect.set(it.getBoundsWith(centerPoint, parentRect))
        }
    }

    override fun draw(canvas: Canvas, onDrawEnd: () -> Unit) {
        //super.draw(canvas, onDrawEnd)
        val nowTime = System.currentTimeMillis()

        if (startDrawTime == 0L) {
            startDrawTime = nowTime

            drawable.let {
                drawRect.set(it.getBoundsWith(drawPoint, parentRect))
                it.bounds = drawRect
                it.draw(canvas)
            }
        } else if (nowTime - startDrawTime > maxMoveTime) {
            onDrawEnd.invoke()
        } else {
            val time = nowTime - startDrawTime
            drawPoint.set(x(time.toInt()).toInt(), y(time.toInt()).toInt())

            drawable.let {
                drawRect.set(it.getBoundsWith(drawPoint, parentRect))
                it.bounds = drawRect
                it.draw(canvas)
            }
        }
    }

    /*起点移动到终点所需要的时间*/
    private val maxMoveTime: Int = 1000

    private val aX: Float = (endPoint.x - startPoint.x).toFloat() / (maxMoveTime * maxMoveTime)
    private val aY: Float = (endPoint.y - startPoint.y).toFloat() / (maxMoveTime * maxMoveTime)

    private fun x(t: Int) = startPoint.x + aX * t * t
    private fun y(t: Int) = startPoint.y + aY * t * t
}