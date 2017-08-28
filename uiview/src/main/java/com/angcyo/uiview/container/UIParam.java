package com.angcyo.uiview.container;

import android.os.Bundle;

import com.angcyo.uiview.view.IView;

import java.lang.ref.WeakReference;

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
     * 需要替换的iview, 只在replaceIView时使用, 用来判断目标的可行性
     */
    protected WeakReference<IView> replaceIViewRef;
    /**
     * IView unLoad 时回调
     */
    protected WeakReference<Runnable> unloadRunnableRef;
    protected WeakReference<Bundle> mBundleRef;

    public UIParam(boolean anim, boolean async, Bundle bundle) {
        mAnim = anim;
        mBundleRef = new WeakReference<>(bundle);
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
        if (replaceIViewRef == null || replaceIViewRef.get() == null) {
            return true;
        }
        return false;
    }

    public boolean isUnloadRunnalbeEmpty() {
        if (unloadRunnableRef == null || unloadRunnableRef.get() == null) {
            return true;
        }
        return false;
    }

    public boolean isBundleEmpty() {
        if (mBundleRef == null || mBundleRef.get() == null) {
            return true;
        }
        return false;
    }

    public Bundle getBundle() {
        if (mBundleRef == null) {
            return null;
        }
        return mBundleRef.get();
    }

    public UIParam setBundle(Bundle bundle) {
        mBundleRef = new WeakReference<>(bundle);
        return this;
    }

    public IView getReplaceIView() {
        if (replaceIViewRef == null) {
            return null;
        }
        return replaceIViewRef.get();
    }

    public UIParam setReplaceIView(IView replaceIView) {
        this.replaceIViewRef = new WeakReference<>(replaceIView);
        return this;
    }

    public Runnable getUnloadRunnable() {
        if (unloadRunnableRef == null) {
            return null;
        }
        return unloadRunnableRef.get();
    }

    public UIParam setUnloadRunnable(Runnable unloadRunnable) {
        this.unloadRunnableRef = new WeakReference<>(unloadRunnable);
        return this;
    }

    public void clear() {
        if (mBundleRef != null) {
            mBundleRef.clear();
        }
        if (replaceIViewRef != null) {
            replaceIViewRef.clear();
        }
        if (unloadRunnableRef != null) {
            unloadRunnableRef.clear();
        }
    }
}
