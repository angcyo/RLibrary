package com.flyco.tablayout.listener;

import android.support.annotation.DrawableRes;

public interface CustomTabEntity {
    String getTabTitle();

    @DrawableRes
    int getTabSelectedIcon();

    @DrawableRes
    int getTabUnselectedIcon();

    //angcyo
    int marginLeft();

    int marginRight();

    CustomTabEntity setTabSelectedIcon(@DrawableRes int res);

    CustomTabEntity setTabUnselectedIcon(@DrawableRes int res);

    boolean isShowArrow();

    boolean isHomeNavigation();

    boolean isShowBackground();

    int getVisibility();
}