package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.angcyo.uiview.kotlin.density

/**
 * Created by angcyo on 2017-09-13.
 */
class BlockSeekBar(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet) {

    private val heightList = listOf(0.5f, 0.6f, 0.7f, 0.8f, 1f, 0.8f)

    private val tempRectF = RectF()
    private val clipRectF = RectF()

    private val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
    }

    private var blockWidth = 6 * density

    private var roundSize = 4 * density
    private var spaceSize = 4 * density

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.DKGRAY)

        drawBlock(canvas, Color.WHITE)

        canvas.save()
        clipRectF.set((measuredWidth / 2).toFloat(), 0f, measuredWidth.toFloat(), measuredHeight.toFloat())
        canvas.clipRect(clipRectF)
        drawBlock(canvas, Color.YELLOW)
        canvas.restore()
    }

    private fun drawBlock(canvas: Canvas, color: Int) {
        var left = paddingLeft.toFloat()
        var index = 0
        while (left + blockWidth < measuredWidth) {
            paint.color = color

            val blockHeight = measuredHeight * heightList[index.rem(heightList.size)]
            val top = (measuredHeight - blockHeight) / 2

            tempRectF.set(left, top, left + blockWidth, top + blockHeight)
            canvas.drawRoundRect(tempRectF, roundSize, roundSize, paint)
            left += blockWidth + spaceSize
            index++
        }
    }
}
