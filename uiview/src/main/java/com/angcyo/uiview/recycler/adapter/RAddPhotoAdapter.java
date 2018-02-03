package com.angcyo.uiview.recycler.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.angcyo.uiview.R;
import com.angcyo.uiview.recycler.RBaseViewHolder;
import com.angcyo.uiview.resources.ResUtil;
import com.angcyo.uiview.utils.ScreenUtil;
import com.angcyo.uiview.utils.UI;
import com.angcyo.uiview.widget.GlideImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：支持最大图片数量, 支持删除, 的添加图片适配器
 * 创建人员：Robi
 * 创建时间：2017/03/14 16:34
 * 修改人员：Robi
 * 修改时间：2017/03/14 16:34
 * 修改备注：
 * Version: 1.0.0
 */
public class RAddPhotoAdapter<T> extends RBaseAdapter<T> {


    public static final int TYPE_ADD = 2;
    public static final int TYPE_NORMAL = 1;

    /**
     * 允许添加的图片最大数量
     */
    private int mMaxPhotoCount = 9;

    /**
     * 每一行中, Item的数量, 用来计算item的宽高
     */
    private int mItemCountLine = 4;

    /**
     * 是否显示删除按钮
     */
    private boolean mDeleteModel = false;

    /**
     * 是否显示添加按钮
     */
    private boolean mShowAddButton = true;

    private ConfigCallback mConfigCallback;

    /**
     * 需要排除多少宽度, 不参与计算的宽度
     */
    private int excludeWidth = 0;

    /**
     * 请使用此属性, 实现分割线的功能
     */
    private int itemPadding = ScreenUtil.dip2px(5);

    public RAddPhotoAdapter(Context context) {
        super(context);
    }

    public RAddPhotoAdapter(Context context, List<T> datas) {
        super(context, datas);
    }

    @Override
    public int getItemType(int position) {
        if (mShowAddButton && (mAllDatas == null || mAllDatas.size() == 0)) {
            return TYPE_ADD;
        }
        if (mShowAddButton && position == mAllDatas.size() && mAllDatas.size() < mMaxPhotoCount) {
            return TYPE_ADD;//最后一个添加item
        }
        return TYPE_NORMAL;
    }

    @Override
    protected int getItemLayoutId(int viewType) {
        return R.layout.base_add_image_adapter;
    }

    @Override
    public int getItemCount() {
        int otherCount = 0;
        if (mShowAddButton) {
            otherCount = 1;
        }
        if (mAllDatas == null || mAllDatas.isEmpty()) {
            return otherCount;
        }
        if (mAllDatas.size() < mMaxPhotoCount) {
            return mAllDatas.size() + otherCount;
        }
        return mAllDatas.size();
    }

    @Override
    protected void onBindView(RBaseViewHolder holder, final int position, final T bean) {
        int itemSize = getItemSize();
        UI.setViewHeight(holder.itemView, itemSize);
        holder.itemView.setPadding(itemPadding, itemPadding, itemPadding, itemPadding);

        final GlideImageView imageView = holder.v(R.id.base_image_view);
        final ImageView deleteView = holder.v(R.id.base_delete_view);

        if (holder.getItemViewType() == TYPE_ADD) {
            deleteView.setVisibility(View.GONE);
            imageView.setImageResource(getAddViewImageResource(position));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mConfigCallback != null) {
                        mConfigCallback.onAddClick(imageView);
                    }
                }
            });

            onBindAddView(imageView);
        } else {
            deleteView.setImageResource(getDeleteViewImageResource(position));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            deleteView.setVisibility(mDeleteModel ? View.VISIBLE : View.GONE);
            if (mConfigCallback != null) {
                mConfigCallback.onDisplayImage(imageView, position);
            }
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mConfigCallback != null) {
                        mConfigCallback.onImageClick(imageView, position);
                    }
                }
            });
            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mConfigCallback != null) {
                        return mConfigCallback.onImageLongClick(imageView, position);
                    }
                    return false;
                }
            });

            onBindImageView(imageView, position);
        }

        deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConfigCallback != null) {
                    if (!mConfigCallback.onDeleteClick(deleteView, position)) {
                        deleteItem(bean);
                    }
                } else {
                    deleteItem(bean);
                }
            }
        });
    }

    protected void onBindImageView(GlideImageView imageView, int position) {

    }

    protected void onBindAddView(GlideImageView imageView) {

    }

    /**
     * 最大图片数量
     */
    public RAddPhotoAdapter setMaxPhotoCount(int maxPhotoCount) {
        mMaxPhotoCount = maxPhotoCount;
        return this;
    }

    /**
     * 每一行显示多少个Item
     */
    public RAddPhotoAdapter setItemCountLine(int itemCountLine) {
        mItemCountLine = itemCountLine;
        return this;
    }

    public RAddPhotoAdapter setDeleteModel(boolean deleteModel) {
        mDeleteModel = deleteModel;
        return this;
    }

    public RAddPhotoAdapter setShowAddButton(boolean showAddButton) {
        boolean oldShow = mShowAddButton;
        mShowAddButton = showAddButton;

        if (oldShow != showAddButton) {
            if (showAddButton) {
                notifyItemInserted(getAllDataCount());
            } else {
                notifyItemRemoved(getAllDataCount());
            }
        }
        return this;
    }

    @Override
    public void appendData(List<T> datas) {
        if (datas == null || datas.size() == 0) {
            return;
        }
        if (this.mAllDatas == null) {
            this.mAllDatas = new ArrayList<>();
        }
//        int startPosition = this.mAllDatas.size();
        this.mAllDatas.addAll(datas);
        notifyDataSetChanged();
    }

    /**
     * 排除多少宽度, 不参与计算item的size
     */
    public RAddPhotoAdapter setExcludeWidth(int excludeWidth) {
        this.excludeWidth = excludeWidth;
        return this;
    }

    public void setDeleteModel(RecyclerView recyclerView, boolean deleteModel) {
        mDeleteModel = deleteModel;
        localRefresh(recyclerView, new OnLocalRefresh() {
            @Override
            public void onLocalRefresh(RBaseViewHolder viewHolder, int position) {
                if (getItemType(position) != TYPE_ADD) {
                    final ImageView deleteView = viewHolder.v(R.id.base_delete_view);
                    deleteView.setVisibility(mDeleteModel ? View.VISIBLE : View.GONE);
                }
            }
        });
    }

    public RAddPhotoAdapter setConfigCallback(ConfigCallback configCallback) {
        mConfigCallback = configCallback;
        return this;
    }

    public RAddPhotoAdapter attachRecyclerView(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            int spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
            mItemCountLine = spanCount;
            UI.setViewHeight(recyclerView, getItemSize());
        }
        return this;
    }

    public int getItemSize() {
        int screenWidth = ResUtil.getScreenWidth(mContext) - excludeWidth;
        int itemSize = screenWidth / mItemCountLine;
        return itemSize;
    }

    public RAddPhotoAdapter setItemPadding(int itemPadding) {
        this.itemPadding = itemPadding;
        return this;
    }

    /**
     * 添加按钮的资源
     */
    protected int getAddViewImageResource(int position) {
        return R.drawable.base_add_border;
    }

    /**
     * 删除按钮的资源
     */
    protected int getDeleteViewImageResource(int position) {
        return R.drawable.base_delete;
    }

    public static abstract class ConfigCallback {
        public void onDisplayImage(GlideImageView imageView, int position) {

        }

        public void onImageClick(GlideImageView imageView, int position) {

        }

        public boolean onImageLongClick(GlideImageView imageView, int position) {
            return false;
        }

        public void onAddClick(View view) {

        }

        public boolean onDeleteClick(View view, int position) {
            return false;
        }
    }
}
