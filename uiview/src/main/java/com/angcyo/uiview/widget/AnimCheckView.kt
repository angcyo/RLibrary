package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.angcyo.animcheckviewdemo.RDrawGradientProgress

/**
 * Created by angcyo on 2018/11/17 23:08
 */

class AnimCheckView(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet) {

    var drawGradientProgress: RDrawGradientProgress

    init {
        drawGradientProgress = RDrawGradientProgress(this, attributeSet)
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        drawGradientProgress.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawGradientProgress.onDraw(canvas!!)
    }
}