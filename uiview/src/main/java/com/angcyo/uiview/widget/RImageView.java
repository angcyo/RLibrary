package com.angcyo.uiview.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：自带点击效果的ImageView
 * 创建人员：Robi
 * 创建时间：2017/03/10 11:45
 * 修改人员：Robi
 * 修改时间：2017/03/10 11:45
 * 修改备注：
 * Version: 1.0.0
 */
public class RImageView extends AppCompatImageView {

    /**
     * 播放按钮图片
     */
    Drawable mPlayDrawable;
    private boolean isAttachedToWindow;

    public RImageView(Context context) {
        super(context);
    }

    public RImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setColor();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                clearColor();
                break;
        }

        return super.onTouchEvent(event);
    }

    public void setColor(@ColorInt int color) {
        Drawable drawable = getDrawable();
        if (drawable != null) {
            drawable.mutate().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        }
    }

    /**
     * 设置混合颜色
     */
    public void setColor() {
        setColor(Color.GRAY);
    }

    public void clearColor() {
        Drawable drawableUp = getDrawable();
        if (drawableUp != null) {
            drawableUp.mutate().clearColorFilter();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clearColor();
        //setImageDrawable(null);
        isAttachedToWindow = false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedToWindow = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mPlayDrawable != null) {
            int height = getMeasuredHeight() / 2;
            int width = getMeasuredWidth() / 2;
            int w = mPlayDrawable.getIntrinsicWidth() / 2;
            int h = mPlayDrawable.getIntrinsicHeight() / 2;
            mPlayDrawable.setBounds(width - w, height - h, width + w, height + h);
            mPlayDrawable.draw(canvas);
        }
    }

    public void setPlayDrawable(Drawable playDrawable) {
        mPlayDrawable = playDrawable;
        if (isAttachedToWindow) {
            postInvalidate();
        }
    }

    public void setPlayDrawable(@DrawableRes int res) {
        setPlayDrawable(ContextCompat.getDrawable(getContext(), res));
    }
}
