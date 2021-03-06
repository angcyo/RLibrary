package com.angcyo.uiview.recycler;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;

import com.angcyo.uiview.recycler.adapter.RBaseAdapter;

import java.util.List;


/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：无限循环的RecyclerView, 换一个高级的实现方式 {@link RExLoopRecyclerView}
 * 创建人员：Robi
 * 创建时间：2017/03/01 11:58
 * 修改人员：Robi
 * 修改时间：2017/03/01 11:58
 * 修改备注：
 * Version: 1.0.0
 */
@Deprecated
public class RLoopRecyclerView extends RRecyclerView {

    private OnPageListener mOnPageListener;
    private RPagerSnapHelper mPagerSnapHelper;

    public RLoopRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RLoopRecyclerView(Context context) {
        this(context, null);
    }

    public RLoopRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //initView();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public LoopAdapter getAdapter() {
        if (isInEditMode()) {
            return null;
        }
        return (LoopAdapter) super.getAdapter();
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (isInEditMode()) {
            super.setAdapter(adapter);
            return;
        }

        if (!(adapter instanceof LoopAdapter)) {
            throw new IllegalArgumentException("adapter must  instanceof LoopAdapter!");
        }
        super.setAdapter(adapter);

    }

    /**
     * 恢复默认的滚动位置
     */
    public void resetScrollPosition() {
        curScrollPosition = getDefaultPosition();
        super.scrollTo(curScrollPosition, false);//开始时的偏移量
    }

    @Override
    public void startAutoScroll() {
        if (curScrollPosition <= 0) {
            resetScrollPosition();
            post(new Runnable() {
                @Override
                public void run() {
                    startAutoScroll();
                }
            });
            return;
        }

        LayoutManager layoutManager = getLayoutManager();
        if (enableScroll && getAdapter() != null && getAdapter().getItemRawCount() > 1 &&
                layoutManager != null && layoutManager instanceof LinearLayoutManager) {
            curScrollPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
            autoScroll();
        }
    }

    /**
     * 默认滚动至
     */
    protected int getDefaultPosition() {
        if (getAdapter() == null) {
            return 0;
        }
        return getAdapter().getItemRawCount() * 10000;
    }

//    @Override
//    public void scrollTo(int position, boolean anim) {
//        super.scrollTo(getDefaultPosition() + position, anim);
//    }


    @Override
    protected void initView(Context context) {
        super.initView(context);
        if (mPagerSnapHelper == null) {
            mPagerSnapHelper = new RPagerSnapHelper();
            mPagerSnapHelper.setOnPageListener(new RPagerSnapHelper.OnPageListener() {
                @Override
                public void onPageSelector(int position) {
                    int index = position % getAdapter().getItemRawCount();
                    if (mOnPageListener != null) {
                        mOnPageListener.onPageSelector(index);
                    }
                }
            });
        }
        mPagerSnapHelper.attachToRecyclerView(this);
    }

    public void setOnPageListener(OnPageListener onPageListener) {
        mOnPageListener = onPageListener;
    }

    public interface OnPageListener {
        void onPageSelector(int position);
    }

    /**
     * 循环数据源适配器
     */
    public static abstract class LoopAdapter<T> extends RBaseAdapter<T> {

        public LoopAdapter(Context context) {
            super(context);
        }

        public LoopAdapter(Context context, List<T> datas) {
            super(context, datas);
        }

        /**
         * 真实数据的大小
         */
        public int getItemRawCount() {
            return mAllDatas == null ? 0 : mAllDatas.size();
        }

        @Override
        final public int getItemType(int position) {
            return getLoopItemViewType(position % getItemRawCount());
        }

        protected int getLoopItemViewType(int position) {
            return 0;
        }

        @Override
        final protected void onBindView(RBaseViewHolder holder, int position, T bean) {
            //L.e("call: onBindView([holder, position, bean])-> " + position);
            int index = position % getItemRawCount();
            onBindLoopViewHolder(holder, index, mAllDatas.size() > index ? mAllDatas.get(index) : null);
        }

        public abstract void onBindLoopViewHolder(RBaseViewHolder holder, int position, T bean);

        @Override
        final public int getItemCount() {
            int rawCount = getItemRawCount();
            if (rawCount > 1) {
                return Integer.MAX_VALUE;
            }
            return rawCount;
        }
    }
}
