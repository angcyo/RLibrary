package com.angcyo.uiview.viewgroup;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.angcyo.uiview.R;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/05/05 16:29
 * 修改人员：Robi
 * 修改时间：2017/05/05 16:29
 * 修改备注：
 * Version: 1.0.0
 */
public class RRelativeLayout extends RelativeLayout {

    private Drawable mBackgroundDrawable;

    public RRelativeLayout(Context context) {
        super(context);
    }

    public RRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RRelativeLayout);
        mBackgroundDrawable = typedArray.getDrawable(R.styleable.RRelativeLayout_r_background);
        typedArray.recycle();
        initLayout();
    }

    private void initLayout() {
        setWillNotDraw(false);
    }

    @Override
    public void draw(Canvas canvas) {
        if (mBackgroundDrawable != null) {
            mBackgroundDrawable.setBounds(canvas.getClipBounds());
            mBackgroundDrawable.draw(canvas);
        }
        super.draw(canvas);
        if (!isEnabled()) {
            canvas.drawColor(ContextCompat.getColor(getContext(), R.color.default_base_tran_dark2));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }
}
