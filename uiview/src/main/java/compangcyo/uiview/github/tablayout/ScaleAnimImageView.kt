package com.angcyo.uiview.github.tablayout

import android.content.Context
import android.support.v4.view.MotionEventCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.BounceInterpolator

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/08/04 15:55
 * 修改人员：Robi
 * 修改时间：2017/08/04 15:55
 * 修改备注：
 * Version: 1.0.0
 */
open class ScaleAnimImageView(context: Context, attributeSet: AttributeSet? = null) : AppCompatImageView(context, attributeSet) {

    var enableScaleAnim = false

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (enableScaleAnim) {
            val masked = MotionEventCompat.getActionMasked(event)
            if (masked == MotionEvent.ACTION_DOWN) {
                ViewCompat.setScaleX(this, 0.5f)
                ViewCompat.setScaleY(this, 0.5f)
                this.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setInterpolator(BounceInterpolator())
                        .setDuration(300)
                        .start()
            }
        }
        return super.onTouchEvent(event)
    }
}