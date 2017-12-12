package com.angcyo.uiview.recycler.adapter;

import android.content.Context;
import android.util.SparseIntArray;

import com.angcyo.uiview.recycler.RBaseViewHolder;

import java.util.List;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：当Item之间无关联, 一条一条数据往下布局时, 可以使用此类
 * 创建人员：Robi
 * 创建时间：2017/08/10 14:26
 * 修改人员：Robi
 * 修改时间：2017/08/10 14:26
 * 修改备注：
 * Version: 1.0.0
 */
public class RDataAdapter extends RExBaseAdapter<String, RBaseDataItem, String> {

    private SparseIntArray itemLayoutMap = new SparseIntArray();

    public RDataAdapter(Context context) {
        super(context);
    }

    public RDataAdapter(Context context, List<RBaseDataItem> datas) {
        super(context, datas);
    }

    /**
     * 保存itemType,对应的LayoutId
     */
    public void registerItemLayout(int itemType, int itemLayoutId) {
        itemLayoutMap.append(itemType, itemLayoutId);
    }

    @Override
    final public int getItemType(int position) {
        return super.getItemType(position);
    }

    @Override
    final protected int getHeaderItemType(int posInHeader) {
        return super.getHeaderItemType(posInHeader);
    }

    /**
     * 对应的ItemType
     */
    @Override
    protected int getDataItemType(int posInData) {
        return getAllDatas().get(posInData).getDataItemType();
    }

    @Override
    final protected int getFooterItemType(int posInFooter) {
        return super.getFooterItemType(posInFooter);
    }

    /**
     * 对应的LayoutId
     */
    @Override
    protected int getItemLayoutId(int viewType) {
        int layoutId = itemLayoutMap.get(viewType, -1);

        if (layoutId == -1) {
            for (RBaseDataItem item : getAllDatas()) {
                if (item.getDataItemType() == viewType) {
                    return item.getItemLayoutId();
                }
            }
            return super.getItemLayoutId(viewType);
        } else {
            return layoutId;
        }
    }


    /**
     * 绑定视图数据
     */
    @Override
    protected void onBindDataView(RBaseViewHolder holder, int posInData, RBaseDataItem dataBean) {
        super.onBindDataView(holder, posInData, dataBean);
        dataBean.onBindDataView(this, holder, posInData);
    }
}
