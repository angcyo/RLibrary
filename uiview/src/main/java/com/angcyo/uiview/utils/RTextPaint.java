package com.angcyo.uiview.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.text.TextUtils;

import com.angcyo.uiview.RApplication;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：用来绘制文本的画笔, 坐标自动从文本的左下角开始计算
 * <p>
 * 默认的笔,绘制坐标是文本的左下角.
 * 这个笔, 绘制坐标是文本的左上角.
 * <p>
 * 创建人员：Robi
 * 创建时间：2017/04/11 17:03
 * 修改人员：Robi
 * 修改时间：2017/04/11 17:03
 * 修改备注：
 * Version: 1.0.0
 */
public class RTextPaint {
    final Rect tempRect = new Rect();
    TextPaint mTextPaint;
    float mTextSize;
    int mTextColor = Color.WHITE;

    public RTextPaint() {
        this(13);
    }

    public RTextPaint(TextPaint textPaint) {
        mTextPaint = new TextPaint(textPaint);
    }

    public RTextPaint(float textSize) {
        mTextSize = RApplication.getApp().getResources().getDisplayMetrics().density * textSize;
    }

    public static RTextPaint instance() {
        return Holder.instance;
    }

    /**
     * 获取文本的高度
     */
    public static double getTextHeight(Paint paint) {
        //Paint.FontMetricsInt fontMetricsInt = paint.getFontMetricsInt();
        //结果和fontMetricsInt.descent - fontMetricsInt.ascent是相同的
        int height = paint.getFontMetricsInt(null);
        //double height = Math.ceil(fontMetricsInt.descent - fontMetricsInt.ascent);//+ 2;
        return height;
    }

    public float getTextHeight() {
        return (float) getTextHeight(mTextPaint);
    }

    public float getTextWidth(String text) {
        initPaint();
        return mTextPaint.measureText(text);
    }

    public Rect getTextBounds(String text) {
        tempRect.set(0, 0, 0, 0);
        if (TextUtils.isEmpty(text)) {
            return tempRect;
        }
        initPaint();
        mTextPaint.getTextBounds(text, 0, text.length(), tempRect);
        return tempRect;
    }

    /**
     * @param x 字符左下小角坐标开始
     * @param y 从字符左下角开始的坐标
     */
    public void drawText(Canvas canvas, String text, float x, float y) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        initPaint();
        Rect bounds = getTextBounds(text);
        canvas.drawText(text, x, y + bounds.height(), mTextPaint);
    }

    public void drawOriginText(Canvas canvas, String text, float x, float y) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        initPaint();
        canvas.drawText(text, x, y - mTextPaint.descent(), mTextPaint);
    }

    private void initPaint() {
        if (mTextPaint == null) {
            mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            mTextPaint.density = RApplication.getApp().getResources().getDisplayMetrics().density;
            mTextPaint.setTextSize(mTextSize);
            mTextPaint.setColor(mTextColor);
        }
    }

    public void setTextSize(float textSize) {
        mTextSize = textSize;
        if (mTextPaint != null) {
            mTextPaint.setTextSize(mTextSize);
        }
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
        if (mTextPaint != null) {
            mTextPaint.setColor(mTextColor);
        }
    }

    private static class Holder {
        static RTextPaint instance = new RTextPaint();
    }
}
