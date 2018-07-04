package com.angcyo.uiview.model;

import com.angcyo.uiview.container.UITitleBarContainer;
import com.angcyo.uiview.widget.RTitleCenterLayout;

public interface OnInitTitleLayout {
    void onInitLayout(RTitleCenterLayout parent);

    void onInitLayout(UITitleBarContainer barLayout, boolean isLayoutFullscreen, int mStatusBarHeight);
}


