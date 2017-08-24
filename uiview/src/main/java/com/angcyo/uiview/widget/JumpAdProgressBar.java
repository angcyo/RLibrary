package com.angcyo.uiview.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.angcyo.library.utils.L;
import com.angcyo.uiview.R;
import com.angcyo.uiview.skin.SkinHelper;


/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：跳过广告
 * 创建人员：Robi
 * 创建时间：2016/11/28 17:57
 * 修改人员：Robi
 * 修改时间：2016/11/28 17:57
 * 修改备注：
 * Version: 1.0.0
 */
public class JumpAdProgressBar extends View {

    int mProgressColor;
    int mBackColor;
    int textColor;

    String drawText;

    Paint mPaint;
    RectF mRectF = new RectF();
    OnJumpListener mOnJumpListener;
    private ValueAnimator mAnimator;
    private int sweepAngle;
    private float strokeWidth;
    private float mDensity;
    /**
     * 广告时间
     */
    private int adTime = 3000;

    public JumpAdProgressBar(Context context) {
        super(context);
        init();
    }

    public JumpAdProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.JumpAdProgressBar);
        mProgressColor = typedArray.getColor(R.styleable.JumpAdProgressBar_r_progress_color, mProgressColor);
        mBackColor = typedArray.getColor(R.styleable.JumpAdProgressBar_r_back_color, mBackColor);
        textColor = typedArray.getColor(R.styleable.JumpAdProgressBar_r_text_color, textColor);
        drawText = typedArray.getString(R.styleable.JumpAdProgressBar_r_show_text);
        strokeWidth = typedArray.getDimensionPixelOffset(R.styleable.JumpAdProgressBar_r_progress_width, (int) strokeWidth);
        adTime = typedArray.getInteger(R.styleable.JumpAdProgressBar_r_jump_time, adTime);
        typedArray.recycle();
    }

    private void init() {
        mDensity = getResources().getDisplayMetrics().density;

        mProgressColor = Color.WHITE;
        mBackColor = Color.parseColor("#474430");
        textColor = Color.WHITE;

        if (!isInEditMode()) {
            mProgressColor = SkinHelper.getSkin().getThemeSubColor();
        }

        //drawText = "跳过";

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(14 * getResources().getDisplayMetrics().density);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);

        strokeWidth = mDensity * 4;
        mPaint.setStrokeWidth(strokeWidth);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });
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
            height = (int) (mDensity * 60);
        }
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
            width = (int) (mDensity * 60);
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
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(strokeWidth);
        canvas.drawArc(mRectF, 0f, 360f, false, mPaint);

        mPaint.setColor(mProgressColor);
        mPaint.setStrokeWidth(strokeWidth + 1.1f);
        canvas.drawArc(mRectF, -90, sweepAngle, false, mPaint);

        if (!TextUtils.isEmpty(drawText)) {
            int width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            int height = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
            float textWidth = mPaint.measureText(drawText, 0, drawText.length());
            float textHeight = mPaint.descent() - mPaint.ascent();
            mPaint.setColor(textColor);
            mPaint.setStrokeWidth(1);
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawText(drawText, getPaddingLeft() + width / 2 - textWidth / 2,
                    getPaddingTop() + height / 2 + textHeight / 2 - mPaint.descent(), mPaint);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //start();
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
                    sweepAngle = (int) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
            mAnimator.setDuration(adTime);
            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (mOnJumpListener != null) {
                        mOnJumpListener.onJump();
                    }
                    L.e("call: onAnimationEnd([animation])-> ");
                }
            });
//            mAnimator.setRepeatCount(ValueAnimator.INFINITE);
//            mAnimator.setRepeatMode(ValueAnimator.RESTART);
        }
        if (!mAnimator.isStarted()) {
            mAnimator.start();
        }
    }

    public void setOnJumpListener(OnJumpListener onJumpListener) {
        mOnJumpListener = onJumpListener;
    }

    public void stop() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
    }

    public interface OnJumpListener {
        void onJump();
    }
}
