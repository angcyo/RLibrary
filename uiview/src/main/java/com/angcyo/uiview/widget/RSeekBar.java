package com.angcyo.uiview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.angcyo.uiview.R;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/05/05 16:32
 * 修改人员：Robi
 * 修改时间：2017/05/05 16:32
 * 修改备注：
 * Version: 1.0.0
 */
public class RSeekBar extends View {

    /**
     * 浮子类型
     */
    public static final int THUMB_CIRCLE = 1;//圆
    public static final int THUMB_DEFAULT = 0;//默认

    /**
     * 轨道背景颜色
     */
    int mTrackBgColor;

    /**
     * 轨道的颜色
     */
    int mTrackColor;
    int mTrackHeight;

    /**
     * 浮子的颜色
     */
    int mThumbColor;
    int mThumbHeight;
    int mThumbWidth;
    //在thumbType==THUMB_CIRCLE 时
    int mThumbRadius;
    /**
     * 浮子圆角大小
     */
    int mThumbRoundSize;
    Paint mPaint;
    /**
     * 当前进度(0-100)
     */
    int curProgress = 0;
    Set<OnProgressChangeListener> mOnProgressChangeListeners = new HashSet<>();
    private float mDensity;
    private RectF mRectF;

    private int thumbType = THUMB_DEFAULT;

    public RSeekBar(Context context) {
        this(context, null);
    }

    public RSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//
//        if (isInEditMode()) {
//            return;
//        }

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RSeekBar);
        mDensity = getResources().getDisplayMetrics().density;

        mTrackBgColor = Color.parseColor("#cccccc");

        mTrackColor = Color.parseColor("#fdc775");
        mTrackHeight = (int) (2 * mDensity);

        mThumbColor = mTrackColor;
        mThumbHeight = (int) (10 * mDensity);
        mThumbWidth = (int) (20 * mDensity);
        mThumbRoundSize = (int) (10 * mDensity);

        mTrackBgColor = typedArray.getColor(R.styleable.RSeekBar_r_track_bg_color, mTrackBgColor);
        mTrackColor = typedArray.getColor(R.styleable.RSeekBar_r_track_color, mTrackColor);
        mThumbColor = typedArray.getColor(R.styleable.RSeekBar_r_thumb_color, mThumbColor);

        mTrackHeight = typedArray.getDimensionPixelOffset(R.styleable.RSeekBar_r_track_height, mTrackHeight);
        mThumbHeight = typedArray.getDimensionPixelOffset(R.styleable.RSeekBar_r_thumb_height, mThumbHeight);
        mThumbWidth = typedArray.getDimensionPixelOffset(R.styleable.RSeekBar_r_thumb_width, mThumbWidth);
        mThumbRoundSize = typedArray.getDimensionPixelOffset(R.styleable.RSeekBar_r_thumb_round_size, mThumbRoundSize);
        curProgress = typedArray.getInteger(R.styleable.RSeekBar_r_cur_progress, curProgress);
        thumbType = typedArray.getInt(R.styleable.RSeekBar_r_thumb_type, THUMB_DEFAULT);
        curProgress = ensureProgress(curProgress);

        typedArray.recycle();

        if (thumbType == THUMB_CIRCLE) {
            mThumbRadius = Math.min(mThumbWidth, mThumbHeight);
            mThumbWidth = mThumbHeight = mThumbRadius;
        }

        initView();
    }

    private void initView() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mRectF = new RectF();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (curProgress != 0) {
            notifyListener();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
            widthSize = (int) (100 * mDensity + mThumbWidth) + getPaddingLeft() + getPaddingRight();
        }

        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            heightSize = Math.max(mThumbHeight, mTrackHeight) + getPaddingBottom() + getPaddingTop();
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        if (isInEditMode()) {
//            canvas.drawColor(Color.BLACK);
//            return;
//        }
        //绘制轨道背景
        mPaint.setColor(mTrackBgColor);
        int trackLeft = getPaddingLeft();
        int trackTop = (getMeasuredHeight() - mTrackHeight) / 2;
        int trackRight = getMeasuredWidth() - getPaddingRight();
        int trackBottom = getMeasuredHeight() / 2 + mTrackHeight / 2;
        canvas.drawRect(trackLeft, trackTop, trackRight, trackBottom, mPaint);

        //绘制轨道
        mPaint.setColor(mTrackColor);
        canvas.drawRect(trackLeft, trackTop,
                trackLeft + curProgress / 100f * getMaxLength(), trackBottom, mPaint);

        //绘制浮子
        mPaint.setColor(mThumbColor);
        updateProgress();
        if (thumbType == THUMB_DEFAULT) {
            canvas.drawRoundRect(mRectF, mThumbRoundSize, mThumbRoundSize, mPaint);
        } else if (thumbType == THUMB_CIRCLE) {
            canvas.drawCircle(mRectF.centerX(), mRectF.centerY(), mThumbHeight / 2, mPaint);
        }
    }

    /**
     * 允许移动的最大距离
     */
    private int getMaxLength() {
        return getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - mThumbWidth;
    }

    private void updateProgress() {
        int left = (int) (getPaddingLeft() + curProgress / 100f * getMaxLength());
        mRectF.set(left, (getMeasuredHeight() - mThumbHeight) / 2,
                left + mThumbWidth, getMeasuredHeight() / 2 + mThumbHeight / 2);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        float eventX = event.getX();
        //L.e("call: onTouchEvent([event])-> " + action + " x:" + eventX);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //L.e("call: onTouchEvent([event])-> DOWN:" + " x:" + eventX);
                calcProgress(eventX);
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                calcProgress(eventX);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return true;
    }

    /**
     * 根据touch坐标, 计算进度
     */
    private void calcProgress(float touchX) {
        float x = touchX - getPaddingLeft() - mThumbWidth / 2;
        int old = this.curProgress;
        this.curProgress = ensureProgress((int) (x / getMaxLength() * 100));
        if (old != curProgress) {
            notifyListener();
        }
        postInvalidate();
    }

    private void notifyListener() {
        for (OnProgressChangeListener listener : mOnProgressChangeListeners) {
            listener.onProgress(curProgress);
        }
    }

    private int ensureProgress(int progress) {
        return Math.max(0, Math.min(100, progress));
    }

    public void addOnProgressChangeListener(OnProgressChangeListener listener) {
        mOnProgressChangeListeners.add(listener);
    }

    public void removeOnProgressChangeListener(OnProgressChangeListener listener) {
        mOnProgressChangeListeners.remove(listener);
    }

    public int getCurProgress() {
        return curProgress;
    }

    public void setCurProgress(int curProgress) {
        this.curProgress = curProgress;
        postInvalidate();
        notifyListener();
    }

    public interface OnProgressChangeListener {
        void onProgress(int progress);
    }

}
