package com.angcyo.uiview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.angcyo.uiview.R;
import com.angcyo.uiview.utils.ScreenUtil;

/**
 * 用来控制状态栏的padding
 * Created by angcyo on 2016-11-05.
 */

public class TitleBarLayout extends FrameLayout {

    boolean enablePadding = true;

    /**
     * 允许的最大高度, 如果为-2px,那么就是屏幕高度的一半, 如果是-3px,那么就是屏幕高度的三分之, 以此内推
     */
    private int maxHeight = -1;

    public TitleBarLayout(Context context) {
        this(context, null);
    }

    public TitleBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleBarLayout);
        maxHeight = typedArray.getDimensionPixelOffset(R.styleable.TitleBarLayout_r_max_height, -1);
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
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        resetMaxHeight();
        requestLayout();
    }

    public void setEnablePadding(boolean enablePadding) {
        this.enablePadding = enablePadding;
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        if (getChildCount() > 1) {
            throw new IllegalArgumentException("Need Only One Child View.");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int statusBarHeight = getResources().getDimensionPixelSize(R.dimen.status_bar_height);

        if (enablePadding) {
            setPadding(getPaddingLeft(), statusBarHeight, getPaddingRight(), getPaddingBottom());
            maxHeight += statusBarHeight;
            heightSize += statusBarHeight;
        }

        if (maxHeight > 0) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(Math.min(maxHeight, heightSize), heightMode));
        } else {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(heightSize, heightMode));
        }
    }
}
