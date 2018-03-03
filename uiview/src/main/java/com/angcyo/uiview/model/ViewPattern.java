package com.angcyo.uiview.model;

import android.view.View;

import com.angcyo.uiview.view.IView;

/**
 * Created by angcyo on 2016-11-12.
 */

public class ViewPattern {
    public IView mIView;
    public View mView;
    public boolean isAnimToEnd = false;//正在播放退出动画
    public boolean isAnimToStart = false;//正在播放进场动画

    public boolean isIViewHide = false;//界面是否被隐藏了, 调用了 HideIView

    public boolean interrupt = false;//在还没有启动完成的时候, 马上调用结束. 需要中断启动操作

    public ViewPattern(IView IView) {
        mIView = IView;
    }

    public ViewPattern(IView IView, View view) {
        mIView = IView;
        mView = view;
    }

    public ViewPattern setView(View view) {
        mView = view;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        return mIView == ((ViewPattern) obj).mIView;
    }

    public <T extends IView> T getIView() {
        return (T) mIView;
    }

    public ViewPattern setIView(IView IView) {
        mIView = IView;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(" mIView:").append(mIView);
        builder.append("\n");
        builder.append(" mView:").append(mView);
        builder.append("\n");
        builder.append(" isAnimToStart:").append(isAnimToStart);
        builder.append("\n");
        builder.append(" isAnimToEnd:").append(isAnimToEnd);
        builder.append("\n");
        builder.append(" interrupt:").append(interrupt);
        builder.append(" isIViewHide:").append(isIViewHide);
        builder.append("\n");
        return builder.toString();
    }

    public void clear() {
        mIView = null;
        mView = null;
    }
}
