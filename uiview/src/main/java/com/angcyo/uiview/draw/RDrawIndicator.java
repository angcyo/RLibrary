package com.angcyo.uiview.draw;

import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.angcyo.uiview.R;
import com.angcyo.uiview.recycler.RExLoopRecyclerView;
import com.angcyo.uiview.recycler.RLoopRecyclerView;
import com.angcyo.uiview.recycler.RPagerSnapHelper;
import com.angcyo.uiview.recycler.RecyclerViewPager;
import com.angcyo.uiview.widget.viewpager.RViewPager;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：用来绘制 指示器, 现在支持简单的圆形
 * 创建人员：Robi
 * 创建时间：2018/06/12 17:28
 * 修改人员：Robi
 * 修改时间：2018/06/12 17:28
 * 修改备注：
 * Version: 1.0.0
 */
public class RDrawIndicator extends BaseDraw {

    public float mCircleSize = 6;//px
    public float mCircleSpace = 5;//px
    public int mPagerCount = 0, mCurrentPager = 0;
    public int offsetY = 0;//Y轴偏移, 根据权重自动修正
    public boolean showIndicator = true;
    @ColorInt
    int focusColor = Color.parseColor("#333333"), defaultColor = Color.parseColor("#999999");
    Paint mPaint;

    public RDrawIndicator(View view, AttributeSet attr) {
        super(view, attr);
        initAttribute(attr);
    }

    @Override
    protected void initAttribute(AttributeSet attr) {
        final float density = getResources().getDisplayMetrics().density;
        mCircleSize *= density;
        mCircleSpace *= density;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);

        TypedArray typedArray = getContext().obtainStyledAttributes(attr, R.styleable.RDrawIndicator);
        focusColor = typedArray.getColor(R.styleable.RDrawIndicator_r_focus_color, focusColor);
        defaultColor = typedArray.getColor(R.styleable.RDrawIndicator_r_default_color, defaultColor);

        mCircleSize = typedArray.getDimensionPixelOffset(R.styleable.RDrawIndicator_r_circle_size, (int) mCircleSize);
        mCircleSpace = typedArray.getDimensionPixelOffset(R.styleable.RDrawIndicator_r_circle_space, (int) mCircleSpace);
        offsetY = typedArray.getDimensionPixelOffset(R.styleable.RDrawIndicator_r_indicator_offset_y, offsetY);

        showIndicator = typedArray.getBoolean(R.styleable.RDrawIndicator_r_show_indicator, showIndicator);

        typedArray.recycle();
        if (isInEditMode()) {
            showIndicator = true;
            mCurrentPager = 1;
            initPagerCount(4);
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

    public void setupRLoopRecyclerView(RLoopRecyclerView recyclerView) {
        recyclerView.setOnPageListener(new RLoopRecyclerView.OnPageListener() {
            @Override
            public void onPageSelector(int position) {
                setCurrentPager(position);
            }
        });

        RLoopRecyclerView.LoopAdapter adapter = recyclerView.getAdapter();
        if (adapter != null) {
            initPagerCount(adapter.getItemRawCount());
        }
    }

    public void setupRExLoopRecyclerView(RExLoopRecyclerView recyclerView) {
        recyclerView.setOnPageListener(new RPagerSnapHelper.OnPageListener() {
            @Override
            public void onPageSelector(int fromPosition, int toPosition) {
                super.onPageSelector(fromPosition, toPosition);
                setCurrentPager(toPosition);
            }
        });
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter != null) {
            initPagerCount(adapter.getItemCount());
        }
    }

    public void setCurrentPager(int index) {
        mCurrentPager = index;
        postInvalidate();
    }

    public void setUpUIViewPager(RViewPager pager, int pagerCount) {
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
        int old = mPagerCount;
        mPagerCount = pagerCount;

        if (old != pagerCount) {
            requestLayout();
        } else {
            postInvalidate();
        }
    }

    public void setFocusColor(int focusColor) {
        this.focusColor = focusColor;
        postInvalidate();
    }

    public void setDefaultColor(int defaultColor) {
        this.defaultColor = defaultColor;
        postInvalidate();
    }

    /**
     * 圆圈的直径
     */
    public void setCircleSize(float circleSize) {
        mCircleSize = circleSize;
        postInvalidate();
    }

    /**
     * 圈与圈之间的间隙
     */
    public void setCircleSpace(float circleSpace) {
        mCircleSpace = circleSpace;
        postInvalidate();
    }

    public void setShowIndicator(boolean showIndicator) {
        this.showIndicator = showIndicator;
        postInvalidate();
    }

    public int getIndicatorWidth() {
        int width;
        width = (int) (Math.max(0, mPagerCount - 1) *
                mCircleSpace +
                mCircleSize *
                        mPagerCount) + getPaddingLeft() + getPaddingRight();
        return width;
    }

    public int getIndicatorHeight() {
        int height;
        height = (int) (mCircleSize + getPaddingTop() + getPaddingBottom());
        return height;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!showIndicator || mPagerCount <= 1 /*只有1个的时候不显示*/) {
            return;
        }

        final float radius = mCircleSize / 2;
        for (int i = 0; i < mPagerCount && mPagerCount > 1; i++) {
            if (i == mCurrentPager) {
                mPaint.setColor(focusColor);
            } else {
                mPaint.setColor(defaultColor);
            }

            //默认横向, 底部居中显示
            float cx = getViewWidth() / 2 - getIndicatorWidth() / 2;
            float cy = getViewHeight() - getPaddingBottom() - radius - offsetY;

            canvas.drawCircle(cx + i * (mCircleSpace + mCircleSize) + radius,
                    cy, radius, mPaint);
        }
    }
}
