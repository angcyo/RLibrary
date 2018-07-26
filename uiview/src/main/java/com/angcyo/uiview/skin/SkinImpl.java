package com.angcyo.uiview.skin;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import com.angcyo.uiview.R;
import com.angcyo.uiview.RApplication;
import com.angcyo.uiview.resources.ResUtil;
import com.angcyo.uiview.utils.ScreenUtil;

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
public class SkinImpl implements ISkin {

    public static final int DEFAULT_TEXT_SIZE_OFFSET = 2;

    @Override
    public String skinName() {
        return "Default";
    }

    @Override
    public int getThemeColor() {
        return Color.BLACK;
    }

    @Override
    public int getThemeSubColor() {
        return Color.GRAY;
    }

    @Override
    public int getThemeDarkColor() {
        return Color.DKGRAY;
    }

    @Override
    public int getThemeDisableColor() {
        return Color.DKGRAY;
    }

    @Override
    public int getThemeTranColor(int alpha) {
        return SkinHelper.getTranColor(getThemeSubColor(), alpha);
    }

    @Override
    public Drawable getThemeTranBackgroundSelector() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return ResUtil.generateRippleDrawable(getThemeSubColor());
        } else {
            return ResUtil.generateBgDrawable(getThemeSubColor(), Color.TRANSPARENT);
        }
    }

    /**
     * @Inherited
     */
    @Override
    public Drawable getThemeTranMaskBackgroundSelector() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return ResUtil.generateRippleMaskDrawable(getThemeSubColor());
        } else {
            return ResUtil.generateBgDrawable(getThemeSubColor(), Color.TRANSPARENT);
        }
    }

    @Override
    public Drawable getThemeMaskBackgroundRoundSelector() {
        return ResUtil.generateRippleRoundMaskDrawable(RApplication.getApp()
                        .getResources()
                        .getDimensionPixelOffset(R.dimen.base_round_little_radius),
                Color.WHITE, getThemeDarkColor(),
                ContextCompat.getColor(RApplication.getApp(), R.color.base_color_disable),
                getThemeSubColor()
        );
    }

    @Override
    public Drawable getThemeMaskBackgroundSelector() {
        return getThemeMaskBackgroundSelector(getThemeDarkColor());
    }

    @Override
    public Drawable getThemeMaskBackgroundSelector(int pressColor) {
        return ResUtil.generateRippleMaskDrawable(Color.WHITE, pressColor, getThemeSubColor());
    }


    @Override
    public Drawable getThemeMaskBackgroundRoundSelector(int pressColor) {
        return ResUtil.generateRippleRoundMaskDrawable(RApplication.getApp()
                        .getResources()
                        .getDimensionPixelOffset(R.dimen.base_round_little_radius),
                Color.WHITE, pressColor, getThemeSubColor()
        );
    }

    /**
     * 0dpi ~ 120dpi	ldpi
     * 120dpi ~ 160dpi	mdpi
     * 160dpi ~ 240dpi	hdpi
     * 240dpi ~ 320dpi	xhdpi
     * 320dpi ~ 480dpi	xxhdpi
     * 480dpi ~ 640dpi	xxxhdpi
     */
    @Override
    public float getMainTextSize() {
        return getTextSize(14);
    }

    @Override
    public float getSubTextSize() {
        return getTextSize(12);
    }

    @Override
    public float getTextSize(float sizePx) {
        float size;
        if (ScreenUtil.densityDpi >= 320) {
            //xxhdpi
            size = ScreenUtil.density * (sizePx + DEFAULT_TEXT_SIZE_OFFSET);
        } else if (ScreenUtil.densityDpi >= 240) {
            //xhdpi
            size = ScreenUtil.density * sizePx;
        } else {
            size = ScreenUtil.density * (sizePx - DEFAULT_TEXT_SIZE_OFFSET);
        }
        return size;
    }
}
