package com.angcyo.uiview.widget.group

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import com.angcyo.uiview.R
import com.angcyo.uiview.resources.RAnimListener
import com.angcyo.uiview.widget.RTextView

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：游戏倒计时提示类
 * 创建人员：Robi
 * 创建时间：2017/12/18 11:30
 * 修改人员：Robi
 * 修改时间：2017/12/18 11:30
 * 修改备注：
 * Version: 1.0.0
 */
class GameTipView(context: Context, attributeSet: AttributeSet? = null) : FrameLayout(context, attributeSet) {

    var tipTextView: RTextView
    var timeTextView: GameCountDownView

    init {
        View.inflate(context, R.layout.base_game_tip_layout, this)
        tipTextView = findViewById(R.id.tip_text_view)
        timeTextView = findViewById(R.id.time_text_view)
    }

    fun setTipText(text: String) {
        tipTextView.text = text
    }

    fun setTimeText(text: String, stopCountDown: Boolean = false) {
        if (stopCountDown) {
            stopCountDown()
        }
        timeTextView.text = text
    }

    /**开始倒计时*/
    fun startCountDown(fromTime: Int /*从多少秒开始倒计时*/, onEnd: (() -> Unit)? = null) {
        timeTextView.startCountDown(fromTime, onEnd)
    }

    /**停止倒计时*/
    fun stopCountDown() {
        timeTextView.stopCountDown()
    }
}

class GameCountDownView(context: Context, attributeSet: AttributeSet? = null) : RTextView(context, attributeSet) {

    /**是否一直显示小时(否则只在值>0时显示)*/
    var showHour = false

    /**是否一直显示分钟(否则只在值>0时显示)*/
    var showMin = false

    /**秒数, 是否显示2位, 自动补齐0*/
    var twoSecBit = false

    private var fromTime = 0

    /**到时间了*/
    var onTimeEnd: (() -> Unit)? = null

    /**开始倒计时*/
    fun startCountDown(fromTime: Int /*从多少秒开始倒计时*/, onEnd: (() -> Unit)? = null) {
        this.onTimeEnd = onEnd
        this.fromTime = fromTime
        text = formatTime(fromTime * 1000L)
        startAnim()
    }

    fun stopCountDown() {
        stopAnim()
    }

    private fun formatTime(millisecond: Long /*毫秒*/): String {
        val mill = millisecond / 1000

        val min = mill / 60
        val hour = min / 60
        val second = mill % 60

        val builder = StringBuilder()
        if (hour > 0 || showHour) {
            builder.append(if (hour >= 10) hour else "0" + hour)
            builder.append(":")
        }
        if (min > 0 || showMin) {
            builder.append(if (min >= 10) min else "0" + min)
            builder.append(":")
        }
        builder.append(if (second >= 10 || !twoSecBit) second else "0" + second)

        return builder.toString()
    }

    private var timeAnim: ValueAnimator? = null
    private fun startAnim() {
        stopAnim()
        timeAnim = ObjectAnimator.ofInt(fromTime, 0).apply {
            duration = fromTime * 1000L
            interpolator = LinearInterpolator()
            addUpdateListener {
                text = formatTime((animatedValue as Int) * 1000L)
            }
            addListener(object : RAnimListener() {
                override fun onAnimationFinish(animation: Animator?, cancel: Boolean) {
                    super.onAnimationFinish(animation, cancel)
                    if (!cancel) {
                        onTimeEnd?.invoke()
                    }
                }
            })
            start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnim()
    }

    fun stopAnim() {
        timeAnim?.cancel()
    }
}