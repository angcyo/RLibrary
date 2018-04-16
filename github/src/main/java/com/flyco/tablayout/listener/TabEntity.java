package com.flyco.tablayout.listener;


import android.support.annotation.DrawableRes;
import android.view.View;

public class TabEntity implements CustomTabEntity {
    public String title;
    public int selectedIcon = -1;
    public int unSelectedIcon = -1;
    /**
     * 显示下拉弹窗箭头
     */
    public boolean showArrow = false;
    public boolean isHomeNavigation = false;
    public boolean isShowBackground = true;
    public int visibility = View.VISIBLE;
    public int marginLeft = 0;
    public int marginRight = 0;

    private String object;

    public TabEntity(String title) {
        this.title = title;
    }

    public TabEntity(String title, int selectedIcon, int unSelectedIcon) {
        this.title = title;
        this.selectedIcon = selectedIcon;
        this.unSelectedIcon = unSelectedIcon;
    }

    public TabEntity(String title, int selectedIcon, int unSelectedIcon, boolean showArrow) {
        this.selectedIcon = selectedIcon;
        this.title = title;
        this.unSelectedIcon = unSelectedIcon;
        this.showArrow = showArrow;
    }

    public TabEntity(boolean isHomeNavigation, String title, int selectedIcon, int unSelectedIcon) {
        this.selectedIcon = selectedIcon;
        this.title = title;
        this.unSelectedIcon = unSelectedIcon;
        this.isHomeNavigation = isHomeNavigation;
    }

    public TabEntity(String title, int selectedIcon, int unSelectedIcon,
                     boolean showArrow, boolean isHomeNavigation, boolean isShowBackground) {
        this.title = title;
        this.selectedIcon = selectedIcon;
        this.unSelectedIcon = unSelectedIcon;
        this.showArrow = showArrow;
        this.isHomeNavigation = isHomeNavigation;
        this.isShowBackground = isShowBackground;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    @Override
    public int marginLeft() {
        return marginLeft;
    }

    @Override
    public int marginRight() {
        return marginRight;
    }

    @Override
    public String getTabTitle() {
        return title;
    }

    @Override
    public int getTabSelectedIcon() {
        return selectedIcon;
    }

    @Override
    public CustomTabEntity setTabSelectedIcon(@DrawableRes int res) {
        this.selectedIcon = res;
        return this;
    }

    @Override
    public int getTabUnselectedIcon() {
        return unSelectedIcon;
    }

    @Override
    public CustomTabEntity setTabUnselectedIcon(@DrawableRes int res) {
        this.unSelectedIcon = res;
        return this;
    }

    @Override
    public boolean isShowArrow() {
        return showArrow;
    }

    @Override
    public boolean isHomeNavigation() {
        return isHomeNavigation;
    }

    @Override
    public boolean isShowBackground() {
        return isShowBackground;
    }

    @Override
    public int getVisibility() {
        return visibility;
    }

    public TabEntity setVisibility(int visibility) {
        this.visibility = visibility;
        return this;
    }
}
