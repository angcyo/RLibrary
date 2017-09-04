package com.angcyo.uiview.recycler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import com.angcyo.uiview.widget.viewpager.RViewPager;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/01/03 14:31
 * 修改人员：Robi
 * 修改时间：2017/01/03 14:31
 * 修改备注：
 * Version: 1.0.0
 */
public class RecyclerViewPagerIndicator extends View {

    float mCircleSize = 6;//px
    float mCircleSpace = 5;//px
    @ColorInt
    int focusColor = Color.parseColor("#333333"), defaultColor = Color.parseColor("#999999");
    Paint mPaint;
    int mPagerCount = 0, mCurrentPager = 0;

    public RecyclerViewPagerIndicator(Context context) {
        this(context, null);
    }

    public RecyclerViewPagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        final float density = getResources().getDisplayMetrics().density;
        mCircleSize *= density;
        mCircleSpace *= density;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);

        if (isInEditMode()) {
            initPagerCount(4);
            mCurrentPager = 1;
        }
    }

    public void setupRecyclerViewPager(RecyclerViewPager recyclerViewPager) {
        recyclerViewPager.addOnViewPagerListener(new RecyclerViewPager.OnViewPagerListener() {
            @Override
            public void onViewPager(int index) {
                setCurrentPager(index);
            }
        });
        initPagerCount(recyclerViewPager.getPagerCount());
    }

    public void setCurrentPager(int index) {
        mCurrentPager = index;
        postInvalidate();
    }

    public void setUpUIViewPager(RViewPager pager,int pagerCount) {
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setCurrentPager(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        initPagerCount(pagerCount);
    }

    public void initPagerCount(int pagerCount) {
        mPagerCount = pagerCount;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width, height;
        width = (int) (Math.max(0, mPagerCount - 1) * mCircleSpace + mCircleSize * mPagerCount) + getPaddingLeft() + getPaddingRight();
        height = (int) (mCircleSize + getPaddingTop() + getPaddingBottom());

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final float radius = mCircleSize / 2;
        for (int i = 0; i < mPagerCount; i++) {
            if (i == mCurrentPager) {
                mPaint.setColor(focusColor);
            } else {
                mPaint.setColor(defaultColor);
            }
            canvas.drawCircle(getPaddingLeft() + i * (mCircleSpace + mCircleSize) + radius,
                    getPaddingTop() + radius, radius, mPaint);
        }
    }
}
