package com.angcyo.uiview.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import com.angcyo.uiview.skin.SkinHelper;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：60秒倒计时的按钮
 * 创建人员：Robi
 * 创建时间：2016/10/20 16:52
 * 修改人员：Robi
 * 修改时间：2016/10/20 16:52
 * 修改备注：
 * Version: 1.0.0
 */
public class VerifyButton2 extends Button implements View.OnClickListener, Runnable {

    public static final int DEFAULT_COUNT = 60;

    OnClickListener mOnClickListener;
    boolean isCountDownStart = false;
    int countDown = DEFAULT_COUNT;
    String oldText = "验证";

    int maxWidth = 0;

    public VerifyButton2(Context context) {
        super(context);
    }

    public VerifyButton2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        maxWidth = Math.max(measuredWidth, maxWidth);
        setMeasuredDimension(maxWidth, getMeasuredHeight());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) {
            setTextColor(Color.BLACK);
        } else {
            setTextColor(SkinHelper.getSkin().getThemeSubColor());
        }
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mOnClickListener = l;
        oldText = getText().toString();
        super.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (isCountDownStart) {
            return;
        }
        if (mOnClickListener != null) {
            mOnClickListener.onClick(this);
        }
    }

    private void startCountDown() {
        isCountDownStart = true;
        countDown--;
        setEnabled(false);
        postDelayed(this, 1000);
    }

    public void endCountDown() {
        isCountDownStart = false;
        countDown = DEFAULT_COUNT;
        setEnabled(true);
        setText(oldText);
        removeCallbacks(this);
    }

    @Override
    public void run() {
        setText(countDown + "s");
        setGravity(Gravity.CENTER);

        if (countDown <= 0) {
            endCountDown();
        } else {
            startCountDown();
        }

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        endCountDown();
    }
}
