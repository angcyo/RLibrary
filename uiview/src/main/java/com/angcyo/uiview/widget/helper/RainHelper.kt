package com.angcyo.uiview.widget.helper

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.support.v4.content.ContextCompat
import android.view.animation.LinearInterpolator
import com.angcyo.library.utils.L
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
        val addNum = 5

        /**每次新增 时间间隔*/
        val interval = 300L

        /**最大数量*/
        val maxNum = 100
    }

    private val rainList = mutableListOf<RainBean>()

    /*已经添加了多少个*/
    private var rainAddNum = 0

    /*资源id*/
    var rainResId = -1

    private var isStart = false

    init {
        rainAnimView.listener = object : RainAnimView.OnTapUpListener {
            override fun onTaoUp(bean: RainBean) {
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
        val removeList = mutableListOf<RainBean>()
        for (bean in rainList) {
            bean.offset(0, (bean.stepY /*ScreenUtil.density*/).toInt())
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

    fun endRain() {
        L.e(TAG, "call: endRain -> ")
        rainList.clear()
        rainAnimView.removeCallbacks(postDelayedRunnable)
        animator.cancel()
        rainAddNum = 0
        isStart = false
        rainAnimView.postInvalidate()
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
            addNewRainInner(rainAddNum + addNum - maxNum)
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
                    stepY = 1 + random.nextInt(5)
                    rainDrawable = ContextCompat.getDrawable(rainAnimView.context, rainResId)
                    val intrinsicWidth = rainDrawable!!.intrinsicWidth
                    val intrinsicHeight = rainDrawable!!.intrinsicHeight
                    setRect(randomX(intrinsicWidth), -intrinsicHeight, intrinsicWidth, intrinsicHeight)
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
}