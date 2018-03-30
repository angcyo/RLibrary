package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.angcyo.uiview.kotlin.debugPaint
import com.angcyo.uiview.kotlin.density
import com.angcyo.uiview.kotlin.valueAnimator

/**
 * 模仿QQ安全验证, 进度条
 * Created by angcyo on 2018/03/30 21:46
 */
class QQFlowProgressView(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet) {
    private val drawRect = RectF()
    var roundRadius = 0f

    init {
        roundRadius = 2f * density
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        drawWidth = 0f
    }

    var drawStep = 1.2f * density
    private var isDrawEnd = false
    private var drawWidth = 0f
        set(value) {
            field = value
            if (field > (measuredWidth - roundRadius - paddingLeft - paddingRight)) {
                isDrawEnd = true
            }
            if (field < roundRadius + paddingLeft) {
                isDrawEnd = false
            }
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        debugPaint.color = Color.WHITE
        debugPaint.style = Paint.Style.FILL_AND_STROKE

        canvas.save()
        if (isDrawEnd) {
            drawRect.set(measuredWidth - drawWidth + roundRadius + paddingLeft, paddingTop.toFloat(),
                    measuredWidth.toFloat() - roundRadius - paddingRight, measuredHeight.toFloat() - paddingBottom)
        } else {
            drawRect.set(roundRadius + paddingLeft, paddingTop.toFloat(),
                    drawWidth - roundRadius - paddingRight, measuredHeight.toFloat() - paddingBottom)
        }
        canvas.drawRoundRect(drawRect, roundRadius, roundRadius, debugPaint)
        canvas.restore()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        valueAnimator.apply {
            addUpdateListener {
                if (isDrawEnd) {
                    drawWidth -= drawStep
                } else {
                    drawWidth += drawStep
                }
                postInvalidateOnAnimation()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        valueAnimator.cancel()
    }
}