package com.angcyo.uiview.viewgroup

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.calcLayoutWidthHeight
import com.angcyo.uiview.kotlin.calcWidthHeightRatio
import com.angcyo.uiview.kotlin.density
import com.angcyo.uiview.kotlin.exactlyMeasure
import com.angcyo.uiview.resources.RAnimListener
import com.angcyo.uiview.utils.ClipHelper
import com.angcyo.uiview.view.UIIViewImpl

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
        /**默认(有一定小圆角的CLIP_TYPE_ROUND)*/
        val CLIP_TYPE_DEFAULT = 1
        /**圆角(可以通过aeqWidth属性, 切换正方形还是长方形)*/
        val CLIP_TYPE_ROUND = 2
        /**圆*/
        val CLIP_TYPE_CIRCLE = 3
        /**直角矩形(无圆角状态)*/
        val CLIP_TYPE_RECT = 4
    }

    var clipType = CLIP_TYPE_NONE

    private val defaultClipRadius = 3 * density

    var rBackgroundDrawable: Drawable? = null

    /**当clipType为CLIP_TYPE_CIRCLE时, 这个值表示圆的半径, 否则就是圆角的半径*/
    var clipRadius = defaultClipRadius

    /**引导模式, 会镂空布局, 只在CLIP_TYPE_CIRCLE生效*/
    var guidMode = false

    /**是否是等宽矩形*/
    private var aeqWidth = true

    private var rLayoutWidth: String? = null
    private var rLayoutHeight: String? = null
    private var rLayoutWidthExclude = 0
    private var rLayoutHeightExclude = 0

    var widthHeightRatio: String? = null

    private val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.ClipLayout)
        clipType = typedArray.getInt(R.styleable.ClipLayout_r_clip_type, clipType)
        clipRadius = typedArray.getDimensionPixelOffset(R.styleable.ClipLayout_r_clip_radius, defaultClipRadius.toInt()).toFloat()
        rBackgroundDrawable = typedArray.getDrawable(R.styleable.ClipLayout_r_background)
        aeqWidth = typedArray.getBoolean(R.styleable.ClipLayout_r_is_aeq_width, aeqWidth)
        rLayoutWidth = typedArray.getString(R.styleable.ClipLayout_r_layout_width)
        rLayoutHeight = typedArray.getString(R.styleable.ClipLayout_r_layout_height)
        rLayoutWidthExclude = typedArray.getDimensionPixelOffset(R.styleable.ClipLayout_r_layout_width_exclude, rLayoutWidthExclude)
        rLayoutHeightExclude = typedArray.getDimensionPixelOffset(R.styleable.ClipLayout_r_layout_height_exclude, rLayoutHeightExclude)
        widthHeightRatio = typedArray.getString(R.styleable.ClipLayout_r_width_height_ratio)
        typedArray.recycle()

        setWillNotDraw(false)
    }

    private val clipPath: Path by lazy { Path() }

    private val roundRectF: RectF by lazy {
        RectF()
    }

    private val size
        get() = Math.min(measuredHeight - paddingTop - paddingBottom, measuredWidth - paddingLeft - paddingRight)

    private val cx
        get() = (paddingLeft + (measuredWidth - paddingLeft - paddingRight) / 2).toFloat()

    private val cy
        get() = (paddingTop + (measuredHeight - paddingTop - paddingBottom) / 2).toFloat()

    private val cr
        get() = (size / 2).toFloat()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (aeqWidth) {
            roundRectF.set(cx - cr, cy - cr, cx + cr, cy + cr)
        } else {
            roundRectF.set(paddingLeft.toFloat(), paddingTop.toFloat(),
                    (measuredWidth - paddingRight).toFloat(), (measuredHeight - paddingBottom).toFloat())
        }
        rBackgroundDrawable?.setBounds(0, 0, measuredWidth, measuredHeight)

        if (guidMode) {
            guidBitmap?.recycle()
            guidBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
            guidCanvas = Canvas(guidBitmap)
        }
    }

    private var guidBitmap: Bitmap? = null

    private var guidCanvas: Canvas? = null

    var guidMaskColor = Color.parseColor("#40000000")
    var guidColor = Color.TRANSPARENT

    override fun draw(canvas: Canvas) {
        if (guidMode) {
            paint.xfermode = null
            paint.color = guidMaskColor
            guidCanvas?.drawRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
            paint.color = guidColor
            guidCanvas?.drawCircle(cx, cy, clipRadius, paint)
            paint.xfermode = null

            canvas.drawBitmap(guidBitmap, 0f, 0f, null)
        } else {
            rBackgroundDrawable?.draw(canvas)
            clipPath.reset()

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
                CLIP_TYPE_RECT -> {
                    clipPath.addRect(roundRectF, Path.Direction.CW)
                    canvas.clipPath(clipPath)
                }
            }
            //canvas.drawColor(Color.RED)
        }
        super.draw(canvas)
    }

    private var animator: ValueAnimator? = null

    var clipAnimTime = UIIViewImpl.DEFAULT_ANIM_TIME

    /**clip到指定的宽高*/
    fun clipRectTo(w: Float, h: Float) {
        clipType = CLIP_TYPE_RECT
        roundRectF.set(cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2)

        //L.e("call: clipRectTo -> W:$w H:$h W2:${roundRectF.width()} H2:${roundRectF.height()}")

        postInvalidateOnAnimation()
    }

    /**toMaxFrom*/
    fun getToMaxWH(fromWidth: Int, fromHeight: Int, fraction: Float /*0-1f*/): FloatArray {
        val w: Float = fromWidth + (measuredWidth - fromWidth) * fraction
        val h: Float = fromHeight + (measuredHeight - fromHeight) * fraction
        return floatArrayOf(w, h)
    }

    /**fromMaxTo*/
    fun getFromMaxWH(toWidth: Int, toHeight: Int, fraction: Float /*0-1f*/): FloatArray {
        val w: Float = measuredWidth - (measuredWidth - toWidth) * fraction
        val h: Float = measuredHeight - (measuredHeight - toHeight) * fraction
        return floatArrayOf(w, h)
    }

    fun toMaxFromCircle(cr: Float, onAnimEnd: (() -> Unit)? = null) {
        if (animator == null) {
            animator = ObjectAnimator.ofFloat(cr, ClipHelper.calcEndRadius(measuredWidth, measuredHeight, cx, cy)).apply {
                duration = clipAnimTime.toLong()
                interpolator = AccelerateInterpolator()
                addUpdateListener { animation ->
                    val value: Float = animation.animatedValue as Float
                    clipType = CLIP_TYPE_CIRCLE
                    clipRadius = value
                    postInvalidateOnAnimation()
                }
                addListener(object : RAnimListener() {
                    override fun onAnimationFinish(animation: Animator?) {
                        super.onAnimationFinish(animation)
                        animator = null
                        onAnimEnd?.invoke()
                    }
                })
                clipType = CLIP_TYPE_CIRCLE
                clipRadius = cr
                postInvalidateOnAnimation()

                start()
            }
        }
    }


    /**
     * 以屏幕中心为坐标
     * 从指定的宽高到本身的宽高
     *
     * */
    fun toMaxFrom(width: Int, height: Int, onAnimEnd: (() -> Unit)? = null) {
        if (animator == null) {
            animator = ObjectAnimator.ofFloat(0f, 1f).apply {
                duration = clipAnimTime.toLong()
                interpolator = AccelerateInterpolator()
                addUpdateListener { animation ->
                    val value: Float = animation.animatedValue as Float
                    val w: Float = width + (measuredWidth - width) * value
                    val h: Float = height + (measuredHeight - height) * value
                    clipRectTo(w, h)
                }
                addListener(object : RAnimListener() {
                    override fun onAnimationFinish(animation: Animator?) {
                        super.onAnimationFinish(animation)
                        animator = null
                    }
                })

                clipRectTo(width.toFloat(), height.toFloat())

                start()
            }
        }
    }

    /**
     * 以屏幕中心为坐标
     * 从本身的宽高到指定的宽高
     *
     * */
    fun fromMaxTo(width: Int, height: Int, onAnimEnd: (() -> Unit)? = null) {
        if (animator == null) {
            animator = ObjectAnimator.ofFloat(0f, 1f).apply {
                duration = clipAnimTime.toLong()
                interpolator = DecelerateInterpolator()
                addUpdateListener { animation ->
                    val value: Float = animation.animatedValue as Float
                    val w: Float = measuredWidth - (measuredWidth - width) * value
                    val h: Float = measuredHeight - (measuredHeight - height) * value
                    clipRectTo(w, h)
                }
                addListener(object : RAnimListener() {
                    override fun onAnimationFinish(animation: Animator?) {
                        super.onAnimationFinish(animation)
                        animator = null
                    }
                })

                clipType = CLIP_TYPE_RECT
                roundRectF.set(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())
                postInvalidate()

                start()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        guidBitmap?.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val layoutWidthHeight = calcLayoutWidthHeight(rLayoutWidth, rLayoutHeight, rLayoutWidthExclude, rLayoutHeightExclude)
        val width = layoutWidthHeight[0]
        val height = layoutWidthHeight[1]
        if (width == -1 && height == -1) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else if (width > 0 && height > 0) {
            super.onMeasure(exactlyMeasure(width), exactlyMeasure(height))
        } else {
            if (width == -1) {
                super.onMeasure(widthMeasureSpec, exactlyMeasure(height))
            } else {
                super.onMeasure(exactlyMeasure(width), heightMeasureSpec)
            }
        }
        calcWidthHeightRatio(widthHeightRatio)?.let {
            super.onMeasure(exactlyMeasure(it[0]), exactlyMeasure(it[1]))
        }
    }
}