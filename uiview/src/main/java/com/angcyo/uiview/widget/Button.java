package com.angcyo.uiview.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;

import com.angcyo.uiview.R;
import com.angcyo.uiview.resources.ResUtil;
import com.angcyo.uiview.skin.SkinHelper;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/05/09 17:44
 * 修改人员：Robi
 * 修改时间：2017/05/09 17:44
 * 修改备注：
 * Version: 1.0.0
 */
public class Button extends RTextView {

    public static final int DEFAULT = 1;
    public static final int ROUND = 2;
    public static final int ROUND_BORDER = 3;

    int mButtonStyle = DEFAULT;

    public Button(Context context) {
        this(context, null);
    }

    public Button(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Button);
        mButtonStyle = typedArray.getInt(R.styleable.Button_r_button_style, DEFAULT);
        typedArray.recycle();

    }

    @Override
    protected void initView() {
        super.initView();

        setGravity(Gravity.CENTER);
        setClickable(true);
        setTextColor(ColorStateList.valueOf(Color.WHITE));
        if (isInEditMode()) {
            setBackgroundColor(Color.BLUE);
        } else {
            if (mButtonStyle == ROUND) {
                setBackground(ResUtil.selector());
            } else if (mButtonStyle == ROUND_BORDER) {
            } else {
                setBackground(SkinHelper.getSkin().getThemeMaskBackgroundRoundSelector());
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if ((heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) &&
                getPaddingTop() == 0 && getPaddingBottom() == 0) {
            setMeasuredDimension(getMeasuredWidth(), getResources().getDimensionPixelOffset(R.dimen.default_button_height));
        }
    }
}
