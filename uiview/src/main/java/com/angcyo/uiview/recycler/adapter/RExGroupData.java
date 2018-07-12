package com.angcyo.uiview.recycler.adapter;

import android.support.annotation.NonNull;

import com.angcyo.uiview.recycler.RBaseViewHolder;
import com.angcyo.uiview.utils.RUtils;
import com.brandongogetap.stickyheaders.exposed.StickyHeader;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：分组数据, 请注意 默认分组 isExpand = false
 * 创建人员：Robi
 * 创建时间：2017/04/07 15:16
 * 修改人员：Robi
 * 修改时间：2017/04/07 15:16
 * 修改备注：
 * Version: 1.0.0
 */
public class RExGroupData<H, T> extends RGroupData<T> {

    /**
     * 分组下面的数据集合
     */
    protected List<H> mAllHeaderDatas;
    private boolean haveStickyHeader = false;

    public RExGroupData() {
        this(new ArrayList<H>(), new ArrayList<T>());
    }

    public RExGroupData(H headerData, List<T> allDatas) {
        super(allDatas);
        mAllHeaderDatas = new ArrayList<>();
        mAllHeaderDatas.add(headerData);
        checkHaveStickyHeader();
    }

    public RExGroupData(List<H> headerDatas, List<T> allDatas) {
        super(allDatas);
        mAllHeaderDatas = headerDatas;
        checkHaveStickyHeader();
    }

    public void checkHaveStickyHeader() {
        if (!RUtils.isListEmpty(mAllHeaderDatas)) {
            if (mAllHeaderDatas.get(0) instanceof StickyHeader) {
                haveStickyHeader = true;
            }
        }
    }

    /**
     * 分组头部的数量, 返回0表示没有头部信息
     */
    public int getGroupCount() {
        return mAllHeaderDatas.size();
    }

    public List<H> getAllHeaderDatas() {
        return mAllHeaderDatas;
    }

    @Override
    public void onDataSizeChanged(@NonNull RGroupAdapter groupAdapter) {
        super.onDataSizeChanged(groupAdapter);
        if (haveStickyHeader) {
            groupAdapter.updateStickyDataList();
        }
    }

    @Override
    public void onExpandChanged(@NonNull RGroupAdapter groupAdapter, boolean fromExpand, boolean toExpand) {
        super.onExpandChanged(groupAdapter, fromExpand, toExpand);
        int startPosition = groupAdapter.getPositionFromGroup(this);
        groupAdapter.notifyItemChanged(startPosition);
    }

    @Override
    protected void onBindGroupView(@NonNull RBaseViewHolder holder, int position, int indexInGroup) {
        super.onBindGroupView(holder, position, indexInGroup);
        onBindGroupView(holder, position, indexInGroup, getAllHeaderDatas().size() > indexInGroup ? getAllHeaderDatas().get(indexInGroup) : null);
    }

    protected void onBindGroupView(@NonNull RBaseViewHolder holder, int position, int indexInGroup, H headerData) {

    }

    @Override
    protected void onBindDataView(@NonNull RBaseViewHolder holder, int position, int indexInData, T dataBean) {
        super.onBindDataView(holder, position, indexInData, dataBean);
    }
}
