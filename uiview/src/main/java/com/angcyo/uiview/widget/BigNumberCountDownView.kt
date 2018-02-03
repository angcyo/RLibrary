package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.TextView
import com.angcyo.uiview.kotlin.density

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/12/21 15:31
 * 修改人员：Robi
 * 修改时间：2017/12/21 15:31
 * 修改备注：
 * Version: 1.0.0
 */

class BigNumberCountDownView(context: Context, attributeSet: AttributeSet? = null) : TextView(context, attributeSet) {
    init {
        setTextColor(Color.WHITE)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, 300 * density)
        paint.flags = paint.flags or Paint.FAKE_BOLD_TEXT_FLAG
    }

    private var countDownNumber = 0

    /*动画过程中的临时变量*/
    private var countDownNumberAnim = 0

    /**到时间了*/
    var onTimeEnd: (() -> Unit)? = null

    private var isStart = false

    /**开始倒计时, 如果已经有倒计时, 那么直接返回*/
    fun startCountDown(fromTime: Int /*从多少秒开始倒计时*/, onEnd: (() -> Unit)? = null) {
        if (isStart) {
            return
        }

        this.onTimeEnd = onEnd
        this.countDownNumber = fromTime
        this.countDownNumberAnim = countDownNumber
        text = countDownNumber.toString()
        //startAnim()
        visibility = View.VISIBLE
        startScaleAnim()
    }

    fun stopCountDown() {
        isStart = false
        clearAnimation()
    }

    private fun startScaleAnim() {
        isStart = true
        scaleX = 1f
        scaleY = 1f
        animate().scaleX(0.2f)
                .scaleY(0.2f)
                .setDuration(1000)
                .setInterpolator(BounceInterpolator())
                .withEndAction {
                    countDownNumberAnim--
                    text = countDownNumberAnim.toString()

                    if (countDownNumberAnim <= 0) {
                        onTimeEnd?.invoke()
                        isStart = false
                        visibility = View.GONE
                    } else {
                        startScaleAnim()
                    }
                }
                .start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopCountDown()
    }
}