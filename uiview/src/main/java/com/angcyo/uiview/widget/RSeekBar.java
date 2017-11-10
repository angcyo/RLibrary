package com.angcyo.uiview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.angcyo.uiview.R;
import com.angcyo.uiview.skin.SkinHelper;

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
    public static final int THUMB_DEFAULT = 0;//默认 (圆角矩形)

    /**
     * 轨道背景颜色
     */
    int mTrackBgColor;

    /**
     * 轨道的颜色
     */
    int mTrackColor;
    int mTrackHeight;// 当浮子是 圆形的时候, 请使用这个变量控制 半径的大小
    /**
     * 轨道的圆角
     */
    int mTrackRadius;

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
    /**
     * 第二进度
     */
    int secondProgress = 0;

    int secondProgressColor;

    int maxProgress = 100;
    boolean isTouchDown = false;
    Rect clipBounds = new Rect();

    Set<OnProgressChangeListener> mOnProgressChangeListeners = new HashSet<>();
    private float mDensity;
    private RectF mProgressRectF = new RectF();
    private RectF mTrackRectF = new RectF();

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
        secondProgressColor = SkinHelper.getTranColor(mTrackColor, 0x80);

        secondProgressColor = typedArray.getColor(R.styleable.RSeekBar_r_second_progress_color, secondProgressColor);

        mThumbColor = typedArray.getColor(R.styleable.RSeekBar_r_thumb_color, mThumbColor);

        mTrackHeight = typedArray.getDimensionPixelOffset(R.styleable.RSeekBar_r_track_height, mTrackHeight);
        mThumbHeight = typedArray.getDimensionPixelOffset(R.styleable.RSeekBar_r_thumb_height, mThumbHeight);
        mThumbWidth = typedArray.getDimensionPixelOffset(R.styleable.RSeekBar_r_thumb_width, mThumbWidth);
        mThumbRoundSize = typedArray.getDimensionPixelOffset(R.styleable.RSeekBar_r_thumb_round_size, mThumbRoundSize);
        mTrackRadius = typedArray.getDimensionPixelOffset(R.styleable.RSeekBar_r_track_round_size, 0);
        curProgress = typedArray.getInteger(R.styleable.RSeekBar_r_cur_progress, curProgress);
        secondProgress = typedArray.getInteger(R.styleable.RSeekBar_r_second_progress, secondProgress);
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
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (curProgress != 0) {
            notifyListenerProgress(false);
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
        canvas.save();
        mPaint.setColor(mTrackBgColor);
        int trackLeft = getPaddingLeft();
        int trackTop = (getMeasuredHeight() - mTrackHeight) / 2;
        int trackRight = getMeasuredWidth() - getPaddingRight();
        int trackBottom = getMeasuredHeight() / 2 + mTrackHeight / 2;
        mTrackRectF.set(trackLeft, trackTop, trackRight, trackBottom);
        canvas.drawRoundRect(mTrackRectF, mTrackRadius, mTrackRadius, mPaint);
        canvas.restore();

        //第二进度
        if (secondProgress > 0) {
            canvas.save();
            mPaint.setColor(secondProgressColor);
            if (thumbType == THUMB_DEFAULT) {
                mTrackRectF.set(trackLeft, trackTop, trackLeft + secondProgress / getMaxProgress() * getMaxLength() + mThumbWidth / 2, trackBottom);
            } else if (thumbType == THUMB_CIRCLE) {
                mTrackRectF.set(trackLeft, trackTop, trackLeft + secondProgress / getMaxProgress() * getMaxLength() + mThumbRadius / 2, trackBottom);
            }
            canvas.drawRoundRect(mTrackRectF, mTrackRadius, mTrackRadius, mPaint);
            canvas.restore();
        }

        //绘制轨道
        canvas.save();
        mPaint.setColor(mTrackColor);
        if (thumbType == THUMB_DEFAULT) {
            mTrackRectF.set(trackLeft, trackTop, trackLeft + curProgress / getMaxProgress() * getMaxLength() + mThumbWidth / 2, trackBottom);
        } else if (thumbType == THUMB_CIRCLE) {
            mTrackRectF.set(trackLeft, trackTop, trackLeft + curProgress / getMaxProgress() * getMaxLength() + mThumbRadius / 2, trackBottom);
        }
        canvas.drawRoundRect(mTrackRectF, mTrackRadius, mTrackRadius, mPaint);
        canvas.restore();

        //绘制浮子
        updateProgress();
        //浮子外圈, 在touch状态下绘制
        if (isTouchDown && thumbType == THUMB_CIRCLE) {
            canvas.save();
            canvas.getClipBounds(clipBounds);
            clipBounds.inset(-mThumbRoundSize * 2, -mThumbRoundSize * 2);
            canvas.clipRect(clipBounds, Region.Op.REPLACE);
            mPaint.setColor(secondProgressColor);
            canvas.drawCircle(mProgressRectF.centerX(), mProgressRectF.centerY(),
                    mThumbRoundSize + mThumbRoundSize / 2, mPaint);
            canvas.restore();
        }
        //浮子
        canvas.save();
        mPaint.setColor(mThumbColor);
        if (thumbType == THUMB_DEFAULT) {
            canvas.drawRoundRect(mProgressRectF, mThumbRoundSize, mThumbRoundSize, mPaint);
        } else if (thumbType == THUMB_CIRCLE) {
            canvas.drawCircle(mProgressRectF.centerX(), mProgressRectF.centerY(), mThumbHeight / 2, mPaint);
        }
        canvas.restore();
    }

    /**
     * 允许移动的最大距离
     */
    private int getMaxLength() {
        return getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - mThumbWidth;
    }

    private void updateProgress() {
        int left = (int) (getPaddingLeft() + curProgress / getMaxProgress() * getMaxLength());
        mProgressRectF.set(left, (getMeasuredHeight() - mThumbHeight) / 2,
                left + mThumbWidth, getMeasuredHeight() / 2 + mThumbHeight / 2);
    }

    public float getMaxProgress() {
        return maxProgress * 1f;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return super.onTouchEvent(event);
        }

        int action = MotionEventCompat.getActionMasked(event);
        float eventX = event.getX();
        //L.e("call: onTouchEvent([event])-> " + action + " x:" + eventX);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //L.e("call: onTouchEvent([event])-> DOWN:" + " x:" + eventX);
                isTouchDown = true;
                notifyListenerStartTouch();
                calcProgress(eventX);
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                calcProgress(eventX);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isTouchDown = false;
                notifyListenerStopTouch();
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
        this.curProgress = ensureProgress((int) (x / getMaxLength() * maxProgress));
        if (old != curProgress) {
            notifyListenerProgress(true);
        }
        postInvalidate();
    }

    private void notifyListenerProgress(boolean fromTouch) {
        for (OnProgressChangeListener listener : mOnProgressChangeListeners) {
            listener.onProgress(curProgress, fromTouch);
        }
    }

    private void notifyListenerStartTouch() {
        for (OnProgressChangeListener listener : mOnProgressChangeListeners) {
            listener.onStartTouch();
        }
    }

    private void notifyListenerStopTouch() {
        for (OnProgressChangeListener listener : mOnProgressChangeListeners) {
            listener.onStopTouch();
        }
    }

    private int ensureProgress(int progress) {
        return Math.max(0, Math.min(maxProgress, progress));
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
        this.curProgress = Math.min(curProgress, maxProgress);
        postInvalidate();
        notifyListenerProgress(false);
    }

    public void setSecondProgress(int secondProgress) {
        this.secondProgress = Math.min(secondProgress, maxProgress);
        postInvalidate();
    }

    public void setTrackBgColor(int trackBgColor) {
        mTrackBgColor = trackBgColor;
    }

    public void setTrackColor(int trackColor) {
        mTrackColor = trackColor;
    }

    public void setThumbColor(int thumbColor) {
        mThumbColor = thumbColor;
    }

    public void setSecondProgressColor(int secondProgressColor) {
        this.secondProgressColor = secondProgressColor;
    }

    public interface OnProgressChangeListener {
        void onProgress(int progress, boolean fromTouch);

        void onStartTouch();

        void onStopTouch();
    }

}
