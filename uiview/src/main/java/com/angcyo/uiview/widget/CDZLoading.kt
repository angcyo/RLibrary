package com.angcyo.uiview.widget

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.angcyo.uiview.kotlin.density
import com.angcyo.uiview.kotlin.getDrawCenterCy
import com.angcyo.uiview.kotlin.nowTime
import com.angcyo.uiview.kotlin.random
import com.angcyo.uiview.utils.RUtils

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：吃豆子加载动画
 * 创建人员：Robi
 * 创建时间：2018/02/05 17:33
 * 修改人员：Robi
 * 修改时间：2018/02/05 17:33
 * 修改备注：
 * Version: 1.0.0
 */
class CDZLoading(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet) {

    private val dzList = mutableListOf<DZBean>()

    //大圆半径(模拟人脸吃豆子)
    var faceRadius = 0f

    private val paint: Paint by lazy {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.strokeCap = Paint.Cap.ROUND
        p.strokeJoin = Paint.Join.ROUND
        p
    }

    private val faceTempRectF = RectF()

    init {
        faceRadius = 10 * density

        for (i in 0 until 4) {
            //添加豆子
            dzList.add(DZBean().apply {
                color = RUtils.randomColor(random) //彩色豆子
                radius = 3 * density //半径
                //maxMoveLength = (faceRadius + 50 * density).toInt()
                delayStartTime = (i * 300).toLong()
                startX = (paddingLeft + faceRadius + faceRadius + 50 * density).toInt() //默认位置
                stepX = (1 * density).toInt()  //每次移动1dp
            })
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(Color.GRAY)

        //绘制3个豆子
        dzList.map {
            paint.color = it.color
            canvas.drawCircle(it.drawX, getDrawCenterCy(), it.radius, paint)
            //L.e("call: onDraw -> ${it.drawX}")
        }

        //绘制嘴巴, 模拟吃豆子
        paint.color = Color.RED

        val c = count % 30

        faceTempRectF.set(paddingLeft.toFloat(), getDrawCenterCy() - faceRadius,
                paddingLeft + 2 * faceRadius,
                getDrawCenterCy() + faceRadius)

//        canvas.save()
//        canvas.drawArc(faceTempRectF, 30f, 300f, true, paint)
//        canvas.restore()
//
//        canvas.save()
//        canvas.translate(2 * faceRadius + 10, 0f)
//        canvas.drawArc(faceTempRectF, 15f, 330f, true, paint)
//        canvas.restore()
//
//        canvas.save()
//        canvas.translate(4 * faceRadius + 20, 0f)
//        canvas.drawCircle(paddingLeft + faceRadius, getDrawCenterCy(), faceRadius, paint)
//        canvas.restore()


        if (c >= 20) {
            //全闭嘴巴
            canvas.drawCircle(paddingLeft + faceRadius, getDrawCenterCy(), faceRadius, paint)
        } else if (c >= 10) {
            //半开嘴巴
            canvas.drawArc(faceTempRectF, 10f, 340f, true, paint)
        } else {
            //全开嘴巴
            canvas.drawArc(faceTempRectF, 30f, 300f, true, paint)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startLoadingAnim()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopLoadingAnim()
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE) {
            startLoadingAnim()
        } else {
            stopLoadingAnim()
        }
    }

    private var count = 0
    private var startTime = 0L
    private val animator: ValueAnimator by lazy {
        ObjectAnimator.ofInt(0, 60).apply {
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            duration = 1000

            addUpdateListener { animation ->
                val value: Int = animation.animatedValue as Int
                //60帧运行速度
                count++

                if (count >= 60) {
                    count = 0
                }

                dzList.map {
                    it.update()
                }

                postInvalidateOnAnimation()
            }
        }
    }

    fun startLoadingAnim() {
        if (!animator.isStarted) {
            if (startTime <= 0) {
                startTime = System.currentTimeMillis()
            }
            animator.start()
        }
    }

    fun stopLoadingAnim() {
        if (animator.isStarted) {
            animator.cancel()
        }
    }

    inner class DZBean {
        //豆子开始的x坐标
        var startX = 0
            set(value) {
                field = value
                drawX = field.toFloat()
            }
        //豆子每次移动的距离
        var stepX = 0
        //豆子的半径
        var radius = 0f
        //豆子的半径
        var color = Color.WHITE
        //豆子正在绘制的x坐标
        var drawX = 0f

        var delayStartTime = 0L //需要延时多久才开始

        /**更新豆子坐标*/
        fun update() {
            if ((nowTime() - startTime) > delayStartTime) {
                drawX -= stepX
                if (drawX < paddingLeft + faceRadius) {
                    drawX = startX.toFloat()
                }
            }
        }
    }
}