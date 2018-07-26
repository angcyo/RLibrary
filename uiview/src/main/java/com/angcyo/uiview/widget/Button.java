package com.angcyo.uiview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;

import com.angcyo.uiview.R;
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
     * 圆角矩形填充样式, 通过 roundRadii 修改圆角大小
     */
    public static final int ROUND = 2;
    /**
     * 可自定义的圆角边框样式
     */
    public static final int ROUND_BORDER = 3;
    /**
     * 正常边框不填充, 按下主题颜色填充
     */
    public static final int ROUND_BORDER_FILL = 4;

    /**
     * 透明渐变, 很小的圆角矩形填充样式
     */
    public static final int ROUND_GRADIENT_RECT = 5;
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

        int defaultValue = (int) (3 * density());
        if (mButtonStyle == ROUND) {
            defaultValue = RADII;
        }

        if (isInEditMode()) {
            themeSubColor = typedArray.getInt(R.styleable.Button_r_button_theme_color, ViewExKt.getColor(this, R.color.theme_color_accent));
            themeDarkColor = typedArray.getInt(R.styleable.Button_r_button_theme_dark_color, ViewExKt.getColor(this, R.color.theme_color_primary_dark));
            disableColor = typedArray.getInt(R.styleable.Button_r_button_disable_color, Color.GRAY);

            borderWidth = typedArray.getDimensionPixelOffset(R.styleable.Button_r_button_border_width, (int) (2 * density()));
            roundRadii = typedArray.getDimensionPixelOffset(R.styleable.Button_r_button_round_radii, defaultValue);
        } else {
            themeSubColor = typedArray.getInt(R.styleable.Button_r_button_theme_color, SkinHelper.getSkin().getThemeSubColor());
            themeDarkColor = typedArray.getInt(R.styleable.Button_r_button_theme_dark_color, SkinHelper.getSkin().getThemeDarkColor());
            disableColor = typedArray.getInt(R.styleable.Button_r_button_disable_color, ContextCompat.getColor(getContext(), R.color.base_color_disable));

            borderWidth = typedArray.getDimensionPixelOffset(R.styleable.Button_r_button_border_width, (int) (1 * density()));
            roundRadii = typedArray.getDimensionPixelOffset(R.styleable.Button_r_button_round_radii, defaultValue);
        }
        typedArray.recycle();

        /*不覆盖系统的background属性*/
        initButton(getBackground() == null);
    }

    private void initButton(boolean refreshBg) {
        setTextColor(getTextColors());

        if (refreshBg) {
            switch (mButtonStyle) {
                case ROUND:
                    setBackground(ResUtil.ripple(themeSubColor,
                            ResUtil.selector(
                                    ResUtil.createDrawable(themeSubColor, roundRadii),
                                    ResUtil.createDrawable(themeDarkColor, roundRadii),
                                    ResUtil.createDrawable(disableColor, roundRadii)
                            )));
                    break;
                case ROUND_BORDER:
                    setBackground(ResUtil.ripple(themeSubColor,
                            ResUtil.selector(
                                    ResUtil.createDrawable(themeSubColor, Color.TRANSPARENT, borderWidth, roundRadii),
                                    ResUtil.createDrawable(themeDarkColor, Color.TRANSPARENT, borderWidth, roundRadii),
                                    ResUtil.createDrawable(disableColor, Color.TRANSPARENT, borderWidth, roundRadii)
                            )));

                    if (useSkinStyle) {
                        int subColor = themeSubColor;
                        setTextColor(ResUtil.generateTextColor(subColor, subColor, disableColor, subColor));
                    }
                    break;
                case ROUND_BORDER_FILL:
                    int subColor = themeSubColor;
                    int lineColor = ViewExKt.getColor(this, R.color.default_base_line);
                    if (useSkinStyle) {
                        lineColor = subColor;
                        setTextColor(ResUtil.generateTextColor(Color.WHITE, subColor));
                    } else {
                        setTextColor(ResUtil.generateTextColor(getCurrentTextColor(), ViewExKt.getColor(this, R.color.base_text_color)));
                    }

                    setBackground(ResUtil.ripple(themeSubColor,
                            ResUtil.selector(
                                    ResUtil.createDrawable(lineColor,
                                            Color.TRANSPARENT, borderWidth,
                                            roundRadii),
                                    ResUtil.createDrawable(themeSubColor,
                                            roundRadii),
                                    ResUtil.createDrawable(disableColor,
                                            roundRadii)
                            )));

                    break;
                case ROUND_GRADIENT_RECT:
                    setBackground(ResUtil.ripple(Color.WHITE,
                            ResUtil.selector(
                                    ResUtil.createGradientDrawable(getContext(), roundRadii),
                                    ResUtil.createGradientDrawable(getContext(), roundRadii),
                                    ResUtil.createGradientDrawable(getContext(), roundRadii))
                    ));
                    break;
                default:
                    setBackground(
                            ResUtil.generateRippleRoundMaskDrawable(roundRadii,
                                    Color.WHITE, themeDarkColor,
                                    disableColor,
                                    themeSubColor
                            ));
                    break;
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
                getPaddingTop() == 0 && getPaddingBottom() == 0 && !aeqWidth) {
            setMeasuredDimension(getMeasuredWidth(), getResources().getDimensionPixelOffset(R.dimen.base_title_bar_item_size));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void updateStyle() {
        themeSubColor = SkinHelper.getSkin().getThemeSubColor();
        themeDarkColor = SkinHelper.getSkin().getThemeDarkColor();
        disableColor = SkinHelper.getSkin().getThemeDisableColor();

        setBackground(null);
        initButton(true);
    }
}
