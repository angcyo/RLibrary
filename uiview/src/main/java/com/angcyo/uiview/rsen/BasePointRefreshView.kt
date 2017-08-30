package com.angcyo.uiview.rsen

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import com.angcyo.uiview.kotlin.density
import com.angcyo.uiview.resources.RAnimListener

/**
 * Created by angcyo on 2017-07-23.
 */
class BasePointRefreshView(context: Context) : View(context),
        RefreshLayout.OnTopViewMoveListener, RefreshLayout.OnBottomViewMoveListener {

    var widthSize: Int = 0
    var heightSize: Int = 0

    /**点的数量*/
    var pointCount = 3
    /**点的大小*/
    var pointSize = 0
    /**点之间间隙的大小*/
    var pointSpaceSize = 0

    var paint: Paint

    /**高亮的点*/
    private var currentPoint = 0

    val valueAnim: ValueAnimator by lazy {
        val anim = ValueAnimator.ofInt(1, pointCount + 1)
        anim.duration = 600
        anim.addUpdateListener {
            currentPoint = it.animatedValue as Int
            //L.e("call: startAnimation -> $currentPoint")
            postInvalidateOnAnimation()
        }
        anim.addListener(object : RAnimListener() {
            override fun onAnimationCancel(animation: Animator?) {
                super.onAnimationCancel(animation)
                currentPoint = 0
                postInvalidateOnAnimation()
            }
        })
        anim.repeatCount = Animation.INFINITE
        anim.repeatMode = Animation.RESTART
        anim.interpolator = LinearInterpolator()
        anim
    }

    init {
        setWillNotDraw(false)
        val dp = density
        pointSize = (dp * 8).toInt()
        pointSpaceSize = (dp * 6).toInt()

        when (pointCount) {
            0 -> {
                widthSize = 0
                heightSize = 0
            }
            1 -> {
                widthSize = pointSize
                heightSize = (dp * 60).toInt()
            }
            else -> {
                widthSize = pointSize * pointCount + (pointCount - 1) * pointSpaceSize
                heightSize = (dp * 60).toInt()
            }
        }

        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.FILL_AND_STROKE
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onDraw(canvas: Canvas) {
        for (i in 1..pointCount) {
            val cx: Float = pointSize / 2f + (i - 1) * (pointSpaceSize + pointSize)
            if (i == currentPoint) {
                paint.color = Color.parseColor("#808080")
            } else {
                paint.color = Color.parseColor("#C0C0C0")
            }
            canvas.drawCircle(cx, (measuredHeight / 2).toFloat(), (pointSize / 2).toFloat(), paint)
        }
    }

    override fun onTopMoveTo(view: View?, top: Int, maxHeight: Int, state: Int) {
        onMove(state)
    }

    override fun onBottomMoveTo(view: View?, bottom: Int, maxHeight: Int, state: Int) {
        onMove(state)
    }

    fun onMove(state: Int) {
        if (state == RefreshLayout.TOP || state == RefreshLayout.BOTTOM) {
            startAnimation()
        } else {
            stopAnimation()
        }
    }

    fun startAnimation() {
        if (!valueAnim.isStarted) {
            valueAnim.start()
        }
    }

    fun stopAnimation() {
        valueAnim.cancel()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }
}
