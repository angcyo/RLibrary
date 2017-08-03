package com.angcyo.uiview.base;

import android.support.annotation.CallSuper;
import android.view.View;

import com.angcyo.uiview.container.UIParam;
import com.angcyo.uiview.net.NonetException;
import com.angcyo.uiview.net.RSubscriber;
import com.angcyo.uiview.receiver.NetworkStateReceiver;

import rx.Subscription;
import rx.observers.SafeSubscriber;
import rx.subscriptions.CompositeSubscription;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2016/12/12 18:15
 * 修改人员：Robi
 * 修改时间：2016/12/12 18:15
 * 修改备注：
 * Version: 1.0.0
 */
public abstract class UIBaseRxView extends UIBaseDataView {
    protected CompositeSubscription mSubscriptions;

    @CallSuper
    @Override
    public void onViewCreate(View rootView) {
        super.onViewCreate(rootView);
        mSubscriptions = new CompositeSubscription();
    }

    @Override
    public void onViewCreate(View rootView, UIParam param) {
        super.onViewCreate(rootView, param);
    }

    @CallSuper
    @Override
    public void onViewUnload() {
        super.onViewUnload();
        if (!mSubscriptions.isUnsubscribed()) {
            mSubscriptions.unsubscribe();
        }
    }

    public void onCancel() {
        if (mSubscriptions != null) {
            mSubscriptions.clear();
        }
    }

    public void add(Subscription subscription) {
        if (mSubscriptions != null) {
            mSubscriptions.add(subscription);
        }
        if (NetworkStateReceiver.getNetType().value() < 2) {
            //2G网络以下, 取消网络请求
            onCancel();
            try {
                if (subscription instanceof SafeSubscriber) {
                    if (((SafeSubscriber) subscription).getActual() instanceof RSubscriber) {
                        ((SafeSubscriber) subscription).getActual().onError(new NonetException());
                    }
                }
            } catch (Exception e) {

            }
        }
    }
}
