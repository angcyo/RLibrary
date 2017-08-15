package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.density

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：圆形图片
 * 创建人员：Robi
 * 创建时间：2017/06/08 17:40
 * 修改人员：Robi
 * 修改时间：2017/06/08 17:40
 * 修改备注：
 * Version: 1.0.0
 */
open class CircleImageView(context: Context, attributeSet: AttributeSet? = null) : AppCompatImageView(context, attributeSet) {

    var showType = NORMAL
        set(value) {
            field = value
            postInvalidate()
        }

    var roundRadius = 0f

    val roundRectF: RectF by lazy {
        RectF()
    }

    var lineWidth = 0f
    var lineColor: Int = Color.WHITE

    /**4个角的圆角信息*/
    val radius: FloatArray
        get() {
            return floatArrayOf(roundRadius, roundRadius, roundRadius, roundRadius,
                    roundRadius, roundRadius, roundRadius, roundRadius)
        }

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.CircleImageView)
        showType = typedArray.getInt(R.styleable.CircleImageView_r_show_type, showType)
        lineColor = typedArray.getColor(R.styleable.CircleImageView_r_line_color, lineColor)
        roundRadius = typedArray.getDimensionPixelOffset(R.styleable.CircleImageView_r_round_radius, (10 * density).toInt()).toFloat()
        lineWidth = typedArray.getDimensionPixelOffset(R.styleable.CircleImageView_r_line_width, (1 * density).toInt()).toFloat()
        typedArray.recycle()
    }

    val clipPath: Path by lazy { Path() }

    val paint: Paint by lazy {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.strokeWidth = lineWidth
        p.strokeJoin = Paint.Join.ROUND
        p.strokeCap = Paint.Cap.ROUND
        p.style = Paint.Style.STROKE
        p.isFilterBitmap = true
        p
    }

    override fun onDraw(canvas: Canvas) {
        when (showType) {
            NORMAL -> {
                super.onDraw(canvas)
            }
            ROUND, CIRCLE -> {
                val size = Math.min(measuredHeight - paddingTop - paddingBottom,
                        measuredWidth - paddingLeft - paddingRight)
                val cx = (paddingLeft + size / 2).toFloat()
                val cy = (paddingTop + size / 2).toFloat()
                val cr = (size / 2).toFloat()
                clipPath.reset()
                paint.color = lineColor

                if (showType == CIRCLE) {
                    clipPath.addCircle(cx, cy, cr, Path.Direction.CW)
                } else {
                    roundRectF.set(cx - cr, cy - cr, cx + cr, cy + cr)
                    clipPath.addRoundRect(roundRectF, radius, Path.Direction.CW)
                }
                //val saveLayer = canvas.saveLayer(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), paint, Canvas.ALL_SAVE_FLAG)
                canvas.clipPath(clipPath, Region.Op.INTERSECT)//交集显示

                super.onDraw(canvas)

                //canvas.drawRoundRect(roundRectF, roundRadius, roundRadius, paint)
                //canvas.drawCircle(cx, cy, cr, paint)
                canvas.drawPath(clipPath, paint)
                //canvas.restoreToCount(saveLayer)
            }
        }
    }

    val drawDrawable: Drawable?
        get() {
            return drawable
        }

    companion object {
        const val NORMAL = 0
        const val CIRCLE = 1
        const val ROUND = 2
    }
}