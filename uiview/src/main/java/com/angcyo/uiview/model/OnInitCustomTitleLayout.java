package com.angcyo.uiview.model;

import com.angcyo.uiview.container.UITitleBarContainer;
import com.angcyo.uiview.recycler.RBaseViewHolder;

public interface OnInitCustomTitleLayout {
    void onInitLayout(UITitleBarContainer barLayout, RBaseViewHolder titleViewHolder,
                      boolean isLayoutFullscreen, int mStatusBarHeight);
}


