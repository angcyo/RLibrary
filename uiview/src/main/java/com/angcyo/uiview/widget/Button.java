package com.angcyo.uiview.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;

import com.angcyo.uiview.R;
import com.angcyo.uiview.RApplication;
import com.angcyo.uiview.kotlin.ViewExKt;
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
    /**
     * 正常边框, 按下主题颜色填充
     */
    public static final int ROUND_BORDER_FILL = 4;
    public static final int RADII = 300;

    int mButtonStyle = DEFAULT;

    int themeSubColor;
    int themeDarkColor;
    int disableColor;

    int borderWidth;
    int roundRadii;

    public Button(Context context) {
        this(context, null);
    }

    public Button(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Button);
        mButtonStyle = typedArray.getInt(R.styleable.Button_r_button_style, DEFAULT);

        if (isInEditMode()) {
            themeSubColor = ViewExKt.getColor(this, R.color.theme_color_accent);
            themeDarkColor = ViewExKt.getColor(this, R.color.theme_color_primary_dark);
            disableColor = Color.GRAY;

            borderWidth = typedArray.getDimensionPixelOffset(R.styleable.Button_r_button_border_width, 2);
            roundRadii = typedArray.getDimensionPixelOffset(R.styleable.Button_r_button_round_radii, 6);
        } else {
            themeSubColor = typedArray.getInt(R.styleable.Button_r_button_theme_color, SkinHelper.getSkin().getThemeSubColor());
            themeDarkColor = typedArray.getInt(R.styleable.Button_r_button_theme_dark_color, SkinHelper.getSkin().getThemeDarkColor());
            disableColor = ContextCompat.getColor(getContext(), R.color.base_color_disable);

            borderWidth = typedArray.getDimensionPixelOffset(R.styleable.Button_r_button_border_width, (int) (1 * density()));
            roundRadii = typedArray.getDimensionPixelOffset(R.styleable.Button_r_button_round_radii, (int) (3 * density()));
        }
        typedArray.recycle();

        initButton();
    }

    private void initButton() {
        if (getBackground() == null) {
            if (mButtonStyle == ROUND) {
                setBackground(ResUtil.ripple(themeSubColor,
                        ResUtil.selector(
                                ResUtil.createDrawable(themeSubColor, RADII),
                                ResUtil.createDrawable(themeDarkColor, RADII),
                                ResUtil.createDrawable(disableColor, RADII)
                        )));
                setTextColor(getTextColors());
            } else if (mButtonStyle == ROUND_BORDER) {
                setBackground(ResUtil.ripple(themeSubColor,
                        ResUtil.selector(
                                ResUtil.createDrawable(themeSubColor, Color.TRANSPARENT, borderWidth, RADII),
                                ResUtil.createDrawable(themeDarkColor, Color.TRANSPARENT, borderWidth, RADII),
                                ResUtil.createDrawable(disableColor, Color.TRANSPARENT, borderWidth, RADII)
                        )));
                setTextColor(ColorStateList.valueOf(themeSubColor));
            } else if (mButtonStyle == ROUND_BORDER_FILL) {
                setBackground(ResUtil.ripple(themeSubColor,
                        ResUtil.selector(
                                ResUtil.createDrawable(ViewExKt.getColor(this, R.color.default_base_line),
                                        Color.TRANSPARENT, borderWidth,
                                        roundRadii),
                                ResUtil.createDrawable(themeSubColor,
                                        roundRadii),
                                ResUtil.createDrawable(disableColor,
                                        roundRadii)
                        )));
                setTextColor(ResUtil.generateTextColor(getCurrentTextColor(), ViewExKt.getColor(this, R.color.base_text_color)));
            } else {
                setTextColor(getTextColors());
                if (isInEditMode()) {
                    setBackground(ResUtil.generateRippleRoundMaskDrawable(roundRadii,
                            Color.WHITE, Color.BLUE, disableColor, Color.BLUE));
                } else {
                    //setBackground(SkinHelper.getSkin().getThemeMaskBackgroundRoundSelector());
                    setBackground(
                            ResUtil.generateRippleRoundMaskDrawable(roundRadii,
                                    Color.WHITE, themeDarkColor,
                                    ContextCompat.getColor(RApplication.getApp(), R.color.base_color_disable),
                                    themeSubColor
                            ));
                }
            }
        }
    }

    @Override
    protected void initView() {
        super.initView();

        setGravity(Gravity.CENTER);
        setClickable(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if ((heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) &&
                getPaddingTop() == 0 && getPaddingBottom() == 0) {
            setMeasuredDimension(getMeasuredWidth(), getResources().getDimensionPixelOffset(R.dimen.base_title_bar_item_size));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
