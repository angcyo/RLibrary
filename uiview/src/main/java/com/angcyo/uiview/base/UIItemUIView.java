package com.angcyo.uiview.base;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.angcyo.uiview.R;
import com.angcyo.uiview.container.ContentLayout;
import com.angcyo.uiview.model.TitleBarPattern;
import com.angcyo.uiview.recycler.RBaseViewHolder;
import com.angcyo.uiview.recycler.RExItemDecoration;
import com.angcyo.uiview.recycler.RRecyclerView;
import com.angcyo.uiview.recycler.adapter.RExBaseAdapter;
import com.angcyo.uiview.recycler.adapter.RItemAdapter;
import com.angcyo.uiview.rsen.PlaceholderView;
import com.angcyo.uiview.rsen.RefreshLayout;
import com.angcyo.uiview.view.RClickListener;
import com.angcyo.uiview.widget.ItemInfoLayout;
import com.angcyo.uiview.widget.RSoftInputLayout;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        return new RItemAdapter<T>(mActivity, mItems) {
            @Override
            public int getItemLayoutIdNeed(int posInData) {
                return UIItemUIView.this.getItemLayoutId(posInData);
            }

            @Nullable
            @Override
            protected View createItemView(@NotNull ViewGroup parent, int viewType) {
                return UIItemUIView.this.createItemView(parent, viewType);
            }

            @Override
            public void onScrollStateChanged(RRecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                UIItemUIView.this.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RRecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                UIItemUIView.this.onScrolled(recyclerView, dx, dy);
            }

            @Override
            public void onScrolledInTouch(RRecyclerView recyclerView, MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                super.onScrolledInTouch(recyclerView, e1, e2, distanceX, distanceY);
                UIItemUIView.this.onScrolledInTouch(recyclerView, e1, e2, distanceX, distanceY);
            }

            @Override
            public void onScrollStateEnd(RRecyclerView rRecyclerView, boolean firstItemVisible, boolean lastItemVisible, boolean topCanScroll, boolean bottomCanScroll) {
                super.onScrollStateEnd(rRecyclerView, firstItemVisible, lastItemVisible, topCanScroll, bottomCanScroll);
                UIItemUIView.this.onScrollStateEnd(rRecyclerView, firstItemVisible, lastItemVisible, topCanScroll, bottomCanScroll);
            }
        };
    }

    public void onScrollStateChanged(RRecyclerView recyclerView, int newState) {
    }

    public void onScrolled(RRecyclerView recyclerView, int dx, int dy) {
    }

    public void onScrolledInTouch(RRecyclerView recyclerView, MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    }

    public void onScrollStateEnd(RRecyclerView rRecyclerView, boolean firstItemVisible, boolean lastItemVisible, boolean topCanScroll, boolean bottomCanScroll) {
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
    protected void initRecyclerView(RRecyclerView recyclerView, ContentLayout baseContentLayout) {
        recyclerView.setItemAnim(false);
        recyclerView.setSupportsChangeAnimations(false);
        super.initRecyclerView(recyclerView, baseContentLayout);
    }

    @Override
    protected void afterInflateView(ContentLayout baseContentLayout) {
        mRefreshLayout.setRefreshDirection(RefreshLayout.BOTH);
        mRefreshLayout.setTopView(new PlaceholderView(mActivity));
        mRefreshLayout.setBottomView(new PlaceholderView(mActivity));
        mRefreshLayout.setNotifyListener(false);

        mExBaseAdapter.setEnableLoadMore(false);

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
