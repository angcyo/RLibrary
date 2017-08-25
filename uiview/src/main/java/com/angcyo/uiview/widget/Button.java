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

    /**
     * 默认就是很小的圆角矩形填充样式
     */
    public static final int DEFAULT = 1;
    /**
     * 圆角矩形填充样式
     */
    public static final int ROUND = 2;
    /**
     * 可自定义的圆角边框样式
     */
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

        initButton();
    }

    private void initButton() {
        if (isInEditMode()) {
            if (mButtonStyle == ROUND) {
                setBackground(ResUtil.ripple(Color.BLUE,
                        ResUtil.selector(
                                ResUtil.createDrawable(Color.BLUE, 300),
                                ResUtil.createDrawable(Color.BLUE, 300)
                        )));
            } else if (mButtonStyle == ROUND_BORDER) {
                setBackground(ResUtil.ripple(Color.BLUE,
                        ResUtil.selector(
                                ResUtil.createDrawable(Color.BLUE, Color.TRANSPARENT, (int) (1 * density()), 300),
                                ResUtil.createDrawable(Color.BLUE, Color.TRANSPARENT, (int) (1 * density()), 300)
                        )));
                setTextColor(ColorStateList.valueOf(Color.BLUE));
            } else {
                setBackground(ResUtil.generateRippleRoundMaskDrawable(getResources().getDimensionPixelOffset(R.dimen.base_round_little_radius),
                        Color.WHITE, Color.BLUE, Color.BLUE));
            }
        } else {
            if (mButtonStyle == ROUND) {
                setBackground(ResUtil.ripple(SkinHelper.getSkin().getThemeSubColor(),
                        ResUtil.selector(
                                ResUtil.createDrawable(SkinHelper.getSkin().getThemeSubColor(), 300),
                                ResUtil.createDrawable(SkinHelper.getSkin().getThemeDarkColor(), 300)
                        )));
            } else if (mButtonStyle == ROUND_BORDER) {
                setBackground(ResUtil.ripple(SkinHelper.getSkin().getThemeSubColor(),
                        ResUtil.selector(
                                ResUtil.createDrawable(SkinHelper.getSkin().getThemeSubColor(), Color.TRANSPARENT, (int) (1 * density()), 300),
                                ResUtil.createDrawable(SkinHelper.getSkin().getThemeDarkColor(), Color.TRANSPARENT, (int) (1 * density()), 300)
                        )));
                setTextColor(ColorStateList.valueOf(SkinHelper.getSkin().getThemeSubColor()));
            } else {
                setBackground(SkinHelper.getSkin().getThemeMaskBackgroundRoundSelector());
            }
        }
    }

    @Override
    protected void initView() {
        super.initView();

        setGravity(Gravity.CENTER);
        setClickable(true);
        setTextColor(ColorStateList.valueOf(Color.WHITE));
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
