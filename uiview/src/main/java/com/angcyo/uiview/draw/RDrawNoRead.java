package com.angcyo.uiview.draw;

import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.angcyo.uiview.R;
import com.angcyo.uiview.kotlin.ExKt;
import com.angcyo.uiview.kotlin.ViewExKt;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：未读红点绘制
 * 创建人员：Robi
 * 创建时间：2018/04/19 09:58
 * 修改人员：Robi
 * 修改时间：2018/04/19 09:58
 * 修改备注：
 * Version: 1.0.0
 */
public class RDrawNoRead extends BaseDraw {

    public static final int LEFT = 0x01;
    public static final int TOP = 0x02;
    public static final int RIGHT = 0x04;
    public static final int BOTTOM = 0x08;
    public static final int CENTER_VERTICAL = 0x10;
    public static final int CENTER_HORIZONTAL = 0x20;
    public static final int CENTER = 0x30;

    /**
     * 是否显示 未读小红点
     */
    private boolean showNoRead = false;
    /**
     * 小红点半径
     */
    private float noReadRadius = 4 * density();
    private float noReadPaddingTop = 0 * density();
    private float noReadPaddingRight = 0 * density();

    private int noReadColor = Color.RED;

    private Paint mPaint;

    private int noreadGravity = TOP | RIGHT;

    public RDrawNoRead(View view, AttributeSet attr) {
        super(view, attr);
        initAttribute(attr);
    }

    @Override
    protected void initAttribute(AttributeSet attr) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attr, R.styleable.RDrawNoread);
        showNoRead = typedArray.getBoolean(R.styleable.RDrawNoread_r_show_noread, showNoRead);
        noReadRadius = typedArray.getDimensionPixelOffset(R.styleable.RDrawNoread_r_noread_radius, (int) noReadRadius);
        noreadGravity = typedArray.getInt(R.styleable.RDrawNoread_r_noread_gravity, noreadGravity);
        noReadPaddingRight = typedArray.getDimensionPixelOffset(R.styleable.RDrawNoread_r_noread_padding_right, (int) noReadPaddingRight);
        noReadPaddingTop = typedArray.getDimensionPixelOffset(R.styleable.RDrawNoread_r_noread_padding_top, (int) noReadPaddingTop);
        noReadColor = typedArray.getColor(R.styleable.RDrawNoread_r_noread_color, noReadColor);

        typedArray.recycle();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (showNoRead /*|| isInEditMode()*/) {
            //未读小红点
            if (mPaint == null) {
                mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            }
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(noReadColor);

            float cx = noReadRadius;
            float cy = noReadRadius;

            if (noreadGravity == CENTER) {
                cx = ViewExKt.getDrawCenterCx(mView) + noReadPaddingRight;
                cy = ViewExKt.getDrawCenterCy(mView) + noReadPaddingTop;
            } else {
                if (ExKt.have(noreadGravity, CENTER_HORIZONTAL)) {
                    cx = ViewExKt.getDrawCenterCx(mView) + noReadPaddingRight;
                }
                if (ExKt.have(noreadGravity, CENTER_VERTICAL)) {
                    cy = ViewExKt.getDrawCenterCy(mView) + noReadPaddingTop;
                }
                if (ExKt.have(noreadGravity, LEFT)) {
                    cx = noReadRadius + noReadPaddingRight;
                }
                if (ExKt.have(noreadGravity, RIGHT)) {
                    cx = getViewWidth() - noReadRadius + noReadPaddingRight;
                }
                if (ExKt.have(noreadGravity, TOP)) {
                    cy = noReadRadius + noReadPaddingTop;
                }
                if (ExKt.have(noreadGravity, BOTTOM)) {
                    cy = getViewHeight() - noReadRadius + noReadPaddingTop;
                }
            }

            canvas.drawCircle(cx, cy, noReadRadius, mPaint);
        }
    }

    /**
     * 默认显示在右上角
     */
    public void setShowNoRead(boolean showNoRead) {
        this.showNoRead = showNoRead;
        postInvalidate();
    }

    /**
     * 半径大小
     */
    public void setNoReadRadius(float noReadRadius) {
        this.noReadRadius = noReadRadius;
        postInvalidate();
    }

    /**
     * 右上角的padding
     */
    public void setNoReadPaddingTop(float noReadPaddingTop) {
        this.noReadPaddingTop = noReadPaddingTop;
        postInvalidate();
    }

    /**
     * 右上角的padding
     */
    public void setNoReadPaddingRight(float noReadPaddingRight) {
        this.noReadPaddingRight = noReadPaddingRight;
        postInvalidate();
    }

    public void setNoReadColor(int noReadColor) {
        this.noReadColor = noReadColor;
        postInvalidate();
    }

    public void setNoreadGravity(int noreadGravity) {
        this.noreadGravity = noreadGravity;
        postInvalidate();
    }
}
