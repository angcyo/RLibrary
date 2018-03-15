package com.angcyo.uiview.github.pickerview;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.angcyo.github.pickerview.adapter.ArrayWheelAdapter;
import com.angcyo.github.pickerview.lib.WheelView;
import com.angcyo.uiview.R;
import com.angcyo.uiview.base.UIIDialogImpl;

import java.util.List;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：滑动选项选择对话框
 * 创建人员：Robi
 * 创建时间：2018/03/07 14:56
 * 修改人员：Robi
 * 修改时间：2018/03/07 14:56
 * 修改备注：C
 * Version: 1.0.0
 */
public class OptionDialog extends UIIDialogImpl {

    List<String> items;
    OnItemOptionSelector mOnItemOptionSelector;
    WheelView optionWheelView;
    boolean isLoop = false;

    /**
     * 默认位置
     */
    int defaultIndex = -1;

    public OptionDialog(List<String> items, OnItemOptionSelector onItemOptionSelector) {
        this.items = items;
        mOnItemOptionSelector = onItemOptionSelector;
    }

    @Override
    protected View inflateDialogView(@NonNull FrameLayout dialogRootLayout, @NonNull LayoutInflater inflater) {
        return inflate(R.layout.pickerview_option);
    }

    public void setLoop(boolean loop) {
        isLoop = loop;
    }

    public void setDefaultIndex(int defaultIndex) {
        this.defaultIndex = defaultIndex;
    }

    @Override
    protected void initDialogContentView() {
        super.initDialogContentView();
        optionWheelView = mViewHolder.v(R.id.options1);
        optionWheelView.setCyclic(isLoop);
        optionWheelView.setIsOptions(true);
        optionWheelView.setAdapter(new ArrayWheelAdapter<>(items));
        mViewHolder.v(R.id.btnSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishDialog(new Runnable() {
                    @Override
                    public void run() {
                        int currentItem = optionWheelView.getCurrentItem();
                        mOnItemOptionSelector.onItemSelector(currentItem, items.get(currentItem));
                    }
                });
            }
        });

        mViewHolder.v(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishDialog();
            }
        });

        if (defaultIndex > -1) {
            optionWheelView.setCurrentItem(defaultIndex);
        }
    }

    public interface OnItemOptionSelector {
        void onItemSelector(int index, String item);
    }
}
