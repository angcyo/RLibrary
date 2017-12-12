package com.angcyo.uiview.recycler.adapter;

import android.support.annotation.LayoutRes;

import com.angcyo.uiview.recycler.RBaseViewHolder;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：配合{@link RDataAdapter}使用,每一个Item, 自己决定自己的layout id, bind view等操作
 * 创建人员：Robi
 * 创建时间：2017/08/10 14:29
 * 修改人员：Robi
 * 修改时间：2017/08/10 14:29
 * 修改备注：
 * Version: 1.0.0
 */
public abstract class RBaseDataItem<T> {
    public static final int BASE_ITEM_TYPE = 0x101010;

    protected T mBaseData;

    public RBaseDataItem(T baseData) {
        mBaseData = baseData;
    }

    /**
     * 返回ItemType
     */
    public int getDataItemType() {
        return BASE_ITEM_TYPE;
    }

    /**
     * 返回LayoutId
     */
    @LayoutRes
    public int getItemLayoutId() {
        return -1;
    }

    /**
     * 绑定视图数据
     */
    protected void onBindDataView(RDataAdapter dataAdapter, RBaseViewHolder holder, int posInData) {

    }
}
