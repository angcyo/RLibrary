package com.angcyo.uiview.recycler.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.angcyo.uiview.recycler.RBaseViewHolder;
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
            View menuView = onCreateMenuView(swipeRecycleViewItemLayout, viewType);
            if (menuView == null) {
                menuView = new SwipeRecycleViewItemLayout.EmptyView(mContext);
            }
            FrameLayout.LayoutParams menuParams = new FrameLayout.LayoutParams(-2, -1);
            menuParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
            swipeRecycleViewItemLayout.addView(menuView, menuParams);


//            ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
//            FrameLayout.LayoutParams contentParams = new FrameLayout.LayoutParams(layoutParams);

            swipeRecycleViewItemLayout.addView(itemView);
            swipeRecycleViewItemLayout.setLayoutParams(itemView.getLayoutParams());

            itemView = swipeRecycleViewItemLayout;
        }
        return super.createBaseViewHolder(viewType, itemView);
    }

    @Override
    public void onBindViewHolder(RBaseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        onBindMenuView(holder, position);
    }

    /**
     * 重写此方法, 设置菜单
     */
    protected void onBindMenuView(RBaseViewHolder holder, int position) {

    }

    /**
     * 重写此方法, 创建菜单
     */
    protected View onCreateMenuView(ViewGroup parent, int viewType) {
        return null;
    }
}
