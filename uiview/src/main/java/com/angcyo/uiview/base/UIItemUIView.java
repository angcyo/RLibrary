package com.angcyo.uiview.base;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.angcyo.uiview.R;
import com.angcyo.uiview.container.ContentLayout;
import com.angcyo.uiview.model.TitleBarPattern;
import com.angcyo.uiview.recycler.RBaseViewHolder;
import com.angcyo.uiview.recycler.RExItemDecoration;
import com.angcyo.uiview.recycler.RRecyclerView;
import com.angcyo.uiview.recycler.adapter.RExBaseAdapter;
import com.angcyo.uiview.rsen.PlaceholderView;
import com.angcyo.uiview.rsen.RefreshLayout;
import com.angcyo.uiview.widget.ItemInfoLayout;
import com.angcyo.uiview.widget.RSoftInputLayout;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by angcyo on 2017-03-12.
 */

public abstract class UIItemUIView<T extends Item> extends UIRecyclerUIView<String, T, String> {

    protected List<T> mItems = new ArrayList<>();
    protected RSoftInputLayout mSoftInputLayout;

    public static void baseInitItem(RBaseViewHolder holder, String itemText, View.OnClickListener onClickListener) {
        ItemInfoLayout infoLayout = holder.v(R.id.base_item_info_layout);
        infoLayout.setItemText(itemText);
        infoLayout.setOnClickListener(onClickListener);
    }

    public static void baseInitItem(RBaseViewHolder holder, @DrawableRes int leftRes, String itemText, View.OnClickListener onClickListener) {
        ItemInfoLayout infoLayout = holder.v(R.id.base_item_info_layout);
        infoLayout.setItemText(itemText);
        infoLayout.setLeftDrawableRes(leftRes);
        infoLayout.setOnClickListener(onClickListener);
    }

    public static void baseInitItem(RBaseViewHolder holder, @DrawableRes int leftRes, String itemText, int leftPadding, View.OnClickListener onClickListener) {
        ItemInfoLayout infoLayout = holder.v(R.id.base_item_info_layout);
        infoLayout.setItemText(itemText);
        infoLayout.setLeftDrawableRes(leftRes);
        infoLayout.setLeftDrawPadding(leftPadding);
        infoLayout.setOnClickListener(onClickListener);
    }

    @NonNull
    @Override
    protected LayoutState getDefaultLayoutState() {
        return LayoutState.CONTENT;
    }

    @Override
    public int getDefaultBackgroundColor() {
        return getColor(R.color.base_chat_bg_color);
    }

    @Override
    protected TitleBarPattern getTitleBar() {
        return super.getTitleBar().setShowBackImageView(true);
    }

    @Override
    protected RExBaseAdapter<String, T, String> createAdapter() {
        refreshLayout();
        return new RExBaseAdapter<String, T, String>(mActivity, mItems) {
            @Override
            protected void onBindDataView(RBaseViewHolder holder, int posInData, T dataBean) {
                dataBean.onBindView(holder, posInData, dataBean);
            }

            @Override
            protected int getDataItemType(int posInData) {
                return posInData;
            }

            @Override
            protected int getItemLayoutId(int viewType) {
                return UIItemUIView.this.getItemLayoutId(viewType);
            }
        };
    }

    /**
     * 更新布局
     */
    public void refreshLayout() {
        mItems.clear();
        createItems(mItems);
        if (mExBaseAdapter != null) {
            mExBaseAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 返回布局
     */
    protected int getItemLayoutId(int viewType) {
        return R.layout.base_item_info_layout;
    }

    /**
     * 创建Item
     */
    protected abstract void createItems(List<T> items);

    protected boolean isLast(int position) {
        return mExBaseAdapter.isLast(position);
    }


    @Override
    protected void createRecyclerRootView(ContentLayout baseContentLayout, LayoutInflater inflater) {
        //为软键盘弹出提供支持
        mSoftInputLayout = new RSoftInputLayout(mActivity);
        registerLifecycler(mSoftInputLayout);//隐藏的时候, 不处理键盘事件
        mRefreshLayout = new RefreshLayout(mActivity);
        mRefreshLayout.setRefreshDirection(RefreshLayout.TOP);
        mRefreshLayout.addOnRefreshListener(this);
        mSoftInputLayout.addView(mRefreshLayout, new ViewGroup.LayoutParams(-1, -1));
        baseContentLayout.addView(mSoftInputLayout, new ViewGroup.LayoutParams(-1, -1));

        mRecyclerView = new RRecyclerView(mActivity);
        initRecyclerView(mRecyclerView, baseContentLayout);
    }

    @Override
    protected void afterInflateView(ContentLayout baseContentLayout) {
        mRefreshLayout.setRefreshDirection(RefreshLayout.BOTH);
        mRefreshLayout.setTopView(new PlaceholderView(mActivity));
        mRefreshLayout.setBottomView(new PlaceholderView(mActivity));
        mRefreshLayout.setNotifyListener(false);

        mExBaseAdapter.setEnableLoadMore(false);

        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mRecyclerView.addItemDecoration(
                new RExItemDecoration(
                        new RExItemDecoration.SingleItemCallback() {


                            @Override
                            public void getItemOffsets2(Rect outRect, int position, int edge) {
                                T t = mItems.get(position);
                                t.setItemOffsets2(outRect, edge);
                            }

                            @Override
                            public void draw(Canvas canvas, TextPaint paint, View itemView, Rect offsetRect, int itemCount, int position) {
                                T t = mItems.get(position);
                                t.draw(canvas, paint, itemView, offsetRect, itemCount, position);
                            }
                        }));
    }
}
