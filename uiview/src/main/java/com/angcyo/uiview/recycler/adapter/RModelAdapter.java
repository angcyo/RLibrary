package com.angcyo.uiview.recycler.adapter;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.view.View;
import android.widget.CompoundButton;

import com.angcyo.uiview.recycler.RBaseViewHolder;
import com.angcyo.uiview.recycler.RRecyclerView;
import com.angcyo.uiview.utils.Reflect;
import com.angcyo.uiview.widget.RCheckGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * 支持单选, 多选的Adapter
 * <p>
 * Created by angcyo on 2016-12-19.
 */

public abstract class RModelAdapter<T> extends RBaseAdapter<T> {
    /**
     * 正常
     */
    public static final int MODEL_NORMAL = 1;
    /**
     * 单选
     */
    public static final int MODEL_SINGLE = MODEL_NORMAL << 1;
    /**
     * 多选
     */
    public static final int MODEL_MULTI = MODEL_SINGLE << 1;

    /**
     * 选中的数据列表
     */
    private ArrayList<T> mSelectorList = new ArrayList<>();

    @Model
    private int mModel = MODEL_NORMAL;

    private HashSet<OnModelChangeListener> mChangeListeners = new HashSet<>();

    private ArrayMap<Integer, RBaseViewHolder> mBaseViewHolderMap = new ArrayMap<>();

    public RModelAdapter(Context context) {
        super(context);
    }

    public RModelAdapter(Context context, List<T> datas) {
        super(context, datas);
    }

    public RModelAdapter(Context context, int model) {
        super(context);
        mModel = model;
    }

    public static void checkedButton(CompoundButton compoundButton, boolean checked) {
        if (compoundButton == null) {
            return;
        }
        if (compoundButton.isChecked() == checked) {
            return;
        }
        CompoundButton.OnCheckedChangeListener onCheckedChangeListener =
                (CompoundButton.OnCheckedChangeListener) Reflect.getMember(CompoundButton.class,
                        compoundButton, "mOnCheckedChangeListener");
        compoundButton.setOnCheckedChangeListener(null);
        compoundButton.setChecked(checked);
        compoundButton.setOnCheckedChangeListener(onCheckedChangeListener);
    }

    @Override
    protected int getItemLayoutId(int viewType) {
        return 0;
    }

    @Override
    final protected void onBindView(@NonNull RBaseViewHolder holder, int position, T bean) {
//        L.e("call: onBindView([holder, position, bean])-> put:" + position);
        mBaseViewHolderMap.put(position, holder);
        onBindCommonView(holder, position, bean);
        if (mModel == MODEL_NORMAL) {
            onBindNormalView(holder, position, bean);
        } else {
            onBindModelView(mModel, isPositionSelector(position), holder, position, bean);
        }
    }

    @Override
    public void onViewDetachedFromWindow(RBaseViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
//        mBaseViewHolderMap.put(holder.getAdapterPosition(), null);
//        L.e("call: onViewDetachedFromWindow([holder])-> remove:" + holder.getAdapterPosition());
    }

    @Override
    public void onViewRecycled(RBaseViewHolder holder) {
        super.onViewRecycled(holder);
        //mBaseViewHolderMap.remove(holder.getAdapterPosition());
        //L.e("call: onViewRecycled([holder])->--------------------------------------------- " + holder.getAdapterPosition());
    }

    @Override
    public void onViewAttachedToWindow(RBaseViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    /**
     * 选择模式下, 和正常模式下都会执行
     */
    protected abstract void onBindCommonView(@NonNull RBaseViewHolder holder, int position, T bean);

    /**
     * 只在选择模式下(单选, 多选),会执行
     */
    protected void onBindModelView(int model, boolean isSelector, @NonNull RBaseViewHolder holder, int position, T bean) {

    }

    /**
     * 只在正常模式下,会执行
     */
    protected void onBindNormalView(@NonNull RBaseViewHolder holder, int position, T bean) {

    }

    /**
     * 在单选模式下, 选择其他项时, 将要先取消之前的选中项. 此时会执行此方法, 取消之前按钮的状态
     * 如果返回true {@link #onBindModelView(int, boolean, RBaseViewHolder, int, Object)}就不会执行
     *
     * @return true 表示处理, false 不处理
     */
    protected boolean onUnSelectorPosition(@NonNull RBaseViewHolder viewHolder, int position, boolean isSelector) {
        return false;
    }

    /**
     * 在单选模式下, 如果不需要自动处理CompoundButton状态的改变, 此时会执行此方法, 自己处理状态
     * 如果返回true {@link #onBindModelView(int, boolean, RBaseViewHolder, int, Object)}就不会执行
     *
     * @return true 表示处理, false 不处理
     */
    protected boolean onSelectorPosition(@NonNull RBaseViewHolder viewHolder, int position, boolean isSelector) {
        return false;
    }

    /**
     * 在执行 {@link #onUnSelectorPosition(RBaseViewHolder, int, boolean)}后, 调用此方法, 可以便捷的取消 CompoundButton 的状态
     */
    public void unSelector(@NonNull List<Integer> list, @NonNull RRecyclerView recyclerView, @NonNull String viewTag) {
        boolean notify = false;

        for (Integer pos : list) {
            removeSelectorPosition(pos);
            RBaseViewHolder vh = (RBaseViewHolder) recyclerView.findViewHolderForAdapterPosition(pos);
            if (vh != null) {
                final View view = vh.tag(viewTag);
                if (view != null) {
                    if (view instanceof CompoundButton) {
                        checkedButton((CompoundButton) view, false);
                    } else if (view instanceof RCheckGroup.ICheckView) {
                        ((RCheckGroup.ICheckView) view).setChecked(false);
                    }
                    notify = true;
                }
            } else {
                notifyItemChanged(pos);
            }
        }
        if (notify) {
            //防止在视图还没有加载的时候,通知事件
            notifySelectorChange();
        }
    }

    public void unSelector(int position, @NonNull RRecyclerView recyclerView, @NonNull String viewTag) {
        boolean notify = false;
        removeSelectorPosition(position);
        RBaseViewHolder vh = (RBaseViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
        if (vh != null) {
            final View view = vh.tag(viewTag);
            if (view != null) {
                if (view instanceof CompoundButton) {
                    checkedButton((CompoundButton) view, false);
                } else if (view instanceof RCheckGroup.ICheckView) {
                    ((RCheckGroup.ICheckView) view).setChecked(false);
                }
                notify = true;
            }
        } else {
            notifyItemChanged(position);
        }

        if (notify) {
            //防止在视图还没有加载的时候,通知事件
            notifySelectorChange();
        }
    }

    /**
     * 在执行 {@link #onUnSelectorPosition(RBaseViewHolder, int, boolean)}后, 调用此方法, 可以便捷的取消 CompoundButton 的状态
     */
    public void unSelector(@NonNull List<Integer> list, @NonNull RRecyclerView recyclerView, @IdRes int viewId) {
        boolean notify = false;

        for (Integer pos : list) {
            removeSelectorPosition(pos);
            RBaseViewHolder vh = (RBaseViewHolder) recyclerView.findViewHolderForAdapterPosition(pos);
            if (vh != null) {
                final View view = vh.v(viewId);
                if (view != null) {
                    if (view instanceof CompoundButton) {
                        checkedButton((CompoundButton) view, false);
                    } else if (view instanceof RCheckGroup.ICheckView) {
                        ((RCheckGroup.ICheckView) view).setChecked(false);
                    }
                    notify = true;
                }
            } else {
                notifyItemChanged(pos);
            }
        }

        if (notify) {
            //防止在视图还没有加载的时候,通知事件
            notifySelectorChange();
        }
    }

    public void unSelector(int position, @NonNull RRecyclerView recyclerView, @IdRes int viewId) {
        RBaseViewHolder vh = (RBaseViewHolder) recyclerView.findViewHolderForAdapterPosition(position);

        boolean notify = false;
        removeSelectorPosition(position);

        if (vh != null) {
            final View view = vh.v(viewId);
            if (view != null) {
                if (view instanceof CompoundButton) {
                    checkedButton((CompoundButton) view, false);
                } else if (view instanceof RCheckGroup.ICheckView) {
                    ((RCheckGroup.ICheckView) view).setChecked(false);
                }
                notify = true;
            }
        } else {
            notifyItemChanged(position);
        }

        if (notify) {
            //防止在视图还没有加载的时候,通知事件
            notifySelectorChange();
        }
    }

    /**
     * 取消所有选择
     */
    public void unSelectorAll(@NonNull RRecyclerView recyclerView, @IdRes int viewId) {
        for (Integer pos : getAllSelectorList()) {
            RBaseViewHolder vh = (RBaseViewHolder) recyclerView.findViewHolderForAdapterPosition(pos);
            if (vh != null) {
                final View view = vh.v(viewId);
                if (view != null) {
                    if (view instanceof CompoundButton) {
                        checkedButton((CompoundButton) view, false);
                    } else if (view instanceof RCheckGroup.ICheckView) {
                        ((RCheckGroup.ICheckView) view).setChecked(false);
                    }
                }
            } else {
                notifyItemChanged(pos);
            }
        }
        unSelectorAll(true);
    }

    /**
     * 通知选中数量改变了
     */
    public void notifySelectorChange() {
        List<Integer> allSelectorList = getAllSelectorList();
        if (getModel() == MODEL_SINGLE && allSelectorList.isEmpty()) {
            //单选模式下, 未选中, 不回调
            return;
        }
        onSelectorChange(allSelectorList);
        final Iterator<OnModelChangeListener> iterator = mChangeListeners.iterator();
        while (iterator.hasNext()) {
            iterator.next().onSelectorChange(allSelectorList);
        }
    }

    protected void onSelectorChange(@NonNull List<Integer> allSelectorList) {

    }

    /**
     * 取消所有选择
     */
    public void unSelectorAll(@NonNull RRecyclerView recyclerView, @NonNull String viewTag) {
        for (Integer pos : getAllSelectorList()) {
            RBaseViewHolder vh = (RBaseViewHolder) recyclerView.findViewHolderForAdapterPosition(pos);
            if (vh != null) {
                final View view = vh.tag(viewTag);
                if (view != null) {
                    if (view instanceof CompoundButton) {
                        checkedButton((CompoundButton) view, false);
                    } else if (view instanceof RCheckGroup.ICheckView) {
                        ((RCheckGroup.ICheckView) view).setChecked(false);
                    }
                }
            } else {
                notifyItemChanged(pos);
            }
        }
        unSelectorAll(true);
    }

    public void unSelectorAll(boolean refresh) {
        List<Integer> allSelectorList = getAllSelectorList();
        mSelectorList.clear();

        if (refresh) {
            //取消选择
            for (int position : allSelectorList) {
                removeSelectorInner(position);
            }

            notifySelectorChange();
        }
    }

    /**
     * 互斥的操作,调用此方法进行选择
     */
    public void setSelectorPosition(int position) {
        setSelectorPosition(position, null);
    }

    /**
     * 选中所有
     */
    public void setSelectorAll(@NonNull RRecyclerView recyclerView, @NonNull String viewTag) {
        if (mModel != MODEL_MULTI) {
            return;
        }

        mSelectorList.clear();
        mSelectorList.addAll(getAllDatas());

        boolean notify = false;

        for (Integer pos : getAllSelectorList()) {
            RBaseViewHolder vh = (RBaseViewHolder) recyclerView.findViewHolderForAdapterPosition(pos);
            if (vh != null) {
                final View view = vh.tag(viewTag);
                if (view != null) {
                    if (view instanceof CompoundButton) {
                        checkedButton((CompoundButton) view, true);
                    } else if (view instanceof RCheckGroup.ICheckView) {
                        ((RCheckGroup.ICheckView) view).setChecked(true);
                    }
                }
                notify = true;
            } else {
                notifyItemChanged(pos);
            }
        }

        if (notify) {
            //防止在视图还没有加载的时候,通知事件
            notifySelectorChange();
        }
    }

    /**
     * 选中所有
     */
    public void setSelectorAll(@NonNull RRecyclerView recyclerView, @IdRes int viewId) {
        setSelectedList(recyclerView, viewId, getAllDatas());
    }

    public void setSelectorPosition(int position, @NonNull RRecyclerView recyclerView, @IdRes int viewId) {
        List<Integer> indexs = new ArrayList<>();
        indexs.add(position);
        setSelectIndexs(recyclerView, viewId, indexs);
    }

    public void setSelectIndexs(@NonNull RRecyclerView recyclerView, @IdRes int viewId, List<Integer> indexList) {
        if (mModel != MODEL_MULTI) {
            return;
        }

        for (int i = 0; i < indexList.size(); i++) {
            T bean = getAllDatas().get(indexList.get(i));
            if (!mSelectorList.contains(bean)) {
                mSelectorList.add(bean);
            }
        }

        boolean notify = false;

        for (Integer pos : getAllSelectorList()) {
            RBaseViewHolder vh = (RBaseViewHolder) recyclerView.findViewHolderForAdapterPosition(pos);
            if (vh != null) {
                final View view = vh.v(viewId);
                if (view != null) {
                    if (view instanceof CompoundButton) {
                        checkedButton((CompoundButton) view, true);
                    } else if (view instanceof RCheckGroup.ICheckView) {
                        ((RCheckGroup.ICheckView) view).setChecked(true);
                    }
                }
                notify = true;
            } else {
                notifyItemChanged(pos);
            }
        }

        if (notify) {
            //防止在视图还没有加载的时候,通知事件
            notifySelectorChange();
        }
    }

    /**
     * 选中指定item
     */
    public void setSelectedList(@NonNull RRecyclerView recyclerView, @IdRes int viewId, List<T> selectedList) {
        List<Integer> indexs = new ArrayList<>();
        for (int i = 0; i < selectedList.size(); i++) {
            int index = mAllDatas.indexOf(selectedList.get(i));
            if (index != -1) {
                indexs.add(i);
            }
        }

        setSelectIndexs(recyclerView, viewId, indexs);
    }

    public void addOnModelChangeListener(OnModelChangeListener listener) {
        mChangeListeners.add(listener);
    }

    public void removeOnModelChangeListener(OnModelChangeListener listener) {
        mChangeListeners.remove(listener);
    }

    public ArrayList<Integer> getAllSelectorList() {
        ArrayList<Integer> integers = new ArrayList<>();
        List<T> allDatas = getAllDatas();
        for (T bean : mSelectorList) {
            integers.add(allDatas.indexOf(bean));
        }
        return integers;
    }

    public ArrayList<T> getAllSelectorData() {
        return getAllSelector();
    }

    public ArrayList<T> getAllSelector() {
        return mSelectorList;
    }

    private void addSelectorDataToList(T bean) {
        if (!mSelectorList.contains(bean)) {
            mSelectorList.add(bean);
        }
    }

    private void addSelectorDataToList(int position) {
        addSelectorDataToList(getAllDatas().get(position));
    }

    /**
     * 返回所有选中的数据
     */
    public List<T> getSelectorData() {
        return getAllSelector();
    }

    public void addSelectorData(T bean) {
        addSelectorInner(getAllDatas().indexOf(bean));
    }

    public void removeSelectorData(T bean) {
        removeSelectorInner(getAllDatas().indexOf(bean));
    }

    /**
     * 互斥的操作, 选择item, 并且自动设置CompoundButton的状态
     */
    public void setSelectorPosition(int position, Object view) {
        if (mModel == MODEL_NORMAL || !isPositionValid(position)) {
            return;
        }
        setSelectorData(getAllDatas().get(position), view);
    }

    public void setSelectorData(T data, Object view) {
        if (mModel == MODEL_NORMAL) {
            return;
        }
        int position = getAllDatas().indexOf(data);
        if (!isPositionValid(position)) {
            return;
        }

        RBaseViewHolder viewHolder = getViewHolderFromPosition(position);
        final boolean selector = mSelectorList.contains(data);

        if (selector) {
            //之前已经选中了
            if (mModel == MODEL_SINGLE) {
                return;
            } else {
                removeSelectorInner(position);
            }
        } else {
            if (mModel == MODEL_SINGLE) {
                List<Integer> allSelectorList = getAllSelectorList();
                for (Integer pos : allSelectorList) {
                    removeSelectorInner(pos);
                }
                mSelectorList.clear();
            }
            addSelectorInner(position);
        }

        if (view instanceof Integer) {
            view = viewHolder.v((Integer) view);
        }

        if (view instanceof CompoundButton) {
            checkedButton((CompoundButton) view, !selector);
        } else if (view instanceof RCheckGroup.ICheckView) {
            ((RCheckGroup.ICheckView) view).setChecked(!selector);
        }

        notifySelectorChange();
    }

    protected void removeSelectorInner(int position) {
        if (!isPositionValid(position)) {
            return;
        }

        RBaseViewHolder viewHolder = getViewHolderFromPosition(position);

        mSelectorList.remove(getAllDatas().get(position));
        if (viewHolder != null) {
            if (!onUnSelectorPosition(viewHolder, position, false)) {
                onBindModelView(mModel, false, viewHolder, position,
                        getAllDatas().size() > position ? getAllDatas().get(position) : null);
            }
        }
    }

    protected void addSelectorInner(int position) {
        if (!isPositionValid(position)) {
            return;
        }

        RBaseViewHolder viewHolder = getViewHolderFromPosition(position);

        addSelectorDataToList(position);
        if (viewHolder != null) {
            if (!onSelectorPosition(viewHolder, position, true)) {
                onBindModelView(mModel, true, viewHolder, position,
                        getAllDatas().size() > position ? getAllDatas().get(position) : null);
            }
        }
    }

    /**
     * 索引是否没有越界
     */
    public boolean isPositionValid(int position) {
        return position >= 0 && position < getAllDatas().size();
    }

    public boolean isPositionSelector(int position) {
        if (!isPositionValid(position)) {
            return false;
        }

        T bean = getAllDatas().get(position);
        if (mSelectorList.contains(bean)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 添加选中位置, 不做任何其他操作, (默认会触发回调)
     */
    public void addSelectorPosition(int position) {
        addSelectorPosition(position, true);
    }

    public void addSelectorPosition(int position, boolean notify) {
        if (mModel == MODEL_SINGLE) {
            mSelectorList.clear();
        }
        addSelectorDataToList(position);
        if (notify) {
            notifySelectorChange();
        }
    }

    public void removeSelectorPosition(int position) {
        mSelectorList.remove(getAllDatas().get(position));
        notifySelectorChange();
    }

    /**
     * 清空所有选择
     */
    public void clearSelectorPosition() {
        mSelectorList.clear();
        notifySelectorChange();
    }

    public int getModel() {
        return mModel;
    }

    /**
     * 设置模式,
     *
     * @param model 单选,多选, 正常
     */
    public RModelAdapter setModel(@Model int model) {
        @Model
        int oldMode = mModel;

        if (oldMode != model) {
            mModel = model;

            onModelChange(oldMode, model);

            final Iterator<OnModelChangeListener> iterator = mChangeListeners.iterator();
            while (iterator.hasNext()) {
                iterator.next().onModelChange(oldMode, model);
            }

//            boolean oldLoadMore = isEnableLoadMore();

            if (oldMode != MODEL_NORMAL) {
                //setEnableLoadMore(false);//不关闭
                mSelectorList.clear();
            } else {
//                setEnableLoadMore(oldLoadMore);
            }
            notifyDataSetChanged();
        }
        notifySelectorChange();
        return this;
    }

    protected void onModelChange(@Model int from, @Model int to) {

    }

    public RBaseViewHolder getViewHolderFromPosition(int position) {
        return mBaseViewHolderMap.get(position);
    }

    @Override
    public void deleteItem(T bean) {
        super.deleteItem(bean);
    }

    @Override
    public void deleteItem(int position) {
        super.deleteItem(position);
    }

    @Override
    protected boolean onDeleteItem(int position) {
        mSelectorList.remove(getAllDatas().get(position));
        notifySelectorChange();
        return true;
    }

    @IntDef({MODEL_NORMAL, MODEL_SINGLE, MODEL_MULTI})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Model {
    }

    public interface OnModelChangeListener {
        void onModelChange(@Model int fromModel, @Model int toModel);

        void onSelectorChange(List<Integer> selectorList);
    }

    public static abstract class SingleChangeListener implements OnModelChangeListener {

        @Override
        public void onModelChange(@Model int fromModel, @Model int toModel) {

        }

        @Override
        public void onSelectorChange(List<Integer> selectorList) {

        }
    }
}
