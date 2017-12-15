package com.angcyo.uiview.widget.helper

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.support.v4.content.ContextCompat
import android.view.animation.LinearInterpolator
import com.angcyo.library.utils.L
import com.angcyo.uiview.helper.BezierHelper
import com.angcyo.uiview.widget.RainAnimView
import com.angcyo.uiview.widget.RainBean
import java.util.*

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/12/13 09:19
 * 修改人员：Robi
 * 修改时间：2017/12/13 09:19
 * 修改备注：
 * Version: 1.0.0
 */
class RainHelper(val rainAnimView: RainAnimView) {
    companion object {
        val TAG = "rainAnim"

        /**每次新增数量*/
        var addNum = 5

        /**每次新增 时间间隔*/
        var interval = 700L

        /**最大数量*/
        var maxNum = 100
    }

    private val rainList = mutableListOf<RainBean>()

    /*已经添加了多少个*/
    private var rainAddNum = 0

    /**资源id*/
    var rainResId = -1

    private var isStart = false

    /**点中了多少个Rain*/
    var touchUpNum = 0

    /**使用贝塞尔曲线*/
    var useBezier = true

    var rainStepY = 2 //px

    /**每个Rain下降的Step是否随机*/
    var randomStep = true

    init {
        rainAnimView.listener = object : RainAnimView.OnTapUpListener {
            override fun onTapUp(bean: RainBean) {
                touchUpNum++
                listener?.onTouchInRain(touchUpNum)
                rainList.remove(bean)
                rainAnimView.postInvalidate()
            }
        }
    }


    var count = 0
    private val animator: ValueAnimator by lazy {
        ObjectAnimator.ofInt(0, 60).apply {
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            duration = 1000

            addUpdateListener { animation ->
                val value: Int = animation.animatedValue as Int
                //60帧运行速度
                //count++
                //L.e(TAG, "->sum:$rainAddNum show:${rainList.size} index:$value time:${animation.animatedFraction} $count")

                updateRainList()
                rainAnimView.postInvalidateOnAnimation()
            }
        }
    }

    private fun updateRainList() {
        if (!rainAnimView.isOnAttachedToWindow) {
            endRain()
            return
        }

        val removeList = mutableListOf<RainBean>()
        for (bean in rainList) {
            bean.offset(0, (bean.stepY /*ScreenUtil.density*/).toInt())

            if (useBezier) {
                val maxY = rainAnimView.measuredHeight / 5 /*分成5份, 循环5次曲线*/
                if (maxY > 0) {
                    val fl = Math.abs(bean.getRect().top % maxY / maxY.toFloat())

                    val dx = (bean.bezierHelper!!.evaluate(fl) - bean.getRect().left).toInt()
                    bean.offset(dx, 0)
                }

                //L.e("call: updateRainList -> fl:$fl dx:$dx")
            }

            if (bean.getTop() > rainAnimView.measuredHeight) {
                //移动到屏幕外了,需要移除
                removeList.add(bean)
            }
        }
        rainList.removeAll(removeList)
    }

    /**开始下降*/
    @Synchronized
    fun startRain() {
        if (isStart) {
            return
        }
        isStart = true
        rainAnimView.rainList = rainList
        addNewRain()
        animator.start()
        postDelayed()
    }

    private val postDelayedRunnable: Runnable = Runnable {
        if (isStart) {
            addNewRain()
            postDelayed()
        }
    }

    private fun postDelayed() {
        rainAnimView.postDelayed(postDelayedRunnable, interval)
    }

    var listener: OnRainListener? = null

    fun endRain() {
        val showNum = rainList.size
        val addNum = rainAddNum
        val maxNum = maxNum
        L.e(TAG, "call: endRain -> showNum:$showNum addNum:$addNum maxNum:$maxNum touchUpNum:$touchUpNum")

        rainList.clear()
        rainAnimView.removeCallbacks(postDelayedRunnable)
        animator.cancel()
        rainAddNum = 0
        isStart = false
        rainAnimView.postInvalidate()

        listener?.onRainEnd(addNum, showNum, maxNum, touchUpNum)
    }

    private fun addNewRain() {
        if (rainAddNum >= maxNum) {
            //所有Rain 添加完毕
            if (rainList.isEmpty()) {
                //所有Rain, 移除了屏幕
                endRain()
            }
        } else if (rainAddNum + addNum > maxNum) {
            //达到最大
            addNewRainInner(maxNum - rainAddNum)
        } else {
            addNewRainInner(addNum)
        }
    }

    //添加Rain
    private fun addNewRainInner(num: Int) {
        //L.e(TAG, "call: addNewRainInner -> $num")
        for (i in 0 until num) {
            rainList.add(RainBean().apply {
                if (rainResId != -1) {
                    val randomStepY = rainStepY + random.nextInt(5)
                    if (randomStep) {
                        stepY = randomStepY
                    } else {
                        stepY = rainStepY
                    }

                    rainDrawable = ContextCompat.getDrawable(rainAnimView.context, rainResId)
                    val intrinsicWidth = rainDrawable!!.intrinsicWidth
                    val intrinsicHeight = rainDrawable!!.intrinsicHeight

                    val w2 = rainAnimView.measuredWidth / 2
                    val w4 = w2 / 2

                    val randomX = if (useBezier) randomX(w4 - intrinsicWidth) else randomX(intrinsicWidth)
                    val randomY = randomY(-intrinsicHeight)
                    setRect(randomX, randomY, intrinsicWidth, intrinsicHeight)

                    val x = (randomX + intrinsicWidth).toFloat()
                    val left = randomStepY % 2 == 0

                    val cp1: Float = if (left) (x + w4) else (x - w4)
                    val cp2: Float = if (left) (x - w4) else (x + w4)

                    bezierHelper = BezierHelper(x, x, cp1, cp2)
                }
            })
            rainAddNum++
        }
        rainAnimView.postInvalidate()
    }

    private val random: Random by lazy {
        Random(System.nanoTime())
    }

    /*随机产生x轴*/
    private fun randomX(w: Int): Int {
        return (random.nextFloat() * (rainAnimView.measuredWidth - w)).toInt()
    }

    /*随机产生y轴*/
    private fun randomY(h: Int): Int {
        return (random.nextFloat() * h).toInt()
    }
}

interface OnRainListener {
    fun onRainEnd(addNum: Int /*已经添加Rain的数量*/, showNum: Int /*还在显示Rain的数量*/, maxNum: Int /*总共Rain的数量*/, touchUpNum: Int /*点中Rain的数量*/)
    fun onTouchInRain(touchUpNum: Int /*点中Rain的数量*/)
}