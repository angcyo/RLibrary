package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.Canvas
import android.support.v4.view.MotionEventCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.density
import com.angcyo.uiview.kotlin.getDrawable
import com.angcyo.uiview.kotlin.max0
import com.angcyo.uiview.kotlin.minValue
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * 评分控件
 * Created by angcyo on 2017/09/18 0018.
 */
class RRatingBar(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet) {

    /*五角星 选中的 图案*/
    var ratingSelectorDrawable by LayoutProperty(getDrawable(R.drawable.base_wujiaoxing_20))

    /*五角星 未选中的 图案*/
    var ratingNormalDrawable by LayoutProperty(getDrawable(R.drawable.base_wujiaoxing_20_n))

    /**星星的数量*/
    var ratingNum: Int by RefreshProperty(5)

    /**当前评分 [1-ratingNum], 小于0, 表示没有*/
    var curRating by RefreshProperty(2)

    /**评分最小允许的值*/
    var minRating = 0

    /**星星之间的间隙*/
    var ratingSpace: Float by RefreshProperty(8 * density)

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.RRatingBar)
        curRating = typedArray.getInteger(R.styleable.RRatingBar_r_cur_rating, curRating)
        ratingNum = typedArray.getInteger(R.styleable.RRatingBar_r_rating_count, ratingNum)
        minRating = typedArray.getInteger(R.styleable.RRatingBar_r_min_rating, minRating)
        val normalDrawable = typedArray.getDrawable(R.styleable.RRatingBar_r_normal_rating)
        val selectorDrawable = typedArray.getDrawable(R.styleable.RRatingBar_r_selected_rating)
        if (normalDrawable != null) {
            ratingNormalDrawable = normalDrawable
        }
        if (selectorDrawable != null) {
            ratingSelectorDrawable = selectorDrawable
        }
        ratingSpace = typedArray.getDimensionPixelOffset(R.styleable.RRatingBar_r_rating_space, ratingSpace.toInt()).toFloat()
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            widthSize = ((heightSize - paddingTop - paddingBottom) * ratingNum +
                    (ratingSpace * (ratingNum - 1)).max0() +
                    paddingLeft + paddingRight).toInt()
        } else {
            if (widthMode != MeasureSpec.EXACTLY) {
                //如果宽度模式不是 指定的定值. (自适应宽度)
                widthSize = (Math.max(ratingSelectorDrawable.intrinsicWidth, ratingNormalDrawable.intrinsicWidth) * ratingNum +
                        (ratingSpace * (ratingNum - 1)).max0() +
                        paddingLeft + paddingRight).toInt()
            } else {

            }
            if (heightMode != MeasureSpec.EXACTLY) {
                //和宽度类似
                heightSize = Math.max(ratingSelectorDrawable.intrinsicHeight, ratingNormalDrawable.intrinsicHeight) + paddingTop + paddingBottom
            }
        }

        setMeasuredDimension(widthSize, heightSize)
    }

    /*有效的绘制大小*/
    private val viewDrawSize: Int
        get() = measuredHeight - paddingTop - paddingBottom

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        //固定星星的bound
        ratingNormalDrawable.setBounds(0, 0, viewDrawSize, viewDrawSize)
        ratingSelectorDrawable.setBounds(0, 0, viewDrawSize, viewDrawSize)
    }

    /*星星的宽度*/
    val ratingWidth: Int
        get() {
            return ratingNormalDrawable.bounds.width()
        }

    override fun onDraw(canvas: Canvas) {
        //调用此方法, 不禁用view本身的功能, 比如:原本的background属性
        super.onDraw(canvas)

        //确保星星的有效数量
        val num = Math.max(0, Math.min(curRating, ratingNum))

        //绘制未选中星星
        var left = paddingLeft
        for (i in num until ratingNum) {
            canvas.save()
            canvas.translate(left + i * ratingWidth + i * ratingSpace, paddingTop.toFloat())
            ratingNormalDrawable.draw(canvas)
            canvas.restore()
        }

        //绘制选中星星
        left = paddingLeft
        for (i in 0 until num) {
            canvas.save()
            canvas.translate(left + i * ratingWidth + i * ratingSpace, paddingTop.toFloat())
            ratingSelectorDrawable.draw(canvas)
            canvas.restore()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return super.onTouchEvent(event)
        }

        val action = MotionEventCompat.getActionMasked(event)
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
                calcRating(event.x)
            }
            MotionEvent.ACTION_MOVE -> {
                calcRating(event.x)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }

    /*根据x的坐标, 计算星星的个数*/
    private fun calcRating(x: Float) {
        var left = paddingLeft
        var index = 0
        for (i in 0 until ratingNum) {
            if (x > left + i * ratingWidth + i * ratingSpace) {
                index = i + 1
            }
        }

        val oldValue = curRating
        val value = index.minValue(minRating)
        if (value != curRating) {
            curRating = value
            postInvalidate()
            onRatingChangeListener?.onRatingChange(oldValue, value)
        }
    }

    var onRatingChangeListener: OnRatingChangeListener? = null

    public abstract class OnRatingChangeListener {
        open fun onRatingChange(from: Int, to: Int) {

        }
    }
}

/*定义一个自动刷新的属性代理*/
class RefreshProperty<T>(var value: T) : ReadWriteProperty<View, T> {
    override fun getValue(thisRef: View, property: KProperty<*>): T = value

    override fun setValue(thisRef: View, property: KProperty<*>, value: T) {
        this.value = value
        thisRef.postInvalidate()
    }
}

class LayoutProperty<T>(var value: T) : ReadWriteProperty<View, T> {
    override fun getValue(thisRef: View, property: KProperty<*>): T = value

    override fun setValue(thisRef: View, property: KProperty<*>, value: T) {
        this.value = value
        thisRef.requestLayout()
    }
}