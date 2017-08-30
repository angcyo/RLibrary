package com.angcyo.uiview.base

import android.support.annotation.CallSuper
import android.view.View
import com.angcyo.uiview.container.UIParam
import com.angcyo.uiview.net.NonetException
import com.angcyo.uiview.net.RSubscriber
import com.angcyo.uiview.receiver.NetworkStateReceiver
import rx.Subscription
import rx.observers.SafeSubscriber
import rx.subscriptions.CompositeSubscription

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/08/18 13:55
 * 修改人员：Robi
 * 修改时间：2017/08/18 13:55
 * 修改备注：
 * Version: 1.0.0
 */
open abstract class UIIDialogRxImpl : UIIDialogImpl() {
    protected val mSubscriptions: CompositeSubscription by lazy {
        CompositeSubscription()
    }

    override fun onViewCreate(rootView: View, param: UIParam) {
        super.onViewCreate(rootView, param)
    }

    @CallSuper
    override fun onViewUnload() {
        super.onViewUnload()
        if (!mSubscriptions.isUnsubscribed) {
            mSubscriptions.unsubscribe()
        }
    }

    override fun release() {
        super.release()
        onCancel()
    }

    fun onCancel() {
        mSubscriptions.clear()
    }

    fun add(subscription: Subscription) {
        mSubscriptions.add(subscription)
        if (NetworkStateReceiver.getNetType().value() < 2) {
            //2G网络以下, 取消网络请求
            onCancel()
            try {
                if (subscription is SafeSubscriber<*>) {
                    if (subscription.actual is RSubscriber<*>) {
                        subscription.actual.onError(NonetException())
                    }
                }
            } catch (e: Exception) {

            }

        }
    }
}