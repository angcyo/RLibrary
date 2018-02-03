package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.support.v4.view.MotionEventCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.density
import com.angcyo.uiview.kotlin.getDrawable

/**
 * Created by angcyo on 2017-09-13.
 */
class BlockSeekBar(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet) {

    /**不规则高度列表*/
    private val heightList = listOf(0.5f, 0.7f, 0.9f, 1.0f, 0.8f, 0.6f, 0.3f, 0.7f, 0.8f)

    private val tempRectF = RectF()
    private val clipRectF = RectF()

    private val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
    }

    /**每一个的宽度*/
    private var blockWidth = 4 * density

    /**圆角大小*/
    private var roundSize = 4 * density

    /**空隙大小*/
    private var spaceSize = 2 * density

    /**滑块*/
    private var sliderDrawable: Drawable? = null

    var blockProgressColor: Int = Color.YELLOW
    var blockProgressBgColor: Int = Color.WHITE

    /**当前的进度, 非百分比*/
    var blockProgress: Int = 0
        set(value) {
            field = when {
                value < 0 -> 0
                value > blockMaxProgress -> blockMaxProgress
                else -> value
            }
            postInvalidate()
        }

    /**最大刻度, 百分比计算的分母*/
    var blockMaxProgress: Int = 100
        set(value) {
            field = value
            postInvalidate()
        }

    /**滑块的最小宽度, 非百分比*/
    var blockMinWidth: Int = 20
        set(value) {
            field = when {
                value < 10 -> 10
                value > blockMaxProgress -> blockMaxProgress - 10
                else -> value
            }
            postInvalidate()
        }

    init {
        sliderDrawable = getDrawable(R.drawable.base_slider)?.apply {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        }

        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.BlockSeekBar)
        blockProgress = typedArray.getInt(R.styleable.BlockSeekBar_r_block_progress, blockProgress)
        blockMaxProgress = typedArray.getInt(R.styleable.BlockSeekBar_r_block_max_progress, blockMaxProgress)
        blockMinWidth = typedArray.getInt(R.styleable.BlockSeekBar_r_block_min_width, blockMinWidth)
        blockProgressColor = typedArray.getColor(R.styleable.BlockSeekBar_r_block_progress_color, blockProgressColor)
        blockProgressBgColor = typedArray.getColor(R.styleable.BlockSeekBar_r_block_progress_bg_color, blockProgressBgColor)
        val drawable = typedArray.getDrawable(R.styleable.BlockSeekBar_r_slider_drawable)
        if (drawable != null) {
            sliderDrawable = drawable.apply {
                setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            }
        }
        typedArray.recycle()
    }

    //滑块的高度
    private val sliderHeight: Int
        get() {
            if (sliderDrawable == null) {
                return 0
            }
            return sliderDrawable!!.intrinsicHeight
        }

    private val sliderWidth: Int
        get() {
            if (sliderDrawable == null) {
                return 0
            }
            return sliderDrawable!!.intrinsicWidth
        }

    //滑块允许绘制的高度
    private val blockDrawHeight: Int
        get() {
            return measuredHeight - sliderHeight - paddingTop - paddingBottom
        }

    //滑块允许绘制的宽度
    private val blockDrawWidth: Int
        get() {
            return sliderDrawWidth - sliderWidth
        }

    private val sliderDrawWidth: Int
        get() {
            return measuredWidth - paddingLeft - paddingRight
        }

    private val sliderDrawLeft: Float
        get() {
            return drawProgress * blockDrawWidth
        }

    private val blockDrawLeft: Float
        get() {
            return paddingLeft.toFloat() + sliderWidth / 2
        }

    /**按照百分比, 转换的进度*/
    private val drawProgress: Float
        get() {
            return Math.min(blockProgress * 1f / blockMaxProgress, 1f)
        }

    //最小绘制宽度的进度比
    private val drawMinBlockProgress: Float
        get() {
            return Math.min(blockMinWidth * 1f / blockMaxProgress, 1f)
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isInEditMode) {
            canvas.drawColor(Color.DKGRAY)
        }

        //绘制标准柱子
        drawBlock(canvas, blockProgressBgColor)

        //绘制进度柱子
        canvas.save()
        val left = blockDrawLeft + blockDrawWidth * drawProgress
        clipRectF.set(left, 0f, left + drawMinBlockProgress * blockDrawWidth, blockDrawHeight.toFloat() + paddingTop)
        canvas.clipRect(clipRectF)
        drawBlock(canvas, blockProgressColor)
        canvas.restore()

        //绘制滑块
        canvas.save()
        canvas.translate(paddingLeft.toFloat() + sliderDrawLeft, blockDrawHeight.toFloat() + paddingTop)
        sliderDrawable?.draw(canvas)
        canvas.restore()
    }

    private fun drawBlock(canvas: Canvas, color: Int) {
        var left = blockDrawLeft
        var index = 0
        while (left + blockWidth < measuredWidth - sliderWidth / 2 - paddingRight) {
            paint.color = color

            val blockHeight = blockDrawHeight * heightList[index.rem(heightList.size)]
            val top = (blockDrawHeight - blockHeight) / 2 + paddingTop

            tempRectF.set(left, top, left + blockWidth, top + blockHeight)
            canvas.drawRoundRect(tempRectF, roundSize, roundSize, paint)
            left += blockWidth + spaceSize
            index++
        }
    }

    //touch处理
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = MotionEventCompat.getActionMasked(event)
        val eventX = event.x
        //L.e("call: onTouchEvent([event])-> " + action + " x:" + eventX);
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                //L.e("call: onTouchEvent([event])-> DOWN:" + " x:" + eventX);
//                isTouchDown = true
//                notifyListenerStartTouch()
                blockSeekListener?.onTouchStart(this)
                calcProgress(eventX)
                parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> calcProgress(eventX)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
//                isTouchDown = false
//                notifyListenerStopTouch()
                blockSeekListener?.onTouchEnd(this)
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }

    /**
     * 根据touch坐标, 计算进度
     */
    private fun calcProgress(touchX: Float) {
        //事件发生在柱子中的比率
        val scale = (touchX - blockDrawLeft) / blockDrawWidth

        val progress = (scale * blockMaxProgress).toInt()

        if (progress <= blockMaxProgress - blockMinWidth) {
            //将比率转换成进度
            blockProgress = progress

            //L.e("call: onSeekChange -> $blockProgress ${blockProgress + blockMinWidth}")
            blockSeekListener?.onSeekChange(this, blockProgress, blockProgress + blockMinWidth)
            postInvalidate()
        }
//        val x = touchX - paddingLeft.toFloat() - (mThumbWidth / 2).toFloat()
//        val old = this.curProgress
//        this.curProgress = ensureProgress((x / getMaxLength() * maxProgress).toInt())
//        if (old != curProgress) {
//            notifyListenerProgress(true)
//        }
    }

    fun setBlockProgressAndNotify(progress: Int) {
        blockProgress = progress
        blockSeekListener?.onSeekChange(this, blockProgress, blockProgress + blockMinWidth)
    }

    /**事件监听*/
    var blockSeekListener: OnBlockSeekListener? = null

    public abstract class OnBlockSeekListener {
        public open fun onTouchStart(view: BlockSeekBar) {

        }

        public open fun onSeekChange(view: BlockSeekBar, startX: Int /*非百分比*/, endX: Int) {

        }

        public open fun onTouchEnd(view: BlockSeekBar) {

        }
    }
}


