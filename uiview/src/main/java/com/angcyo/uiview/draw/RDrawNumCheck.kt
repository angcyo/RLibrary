package com.angcyo.uiview.draw

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.textHeight
import com.angcyo.uiview.kotlin.textWidth
import com.angcyo.uiview.skin.SkinHelper

/**
 * Created by angcyo on 2018/06/17 11:56
 */
class RDrawNumCheck(view: View, attr: AttributeSet? = null) : BaseDraw(view, attr) {

    companion object {
        const val DRAW_GRAVITY_CENTER = 0
        const val DRAW_GRAVITY_LEFT = 1
        const val DRAW_GRAVITY_TOP = 2
        const val DRAW_GRAVITY_RIGHT = 3
        const val DRAW_GRAVITY_BOTTOM = 4
    }

    var drawGravity = DRAW_GRAVITY_CENTER
    var drawColor = Color.BLUE
    var drawRadius = 30 * density()

    var showBorder = true
    var borderColor = Color.WHITE
    var borderWidth = 2 * density()

    var drawNum = ""
        set(value) {
            field = value
            postInvalidate()
        }

    var drawNumColor = Color.WHITE

    init {
        initAttribute(attr)
    }

    override fun initAttribute(attr: AttributeSet?) {
        val typedArray = obtainStyledAttributes(attr, R.styleable.RDrawNumCheck)
        drawGravity = typedArray.getInt(R.styleable.RDrawNumCheck_r_draw_gravity, drawGravity)
        if (!isInEditMode) {
            drawColor = SkinHelper.getSkin().themeSubColor
        }
        drawColor = typedArray.getColor(R.styleable.RDrawNumCheck_r_draw_color, drawColor)
        borderColor = typedArray.getColor(R.styleable.RDrawNumCheck_r_draw_border_color, borderColor)
        borderWidth = typedArray.getDimensionPixelOffset(R.styleable.RDrawNumCheck_r_draw_border_width, borderWidth.toInt()).toFloat()
        drawRadius = typedArray.getDimensionPixelOffset(R.styleable.RDrawNumCheck_r_draw_radius, drawRadius.toInt()).toFloat()
        showBorder = typedArray.getBoolean(R.styleable.RDrawNumCheck_r_draw_show_border, showBorder)
        drawNum = typedArray.getString(R.styleable.RDrawNumCheck_r_draw_num) ?: ""

        typedArray.recycle()
    }

    fun getDrawWidth(): Int = drawRadius.toInt()
    fun getDrawHeight(): Int = drawRadius.toInt()

    /**是否是选中状态*/
    fun isCheck(): Boolean {
        return (drawNum.toIntOrNull() ?: 0) > 0
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        when (drawGravity) {
            DRAW_GRAVITY_CENTER -> {

                val cx = (paddingLeft + viewDrawWidth / 2).toFloat()
                val cy = (paddingTop + viewDrawHeight / 2).toFloat()

                if (isCheck()) {
                    mBasePaint.style = Paint.Style.FILL
                    mBasePaint.color = drawColor
                    canvas.drawCircle(cx, cy, drawRadius, mBasePaint)

                    mBasePaint.color = drawNumColor
                    canvas.drawText(drawNum, cx - mBasePaint.textWidth(drawNum) / 2, cy + mBasePaint.textHeight() / 2 - mBasePaint.descent(), mBasePaint)
                } else {
                    //未选中状态, 只绘制边框
                }

                mBasePaint.color = borderColor
                mBasePaint.style = Paint.Style.STROKE
                mBasePaint.strokeWidth = borderWidth
                canvas.drawCircle(cx, cy, drawRadius - borderWidth / 2, mBasePaint)
            }
        }
    }

}