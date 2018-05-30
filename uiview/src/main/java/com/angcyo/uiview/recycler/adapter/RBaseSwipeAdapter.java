package com.angcyo.uiview.recycler.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.view.View;

import com.angcyo.uiview.recycler.RBaseViewHolder;
import com.angcyo.uiview.recycler.RSwipeRecycleView;
import com.angcyo.uiview.recycler.widget.MenuBuilder;
import com.angcyo.uiview.recycler.widget.SwipeRecycleViewItemLayout;

import java.util.List;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：{@link com.angcyo.uiview.recycler.RSwipeRecycleView}
 * <p>
 * 需要配合 {@link RBaseSwipeAdapter} 才能正常使用
 * <p>
 * 创建人员：Robi
 * 创建时间：2017/05/10 17:42
 * 修改人员：Robi
 * 修改时间：2017/05/10 17:42
 * 修改备注：
 * Version: 1.0.0
 */
public abstract class RBaseSwipeAdapter<H, T, F> extends RExBaseAdapter<H, T, F> {
    private SparseArray<MenuBuilder> menuCache = new SparseArray<>();

    public RBaseSwipeAdapter(Context context) {
        super(context);
    }

    public RBaseSwipeAdapter(Context context, List<T> datas) {
        super(context, datas);
    }

    @Override
    protected int getItemLayoutId(int viewType) {
        return super.getItemLayoutId(viewType);
    }

    @NonNull
    @Override
    protected RBaseViewHolder createBaseViewHolder(int viewType, View itemView) {
        if (itemView instanceof SwipeRecycleViewItemLayout) {

        } else {
            SwipeRecycleViewItemLayout swipeRecycleViewItemLayout = new SwipeRecycleViewItemLayout(mContext);
            itemView.setClickable(true);//防止穿透内容布局, 直接点到了menu上
            swipeRecycleViewItemLayout.addView(itemView);
            swipeRecycleViewItemLayout.setLayoutParams(itemView.getLayoutParams());

            itemView = swipeRecycleViewItemLayout;
        }
        return super.createBaseViewHolder(viewType, itemView);
    }

    @Override
    protected void onBindCommonView(@NonNull RBaseViewHolder holder, int position, T bean) {
        super.onBindCommonView(holder, position, bean);

        MenuBuilder menuBuilder = menuCache.get(position);
        if (menuBuilder == null) {
            menuBuilder = new MenuBuilder(mContext);
            menuCache.put(position, menuBuilder);
        } else {
            menuBuilder.clear();
        }

        onBindMenuView(holder, menuBuilder, holder.getViewType(), position);
        menuBuilder.build((SwipeRecycleViewItemLayout) holder.itemView);
    }

    /**
     * 关闭所有侧滑按钮
     */
    public void closeAllMenu(RSwipeRecycleView swipeRecycleView) {
        if (swipeRecycleView != null) {
            swipeRecycleView.closeAllSwipeItem();
        }
    }


    @Deprecated
    protected void onBindMenuView(@NonNull MenuBuilder menuBuilder, int viewType, int position) {
    }

    /**
     * 重写此方法, 设置菜单
     */
    protected void onBindMenuView(@NonNull RBaseViewHolder holder, @NonNull MenuBuilder menuBuilder, int viewType, int position) {
        onBindMenuView(menuBuilder, viewType, position);
    }

}
