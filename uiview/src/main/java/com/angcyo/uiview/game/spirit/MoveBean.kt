package com.angcyo.uiview.game.spirit

import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import com.angcyo.uiview.kotlin.getBoundsWith

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：可以从开始点 移动到 结束点的精灵
 * 创建人员：Robi
 * 创建时间：2017/12/16 12:20
 * 修改人员：Robi
 * 修改时间：2017/12/16 12:20
 * 修改备注：
 * Version: 1.0.0
 */
open class MoveBean(drawables: Array<Drawable>,
                    val startPoint: Point /*开始的点*/,
                    val endPoint: Point /*结束的点*/) : FrameBean(drawables, startPoint) {

    val drawRect = Rect()

    private var startDrawTime = 0L

    /**起点移动到终点所需要的时间 毫秒*/
    var maxMoveTime: Int = 1000
        get() {
            return field / 1000
        }

    /**是否是匀速移动*/
    var constantSpeed = true

    /**是否循环移动*/
    var isLoopMove = false

    /**激活自动透明, 离终点越近越透明*/
    var enableAlpha = false

    init {
        loopDrawFrame = true
        drawDrawable.let {
            drawRect.set(it.getBoundsWith(centerPoint, parentRect))
        }
    }

    override fun draw(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long, onDrawEnd: (() -> Unit)?) {
        val nowTime = System.currentTimeMillis()
        if (startDrawTime > 0L && (nowTime - startDrawTime) / 1000F > maxMoveTime) {
            if (isLoopMove) {
                onLoopMove()
            } else {
                onDrawEnd?.invoke()
            }
        } else {
            if (enableAlpha) {
                val x1 = endPoint.x - startPoint.x
                val x2 = drawPoint.x - startPoint.x
                val fl = 1 - Math.abs(x2) * 1f / Math.abs(x1)
                //L.e("call: onDraw -> $fl")
                drawableAlpha = (255 * fl).toInt()
            }
            //L.i("call: onDraw -> ${maxMoveTime} $time $drawPoint $startPoint $endPoint ${aX()} ${aY()}")
            super.draw(canvas, gameStartTime, lastRenderTime, nowRenderTime, onDrawEnd)
            //L.w("${drawDrawable.bounds}")
        }
    }

    override fun drawThread(gameStartTime: Long, lastRenderTimeThread: Long, nowRenderTime: Long) {
        super.drawThread(gameStartTime, lastRenderTimeThread, nowRenderTime)
        val nowTime = System.currentTimeMillis()
        if (startDrawTime == 0L) {
            startDrawTime = nowTime
        }
        val time = (nowTime - startDrawTime) / 1000F
        drawPoint.set(x(time).toInt(), y(time).toInt())
    }

    open fun onLoopMove() {
        startDrawTime = 0L
        drawPoint.set(startPoint.x, startPoint.y)
    }

    /**加速移动时的x*/
    private fun aX(): Float = (endPoint.x - startPoint.x).toFloat() / (maxMoveTime * maxMoveTime)

    private fun aY(): Float = (endPoint.y - startPoint.y).toFloat() / (maxMoveTime * maxMoveTime)

    /**匀速移动时的x*/
    private fun vX() = ((endPoint.x - startPoint.x).toFloat() / maxMoveTime)

    private fun vY() = ((endPoint.y - startPoint.y).toFloat() / maxMoveTime)

    /**自动判断是加速还是匀速*/
    private fun x(t: Float) = startPoint.x + if (constantSpeed) vX() * t else aX() * t * t

    private fun y(t: Float) = startPoint.y + if (constantSpeed) vY() * t else aY() * t * t
}