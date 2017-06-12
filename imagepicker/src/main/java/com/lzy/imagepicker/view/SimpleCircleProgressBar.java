package com.lzy.imagepicker.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;


/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：简单圆形加载进度bar
 * 创建人员：Robi
 * 创建时间：2016/11/28 17:57
 * 修改人员：Robi
 * 修改时间：2016/11/28 17:57
 * 修改备注：
 * Version: 1.0.0
 */
public class SimpleCircleProgressBar extends View {

    int mProgressColor;
    int mBackColor;

    Paint mPaint;
    RectF mRectF = new RectF();

    private ValueAnimator mAnimator;
    private int startAngle;
    private float strokeWidth;
    private float mDensity;

    public SimpleCircleProgressBar(Context context) {
        super(context);
        init();
    }

    public SimpleCircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mDensity = getResources().getDisplayMetrics().density;

        mProgressColor = Color.WHITE;
        mBackColor = Color.parseColor("#474430");

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);

        strokeWidth = mDensity * 2;
        mPaint.setStrokeWidth(strokeWidth);
    }

    public void setProgressColor(int progressColor) {
        mProgressColor = progressColor;
    }

    public void setBackColor(int backColor) {
        mBackColor = backColor;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            height = (int) (mDensity * 20);
        }
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
            width = (int) (mDensity * 20);
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRectF.set(0, 0, w, h);
        mRectF.inset(strokeWidth, strokeWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(mBackColor);
        mPaint.setStrokeWidth(strokeWidth);
        canvas.drawArc(mRectF, 0f, 360f, false, mPaint);

        mPaint.setColor(mProgressColor);
        mPaint.setStrokeWidth(strokeWidth + 1.1f);
        canvas.drawArc(mRectF, startAngle, 90f, false, mPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();
    }

    public void start() {
        if (mAnimator == null) {
            mAnimator = ValueAnimator.ofInt(0, 360);
            mAnimator.setInterpolator(new LinearInterpolator());
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    startAngle = (int) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
            mAnimator.setDuration(1000);
            mAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mAnimator.setRepeatMode(ValueAnimator.RESTART);
        }
        if (!mAnimator.isStarted()) {
            mAnimator.start();
        }
    }

    public void stop() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
    }
}
