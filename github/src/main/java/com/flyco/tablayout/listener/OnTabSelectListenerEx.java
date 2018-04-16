package com.flyco.tablayout.listener;

import android.view.View;

//angcyo
public abstract class OnTabSelectListenerEx implements OnTabSelectListener {

    @Override
    public void onTabReselect(int position) {

    }

    public abstract void onTabAdd(int position, View tabView);

    public abstract void onTabReselect(int position, View tabView);

    public abstract void onUpdateTabStyles(int position, boolean isSelector, View tabView);
}