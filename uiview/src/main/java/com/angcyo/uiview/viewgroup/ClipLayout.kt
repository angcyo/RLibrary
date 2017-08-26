package com.angcyo.uiview.viewgroup

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.density

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/08/26 13:39
 * 修改人员：Robi
 * 修改时间：2017/08/26 13:39
 * 修改备注：
 * Version: 1.0.0
 */
open class ClipLayout(context: Context, attributeSet: AttributeSet? = null) : FrameLayout(context, attributeSet) {

    companion object {
        /**不剪切*/
        val CLIP_TYPE_NONE = 0
        /**默认(小圆角)*/
        val CLIP_TYPE_DEFAULT = 1
        /**圆角*/
        val CLIP_TYPE_ROUND = 2
        /**圆*/
        val CLIP_TYPE_CIRCLE = 3
    }

    var clipType = CLIP_TYPE_DEFAULT

    private val defaultClipRadius = 3 * density

    /**当clipType为CLIP_TYPE_CIRCLE时, 这个值表示圆的半径, 否则就是圆角的半径*/
    var clipRadius = defaultClipRadius

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.ClipLayout)
        clipType = typedArray.getInt(R.styleable.ClipLayout_r_clip_type, CLIP_TYPE_DEFAULT)
        clipRadius = typedArray.getDimensionPixelOffset(R.styleable.ClipLayout_r_clip_radius, 3).toFloat()
        typedArray.recycle()

        setWillNotDraw(false)
    }

    private val clipPath: Path by lazy { Path() }

    private val roundRectF: RectF by lazy {
        RectF()
    }

    override fun draw(canvas: Canvas) {
        clipPath.reset()
        val size = Math.min(measuredHeight - paddingTop - paddingBottom,
                measuredWidth - paddingLeft - paddingRight)
        val cx = (paddingLeft + size / 2).toFloat()
        val cy = (paddingTop + size / 2).toFloat()
        val cr = (size / 2).toFloat()
        roundRectF.set(cx - cr, cy - cr, cx + cr, cy + cr)

        when (clipType) {
            CLIP_TYPE_NONE -> {
            }
            CLIP_TYPE_ROUND -> {
                clipPath.addRoundRect(roundRectF, floatArrayOf(clipRadius, clipRadius, clipRadius, clipRadius,
                        clipRadius, clipRadius, clipRadius, clipRadius), Path.Direction.CW)
                canvas.clipPath(clipPath)
            }
            CLIP_TYPE_CIRCLE -> {
                clipPath.addCircle(cx, cy, clipRadius, Path.Direction.CW)
                canvas.clipPath(clipPath)
            }
            CLIP_TYPE_DEFAULT -> {
                clipPath.addRoundRect(roundRectF, floatArrayOf(defaultClipRadius, defaultClipRadius, defaultClipRadius, defaultClipRadius,
                        defaultClipRadius, defaultClipRadius, defaultClipRadius, defaultClipRadius), Path.Direction.CW)
                canvas.clipPath(clipPath)
            }
        }
        super.draw(canvas)
    }
}