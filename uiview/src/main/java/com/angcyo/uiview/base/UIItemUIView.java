package com.angcyo.uiview.base;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.angcyo.uiview.R;
import com.angcyo.uiview.container.ContentLayout;
import com.angcyo.uiview.model.TitleBarPattern;
import com.angcyo.uiview.recycler.RBaseViewHolder;
import com.angcyo.uiview.recycler.RExItemDecoration;
import com.angcyo.uiview.recycler.RRecyclerView;
import com.angcyo.uiview.recycler.adapter.RExBaseAdapter;
import com.angcyo.uiview.rsen.PlaceholderView;
import com.angcyo.uiview.rsen.RefreshLayout;
import com.angcyo.uiview.view.RClickListener;
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

    public static void baseInitItem(RBaseViewHolder holder, String itemText, final View.OnClickListener onClickListener) {
        ItemInfoLayout infoLayout = holder.v(R.id.base_item_info_layout);
        infoLayout.setItemText(itemText);
        infoLayout.setOnClickListener(new RClickListener(300) {
            @Override
            public void onRClick(View view) {
                if (onClickListener != null) {
                    onClickListener.onClick(view);
                }
            }
        });
    }

    public static void baseInitItem(RBaseViewHolder holder, @DrawableRes int leftRes, String itemText, final View.OnClickListener onClickListener) {
        ItemInfoLayout infoLayout = holder.v(R.id.base_item_info_layout);
        infoLayout.setItemText(itemText);
        infoLayout.setLeftDrawableRes(leftRes);
        infoLayout.setOnClickListener(new RClickListener(300) {
            @Override
            public void onRClick(View view) {
                if (onClickListener != null) {
                    onClickListener.onClick(view);
                }
            }
        });
    }

    public static void baseInitItem(RBaseViewHolder holder, @DrawableRes int leftRes, String itemText, int leftPadding, final View.OnClickListener onClickListener) {
        ItemInfoLayout infoLayout = holder.v(R.id.base_item_info_layout);
        infoLayout.setItemText(itemText);
        infoLayout.setLeftDrawableRes(leftRes);
        infoLayout.setLeftDrawPadding(leftPadding);
        infoLayout.setOnClickListener(new RClickListener(300) {
            @Override
            public void onRClick(View view) {
                if (onClickListener != null) {
                    onClickListener.onClick(view);
                }
            }
        });
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
        return super.getTitleBar();
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
                T t = mItems.get(viewType);
                int itemLayoutId = t.getItemLayoutId();

                if (itemLayoutId == -1) {
                    return UIItemUIView.this.getItemLayoutId(viewType);
                } else {
                    return itemLayoutId;
                }
            }

            @Override
            protected View createItemView(ViewGroup parent, int viewType) {
                return UIItemUIView.this.createItemView(parent, viewType);
            }
        };
    }

    /**
     * 更新布局, 重新创建了items, 如果item的数量有变化, 建议使用这个方法
     */
    public void refreshLayout() {
        mItems.clear();
        createItems(mItems);
        if (mExBaseAdapter != null) {
            mExBaseAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 如果只是要更新item的数据, 建议使用此方法
     */
    public void updateItemsLayout() {
        if (mExBaseAdapter != null) {
            for (int i = 0; i < mItems.size(); i++) {
                T item = mItems.get(i);
                RBaseViewHolder viewHolder = mExBaseAdapter.getViewHolderFromPosition(i);
                if (viewHolder != null) {
                    item.onBindView(viewHolder, i, item);
                }
            }
        }
    }

    /**
     * 返回布局
     */
    protected int getItemLayoutId(int position) {
        return R.layout.base_item_info_layout;
    }

    protected View createItemView(ViewGroup parent, int position) {
        return null;
    }

    /**
     * 创建Item
     */
    protected abstract void createItems(List<T> items);

    protected boolean isLast(int position) {
        return mExBaseAdapter.isLast(position);
    }

    @Override
    public void onUILoadData(int page) {
        //
    }

    /**
     * 如果需要在界面中处理键盘事件, 请返回true, 否则 RSoftInputLayout 将忽略键盘处理
     */
    protected boolean haveSoftInput() {
        return false;
    }

    @Override
    protected void createRecyclerRootView(ContentLayout baseContentLayout, LayoutInflater inflater) {
        //为软键盘弹出提供支持
        mSoftInputLayout = new RSoftInputLayout(mActivity);
        registerLifecycler(mSoftInputLayout);//隐藏的时候, 不处理键盘事件
        mSoftInputLayout.setEnableSoftInput(haveSoftInput());

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
