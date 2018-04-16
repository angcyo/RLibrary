package com.flyco.tablayout.utils;

import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.support.annotation.ColorInt;
import android.support.v4.view.ViewPager;
import android.widget.LinearLayout;

import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.SlidingTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;

import java.util.ArrayList;

/**
 * Created by angcyo on 2017-03-12.
 */

public class TabLayoutUtil {
    public static void initCommonTab(CommonTabLayout tabLayout, ArrayList<CustomTabEntity> entities, OnTabSelectListener listener) {
        tabLayout.setTabData(entities);
        tabLayout.setOnTabSelectListener(listener);
    }

    public static void initSegmentTab(SegmentTabLayout tabLayout, String[] titles, OnTabSelectListener listener) {
        tabLayout.setTabData(titles);
        tabLayout.setOnTabSelectListener(listener);
    }

    public static void initSlidingTab(SlidingTabLayout tabLayout, ViewPager viewPager, OnTabSelectListener listener) {
        tabLayout.setViewPager(viewPager);
        tabLayout.setOnTabSelectListener(listener);
    }

    public static void setCommonTabDivider(CommonTabLayout tabLayout, @ColorInt int color, int showDividers, int padding) {
        LinearLayout linearLayout = (LinearLayout) tabLayout.getChildAt(0);
        linearLayout.setDividerPadding(padding);
        RectShape rectShape = new RectShape();
        float density = tabLayout.getResources().getDisplayMetrics().density;
        ShapeDrawable shapeDrawable = new ShapeDrawable(rectShape);
        shapeDrawable.setIntrinsicWidth((int) density);
        shapeDrawable.setIntrinsicHeight((int) density);
        shapeDrawable.getPaint().setColor(color);
        shapeDrawable.getPaint().setStyle(Paint.Style.STROKE);
        linearLayout.setDividerDrawable(shapeDrawable);
        linearLayout.setShowDividers(showDividers);
    }
}
