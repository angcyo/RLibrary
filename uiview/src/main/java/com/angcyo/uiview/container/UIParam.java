package com.angcyo.uiview.container;

import android.os.Bundle;

import com.angcyo.uiview.view.IView;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：启动时候的布局参数
 * 创建人员：Robi
 * 创建时间：2016/12/19 10:17
 * 修改人员：Robi
 * 修改时间：2016/12/19 10:17
 * 修改备注：
 * Version: 1.0.0
 */
public class UIParam {

    public static final int NORMAL = 0;
    public static final int SINGLE_TOP = 1;//如果已经添加了IView, 则最前显示IView

    public boolean mAnim = true;
    public boolean mAsync = true;
    /**
     * 是否是滑动返回, true 不判断是否允许退出
     */
    public boolean isSwipeBack = false;
    /**
     * 是否安静执行, 不回调部分生命周期, 影响部分动画的执行.
     */
    public boolean isQuiet = false;
    /**
     * 启动模式
     */
    public int start_mode = NORMAL;
    /**
     * 启动一个新的IView 时, 是否隐藏之前顶部的IView
     */
    public boolean hideLastIView = false;
    /**
     * 是否需要过渡动画, 在低设备上默认会关闭动画, 可以通过这2个方法, 强行开启动画
     */
    public boolean needTransitionStartAnim = false;
    public boolean needTransitionExitAnim = false;
    /**
     * 点击在标题栏上的返回按钮, 用来控制当只有一个UIView时, 关闭Activity的
     */
    public boolean clickOnTitleBack = false;
    /**
     * 需要替换的iview, 只在replaceIView时使用, 用来判断目标的可行性
     */
    protected IView replaceIView;
    /**
     * IView unLoad 时回调
     */
    protected Runnable unloadRunnable;
    protected Bundle mBundle;

    public UIParam(boolean anim, boolean async, Bundle bundle) {
        mAnim = anim;
        mBundle = bundle;
        mAsync = async;
    }

    public UIParam(boolean anim, boolean async) {
        this(anim, async, null);
    }

    public UIParam(boolean anim) {
        mAnim = anim;
    }

    public UIParam() {
    }

    public UIParam(Runnable unloadRunnable) {
        this.unloadRunnable = unloadRunnable;
    }

    public UIParam(boolean anim, boolean isSwipeBack, boolean isQuiet) {
        mAnim = anim;
        this.isSwipeBack = isSwipeBack;
        this.isQuiet = isQuiet;
    }

    public UIParam setAnim(boolean anim) {
        mAnim = anim;
        return this;
    }

    public UIParam setAsync(boolean async) {
        mAsync = async;
        return this;
    }

    public UIParam setSwipeBack(boolean swipeBack) {
        isSwipeBack = swipeBack;
        return this;
    }

    public UIParam setQuiet(boolean quiet) {
        isQuiet = quiet;
        return this;
    }

    public UIParam setStart_mode(int start_mode) {
        this.start_mode = start_mode;
        return this;
    }

    public UIParam setHideLastIView(boolean hideLastIView) {
        this.hideLastIView = hideLastIView;
        return this;
    }

    /**
     * 设置启动模式
     */
    public UIParam setLaunchMode(int mode) {
        start_mode = mode;
        return this;
    }

    public boolean isReplaceIViewEmpty() {
        if (replaceIView == null) {
            return true;
        }
        return false;
    }

    public boolean isUnloadRunnalbeEmpty() {
        if (unloadRunnable == null) {
            return true;
        }
        return false;
    }

    public boolean isBundleEmpty() {
        if (mBundle == null) {
            return true;
        }
        return false;
    }

    public Bundle getBundle() {
        return mBundle;
    }

    public UIParam setBundle(Bundle bundle) {
        mBundle = bundle;
        return this;
    }

    public IView getReplaceIView() {
        return replaceIView;
    }

    public UIParam setReplaceIView(IView replaceIView) {
        this.replaceIView = replaceIView;
        return this;
    }

    public Runnable getUnloadRunnable() {
        return unloadRunnable;
    }

    public UIParam setUnloadRunnable(Runnable unloadRunnable) {
        this.unloadRunnable = unloadRunnable;
        return this;
    }

    public UIParam setClickOnTitleBack(boolean clickOnTitleBack) {
        this.clickOnTitleBack = clickOnTitleBack;
        return this;
    }

    public void clear() {
        mBundle = null;
        replaceIView = null;
        unloadRunnable = null;
    }

    public UIParam setNeedTransitionStartAnim(boolean needTransitionStartAnim) {
        this.needTransitionStartAnim = needTransitionStartAnim;
        return this;
    }

    public UIParam setNeedTransitionExitAnim(boolean needTransitionExitAnim) {
        this.needTransitionExitAnim = needTransitionExitAnim;
        return this;
    }
}
