package com.angcyo.uiview.draw;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/07/20 16:49
 * 修改人员：Robi
 * 修改时间：2018/07/20 16:49
 * 修改备注：
 * Version: 1.0.0
 */
public class ShapeSpan extends DynamicDrawableSpan {
    int size;
    RectF mRectF;
    int bgColor;
    int textColor = -1;
    float roundSize;

    int paddingLeft = 0;
    int paddingRight = 0;
    int paddingTop = 0;
    int paddingBottom = 0;

    int marginLeft = 0;
    int marginTop = 0;
    int marginRight = 0;
    int marginBottom = 0;

    public ShapeSpan(int bgColor, float roundSize) {
        this.bgColor = bgColor;
        this.roundSize = roundSize;
    }

    @Override
    public Drawable getDrawable() {
        return null;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        size = (int) paint.measureText(text.subSequence(start, end).toString()) + paddingLeft + paddingRight + marginLeft + marginRight;
        return size;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        int oldColor = paint.getColor();
        paint.setColor(bgColor);
        if (mRectF == null) {
            mRectF = new RectF();
        }
        mRectF.set(x + marginLeft, top - marginTop, x + size - marginRight, bottom + marginBottom);
        canvas.drawRoundRect(mRectF, roundSize, roundSize, paint);
        if (textColor == -1) {
            paint.setColor(oldColor);
        } else {
            paint.setColor(textColor);
        }
        canvas.drawText(text, start, end, x + marginLeft + paddingLeft, y, paint);
    }

    public void setPaddingLeft(int paddingLeft) {
        this.paddingLeft = paddingLeft;
    }

    public void setPaddingRight(int paddingRight) {
        this.paddingRight = paddingRight;
    }

    public void setPaddingTop(int paddingTop) {
        this.paddingTop = paddingTop;
    }

    public void setPaddingBottom(int paddingBottom) {
        this.paddingBottom = paddingBottom;
    }

    public void setMarginLeft(int marginLeft) {
        this.marginLeft = marginLeft;
    }

    public void setMarginTop(int marginTop) {
        this.marginTop = marginTop;
    }

    public void setMarginRight(int marginRight) {
        this.marginRight = marginRight;
    }

    public void setMarginBottom(int marginBottom) {
        this.marginBottom = marginBottom;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setRoundSize(float roundSize) {
        this.roundSize = roundSize;
    }
}
