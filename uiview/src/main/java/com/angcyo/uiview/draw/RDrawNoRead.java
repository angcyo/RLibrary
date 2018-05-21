package com.angcyo.uiview.draw;

import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.angcyo.uiview.R;

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
    /**
     * 是否显示 未读小红点
     */
    private boolean showNoRead = false;
    /**
     * 小红点半径
     */
    private float noReadRadius = 4 * density();
    private float noReadPaddingTop = 2 * density();
    private float noReadPaddingRight = 2 * density();

    private int noReadColor = Color.RED;

    private Paint mPaint;

    public RDrawNoRead(View view, AttributeSet attr) {
        super(view, attr);
        initAttribute(attr);
    }

    @Override
    protected void initAttribute(AttributeSet attr) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attr, R.styleable.RDrawNoread);
        showNoRead = typedArray.getBoolean(R.styleable.RDrawNoread_r_show_noread, showNoRead);
        noReadRadius = typedArray.getDimensionPixelOffset(R.styleable.RDrawNoread_r_noread_radius, (int) noReadRadius);
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
            //默认位置在右上角
            canvas.drawCircle(mView.getMeasuredWidth() - noReadPaddingRight - noReadRadius, noReadPaddingTop + noReadRadius, noReadRadius, mPaint);
        }
    }

    /**
     * 默认显示在右上角
     */
    public void setShowNoRead(boolean showNoRead) {
        this.showNoRead = showNoRead;
        mView.postInvalidate();
    }

    /**
     * 半径大小
     */
    public void setNoReadRadius(float noReadRadius) {
        this.noReadRadius = noReadRadius;
        mView.postInvalidate();
    }

    /**
     * 右上角的padding
     */
    public void setNoReadPaddingTop(float noReadPaddingTop) {
        this.noReadPaddingTop = noReadPaddingTop;
        mView.postInvalidate();
    }

    /**
     * 右上角的padding
     */
    public void setNoReadPaddingRight(float noReadPaddingRight) {
        this.noReadPaddingRight = noReadPaddingRight;
        mView.postInvalidate();
    }

    public void setNoReadColor(int noReadColor) {
        this.noReadColor = noReadColor;
        mView.postInvalidate();
    }
}
