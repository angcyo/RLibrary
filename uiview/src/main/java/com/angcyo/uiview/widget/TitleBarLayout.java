package com.angcyo.uiview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.angcyo.uiview.R;
import com.angcyo.uiview.utils.ScreenUtil;
import com.angcyo.uiview.view.UIIViewImpl;

/**
 * 用来控制状态栏的padding
 * Created by angcyo on 2016-11-05.
 */

public class TitleBarLayout extends FrameLayout {

    boolean enablePadding = true;
    boolean fitActionBar = false;
    boolean isScreenHeight = false;

    /**
     * 允许的最大高度, 如果为-2px,那么就是屏幕高度的一半, 如果是-3px,那么就是屏幕高度的三分之, 以此内推
     */
    private int maxHeight = -1;

    public TitleBarLayout(Context context) {
        this(context, null);
    }

    public TitleBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleBarLayout);
            maxHeight = typedArray.getDimensionPixelOffset(R.styleable.TitleBarLayout_r_max_height, -1);
            enablePadding = UIIViewImpl.isLollipop();
            fitActionBar = typedArray.getBoolean(R.styleable.TitleBarLayout_r_fit_action_bar_height, fitActionBar);
            enablePadding = typedArray.getBoolean(R.styleable.TitleBarLayout_r_fit_status_bar_height, enablePadding);
            isScreenHeight = typedArray.getBoolean(R.styleable.TitleBarLayout_r_is_screen_height, isScreenHeight);

            resetMaxHeight();
            typedArray.recycle();
        }
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
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int statusBarHeight = getResources().getDimensionPixelSize(R.dimen.status_bar_height);
        int actionBarHeight = getResources().getDimensionPixelSize(R.dimen.action_bar_height);
        int topHeight = 0;
        int viewHeight = 0;

        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        if (fitActionBar && enablePadding) {
            topHeight = statusBarHeight + actionBarHeight;
        } else if (enablePadding) {
            topHeight = statusBarHeight;
        } else if (fitActionBar) {
            topHeight = actionBarHeight;
        } else {
            topHeight = 0;
        }

        setPadding(getPaddingLeft(), topHeight, getPaddingRight(), getPaddingBottom());
        if (maxHeight > 0) {
            maxHeight += topHeight;
        }

        if (maxHeight > 0) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(Math.min(maxHeight, heightSize + topHeight), heightMode));
        } else {
            viewHeight = Math.min(heightSize + topHeight, screenHeight);
            if (heightMode == MeasureSpec.EXACTLY) {
                int childWidth = widthSize;
                if (getChildCount() > 0) {
                    if (heightSize == screenHeight || isScreenHeight) {
                        heightSize -= topHeight;
                    }
                    getChildAt(0).measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(heightSize - getPaddingBottom(), heightMode));
                    childWidth = getChildAt(0).getMeasuredWidth();
                }
                if (widthMode == MeasureSpec.EXACTLY) {
                    setMeasuredDimension(widthSize, viewHeight);
                } else {
                    setMeasuredDimension(childWidth, viewHeight);
                }
            } else {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(viewHeight, heightMode));
            }
        }
    }
}
