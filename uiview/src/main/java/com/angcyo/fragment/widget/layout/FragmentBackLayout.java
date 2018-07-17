package com.angcyo.fragment.widget.layout;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.view.View;

import com.angcyo.uiview.container.SwipeBackLayout;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：可以用来像任务管理一样, 查看当前Activity中 有哪些Fragment
 * 创建人员：Robi
 * 创建时间：2018/07/17 11:55
 * 修改人员：Robi
 * 修改时间：2018/07/17 11:55
 * 修改备注：
 * Version: 1.0.0
 */
public class FragmentBackLayout extends SwipeBackLayout {
    private float mTranslationOffsetX;

    public FragmentBackLayout(Context context) {
        super(context);
    }

    public FragmentBackLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canTryCaptureView(View child) {
        if (getScreenOrientation() != Configuration.ORIENTATION_PORTRAIT) {
            //非竖屏, 禁用滑动返回
            return false;
        }
        return true;
    }

    private void translation(float percent /*如果为0, 表示滑动关闭了*/) {
        View lastView = getLastTranslationView();
        if (lastView != null) {
            float tx = -mTranslationOffsetX * percent;
            if (lastView.getTranslationX() != tx) {
                lastView.setTranslationX(tx);
            }
        }
    }

    private View getLastTranslationView() {
        int childCount = getChildCount();
        if (childCount > 1) {
            return getChildAt(childCount - 2);
        }
        return null;
    }

    /**
     * 滚动到关闭状态
     */
    @Override
    protected void onRequestClose() {
        super.onRequestClose();
        translation(0);
        if (getChildCount() == 1) {
            finishActivity();
        } else {
            finishFragment();
        }
    }

    /**
     * 默认状态
     */
    @Override
    protected void onRequestOpened() {
        super.onRequestOpened();
        translation(0);
    }

    @Override
    protected void onSlideChange(float percent) {
        super.onSlideChange(percent);
        translation(percent);
    }

    @Override
    protected void onStateIdle() {
        super.onStateIdle();
    }

    /**
     * 滑动中
     */
    @Override
    protected void onStateDragging() {
        super.onStateDragging();

        //开始偏移时, 偏移的距离
        mTranslationOffsetX = getMeasuredWidth() * 0.3f;
        //viewPattern.mView.setTranslationX(-mTranslationOffsetX);

        View lastView = getLastTranslationView();
        if (lastView != null) {
            lastView.setTranslationX(mTranslationOffsetX);
        }
    }

    protected void finishActivity() {
        Context context = getContext();
        if (context instanceof Activity) {
            ((Activity) context).finish();
            ((Activity) context).overridePendingTransition(0, 0);
        }
    }

    protected void finishFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            fragmentManager.popBackStack();
        }
    }

    protected FragmentManager getFragmentManager() {
        Context context = getContext();
        if (context instanceof FragmentActivity) {
            return ((FragmentActivity) context).getSupportFragmentManager();
        }
        return null;
    }
}
