package com.angcyo.uiview.game.layer

import android.graphics.Canvas
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.view.MotionEvent
import com.angcyo.library.utils.L
import com.angcyo.uiview.RApplication
import com.angcyo.uiview.game.GameRenderView
import com.angcyo.uiview.game.spirit.BaseLayerBean
import com.angcyo.uiview.game.spirit.FrameBean
import com.angcyo.uiview.helper.BezierHelper
import com.angcyo.uiview.utils.ScreenUtil

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：接收Touch事件, 可以用来操作界面的Layer
 * 创建人员：Robi
 * 创建时间：2017/12/16 14:04
 * 修改人员：Robi
 * 修改时间：2017/12/16 14:04
 * 修改备注：
 * Version: 1.0.0
 */
abstract class BaseTouchLayer : BaseExLayer() {

    val TAG = "BaseTouchLayer"

    /**每次新增精灵的数量*/
    var addSpiritNum = 5

    /**需要绘制多少个贝塞尔曲线周期*/
    var bezierPart = 0.3F

    /**每次新增 时间间隔*/
    var spiritAddInterval = 700L
        set(value) {
            drawIntervalTime = value
            field = value
        }

    /**最大数量*/
    var maxSpiritNum = 100

    /*精灵列表*/
    private val spiritList = frameList

    /**精灵点击事件*/
    var onClickSpiritListener: OnClickSpiritListener? = null

    /*已经添加了多少个*/
    private var spiritAddNumEd = 0

    /**点中了多少个Rain*/
    var touchUpSpiritNum = 0

    /**是否结束添加*/
    protected var isSpiritAddEnd = false

    /**是否需要检查Touch事件*/
    protected var checkTouchEvent = true

    init {
        drawIntervalTime = spiritAddInterval
    }

    /**重置精灵状态参数*/
    fun reset() {
        spiritList.clear()
        spiritAddNumEd = 0
        touchUpSpiritNum = 0
        isSpiritAddEnd = false
    }

    /**结束绘制*/
    fun endSpirit() {
        isSpiritAddEnd = true
        val showNum = spiritList.size
        val addNum = spiritAddNumEd
        val maxNum = maxSpiritNum
        L.e(TAG, "call: endSpirit -> showNum:$showNum addNum:$addNum maxNum:$maxNum touchUpNum:$touchUpSpiritNum")
        //listener?.onRainEnd(addNum, showNum, maxNum, touchUpNum)
    }

    override fun draw(canvas: Canvas, gameStartTime: Long /*最开始渲染的时间*/, lastRenderTime: Long, nowRenderTime: Long /*现在渲染的时候*/) {
        if (isSpiritAddEnd) {
            return
        }

        super.draw(canvas, gameStartTime, lastRenderTime, nowRenderTime)

        updateSpiritList()
    }

    override fun onDraw(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long) {
        super.onDraw(canvas, gameStartTime, lastRenderTime, nowRenderTime)
        checkAddNewSpirit()
    }

    var gameRenderView: GameRenderView? = null
    override fun onRenderStart(gameRenderView: GameRenderView) {
        super.onRenderStart(gameRenderView)
        this.gameRenderView = gameRenderView
    }

    /**移除Spirit*/
    fun removeSpirit(bean: TouchSpiritBean) {
        spiritList.remove(bean)
    }

    override fun onTouchEvent(event: MotionEvent, point: PointF): Boolean {
        if (!checkTouchEvent) {
            return super.onTouchEvent(event, point)
        }
        if (isSpiritAddEnd) {
            return super.onTouchEvent(event, point)
        }
        if (!spiritList.isEmpty()) {
            return checkTouchListener(point.x.toInt(), point.y.toInt())
        }
        return super.onTouchEvent(event, point)
    }

    protected fun checkTouchListener(x: Int, y: Int): Boolean {
        var isIn = false
        for (i in spiritList.size - 1 downTo 0) {
            val rainBean: TouchSpiritBean = spiritList[i] as TouchSpiritBean
            //L.w("check", "${rainBean.getRect()} $x $y ${rainBean.isIn(x, y)}")
            if (rainBean.isIn(x, y)) {
                touchUpSpiritNum++
                onClickSpiritListener?.onClickSpirit(this, rainBean)
                isIn = true
                break
            }
        }
        return isIn
    }


    /**检查是否需要添加新的精灵*/
    protected fun checkAddNewSpirit() {
        if (addSpiritNum >= maxSpiritNum) {
            //所有Rain 添加完毕
            if (spiritList.isEmpty()) {
                //所有Rain, 移除了屏幕
                endSpirit()
            }
        } else if (spiritAddNumEd + addSpiritNum > maxSpiritNum) {
            //达到最大
            addNewRainInner(maxSpiritNum - spiritAddNumEd)
        } else {
            addNewRainInner(addSpiritNum)
        }
    }

    /**更新精灵配置*/
    protected fun updateSpiritList() {
        val removeList = mutableListOf<TouchSpiritBean>()
        for (b in spiritList) {
            val bean: TouchSpiritBean = b as TouchSpiritBean
            bean.offset(0, (bean.stepY /*ScreenUtil.density*/).toInt())

            if (bean.useBezier) {
                val maxY = gameRenderView!!.measuredHeight / bezierPart /*分成5份, 循环5次曲线*/
                if (maxY > 0) {
                    val fl = Math.abs(bean.getRect().top % maxY / maxY.toFloat())

                    val dx = (bean.bezierHelper!!.evaluate(fl) - bean.getRect().left).toInt()
                    bean.offset(dx, 0)
                }

                //L.e("call: updateRainList -> fl:$fl dx:$dx")
            }

            if (bean.getTop() > gameRenderView!!.measuredHeight) {
                //移动到屏幕外了,需要移除
                removeList.add(bean)
            }
        }
        spiritList.removeAll(removeList)
    }

    //添加精灵
    protected fun addNewRainInner(num: Int) {
        //L.e(TAG, "call: addNewRainInner -> $num")
        for (i in 0 until num) {
            spiritList.add(onAddNewSpirit())
            spiritAddNumEd++
        }
    }

    fun getDrawable(id: Int): Drawable {
        return if (gameRenderView == null) {
            ContextCompat.getDrawable(RApplication.getApp(), id)
        } else {
            ContextCompat.getDrawable(gameRenderView!!.context, id)
        }
    }

    abstract fun onAddNewSpirit(): TouchSpiritBean

    /**基础配置*/
    protected fun initSpirit(spiritBean: TouchSpiritBean) {
        val randomStepY = spiritBean.stepY + random.nextInt(5)
        if (spiritBean.randomStep) {
            spiritBean.stepY = randomStepY
        }

        val sw = if (gameRenderView!!.measuredWidth == 0) ScreenUtil.screenWidth else gameRenderView!!.measuredWidth

        val drawable = spiritBean.drawableArray[0]
        val intrinsicWidth = drawable.intrinsicWidth
        val intrinsicHeight = drawable.intrinsicHeight

        val w2 = sw / 2
        val w4 = w2 / 2

        val randomX = if (spiritBean.useBezier) randomX(sw, w4 - intrinsicWidth) else randomX(sw, intrinsicWidth)
        val randomY = randomY(-intrinsicHeight)
        spiritBean.setRect(randomX, randomY, intrinsicWidth, intrinsicHeight)

        val x = (randomX + intrinsicWidth).toFloat()
        val left = randomStepY % 2 == 0

        val cp1: Float = if (left) (x + w4) else (x - w4)
        val cp2: Float = if (left) (x - w4) else (x + w4)

        spiritBean.bezierHelper = BezierHelper(x, x, cp1, cp2)
    }

    /*随机产生x轴*/
    private fun randomX(sw: Int, w: Int): Int {
        return (random.nextFloat() * (sw - w)).toInt()
    }

    /*随机产生y轴*/
    private fun randomY(h: Int): Int {
        return (random.nextFloat() * h).toInt()
    }

    override fun addFrameBean(frameBean: BaseLayerBean) {
        //super.addFrameBean(frameBean)
    }
}

interface OnClickSpiritListener {
    fun onClickSpirit(baseTouchLayer: BaseTouchLayer, spiritBean: TouchSpiritBean)
}

open class TouchSpiritBean(drawableArray: Array<Drawable>) : FrameBean(drawableArray, Point()) {
    /**坐标位置*/
    private val rect = Rect()

    /**Y轴每次移动的步长 dp单位 可以单独控制某一个的下降速度*/
    var stepY = 2 //px

    var bezierHelper: BezierHelper? = null

    /**使用贝塞尔曲线*/
    var useBezier = true

    /**随机step*/
    var randomStep = true

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

    override fun getDrawDrawableBounds(drawable: Drawable): Rect {
        return rect
    }

    override fun getDrawPointFun(): Point {
        return Point(rect.centerX(), rect.centerY())
    }
}