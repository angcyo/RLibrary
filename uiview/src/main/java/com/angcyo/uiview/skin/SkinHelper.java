package com.angcyo.uiview.skin;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;

import com.angcyo.uiview.R;
import com.angcyo.uiview.RApplication;
import com.angcyo.uiview.container.ILayout;
import com.angcyo.uiview.resources.ResUtil;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/04/01 15:14
 * 修改人员：Robi
 * 修改时间：2017/04/01 15:14
 * 修改备注：
 * Version: 1.0.0
 */
public class SkinHelper {

    static ISkin mSkin;

    public static void init(Context context) {
        if (mSkin == null) {
            createDefaultSkin(context);
        }
    }

    public static void init(ISkin skin) {
        mSkin = skin;
    }

    private static void createDefaultSkin(Context context) {
        mSkin = new MainSkin(context);
    }

    /**
     * 获取当前的皮肤
     */
    public static ISkin getSkin() {
        if (mSkin == null) {
            throw new NullPointerException("please call SkinHelper#init method.");
            //init(RApplication.getApp());
        }
        return mSkin;
    }

    public static void setSkin(ISkin iSkin) {
        SkinHelper.mSkin = iSkin;
    }

    /**
     * 改变皮肤
     */
    public static void changeSkin(ISkin skin, ILayout layout) {
        SkinHelper.mSkin = skin;
        if (layout != null) {
            layout.onSkinChanged(skin);
        }
    }

    /**
     * 返回一个颜色的透明颜色,
     *
     * @param alpha [0..255]
     */
    public static int getTranColor(@ColorInt int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    /**
     * 主题颜色的文本selector
     */
    public static ColorStateList getThemeTextColorSelector() {
        return getThemeTextColorSelector(Color.WHITE);
    }

    public static ColorStateList getThemeTextColorSelector(int defaultColor) {
        return ResUtil.generateTextColor(getSkin().getThemeSubColor(), defaultColor);
    }

    /**
     * 主题颜色的圆角边框selector
     */
    public static Drawable getThemeRoundBorderSelector() {
        return getRoundBorderSelector(SkinHelper.getSkin().getThemeTranColor(80));
    }

    public static Drawable getRoundBorderSelector(int defaultColor) {
        return ResUtil.generateRoundBorderDrawable(
                RApplication.getApp().getResources().getDimensionPixelOffset(R.dimen.base_round_little_radius),
                RApplication.getApp().getResources().getDimensionPixelOffset(R.dimen.base_line),
                SkinHelper.getSkin().getThemeSubColor(),
                defaultColor
        );
    }
}
