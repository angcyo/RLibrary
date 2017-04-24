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
        mTextSize = RApplication.getApp().getResources().getDisplayMetrics().density * 13;
    }

    public RTextPaint(float textSize) {
        mTextSize = textSize;
    }

    public static RTextPaint instance() {
        return Holder.instance;
    }

    /**
     * 获取文本的高度
     */
    public static double getTextHeight(Paint paint) {
        Paint.FontMetricsInt fontMetricsInt = paint.getFontMetricsInt();
        double height = Math.ceil(fontMetricsInt.descent - fontMetricsInt.ascent) + 2;
        return height;
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

    private void initPaint() {
        if (mTextPaint == null) {
            mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        }
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
    }

    public void setTextSize(float textSize) {
        mTextSize = textSize;
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
    }

    private static class Holder {
        static RTextPaint instance = new RTextPaint();
    }
}
