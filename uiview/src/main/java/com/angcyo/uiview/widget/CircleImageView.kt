package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v7.widget.AppCompatImageView
import android.text.TextUtils
import android.util.AttributeSet
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.calcWidthHeightRatio
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

    private val roundRectF: RectF by lazy {
        RectF()
    }

    var lineWidth = 0f
    var lineColor: Int = Color.WHITE

    /**是否绘制边框*/
    var drawBorder = true

    /**4个角的圆角信息*/
    val radius: FloatArray
        get() {
            return floatArrayOf(roundRadius, roundRadius, roundRadius, roundRadius,
                    roundRadius, roundRadius, roundRadius, roundRadius)
        }

    /**高度等于宽度*/
    protected var equWidth: Boolean = false

    var widthHeightRatio: String? = null

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.CircleImageView)
        showType = typedArray.getInt(R.styleable.CircleImageView_r_show_type, showType)
        lineColor = typedArray.getColor(R.styleable.CircleImageView_r_line_color, lineColor)
        roundRadius = typedArray.getDimensionPixelOffset(R.styleable.CircleImageView_r_round_radius, (10 * density).toInt()).toFloat()
        lineWidth = typedArray.getDimensionPixelOffset(R.styleable.CircleImageView_r_line_width, (1 * density).toInt()).toFloat()
        equWidth = typedArray.getBoolean(R.styleable.CircleImageView_r_equ_width, equWidth)
        drawBorder = typedArray.getBoolean(R.styleable.CircleImageView_r_draw_border, drawBorder)
        widthHeightRatio = typedArray.getString(R.styleable.CircleImageView_r_width_height_ratio)
        typedArray.recycle()
    }

    private val clipPath: Path by lazy { Path() }
    private val borderPath: Path by lazy { Path() }

    val paint: Paint by lazy {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.strokeWidth = lineWidth
        p.strokeJoin = Paint.Join.ROUND
        p.strokeCap = Paint.Cap.ROUND
        p.style = Paint.Style.STROKE
        p.isFilterBitmap = true
        p
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (equWidth) {
            setMeasuredDimension(measuredWidth, measuredWidth)
        } else {
            calcWidthHeightRatio(widthHeightRatio)?.let {
                setMeasuredDimension(it[0], it[1])
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        when (showType) {
            NORMAL -> {
                super.onDraw(canvas)
            }
            ROUND, CIRCLE, ROUND_RECT -> {
                val size = Math.min(measuredHeight - paddingTop - paddingBottom,
                        measuredWidth - paddingLeft - paddingRight)
                val cx = (paddingLeft + size / 2).toFloat()
                val cy = (paddingTop + size / 2).toFloat()
                val cr = (size / 2).toFloat()
                clipPath.reset()
                borderPath.reset()
                paint.color = lineColor

                if (showType == CIRCLE) {
                    clipPath.addCircle(cx, cy, cr, Path.Direction.CW)
                    borderPath.addCircle(cx, cy, cr - lineWidth / 2, Path.Direction.CW)
                } else {
                    if (showType == ROUND_RECT) {
                        roundRectF.set(paddingLeft.toFloat(), paddingTop.toFloat(),
                                (measuredWidth - paddingRight).toFloat(), (measuredHeight - paddingBottom).toFloat())
                    } else {
                        roundRectF.set(cx - cr, cy - cr, cx + cr, cy + cr)
                    }
                    clipPath.addRoundRect(roundRectF, radius, Path.Direction.CW)
                    roundRectF.inset(lineWidth / 2, lineWidth / 2)
                    borderPath.addRoundRect(roundRectF, radius, Path.Direction.CW)
                }

                fun save(canvas: Canvas) = if (TextUtils.equals(Build.MANUFACTURER, "HUAWEI") &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP /*华为手机处理圆角, 偶尔会失败.*/) {
                    canvas.saveLayer(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), paint, Canvas.ALL_SAVE_FLAG)
                } else {
                    canvas.save()
                }

                var save1 = save(canvas)
                canvas.clipPath(clipPath, Region.Op.INTERSECT)//交集显示

                //val savePath = canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG)
                var save2 = save(canvas)
//                if (TextUtils.equals(Build.MODEL, "VTR-AL00") /*华为P10处理圆角, 偶尔会失败.*/) {
//                if (TextUtils.equals(Build.MANUFACTURER, "HUAWEI") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP /*华为手机处理圆角, 偶尔会失败.*/) {
//                    save2 = canvas.saveLayer(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), paint, Canvas.ALL_SAVE_FLAG)
//                } else {
//                    save2 = canvas.save()
//                }
                super.onDraw(canvas)
                canvas.restoreToCount(save2)
                canvas.restoreToCount(save1)

                if (drawBorder) {
                    canvas.drawPath(borderPath, paint)
                }

                //canvas.restoreToCount(savePath)

                //canvas.drawRoundRect(roundRectF, roundRadius, roundRadius, paint)
                //canvas.drawCircle(cx, cy, cr, paint)
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
        const val ROUND = 2 //正方形的圆角
        const val ROUND_RECT = 3 //长方形的圆角
    }
}