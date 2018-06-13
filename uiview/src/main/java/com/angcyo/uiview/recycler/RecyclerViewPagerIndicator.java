package com.angcyo.uiview.recycler;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.angcyo.uiview.draw.RDrawIndicator;
import com.angcyo.uiview.widget.viewpager.RViewPager;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：简单的圈圈指示器
 * 创建人员：Robi
 * 创建时间：2017/01/03 14:31
 * 修改人员：Robi
 * 修改时间：2017/01/03 14:31
 * 修改备注：
 * Version: 1.0.0
 */
public class RecyclerViewPagerIndicator extends View {

    RDrawIndicator mRDrawIndicator;

    public RecyclerViewPagerIndicator(Context context) {
        this(context, null);
    }

    public RecyclerViewPagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRDrawIndicator = new RDrawIndicator(this, attrs);
    }

    public void setupRecyclerViewPager(RecyclerViewPager recyclerViewPager) {
        mRDrawIndicator.setupRecyclerViewPager(recyclerViewPager);
    }

    public void setupRLoopRecyclerView(RLoopRecyclerView recyclerView) {
        mRDrawIndicator.setupRLoopRecyclerView(recyclerView);
    }

    public void setCurrentPager(int index) {
        mRDrawIndicator.setCurrentPager(index);
    }

    public void setUpUIViewPager(RViewPager pager, int pagerCount) {
        mRDrawIndicator.setUpUIViewPager(pager, pagerCount);
    }

    public void initPagerCount(int pagerCount) {
        mRDrawIndicator.initPagerCount(pagerCount);
    }

    public void setFocusColor(int focusColor) {
        mRDrawIndicator.setFocusColor(focusColor);
    }

    public void setDefaultColor(int defaultColor) {
        mRDrawIndicator.setDefaultColor(defaultColor);
    }

    /**
     * 圆圈的直径
     */
    public void setCircleSize(float circleSize) {
        mRDrawIndicator.setCircleSize(circleSize);
    }

    /**
     * 圈与圈之间的间隙
     */
    public void setCircleSpace(float circleSpace) {
        mRDrawIndicator.setCircleSpace(circleSpace);
    }

    public void setupRExLoopRecyclerView(RExLoopRecyclerView recyclerView) {
        mRDrawIndicator.setupRExLoopRecyclerView(recyclerView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mRDrawIndicator.getIndicatorWidth(), mRDrawIndicator.getIndicatorHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mRDrawIndicator.onDraw(canvas);
    }
}
