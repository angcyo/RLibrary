package com.angcyo.uiview.recycler.adapter;

import com.angcyo.uiview.recycler.RBaseViewHolder;
import com.angcyo.uiview.recycler.RRecyclerView;

import java.util.ArrayList;
import java.util.List;

import static com.angcyo.uiview.recycler.adapter.RGroupAdapter.TYPE_GROUP_DATA;
import static com.angcyo.uiview.recycler.adapter.RGroupAdapter.TYPE_GROUP_HEAD;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：分组数据
 * 创建人员：Robi
 * 创建时间：2017/04/07 15:16
 * 修改人员：Robi
 * 修改时间：2017/04/07 15:16
 * 修改备注：
 * Version: 1.0.0
 */
public class RGroupData<T> {


    /**
     * 分组下面的数据集合
     */
    protected List<T> mAllDatas;

    /**
     * 分组是否展开
     */
    protected boolean isExpand = true;

    public RGroupData() {
        this(new ArrayList<T>());
    }

    public RGroupData(List<T> allDatas) {
        mAllDatas = allDatas;
    }

    /**
     * 头部+数据的总数量
     */
    public int getCount() {
        return getGroupCount() + getDataCount();
    }

    /**
     * 分组头部的数量, 返回0表示没有头部信息
     */
    public int getGroupCount() {
        return 1;
    }

    /**
     * 返回数据的数量
     */
    public int getDataCount() {
        return isExpand ? (mAllDatas == null ? 0 : mAllDatas.size()) : 0;
    }

    public List<T> getAllDatas() {
        if (mAllDatas == null) {
            mAllDatas = new ArrayList<>();
        }
        return mAllDatas;
    }

    public void resetDatas(List<T> allDatas) {
        mAllDatas = allDatas;
    }

    public void appendDatas(List<T> allDatas) {
        if (mAllDatas == null) {
            mAllDatas = new ArrayList<>();
        }
        mAllDatas.addAll(allDatas);
    }


    public void resetDatas(RGroupAdapter groupAdapter, List<T> allDatas) {

        int oldSize = RBaseAdapter.getListSize(mAllDatas);
        int newSize = RBaseAdapter.getListSize(allDatas);

        resetDatas(allDatas);

        if (mAllDatas == null) {
            this.mAllDatas = new ArrayList<>();
        } else {
            this.mAllDatas = allDatas;
        }

        if (oldSize == newSize) {
            int positionFromGroup = groupAdapter.getPositionFromGroup(this);
            groupAdapter.notifyItemRangeChanged(positionFromGroup, getCount());
        } else {
            groupAdapter.notifyDataSetChanged();
        }
    }

    public void appendDatas(RGroupAdapter groupAdapter, List<T> allDatas) {

        if (allDatas == null || allDatas.size() == 0) {
            return;
        }

        int count = getCount();

        appendDatas(allDatas);

        if (this.mAllDatas == null) {
            this.mAllDatas = new ArrayList<>();
        }

        int startPosition = groupAdapter.getPositionFromGroup(this) + count;

        groupAdapter.notifyItemRangeInserted(startPosition, allDatas.size());
        groupAdapter.notifyItemRangeChanged(startPosition, groupAdapter.getItemCount());
    }

    public void notifyItemUpdate(RGroupAdapter groupAdapter) {
        int startPosition = groupAdapter.getPositionFromGroup(this);
        groupAdapter.notifyItemRangeChanged(startPosition, getCount());
    }

    public void localRefresh(RRecyclerView recyclerView, final RGroupAdapter groupAdapter) {
        RBaseAdapter.localRefresh(recyclerView, new RBaseAdapter.OnLocalRefresh() {
            @Override
            public void onLocalRefresh(RBaseViewHolder viewHolder, int position) {
                RGroupData groupData = groupAdapter.getGroupDataFromPosition(position);
                if (groupData != null && RGroupData.this == groupData) {
                    int groupIndex = groupAdapter.getGroupIndex(position);
                    if (groupIndex >= 0) {
                        //有分组
                        groupData.onBindGroupView(viewHolder, position, groupIndex);
                    }

                    int dataIndex = groupAdapter.getDataIndex(position);
                    if (dataIndex >= 0) {
                        groupData.onBindDataView(viewHolder, position, dataIndex);
                    }
                }
            }
        });
    }


    /**
     * @param indexInGroup 当有多个分组时, index表示从0开始的索引
     */
    public int getGroupLayoutId(int indexInGroup) {
        return -1;
    }

    public int getDataLayoutId(int indexInData) {
        return -1;
    }

    public int getGroupItemType(int indexInGroup) {
        return TYPE_GROUP_HEAD;
    }

    public int getDataItemType(int indexInData) {
        return TYPE_GROUP_DATA;
    }

    /**
     * @param indexInGroup 在头部数据中, 从0开始的索引
     */
    protected void onBindGroupView(RBaseViewHolder holder, int position, int indexInGroup) {

    }

    /**
     * @param indexInData 在数据中, 从0开始的索引
     */
    protected void onBindDataView(RBaseViewHolder holder, int position, int indexInData) {
        onBindDataView(holder, position, indexInData, getAllDatas().size() > indexInData ? getAllDatas().get(indexInData) : null);
    }

    protected void onBindDataView(RBaseViewHolder holder, int position, int indexInData, T bean) {

    }
}
