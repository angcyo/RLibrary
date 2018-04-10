package com.angcyo.uiview.recycler.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.angcyo.library.utils.L;
import com.angcyo.uiview.R;
import com.angcyo.uiview.RApplication;
import com.angcyo.uiview.recycler.RBaseViewHolder;
import com.angcyo.uiview.recycler.RRecyclerView;
import com.angcyo.uiview.recycler.widget.ILoadMore;
import com.angcyo.uiview.recycler.widget.IShowState;
import com.angcyo.uiview.recycler.widget.ItemShowStateLayout;
import com.angcyo.uiview.utils.RUtils;
import com.brandongogetap.stickyheaders.exposed.StickyHeaderHandler;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Func2;

/**
 * Created by angcyo on 16-01-18-018.
 */
public abstract class RBaseAdapter<T> extends RecyclerView.Adapter<RBaseViewHolder>
        implements RecyclerView.OnChildAttachStateChangeListener, StickyHeaderHandler {

    public static final int ITEM_TYPE_LOAD_MORE = 666;
    public static final int ITEM_TYPE_SHOW_STATE = 667;
    protected List<T> mAllDatas;
    protected Context mContext;
    /**
     * 是否激活加载更多
     */
    protected boolean mEnableLoadMore = false;

    /**
     * 当调用setLoadMoreEnd时, 是否自动开启mEnableLoadMore
     */
    protected boolean mAutoEnableLoadMore = true;

    /**
     * 激活加载到倒数第几个item时, 回调加载更多, 此值需要mEnableLoadMore=true
     * -1, 表示关闭
     */
    protected int mEnableLoadMoreWithLastIndex = -1;

    protected ILoadMore mLoadMoreView;
    protected OnAdapterLoadMoreListener mLoadMoreListener;
    /**
     * 是否激活布局状态显示, 可以在Item中显示,空布局, 无网络布局, 加载中布局,和错误布局
     */
    protected boolean mEnableShowState = true;
    protected IShowState mIShowState;
    /**
     * 当前加载状态
     */
    int mLoadState = ILoadMore.NORMAL;
    /**
     * 当前显示的状态
     */
    int mShowState = IShowState.NORMAL;
    /**
     * 切换显示状态, 是否执行动画
     */
    boolean animToShowState = false;


    public RBaseAdapter(Context context) {
        mAllDatas = new ArrayList<>();
        this.mContext = context;
    }

    public RBaseAdapter(Context context, List<T> datas) {
        this.mAllDatas = datas == null ? new ArrayList<T>() : datas;
        this.mContext = context;
    }

    public static int getListSize(List list) {
        return list == null ? 0 : list.size();
    }

    /**
     * 使用布局局部刷新
     */
    public static void localRefresh(RecyclerView recyclerView, OnLocalRefresh localRefresh) {
        if (recyclerView == null || localRefresh == null) {
            return;
        }
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            int firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
            for (int i = 0; i < layoutManager.getChildCount(); i++) {
                int position = firstVisibleItemPosition + i;
                RBaseViewHolder vh = (RBaseViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
                if (vh != null) {
                    localRefresh.onLocalRefresh(vh, position);
                }
            }
        }
    }

    public RBaseAdapter setOnLoadMoreListener(OnAdapterLoadMoreListener loadMoreListener) {
        mLoadMoreListener = loadMoreListener;
        return this;
    }

    @Override
    public void onViewAttachedToWindow(RBaseViewHolder holder) {
        super.onViewAttachedToWindow(holder);
//        L.w("onViewAttachedToWindow");
    }

    //--------------标准的方法-------------//

    @Override
    public void onViewDetachedFromWindow(RBaseViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
//        L.w("onViewDetachedFromWindow");
    }

    /**
     * 返回是否激活加载更多
     */
    public boolean isEnableLoadMore() {
        return mEnableLoadMore || mEnableLoadMoreWithLastIndex != -1;
    }

    /**
     * 启用加载更多功能
     */
    public void setEnableLoadMore(boolean enableLoadMore) {
        boolean loadMore = mEnableLoadMore;
        mEnableLoadMore = enableLoadMore;

        if (isStateLayout()) {
            return;
        }

        if (enableLoadMore && !loadMore) {
            notifyItemInserted(getLastPosition());
        } else if (!enableLoadMore && loadMore) {
            notifyItemRemoved(getLastPosition());
        }
    }

//    /**用来实现...*/
//    @NonNull
//    protected RBaseViewHolder createItemViewHolder(ViewGroup parent, int viewType) {
//        return null;
//    }

    //是否该显示状态布局
    protected boolean isStateLayout() {
        return mEnableShowState && mShowState != IShowState.NORMAL;
    }

    @NonNull
    protected RBaseViewHolder createBaseViewHolder(int viewType, View itemView) {
        return new RBaseViewHolder(itemView, viewType);
    }

    @Override
    final public void onBindViewHolder(RBaseViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        //L.e("call: onBindViewHolder([holder, position, payloads])-> " + position);
    }

    @Override
    final public int getItemViewType(int position) {
        if (isStateLayout()) {
            return ITEM_TYPE_SHOW_STATE;
        }
        if (mEnableLoadMore && isLast(position)) {
            return ITEM_TYPE_LOAD_MORE;
        }
        return getItemType(position);
    }

    @Override
    public RBaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        RBaseViewHolder viewHolder = null;
        int itemLayoutId = -1;
        try {

            if (mContext == null) {
                L.w("RBaseViewHolder onCreateViewHolder:注意 mContext=null, 已使用parent.getContext()替换");
                mContext = parent.getContext();
            }

            if (mContext == null) {
                throw new NullPointerException("RBaseViewHolder onCreateViewHolder:错误 mContext=null");
            }

            if (mEnableShowState && viewType == ITEM_TYPE_SHOW_STATE) {
                itemView = LayoutInflater.from(mContext)
                        .inflate(R.layout.base_item_show_state_layout, parent, false);
                mIShowState = (IShowState) itemView;
            } else if (mEnableLoadMore && viewType == ITEM_TYPE_LOAD_MORE) {
                itemView = LayoutInflater.from(mContext)
                        .inflate(R.layout.base_item_load_more_layout, parent, false);
                mLoadMoreView = (ILoadMore) itemView;
            } else {
                itemView = createItemView(parent, viewType);
                if (itemView == null) {
                    itemLayoutId = getItemLayoutId(viewType);
                    try {
                        itemView = LayoutInflater.from(mContext).inflate(itemLayoutId, parent, false);
                    } catch (Exception e) {
                        L.e("xml inflate 失败, 请检查自定义View的应用路径.");
                        e.printStackTrace();
                    }
                }
            }
            viewHolder = createBaseViewHolder(viewType, itemView);
        } catch (Exception e) {
            L.e("请及时处理此处BUG. (RBaseAdapter.java:150)#onCreateViewHolder \nitemView=" + itemView);
            e.printStackTrace();
        }
        if (viewHolder == null) {
            throw new NullPointerException("RBaseViewHolder 创建失败, itemView=" + itemView + " itemLayoutId:" + itemLayoutId);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RBaseViewHolder holder, int position) {
        //L.e("call: onBindViewHolder([holder, position])-> " + position);
        try {
            if (isStateLayout()) {
                if (mIShowState != null) {
                    mIShowState.setShowState(mShowState);
                    onBindShowStateView((ItemShowStateLayout) mIShowState, mShowState);
                }
            } else if (mEnableLoadMore && isLast(position)) {
                /**如果第一个就是加载更多的布局, 需要调用加载更多么?*/
                onBindLoadMore(position);
                onBindLoadMoreView(holder, position);
            } else {
                if ((mEnableLoadMoreWithLastIndex > 0 &&
                        (getAllDataCount() - position) <= mEnableLoadMoreWithLastIndex)) {
                    onBindLoadMore(position);
                }
                onBindView(holder, position, mAllDatas.size() > position ? mAllDatas.get(position) : null);
            }
        } catch (Exception e) {
            L.e("请及时处理此处BUG.(RBaseAdapter.java:239)#onBindViewHolder#" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 不同的状态, 显示不同的布局
     */
    protected void onBindShowStateView(ItemShowStateLayout showStateLayout, int showState) {

    }

    /**
     * @see #onBindLoadMoreView(RBaseViewHolder, int)
     */
    @Deprecated
    private void onBindLoadMore(int position) {
        if (mLoadState == ILoadMore.NORMAL
                || mLoadState == ILoadMore.LOAD_ERROR) {

            /**如果第一个就是加载更多的布局, 需要调用加载更多么?*/
            if (position != 0) {
                mLoadState = ILoadMore.LOAD_MORE;
                onLoadMore();
                if (mLoadMoreListener != null) {
                    mLoadMoreListener.onAdapterLodeMore(this);
                }
            }
        }

        updateLoadMoreView();
    }

    /**
     * 重写此方法, 可以修改加载更多视图
     */
    protected void onBindLoadMoreView(@NonNull RBaseViewHolder holder, int position) {
        //holder.tv(R.id.base_load_tip_view).setText();
        //holder.tv(R.id.base_error_tip_view).setText();
        //holder.tv(R.id.base_no_more_tip_view).setText("");

        if (TextUtils.equals(RApplication.getApp().getPackageName(), "com.hn.d.valley")) {
            holder.tv(R.id.base_no_more_tip_view).setText("恐龙君到底啦");
        }
    }

    private void updateLoadMoreView() {
        if (mLoadMoreView != null) {
            mLoadMoreView.setLoadState(mLoadState);
        }
    }

    /**
     * 重写此方法, 实现加载更多功能
     */
    protected void onLoadMore() {

    }

    /**
     * 结束加载更多的标识, 方便下一次回调
     */
    public void setLoadMoreEnd() {
        mLoadState = ILoadMore.NORMAL;
        if (mAutoEnableLoadMore) {
            setEnableLoadMore(true);
        }
        updateLoadMoreView();
    }

    public void setLoadError() {
        mLoadState = ILoadMore.LOAD_ERROR;
        if (mAutoEnableLoadMore) {
            setEnableLoadMore(true);
        }
        updateLoadMoreView();
    }

    public void setNoMore() {
        setNoMore(false);
    }

    public void setNoMore(boolean refresh) {
        mLoadState = ILoadMore.NO_MORE;
        if (mAutoEnableLoadMore) {
            setEnableLoadMore(true);
        }
        if (refresh) {
            updateLoadMoreView();//不需要及时刷新
        }
    }

    public boolean isLast(int position) {
        return position == getLastPosition();
    }

    public boolean isBaseDataLast(int posInData) {
        return posInData == getDataLastPosition();
    }

    private int getLastPosition() {
        return getItemCount() - 1;
    }

    private int getDataLastPosition() {
        int dataSize = mAllDatas == null ? 0 : mAllDatas.size();
        return dataSize - 1;
    }

    //--------------需要实现的方法------------//

    /**
     * 数据是否为空
     */
    public boolean isItemEmpty() {
        if (mAllDatas == null || mAllDatas.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * 根据position返回Item的类型.
     */
    public int getItemType(int position) {
        return 0;
    }

    /**
     * 获取数据的数量
     */
    public int getAllDataCount() {
        return mAllDatas == null ? 0 : mAllDatas.size();
    }

    @Override
    public int getItemCount() {
        if (isStateLayout()) {
            return 1;
        }

        int size = mAllDatas == null ? 0 : mAllDatas.size();
        if (mEnableLoadMore) {
            size += 1;
        }
        return size;
    }

    /**
     * 当 {@link #getItemLayoutId(int)} 返回0的时候, 会调用此方法
     */
    protected View createItemView(ViewGroup parent, int viewType) {
        return null;
    }

    //---------------滚动事件的处理--------------------//

    protected abstract int getItemLayoutId(int viewType);

    protected abstract void onBindView(@NonNull RBaseViewHolder holder, int position, T bean);

    //----------------Item 数据的操作-----------------//

    public void onScrollStateChanged(RRecyclerView recyclerView, int newState) {
    }

    public void onScrolled(RRecyclerView recyclerView, int dx, int dy) {
    }

    public void onScrolledInTouch(RRecyclerView recyclerView, MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    }

    /**
     * 在最后的位置插入数据
     */
    public void addLastItem(T bean) {
        if (mAllDatas == null) {
            mAllDatas = new ArrayList<>();
        }
        int startPosition = mAllDatas.size();
        mAllDatas.add(bean);
        notifyItemInserted(startPosition);
        notifyItemRangeChanged(startPosition, getItemCount());
    }

    /**
     * 解决九宫格添加图片后,添加按钮消失时崩溃的bug
     */
    public void addLastItemSafe(T bean) {
        if (mAllDatas == null) {
            mAllDatas = new ArrayList<>();
        }

        int startPosition = mAllDatas.size();
        mAllDatas.add(bean);
        int itemCount = getItemCount();
        if (itemCount > startPosition + 1) {
            notifyItemInserted(startPosition);
            notifyItemRangeChanged(startPosition, getItemCount());
        } else {
            notifyItemChanged(itemCount - 1);//
        }
    }

    public void addFirstItem(T bean) {
        insertItem(0, bean);
    }

    public void insertItem(int position, T bean) {
        if (mAllDatas == null) {
            mAllDatas = new ArrayList<>();
        }
        mAllDatas.add(position, bean);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    /**
     * delete item with object
     */
    public void deleteItem(T bean) {
        if (mAllDatas != null) {
            int indexOf = mAllDatas.indexOf(bean);
            if (indexOf > -1) {
                deleteItem(indexOf);
            }
        }
    }

    public void deleteItem(int position) {
        if (mAllDatas != null) {
            int size = getItemCount();
            if (size > position) {
                if (onDeleteItem(position)) {
                    mAllDatas.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, size - position);
                }
            }
        }
    }

    /**
     * 是否可以删除bean
     */
    protected boolean onDeleteItem(int position) {
        return true;
    }

    public void removeFirstItem() {
        mAllDatas.remove(0);
        notifyItemRemoved(0);
        notifyItemRangeChanged(0, getItemCount());
    }

    public void removeLastItem() {
        int last = mAllDatas.size() - 1;
        mAllDatas.remove(last);
        notifyItemRemoved(last);
        notifyItemRangeChanged(last, getItemCount());
    }

    /**
     * 重置数据
     */
    public void resetData(List<T> datas) {
        int oldSize = getListSize(mAllDatas);
        int newSize = getListSize(datas);

        if (datas == null) {
            this.mAllDatas = new ArrayList<>();
        } else {
            this.mAllDatas = datas;
        }
        if (oldSize == newSize && newSize > 0) {
            if (isEnableLoadMore()) {
                oldSize += 1;
            }
            notifyItemRangeChanged(0, oldSize);
        } else {
            notifyDataSetChanged();
        }
    }

    public void resetData(List<T> datas, RDiffCallback<T> diffCallback) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new RDiffCallback<>(mAllDatas, datas, diffCallback));
        if (datas == null) {
            this.mAllDatas = new ArrayList<>();
        } else {
            this.mAllDatas = datas;
        }
        diffResult.dispatchUpdatesTo(this);
    }

    /**
     * 追加数据
     */
    public void appendData(List<T> datas) {
        if (datas == null || datas.size() == 0) {
            return;
        }
        if (this.mAllDatas == null) {
            this.mAllDatas = new ArrayList<>();
        }
        int startPosition = this.mAllDatas.size();
        this.mAllDatas.addAll(datas);
        notifyItemRangeInserted(startPosition, datas.size());
        notifyItemRangeChanged(startPosition, getItemCount());
    }

    public List<T> getAllDatas() {
        return mAllDatas;
    }

    /**
     * 空布局, 无网络布局, 加载中布局,和错误布局
     */
    public void setEnableShowState(boolean enableShowState) {
        mEnableShowState = enableShowState;
    }

    /**
     * 设置布局显示状态
     *
     * @see IShowState
     */
    public void setShowState(int showState) {
        int oldState = this.mShowState;

        if (oldState == showState) {
            return;
        }
        this.mShowState = showState;

        if (isEnableLoadMore()) {
            setLoadMoreEnd();
        }

        if (mIShowState == null ||
                oldState == IShowState.NORMAL ||
                showState == IShowState.NORMAL) {
            if (mIShowState != null &&
                    mIShowState instanceof ItemShowStateLayout) {
                if (animToShowState) {
                    ((ItemShowStateLayout) mIShowState).animToHide(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    });
                } else {
                    notifyDataSetChanged();
                }
            } else {
                notifyDataSetChanged();
            }
        } else {
            mIShowState.setShowState(showState);
        }
    }

    public boolean isAutoEnableLoadMore() {
        return mAutoEnableLoadMore;
    }

    public void setAutoEnableLoadMore(boolean autoEnableLoadMore) {
        mAutoEnableLoadMore = autoEnableLoadMore;
    }

    @Override
    public void onChildViewAttachedToWindow(View view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams instanceof RecyclerView.LayoutParams) {
            int viewAdapterPosition = ((RecyclerView.LayoutParams) layoutParams).getViewAdapterPosition();
            int viewLayoutPosition = ((RecyclerView.LayoutParams) layoutParams).getViewLayoutPosition();
            onChildViewAttachedToWindow(view, viewAdapterPosition, viewLayoutPosition);
        }
    }

    @Override
    public void onChildViewDetachedFromWindow(View view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams instanceof RecyclerView.LayoutParams) {
            int viewAdapterPosition = ((RecyclerView.LayoutParams) layoutParams).getViewAdapterPosition();
            int viewLayoutPosition = ((RecyclerView.LayoutParams) layoutParams).getViewLayoutPosition();
            onChildViewDetachedFromWindow(view, viewAdapterPosition, viewLayoutPosition);
        }
    }

    protected void onChildViewAttachedToWindow(View view, int adapterPosition, int layoutPosition) {
        //L.v("call: onChildViewAttachedToWindow -> " + adapterPosition + " " + layoutPosition + " " + view);
    }

    protected void onChildViewDetachedFromWindow(View view, int adapterPosition, int layoutPosition) {
        //L.v("call: onChildViewDetachedFromWindow -> " + adapterPosition + " " + layoutPosition + " " + view);
    }

    public RBaseAdapter<T> setEnableLoadMoreWithLastIndex(int enableLoadMoreWithLastIndex) {
        mEnableLoadMoreWithLastIndex = enableLoadMoreWithLastIndex;
        return this;
    }

    public void onScrollStateEnd(RRecyclerView rRecyclerView, boolean firstItemVisible, boolean lastItemVisible, boolean topCanScroll, boolean bottomCanScroll) {

    }

    /**
     * 判断当前的数据, 是否和之前的数据一样, 用来在区分加载不同数据使用
     */
    public boolean isDataEqualPrev(int posInData, T bean) {
        if (posInData <= 0) {
            return false;
        }
        List<T> allDatas = getAllDatas();
        if (RUtils.isListEmpty(allDatas)) {
            return false;
        }
        T prevData = allDatas.get(posInData - 1);
        return isDataEqual(prevData, bean);
    }

    public boolean isDataEqual(T prevData, T data) {
        return prevData == data;
    }

    public boolean isDataEqualPrev(int posInData, T bean, Func2<T, T, Boolean> equal) {
        if (posInData <= 0) {
            return false;
        }
        List<T> allDatas = getAllDatas();
        if (RUtils.isListEmpty(allDatas)) {
            return false;
        }
        T prevData = allDatas.get(posInData - 1);
        return equal.call(prevData, bean);
    }

    /**
     * 刷新某一个item
     */
    public void notifyItemChanged(T item) {
        if (RUtils.isListEmpty(mAllDatas)) {
            return;
        }
        int indexOf = mAllDatas.indexOf(item);
        if (indexOf > -1) {
            notifyItemChanged(indexOf);
        }
    }

    /**
     * Item悬停库的支持
     */
    @Override
    public List<?> getAdapterData() {
        return mAllDatas;
    }

    public interface OnAdapterLoadMoreListener {
        void onAdapterLodeMore(RBaseAdapter baseAdapter);
    }

    public interface OnLocalRefresh {
        void onLocalRefresh(RBaseViewHolder viewHolder, int position);
    }

    public static class RDiffCallback<T> extends DiffUtil.Callback {
        List<T> oldDatas;
        List<T> newDatas;
        RDiffCallback<T> mDiffCallback;

        public RDiffCallback() {
        }

        public RDiffCallback(List<T> oldDatas, List<T> newDatas, RDiffCallback<T> diffCallback) {
            this.oldDatas = oldDatas;
            this.newDatas = newDatas;
            mDiffCallback = diffCallback;
        }

        @Override
        public int getOldListSize() {
            return getListSize(oldDatas);
        }

        @Override
        public int getNewListSize() {
            return getListSize(newDatas);
        }

        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            return super.getChangePayload(oldItemPosition, newItemPosition);
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return mDiffCallback.areItemsTheSame(oldDatas.get(oldItemPosition), newDatas.get(newItemPosition));
        }

        /**
         * 被DiffUtil调用，用来检查 两个item是否含有相同的数据
         * 这个方法仅仅在areItemsTheSame()返回true时，才调用。
         */
        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return mDiffCallback.areContentsTheSame(oldDatas.get(oldItemPosition), newDatas.get(newItemPosition));
        }

        /**
         * 重写此方法, 判断数据是否相等,
         * 如果item不相同, 会先调用 notifyItemRangeRemoved, 再调用 notifyItemRangeInserted
         */
        public boolean areItemsTheSame(@NonNull T oldData, @NonNull T newData) {
            return oldData.equals(newData);
        }

        /**
         * 重写此方法, 判断内容是否相等,
         * 如果内容不相等, 会调用notifyItemRangeChanged
         */
        public boolean areContentsTheSame(@NonNull T oldData, @NonNull T newData) {
            return oldData.equals(newData);
        }
    }

}
