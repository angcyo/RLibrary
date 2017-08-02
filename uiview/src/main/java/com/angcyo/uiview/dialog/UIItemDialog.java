package com.angcyo.uiview.dialog;

import android.graphics.Color;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.angcyo.uiview.R;
import com.angcyo.uiview.base.UIIDialogImpl;
import com.angcyo.uiview.recycler.RBaseViewHolder;
import com.angcyo.uiview.skin.SkinHelper;

import java.util.ArrayList;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：提供Item选择样式的对话框
 * 创建人员：Robi
 * 创建时间：2016/12/13 17:20
 * 修改人员：Robi
 * 修改时间：2016/12/13 17:20
 * 修改备注：
 * Version: 1.0.0
 */
public class UIItemDialog extends UIIDialogImpl {

    protected LinearLayout mItemContentLayout;
    protected TextView mCancelView, mTitleView;
    protected RBaseViewHolder mViewHolder;
    protected ArrayList<ItemInfo> mItemInfos = new ArrayList<>();

    protected boolean showCancelButton = true;

    /**
     * 使用全屏占满形式的item
     */
    protected boolean useFullItem = false;

    protected ItemConfig mItemConfig;

    protected String dialogTitle = "";

    public UIItemDialog() {
    }

    public static UIItemDialog build() {
        return new UIItemDialog();
    }

    public UIItemDialog addItem(String text, View.OnClickListener clickListener) {
        addItem(new ItemInfo(text, clickListener));
        return this;
    }

    public UIItemDialog addItem(String text, @DrawableRes int leftRes, View.OnClickListener clickListener) {
        addItem(new ItemInfo(text, leftRes, clickListener));
        return this;
    }

    public UIItemDialog addItem(ItemInfo itemInfo) {
        mItemInfos.add(itemInfo);
        return this;
    }

    public UIItemDialog setShowCancelButton(boolean showCancelButton) {
        this.showCancelButton = showCancelButton;
        return this;
    }

    public UIItemDialog setUseFullItem(boolean useFullItem) {
        this.useFullItem = useFullItem;
        return this;
    }

    public UIItemDialog setItemConfig(ItemConfig itemConfig) {
        mItemConfig = itemConfig;
        return this;
    }

    public UIItemDialog setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
        return this;
    }

    @Override
    protected View inflateDialogView(FrameLayout dialogRootLayout, LayoutInflater inflater) {
        return inflate(R.layout.base_dialog_item_layout);
    }

    @Override
    public void loadContentView(View rootView) {
        super.loadContentView(rootView);
        mViewHolder = new RBaseViewHolder(rootView);
        mItemContentLayout = mViewHolder.v(R.id.item_content_layout);
        mCancelView = mViewHolder.v(R.id.cancel_view);
        mTitleView = mViewHolder.v(R.id.base_title_view);
        mCancelView.setTextColor(SkinHelper.getSkin().getThemeSubColor());

        if (TextUtils.isEmpty(dialogTitle)) {
            mTitleView.setVisibility(View.GONE);
        } else {
            mTitleView.setVisibility(View.VISIBLE);
            mTitleView.setText(dialogTitle);
        }

        mCancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishDialog();
            }
        });

        if (!useFullItem) {
            int offset = getDimensionPixelOffset(R.dimen.base_xxhdpi);
            rootView.setPadding(offset, 0, offset, 0);
        }

        try {
            if (showCancelButton) {
                if (useFullItem) {
                    mViewHolder.v(R.id.cancel_layout).setBackgroundColor(Color.WHITE);
                    mViewHolder.v(R.id.cancel_view).setBackgroundResource(R.drawable.base_bg_selector);
                    mViewHolder.v(R.id.line1).setVisibility(View.GONE);
                    mViewHolder.v(R.id.line2).setVisibility(View.GONE);
                    mViewHolder.v(R.id.line3).setVisibility(View.VISIBLE);
                }
            } else {
                mViewHolder.v(R.id.cancel_layout).setVisibility(View.GONE);
                mViewHolder.v(R.id.line1).setVisibility(View.GONE);
                mViewHolder.v(R.id.line2).setVisibility(View.GONE);
            }
        } catch (Exception e) {

        }

        if (useFullItem) {
            mItemContentLayout.setBackgroundColor(Color.WHITE);
        } else {
            mItemContentLayout.setBackgroundResource(R.drawable.base_white_round_bg);
        }

        inflateItem();

        if (mItemConfig != null) {
            mItemConfig.onLoadContent(this, mViewHolder);
        }
    }

    /**
     * 填充item信息
     */
    protected void inflateItem() {
        int size = mItemInfos.size();
        for (int i = 0; i < size; i++) {
            ItemInfo info = mItemInfos.get(i);
            View itemView = createItem(info);

            if (itemView instanceof TextView) {
                TextView textView = (TextView) itemView;
                if (useFullItem) {
                    textView.setBackgroundResource(R.drawable.base_bg_selector);
                    textView.setTextColor(getColor(R.color.base_text_color));
                } else {
                    if (size == 1) {
                        textView.setBackgroundResource(R.drawable.base_round_bg_selector);
                    } else {
                        if (i == 0) {
                            textView.setBackgroundResource(R.drawable.base_top_round_bg_selector);
                        } else if (i == size - 1) {
                            textView.setBackgroundResource(R.drawable.base_bottom_round_bg_selector);
                        } else {
                            textView.setBackgroundResource(R.drawable.base_bg_selector);
                        }
                    }
                    textView.setTextColor(SkinHelper.getSkin().getThemeSubColor());

                    if (mItemConfig != null) {
                        mItemConfig.onCreateItem(textView);
                    }
                }
            }

            mItemContentLayout.addView(itemView,
                    new ViewGroup.LayoutParams(-1,
                            mActivity.getResources().getDimensionPixelSize(R.dimen.base_item_size)));
        }
    }

    protected View createItem(final ItemInfo info) {
        TextView textView = new TextView(mActivity);
        textView.setText(info.mItemText);
        textView.setTextColor(SkinHelper.getSkin().getThemeSubColor());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                mActivity.getResources().getDimensionPixelSize(R.dimen.default_text_size));
        textView.setGravity(Gravity.CENTER);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                info.mClickListener.onClick(v);
                if (info.autoCloseDialog) {
                    finishDialog();
                }
            }
        });

        return textView;
    }

    public interface ItemConfig {
        void onCreateItem(TextView itemView);

        void onLoadContent(UIItemDialog dialog, RBaseViewHolder viewHolder);
    }

    public static class ItemInfo {
        public String mItemText;
        public View.OnClickListener mClickListener;
        public boolean autoCloseDialog = true;
        @DrawableRes
        public int leftRes = 0;

        public ItemInfo(String itemText, View.OnClickListener clickListener) {
            mItemText = itemText;
            mClickListener = clickListener;
        }

        public ItemInfo(String itemText, int leftRes, View.OnClickListener clickListener) {
            mItemText = itemText;
            this.leftRes = leftRes;
            mClickListener = clickListener;
        }

        public ItemInfo(String itemText, View.OnClickListener clickListener, boolean autoCloseDialog) {
            mItemText = itemText;
            mClickListener = clickListener;
            this.autoCloseDialog = autoCloseDialog;
        }

        public ItemInfo setItemText(String itemText) {
            mItemText = itemText;
            return this;
        }

        public ItemInfo setClickListener(View.OnClickListener clickListener) {
            mClickListener = clickListener;
            return this;
        }

        public ItemInfo setAutoCloseDialog(boolean autoCloseDialog) {
            this.autoCloseDialog = autoCloseDialog;
            return this;
        }

        public ItemInfo setLeftRes(@DrawableRes int leftRes) {
            this.leftRes = leftRes;
            return this;
        }
    }
}
