package com.angcyo.uiview.viewgroup;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.angcyo.uiview.R;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/05/05 16:29
 * 修改人员：Robi
 * 修改时间：2017/05/05 16:29
 * 修改备注：
 * Version: 1.0.0
 */
public class RRelativeLayout extends RelativeLayout {

    onInterceptTouchListener mOnInterceptTouchListener;
    private Drawable mBackgroundDrawable;
    private GestureDetectorCompat mGestureDetectorCompat;

    private RDrawLine mDrawLine;
    private boolean showNoEnableMark = false;

    public RRelativeLayout(Context context) {
        this(context, null);
    }

    public RRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RRelativeLayout);
        mBackgroundDrawable = typedArray.getDrawable(R.styleable.RRelativeLayout_r_background);

        mDrawLine = new RDrawLine(this);

        mDrawLine.drawLine = typedArray.getInt(R.styleable.RRelativeLayout_r_draw_line, mDrawLine.drawLine);
        mDrawLine.drawLineOffsetLeft = typedArray.getDimensionPixelOffset(R.styleable.RRelativeLayout_r_draw_line_offset_left, mDrawLine.drawLineOffsetLeft);
        mDrawLine.drawLineOffsetRight = typedArray.getDimensionPixelOffset(R.styleable.RRelativeLayout_r_draw_line_offset_right, mDrawLine.drawLineOffsetRight);
        mDrawLine.drawLineColor = typedArray.getColor(R.styleable.RRelativeLayout_r_draw_line_color, ContextCompat.getColor(getContext(), R.color.base_chat_bg_color));
        mDrawLine.drawLineWidth = typedArray.getDimensionPixelOffset(R.styleable.RRelativeLayout_r_draw_line_width, (int) mDrawLine.drawLineWidth);
        mDrawLine.isDashLine = typedArray.getBoolean(R.styleable.RRelativeLayout_r_draw_dash_line, mDrawLine.isDashLine);

        typedArray.recycle();
        initLayout();
    }

    private void initLayout() {
        setWillNotDraw(false);
        mGestureDetectorCompat = new GestureDetectorCompat(getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        if (mOnInterceptTouchListener != null) {
                            return mOnInterceptTouchListener.onSingleTapUp(e);
                        }
                        return super.onSingleTapUp(e);
                    }
                });
    }

    @Override
    public void draw(Canvas canvas) {
        if (mBackgroundDrawable != null) {
            mBackgroundDrawable.setBounds(canvas.getClipBounds());
            mBackgroundDrawable.draw(canvas);
        }
        super.draw(canvas);
        mDrawLine.draw(canvas);
        if (!isEnabled() && showNoEnableMark) {
            canvas.drawColor(ContextCompat.getColor(getContext(), R.color.default_base_tran_dark2));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void setOnInterceptTouchListener(onInterceptTouchListener onInterceptTouchListener) {
        mOnInterceptTouchListener = onInterceptTouchListener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mGestureDetectorCompat.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void setRBackgroundDrawable(@ColorInt int color) {
        setRBackgroundDrawable(new ColorDrawable(color));
    }

    public void setRBackgroundDrawable(Drawable drawable) {
        mBackgroundDrawable = drawable;
    }

    public interface onInterceptTouchListener {
        boolean onSingleTapUp(MotionEvent e);
    }
}
