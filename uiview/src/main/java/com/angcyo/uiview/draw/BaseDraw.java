package com.angcyo.uiview.draw;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/04/19 09:59
 * 修改人员：Robi
 * 修改时间：2018/04/19 09:59
 * 修改备注：
 * Version: 1.0.0
 */
public abstract class BaseDraw {
    protected View mView;
    protected Paint mBasePaint;

    /**
     * 请注意, 需要在继承类 中手动调用 {@link #initAttribute(AttributeSet)} 方法
     */
    public BaseDraw(View view, AttributeSet attr) {
        mView = view;
        mBasePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBasePaint.setFilterBitmap(true);
        mBasePaint.setStyle(Paint.Style.FILL);
        mBasePaint.setTextSize(12 * density());

        //initAttribute(attr);//父类当中调用此方法初始化子类的成员, 会导致被覆盖的BUG
        //所以此方法, 请在子类当中触发
    }

    protected float density() {
        return getContext().getResources().getDisplayMetrics().density;
    }

    protected Context getContext() {
        return mView.getContext();
    }

    protected Resources getResources() {
        return getContext().getResources();
    }

    protected boolean isInEditMode() {
        return mView.isInEditMode();
    }

    protected void postInvalidate() {
        mView.postInvalidate();
    }

    protected int getPaddingBottom() {
        return mView.getPaddingBottom();
    }

    protected int getPaddingRight() {
        return mView.getPaddingRight();
    }

    protected int getPaddingLeft() {
        return mView.getPaddingLeft();
    }

    protected int getPaddingTop() {
        return mView.getPaddingTop();
    }

    protected int getViewDrawWidth() {
        return getViewWidth() - getPaddingLeft() - getPaddingRight();
    }

    protected int getViewDrawHeight() {
        return getViewHeight() - getPaddingTop() - getPaddingBottom();
    }

    protected int getViewWidth() {
        return mView.getMeasuredWidth();
    }

    protected int getViewHeight() {
        return mView.getMeasuredHeight();
    }

    protected void requestLayout() {
        mView.requestLayout();
    }

    public void onDraw(@NonNull Canvas canvas) {

    }

    protected TypedArray obtainStyledAttributes(AttributeSet set, int[] attrs) {
        return getContext().obtainStyledAttributes(set, attrs);
    }

    protected abstract void initAttribute(AttributeSet attr);
}
