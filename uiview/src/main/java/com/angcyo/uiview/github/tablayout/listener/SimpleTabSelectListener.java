package com.angcyo.uiview.github.tablayout.listener;

import android.view.View;

import com.angcyo.uiview.github.tablayout.SegmentTabLayout;

public abstract class SimpleTabSelectListener extends SegmentTabLayout.OnTabSelectListenerEx {

    @Override
    public void onTabSelect(int position) {

    }

    @Override
    public void onTabReselect(int position) {

    }

    @Override
    public void onTabAdd(int position, View tabView) {

    }

    @Override
    public void onUpdateTabStyles(int position, boolean isSelector, View tabView) {

    }


    public void onTabSelect(int position, View tabView) {

    }

    @Override
    public void onTabReselect(int position, View tabView) {

    }
}
