package com.angcyo.uiview.recycler.adapter;

import android.support.annotation.NonNull;

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
    protected boolean isExpand = false;

    public RGroupData() {
        this(new ArrayList<T>());
    }

    public RGroupData(List<T> allDatas) {
        mAllDatas = allDatas;
    }

    public boolean isExpand() {
        return isExpand;
    }

    public RGroupData setExpand(boolean expand) {
        isExpand = expand;
        return this;
    }

    /**
     * 展开/关闭 分组数据的显示
     */
    public void setExpand(RGroupAdapter groupAdapter, boolean expand) {
        setExpand(groupAdapter, expand, false);
    }

    public void expandGroup(RGroupAdapter groupAdapter) {
        expandGroup(groupAdapter, false);
    }

    public void expandGroup(RGroupAdapter groupAdapter, boolean closeOther) {
        setExpand(groupAdapter, !isExpand, closeOther);
    }

    /**
     * 展开/关闭 分组数据的显示
     */
    public void setExpand(RGroupAdapter groupAdapter, boolean expand, boolean closeOther /*关闭之前打开的分组*/) {
        if (isExpand == expand) {
            return;
        }
        if (closeOther) {
            groupAdapter.expandAll(false);
        }
        boolean oldExpand = isExpand;
        if (oldExpand) {
            //已经展开了, 那么就是关闭分组数据显示
            if (getDataCount() > 0) {
                int startPosition = groupAdapter.getPositionFromGroup(this) + getGroupCount();
                groupAdapter.notifyItemRangeRemoved(startPosition, getDataCount());
                groupAdapter.notifyItemRangeChanged(startPosition, groupAdapter.getItemCount());
            }
            isExpand = expand;
        } else {
            isExpand = expand;
            //已经关闭了, 需要展开
            int startPosition = groupAdapter.getPositionFromGroup(this) + getGroupCount();
            groupAdapter.notifyItemRangeInserted(startPosition, getDataCount());
            groupAdapter.notifyItemRangeChanged(startPosition, groupAdapter.getItemCount());
        }
        onExpandChanged(groupAdapter, oldExpand, expand);
        onDataSizeChanged(groupAdapter);
    }

    /**
     * 展开和关闭状态改变了
     */
    public void onExpandChanged(RGroupAdapter groupAdapter, boolean fromExpand, boolean toExpand) {

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
     * 返回数据需要显示的数量
     */
    public int getDataCount() {
        return isExpand ? (mAllDatas == null ? 0 : mAllDatas.size()) : 0;
    }

    /**
     * 数据真实的数量
     */
    public int getDataRawCount() {
        return mAllDatas == null ? 0 : mAllDatas.size();
    }

    public List<T> getAllDatas() {
        if (mAllDatas == null) {
            mAllDatas = new ArrayList<>();
        }
        return mAllDatas;
    }

    public void resetDatas(List<T> allDatas) {
        if (allDatas == null) {
            allDatas = new ArrayList<>();
        }
        mAllDatas = allDatas;
    }

    public void appendDatas(List<T> allDatas) {
        if (mAllDatas == null) {
            mAllDatas = new ArrayList<>();
        }
        if (allDatas == null) {
            allDatas = new ArrayList<>();
        }
        mAllDatas.addAll(allDatas);
    }

    public void appendData(T data) {
        if (mAllDatas == null) {
            mAllDatas = new ArrayList<>();
        }
        mAllDatas.add(data);
    }

    public void resetDatas(RGroupAdapter groupAdapter, List<T> allDatas) {

        int oldSize = RBaseAdapter.getListSize(mAllDatas);
        int newSize = RBaseAdapter.getListSize(allDatas);

        resetDatas(allDatas);

        if (oldSize == newSize) {
            int positionFromGroup = groupAdapter.getPositionFromGroup(this);
            groupAdapter.notifyItemRangeChanged(positionFromGroup, getCount());
        } else {
            groupAdapter.notifyDataSetChanged();

            onDataSizeChanged(groupAdapter);
        }
    }

    public void appendDatas(RGroupAdapter groupAdapter, List<T> allDatas) {

        if (allDatas == null || allDatas.size() == 0) {
            return;
        }

        int count = getCount();

        appendDatas(allDatas);

        int startPosition = groupAdapter.getPositionFromGroup(this) + count;

        groupAdapter.notifyItemRangeInserted(startPosition, allDatas.size());
        groupAdapter.notifyItemRangeChanged(startPosition, groupAdapter.getItemCount());

        onDataSizeChanged(groupAdapter);
    }

    /**
     * 更多所有datas
     */
    public void notifyItemUpdate(RGroupAdapter groupAdapter) {
        int startPosition = groupAdapter.getPositionFromGroup(this);
        groupAdapter.notifyItemRangeChanged(startPosition, getCount());
    }

    /**
     * 更新指定位置item
     */
    public void notifyItemUpdate(RGroupAdapter groupAdapter, int posInGroupData) {
        if (posInGroupData < 0) {
            return;
        }
        int startPosition = groupAdapter.getPositionFromGroup(this) + getGroupCount() + posInGroupData;
        groupAdapter.notifyItemChanged(startPosition);
    }

    public void notifyItemUpdate(RGroupAdapter groupAdapter, T data) {
        int index = getAllDatas().indexOf(data);
        notifyItemUpdate(groupAdapter, index);
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
     * 数据源的数量改变了, 用来支持头部悬停数据更新
     */
    public void onDataSizeChanged(@NonNull RGroupAdapter groupAdapter) {

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
    protected void onBindGroupView(@NonNull RBaseViewHolder holder, int position, int indexInGroup) {

    }

    /**
     * @param indexInData 在数据中, 从0开始的索引
     */
    protected void onBindDataView(@NonNull RBaseViewHolder holder, int position, int indexInData) {
        onBindDataView(holder, position, indexInData, getAllDatas().size() > indexInData ? getAllDatas().get(indexInData) : null);
    }

    protected void onBindDataView(@NonNull RBaseViewHolder holder, int position, int indexInData, T dataBean) {

    }
}
