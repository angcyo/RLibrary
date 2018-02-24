package com.angcyo.uiview.net;

import android.text.TextUtils;

import com.angcyo.uiview.container.ILayout;
import com.angcyo.uiview.dialog.UILoading;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2016/12/15 11:53
 * 修改人员：Robi
 * 修改时间：2016/12/15 11:53
 * 修改备注：
 * Version: 1.0.0
 */
public abstract class RLoadingSubscriber<T> extends RSubscriber<T> {

    protected ILayout ilayout;
    private String tip;

    public RLoadingSubscriber(ILayout ilayout) {
        this.ilayout = ilayout;
    }

    public RLoadingSubscriber(ILayout ilayout, String tip) {
        this.ilayout = ilayout;
        this.tip = tip;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (TextUtils.isEmpty(tip)) {
            UILoading.show2(ilayout);
        } else {
            UILoading.show2(ilayout).setLoadingTipText(tip);
        }
    }

    @Override
    public void onSucceed(T bean) {
        super.onSucceed(bean);
        UILoading.hide();
    }

    @Override
    public void onError(int code, String msg) {
        super.onError(code, msg);
        UILoading.hide();
    }

    @Override
    public void onEnd(boolean isError, boolean isNoNetwork, RException e) {
        super.onEnd(isError, isNoNetwork, e);
        UILoading.hide();
    }
}
