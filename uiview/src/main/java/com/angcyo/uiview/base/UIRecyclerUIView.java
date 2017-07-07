package com.angcyo.uiview.base;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.angcyo.uiview.R;
import com.angcyo.uiview.container.UIParam;
import com.angcyo.uiview.recycler.RRecyclerView;
import com.angcyo.uiview.recycler.adapter.RBaseAdapter;
import com.angcyo.uiview.recycler.adapter.RExBaseAdapter;
import com.angcyo.uiview.rsen.RGestureDetector;
import com.angcyo.uiview.rsen.RefreshLayout;

/**
 * Created by angcyo on 2017-03-11.
 */

public abstract class UIRecyclerUIView<H, T, F> extends UIContentView
        implements RefreshLayout.OnRefreshListener, RBaseAdapter.OnAdapterLoadMoreListener {

    /**
     * 刷新控件
     */
    protected RefreshLayout mRefreshLayout;
    /**
     * 列表
     */
    protected RRecyclerView mRecyclerView;
    protected RExBaseAdapter<H, T, F> mExBaseAdapter;

    protected int mBaseOffsetSize;
    protected int mBaseLineSize;

    @NonNull
    @Override
    protected LayoutState getDefaultLayoutState() {
        return LayoutState.LOAD;
    }

    @Override
    public void onViewCreate(View rootView, UIParam param) {
        super.onViewCreate(rootView, param);
        mBaseOffsetSize = getDimensionPixelOffset(R.dimen.base_xhdpi);
        mBaseLineSize = getDimensionPixelOffset(R.dimen.base_line);
    }

    @Override
    final protected void inflateContentLayout(RelativeLayout baseContentLayout, LayoutInflater inflater) {
        beforeInflateView(baseContentLayout);

        createRecyclerRootView(baseContentLayout, inflater);

        afterInflateView(baseContentLayout);

        baseInitLayout();
    }

    /**
     * 复写此方法, 重写根布局
     */
    protected void createRecyclerRootView(RelativeLayout baseContentLayout, LayoutInflater inflater) {
        mRefreshLayout = new RefreshLayout(mActivity);
        initRefreshLayout(mRefreshLayout, baseContentLayout);
        mRecyclerView = new RRecyclerView(mActivity);
        initRecyclerView(mRecyclerView, baseContentLayout);
    }

    /**
     * 自动监听滚动事件, 设置标题栏的透明度
     */
    protected boolean hasScrollListener() {
        return false;
    }

    /**
     * 双击自动自动滚动置顶
     */
    public void onDoubleScrollToTop() {
        if (mRecyclerView != null) {
            mRecyclerView.smoothScrollToPosition(0);
        }
    }

    private void baseInitLayout() {
        if (getUITitleBarContainer() != null) {
            //双击标题, 自动滚动到顶部
            RGestureDetector.onDoubleTap(getUITitleBarContainer(), new RGestureDetector.OnDoubleTapListener() {
                @Override
                public void onDoubleTap() {
                    onDoubleScrollToTop();
                }
            });
        }

        if (mRecyclerView != null && hasScrollListener()) {
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (mUITitleBarContainer != null) {
                        mUITitleBarContainer.evaluateBackgroundColorSelf(recyclerView.computeVerticalScrollOffset());
                    }
                }
            });
        }
    }

    /**
     * 填充试图之前调用
     */
    protected void beforeInflateView(RelativeLayout baseContentLayout) {

    }

    /**
     * 内容试图填充之后调用
     */
    protected void afterInflateView(RelativeLayout baseContentLayout) {

    }

    protected void initRecyclerView(RRecyclerView recyclerView, RelativeLayout baseContentLayout) {
        if (recyclerView == null) {
            return;
        }
        final RecyclerView.ItemDecoration itemDecoration = createItemDecoration();
        if (itemDecoration != null) {
            recyclerView.addItemDecoration(itemDecoration);
        }
        mExBaseAdapter = createAdapter();
        recyclerView.setAdapter(mExBaseAdapter);

        if (mExBaseAdapter != null) {
            mExBaseAdapter.setOnLoadMoreListener(this);
        }

        if (recyclerView.getParent() == null) {
            if (mRefreshLayout == null) {
                baseContentLayout.addView(recyclerView, new ViewGroup.LayoutParams(-1, -1));
            } else {
                mRefreshLayout.addView(recyclerView, new ViewGroup.LayoutParams(-1, -1));
            }
        }
    }


    protected abstract RExBaseAdapter<H, T, F> createAdapter();

    protected RecyclerView.ItemDecoration createItemDecoration() {
        return null;
    }

    protected void initRefreshLayout(RefreshLayout refreshLayout, RelativeLayout baseContentLayout) {
        if (refreshLayout == null) {
            return;
        }
        refreshLayout.setRefreshDirection(RefreshLayout.TOP);
        refreshLayout.addOnRefreshListener(this);
        if (refreshLayout.getParent() == null) {
            baseContentLayout.addView(refreshLayout, new ViewGroup.LayoutParams(-1, -1));
        }
    }

    /**
     * 刷新控件, 刷新事件回调
     */
    @Override
    public void onRefresh(@RefreshLayout.Direction int direction) {
        if (direction == RefreshLayout.TOP) {
            //刷新事件
            onBaseLoadData();
        } else if (direction == RefreshLayout.BOTTOM) {
            //加载更多事件
            onBaseLoadMore();
        }
    }

    public void onBaseLoadMore() {

    }

    public void onBaseLoadData() {

    }

    /**
     * adapter加载更多回调
     */
    @Override
    public void onAdapterLodeMore(RBaseAdapter baseAdapter) {
        onBaseLoadMore();
    }
}
