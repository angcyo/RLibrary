package com.angcyo.uiview.widget.group

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import com.angcyo.uiview.R
import com.angcyo.uiview.resources.RAnimListener
import com.angcyo.uiview.widget.RTextView

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/02/02 11:40
 * 修改人员：Robi
 * 修改时间：2018/02/02 11:40
 * 修改备注：
 * Version: 1.0.0
 */
class GameCountDownView(context: Context, attributeSet: AttributeSet? = null) : RTextView(context, attributeSet) {

    /**是否一直显示小时(否则只在值>0时显示)*/
    var showHour = false

    /**是否一直显示分钟(否则只在值>0时显示)*/
    var showMin = false

    /**秒数, 是否显示2位, 自动补齐0*/
    var twoSecBit = false

    private var fromTime = 0/*秒*/

    /**已用时*/
    var useTime = 0 /*秒*/
        get() = if (field <= 0) {
            1
        } else {
            field
        }

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.GameCountDownView)
        showHour = typedArray.getBoolean(R.styleable.GameCountDownView_r_show_hour, showHour)
        showMin = typedArray.getBoolean(R.styleable.GameCountDownView_r_show_min, showMin)
        twoSecBit = typedArray.getBoolean(R.styleable.GameCountDownView_r_two_sec_bit, twoSecBit)
        typedArray.recycle()
    }

    /**到时间了*/
    var onTimeEnd: (() -> Unit)? = null

    /**开始倒计时, 如果已经有倒计时, 那么直接返回*/
    fun startCountDown(fromTime: Int /*从多少秒开始倒计时*/, onEnd: (() -> Unit)? = null) {
        startCountDown(fromTime, "", onEnd)
    }

    /**开始倒计时, 如果已经有倒计时, 那么直接返回*/
    fun startCountDown(fromTime: Int /*从多少秒开始倒计时*/, tag: String, onEnd: (() -> Unit)? = null) {
        if (timeAnim != null && timeAnim!!.isStarted) {
            return
        }

        this.onTimeEnd = onEnd
        this.fromTime = fromTime
        setTag(tag)

        useTime = 0
        if (fromTime > 0) {
            text = formatTime(fromTime * 1000L)
            startAnim()
        }/* else if (fromTime == 0) {
            text = "0"
            onTimeEnd?.invoke()
        }*/ else {
            text = "0"
            onTimeEnd?.invoke()
        }
    }

    /**重新开始倒计时*/
    fun restartCountDown(fromTime: Int /*从多少秒开始倒计时*/, onEnd: (() -> Unit)? = null) {
        this.onTimeEnd = onEnd
        this.fromTime = fromTime
        text = formatTime(fromTime * 1000L)
        startAnim()
    }

    fun stopCountDown() {
        stopAnim()
    }

    fun formatTime(millisecond: Long /*毫秒*/): String {
        val mill = millisecond / 1000

        var min = mill / 60
        var hour = min / 60
        val second = mill % 60
        min %= 60
        hour %= 24

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

    fun formatTime2(millisecond: Long /*毫秒*/): String {
        val mill = millisecond / 1000

        var min = mill / 60
        var hour = min / 60
        val second = mill % 60
        min %= 60
        hour %= 24

        val builder = StringBuilder()
        if (hour > 0 || showHour) {
            builder.append(hour)
            builder.append("小时")
        }
        if (min > 0 || showMin) {
            builder.append(min)
            builder.append("分")
        }
        builder.append(if (second >= 10 || !twoSecBit) second else second)
        builder.append("秒")
        return builder.toString()
    }

    private var timeAnim: ValueAnimator? = null
    private fun startAnim() {
        stopAnim()
        timeAnim = ObjectAnimator.ofInt(fromTime, 0).apply {
            duration = fromTime * 1000L
            interpolator = LinearInterpolator()
            addUpdateListener {
                val value = animatedValue as Int
                useTime = fromTime - value
                text = formatTime(value * 1000L)
            }
            addListener(object : RAnimListener() {
                override fun onAnimationFinish(animation: Animator?, cancel: Boolean) {
                    super.onAnimationFinish(animation, cancel)
                    if (!cancel) {
                        post {
                            onTimeEnd?.invoke()
                        }
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