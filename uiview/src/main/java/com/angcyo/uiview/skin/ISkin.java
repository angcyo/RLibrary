package com.angcyo.uiview.skin;

import android.graphics.drawable.Drawable;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：皮肤接口
 * 创建人员：Robi
 * 创建时间：2017/04/01 15:14
 * 修改人员：Robi
 * 修改时间：2017/04/01 15:14
 * 修改备注：
 * Version: 1.0.0
 */
public interface ISkin {

    /**
     * 皮肤的名称
     */
    String skinName();

    int getThemeColor();

    int getThemeSubColor();

    int getThemeDarkColor();

    int getThemeDisableColor();

    int getThemeTranColor(int alpha);

    /**
     * 波纹无限制, 默认是个圆形波纹
     */
    Drawable getThemeTranBackgroundSelector();

    /**
     * 波纹有限制
     */
    Drawable getThemeTranMaskBackgroundSelector();

    /**
     * 圆角波纹背景
     */
    Drawable getThemeMaskBackgroundRoundSelector();

    /**
     * 带mask, 显示内容Drawable的ripple
     */
    Drawable getThemeMaskBackgroundSelector();

    Drawable getThemeMaskBackgroundSelector(int pressColor);

    /**
     * 圆角带Mask
     */
    Drawable getThemeMaskBackgroundRoundSelector(int pressColor);

    //2017-8-24 添加对字体的支持

    /**
     * 获取主要主字体大小
     *
     * @return 返回计算后的字体大小, {@link android.widget.TextView#setTextSize(int, float)} 时,请使用 {@link android.util.TypedValue#COMPLEX_UNIT_PX}
     */
    float getMainTextSize();

    /**
     * 获取次要字体大小
     *
     * @return 返回计算后的字体大小, {@link android.widget.TextView#setTextSize(int, float)} 时,请使用 {@link android.util.TypedValue#COMPLEX_UNIT_PX}
     */
    float getSubTextSize();

    /**
     * 获取字体大小
     *
     * @param sizePx 单位px
     * @return 返回计算后的字体大小, {@link android.widget.TextView#setTextSize(int, float)} 时,请使用 {@link android.util.TypedValue#COMPLEX_UNIT_PX}
     */
    float getTextSize(float sizePx);
}
