package com.angcyo.uiview.container;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.angcyo.uiview.R;
import com.angcyo.uiview.utils.ScreenUtil;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/07/11 11:46
 * 修改人员：Robi
 * 修改时间：2017/07/11 11:46
 * 修改备注：
 * Version: 1.0.0
 */
public class ContentLayout extends FrameLayout {

    /**
     * 允许的最大高度, 如果为-2px,那么就是屏幕高度的一半, 如果是-3px,那么就是屏幕高度的三分之, 以此内推
     */
    private int maxHeight = -1;
    private int minHeight = -1;

    public ContentLayout(Context context) {
        this(context, null);
    }

    public ContentLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContentLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ContentLayout);
        maxHeight = typedArray.getDimensionPixelOffset(R.styleable.ContentLayout_r_max_height, -1);
        minHeight = typedArray.getDimensionPixelOffset(R.styleable.ContentLayout_r_min_height, -1);
        resetMaxHeight();
        typedArray.recycle();

        initLayout();
    }

    private void initLayout() {

    }

    private void resetMaxHeight() {
        if (maxHeight < -1) {
            int num = Math.abs(maxHeight);
            maxHeight = ScreenUtil.screenHeight / num;
        }
        if (minHeight < -1) {
            int num = Math.abs(minHeight);
            minHeight = ScreenUtil.screenHeight / num;
        }
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        resetMaxHeight();
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (maxHeight > 0) {
            if (minHeight > 0) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(minHeight, MeasureSpec.AT_MOST));
            } else {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST));
            }
            int measuredHeight = getMeasuredHeight();
            if (measuredHeight > maxHeight) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(maxHeight, heightMode));
            } else {
                setMeasuredDimension(getMeasuredWidth(), measuredHeight);
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }
}
