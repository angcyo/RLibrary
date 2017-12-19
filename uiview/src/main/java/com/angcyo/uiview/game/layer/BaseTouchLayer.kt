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
import com.angcyo.uiview.kotlin.scale
import com.angcyo.uiview.utils.ScreenUtil
import com.angcyo.uiview.utils.ThreadExecutor

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
    var bezierPart = 0.25F

    /**每次新增 时间间隔*/
    var spiritAddInterval = 700L
        set(value) {
            drawIntervalTime = value
            field = value
        }

    /**最大数量*/
    var maxSpiritNum = 100

    /**点到的时候, 自动移出*/
    var removeOnTouch = true

    /*精灵列表*/
    private val spiritList = frameList

    /**精灵点击事件*/
    var onClickSpiritListener: OnClickSpiritListener? = null

    /**游戏开始渲染后的绘制回调*/
    var onLayerDrawListener: OnLayerDrawListener? = null

    /*已经添加了多少个*/
    private var spiritAddNumEd = 0

    /**点中了多少个Rain*/
    var touchUpSpiritNum = 0

    /**是否结束添加*/
    protected var isSpiritAddEnd = false

    /**是否需要检查Touch事件*/
    protected var checkTouchEvent = true

    /*开始绘制的时间, 游戏开始的时间*/
    private var startDrawTime = 0L

    override var pauseDrawFrame: Boolean = true
        set(value) {
            field = value
            if (!field) {
                //开始绘制
                startDrawTime = System.currentTimeMillis()
            } else {
                startDrawTime = 0L
            }
        }

    init {
        drawIntervalTime = spiritAddInterval
        pauseDrawFrame = true //暂停绘制
    }

    /**重置精灵状态参数*/
    fun reset() {
        spiritList.clear()
        spiritAddNumEd = 0
        touchUpSpiritNum = 0
        isSpiritAddEnd = false
        pauseDrawFrame = true //暂停绘制
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

    /**当屏幕上的Spirit滚动出屏之后, 再结束*/
    fun delayEndSpirit() {
        spiritAddNumEd = maxSpiritNum
    }

    override fun draw(canvas: Canvas, gameStartTime: Long /*最开始渲染的时间*/, lastRenderTime: Long, nowRenderTime: Long /*现在渲染的时候*/) {
        if (!pauseDrawFrame) {
            onLayerDrawListener?.onLayerDraw(startDrawTime, nowRenderTime)
        }

        if (isSpiritAddEnd) {
            return
        }
        super.draw(canvas, gameStartTime, lastRenderTime, nowRenderTime)
        //updateSpiritList()
    }

    override fun drawThread(gameStartTime: Long, lastRenderTimeThread: Long, nowRenderTime: Long) {
        if (isSpiritAddEnd) {
            return
        }
        super.drawThread(gameStartTime, lastRenderTimeThread, nowRenderTime)
        updateSpiritList()
    }

    override fun onDraw(canvas: Canvas, gameStartTime: Long, lastRenderTime: Long, nowRenderTime: Long) {
        super.onDraw(canvas, gameStartTime, lastRenderTime, nowRenderTime)
        //checkAddNewSpirit()
    }

    override fun onDrawThread(gameStartTime: Long, lastRenderTimeThread: Long, nowRenderTime: Long) {
        super.onDrawThread(gameStartTime, lastRenderTimeThread, nowRenderTime)
        checkAddNewSpirit()
    }

    var gameRenderView: GameRenderView? = null
    override fun onRenderStart(gameRenderView: GameRenderView) {
        super.onRenderStart(gameRenderView)
        this.gameRenderView = gameRenderView
    }

    /**移除Spirit*/
    fun removeSpirit(bean: TouchSpiritBean) {
        synchronized(lock) {
            spiritList.remove(bean)
        }
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
            val spiritBean: TouchSpiritBean = spiritList[i] as TouchSpiritBean
            //L.w("check", "${spiritBean.getRect()} $x $y ${spiritBean.isIn(x, y)}")
            if (spiritBean.isIn(x, y)) {
                touchUpSpiritNum++
                onClickSpiritListener?.onClickSpirit(this, spiritBean, x, y)
                isIn = true
                if (removeOnTouch) {
                    removeSpirit(spiritBean)
                }
                break
            }
        }
        return isIn
    }


    /**检查是否需要添加新的精灵*/
    protected fun checkAddNewSpirit() {
        if (spiritAddNumEd >= maxSpiritNum) {
            //所有Rain 添加完毕
            if (spiritList.isEmpty()) {
                //所有Rain, 移除了屏幕
                ThreadExecutor.instance().onMain {
                    endSpirit()
                }
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
        for (index in 0 until spiritList.size) {
            val bean: TouchSpiritBean = spiritList[index] as TouchSpiritBean

            //L.i("call: updateSpiritList1 -> index:${bean.updateIndex} startY:${bean.startY} top:${bean.getTop()}")
            bean.offset(0, (bean.stepY /*ScreenUtil.density*/).toInt())

            if (bean.useBezier) {
                val maxY = (gameRenderView!!.measuredHeight - bean.startY) / bezierPart /*分成5份, 循环5次曲线*/
                if (maxY > 0) {
                    val fl = Math.abs((bean.getRect().top - bean.startY) % (maxY + 1) / maxY.toFloat())

                    val dx = (bean.bezierHelper!!.evaluate(fl) - bean.getRect().left).toInt()
                    if (dx < bean.getRect().width()) {
                        //控制位移的幅度, 防止漂移现象
                        bean.offset(dx, 0)
                    }
                    //L.e("call: updateRainList -> fl:$fl dx:$dx ${bean.getRect()} $maxY")
                }
            }

            bean.updateIndex++

            val top = bean.getTop()
            //L.w("call: updateSpiritList2 -> index:${bean.updateIndex} startY:${bean.startY} top:$top")
            synchronized(lock) {
                if (top > gameRenderView!!.measuredHeight) {
                    //移动到屏幕外了,需要移除
                    removeList.add(bean)
                }
            }
        }
        synchronized(lock) {
            spiritList.removeAll(removeList)
        }
    }

    //添加精灵
    protected fun addNewRainInner(num: Int) {
        //L.e(TAG, "call: addNewRainInner -> $num")
        synchronized(lock) {
            for (i in 0 until num) {
                spiritList.add(onAddNewSpirit())
                spiritAddNumEd++
            }
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

        spiritBean.startX = getSpiritStartX(spiritBean, sw)
        spiritBean.startY = getSpiritStartY(spiritBean)

        spiritBean.bezierHelper = createBezierHelper(spiritBean.startX, spiritBean.startY, intrinsicWidth, intrinsicHeight)

        var sx = spiritBean.startX
        var sy = spiritBean.startY

        if (spiritBean.useBezier) {
            sx = spiritBean.bezierHelper!!.evaluate(0f).toInt()
            //L.w("updateRainList getSpiritStartX:$getSpiritStartX $sx ${spiritBean.bezierHelper!!.evaluate(1f).toInt()}")
        }
        //L.e("call: initSpirit -> startX:$sx startY:$sy step:${spiritBean.stepY}")
        spiritBean.setRect(sx, sy, intrinsicWidth, intrinsicHeight)
    }

    open protected fun createBezierHelper(randomX: Int, randomY: Int, intrinsicWidth: Int, intrinsicHeight: Int): BezierHelper {
        val randomStepY = random.nextInt(7)

        val x: Float = randomX.toFloat()
        val left = randomStepY % 2 == 0 //优先向左飘

        val cp1: Float = if (left) (x - intrinsicWidth) else (x + intrinsicWidth)
        val cp2: Float = if (left) (x + intrinsicWidth) else (x - intrinsicWidth)

        return BezierHelper(x, x, cp1, cp2)
    }

    /*随机产生x轴*/
    open protected fun getSpiritStartX(spiritBean: TouchSpiritBean, sw: Int): Int {
        if (spiritBean.useBezier) {
            return (random.nextFloat() * (sw - 2 * spiritBean.width())).toInt() + spiritBean.width()
        } else {
            return (random.nextFloat() * (sw - spiritBean.width())).toInt() + spiritBean.width() / 4
        }
    }

    /*随机产生y轴*/
    open protected fun getSpiritStartY(spiritBean: TouchSpiritBean): Int {
        return -(random.nextFloat() * spiritBean.height() / 2).toInt() - spiritBean.height()
    }

    override fun addFrameBean(frameBean: BaseLayerBean) {
        //super.addFrameBean(frameBean)
    }
}

interface OnClickSpiritListener {
    fun onClickSpirit(baseTouchLayer: BaseTouchLayer, spiritBean: TouchSpiritBean, x: Int, y: Int)
}

interface OnLayerDrawListener {
    fun onLayerDraw(startDrawTime: Long, nowDrawTime: Long)
}

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
