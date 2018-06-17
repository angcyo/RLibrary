package com.angcyo.uiview.draw

import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import com.angcyo.uiview.R

/**
 * Created by angcyo on 2018/06/17 11:56
 */
class RDrawMaskColor(view: View, attr: AttributeSet? = null) : BaseDraw(view, attr) {

    var drawMaskColorShow = false
        set(value) {
            field = value
            postInvalidate()
        }
    var drawMaskColorColor = Color.parseColor("#80000000")

    init {
        initAttribute(attr)
    }

    override fun initAttribute(attr: AttributeSet?) {
        val typedArray = obtainStyledAttributes(attr, R.styleable.RDrawMaskColor)
        drawMaskColorColor = typedArray.getColor(R.styleable.RDrawMaskColor_r_draw_mask_color_color, drawMaskColorColor)
        drawMaskColorShow = typedArray.getBoolean(R.styleable.RDrawMaskColor_r_draw_mask_color_show, drawMaskColorShow)

        typedArray.recycle()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (drawMaskColorShow) {
            canvas.clipRect(0, 0, viewWidth, viewHeight)
            canvas.drawColor(drawMaskColorColor)
        }
    }

}