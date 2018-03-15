package com.angcyo.uiview.github.pickerview;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.angcyo.github.pickerview.adapter.ArrayWheelAdapter;
import com.angcyo.github.pickerview.lib.WheelView;
import com.angcyo.github.pickerview.listener.OnItemSelectedListener;
import com.angcyo.uiview.R;
import com.angcyo.uiview.base.UIIDialogImpl;
import com.angcyo.uiview.utils.RUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：滑动选项选择对话框 联动 (目前支持2级联动)
 * 创建人员：Robi
 * 创建时间：2018/03/07 14:56
 * 修改人员：Robi
 * 修改时间：2018/03/07 14:56
 * 修改备注：C
 * Version: 1.0.0
 */
public class OptionExDialog extends UIIDialogImpl {

    OnItemOptionSelector mOnItemOptionSelector;
    WheelView optionWheelView1, optionWheelView2;
    boolean isLoop = false;

    /**
     * 默认位置
     */
    int defaultIndex = -1;

    boolean showOption2 = true;

    public OptionExDialog(OnItemOptionSelector onItemOptionSelector) {
        mOnItemOptionSelector = onItemOptionSelector;
    }

    @Override
    protected View inflateDialogView(@NonNull FrameLayout dialogRootLayout, @NonNull LayoutInflater inflater) {
        return inflate(R.layout.pickerview_option_ex);
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
        optionWheelView1 = mViewHolder.v(R.id.options1);
        optionWheelView1.setCyclic(isLoop);
        optionWheelView1.setIsOptions(true);

        optionWheelView2 = mViewHolder.v(R.id.options2);
        optionWheelView2.setCyclic(isLoop);
        optionWheelView2.setIsOptions(true);

        optionWheelView1.setAdapter(new ArrayWheelAdapter<>(mOnItemOptionSelector.getFirstItems()));

        mViewHolder.v(R.id.btnSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishDialog(new Runnable() {
                    @Override
                    public void run() {
                        int currentItem = optionWheelView1.getCurrentItem();
                        int currentItem2 = optionWheelView2.getCurrentItem();
                        mOnItemOptionSelector.onItemSelector(currentItem, currentItem2);
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

        optionWheelView1.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                List<Object> secondItems = mOnItemOptionSelector.getSecondItems(index);
                if (RUtils.isListEmpty(secondItems)) {
                    optionWheelView2.setAdapter(new ArrayWheelAdapter<>(new ArrayList<>()));
                } else {
                    optionWheelView2.setAdapter(new ArrayWheelAdapter<>(secondItems));
                }
                optionWheelView2.setCurrentItem(0);
            }
        });

        if (defaultIndex > -1) {
            optionWheelView1.setCurrentItem(defaultIndex);
            optionWheelView1.onItemSelected();//触发事件
        }

        if (!showOption2) {
            optionWheelView2.setVisibility(View.GONE);
        }
    }

    public List getFirstDataItems() {
        return ((ArrayWheelAdapter) optionWheelView1.getAdapter()).getItems();
    }

    public List getSecondDataItems() {
        return ((ArrayWheelAdapter) optionWheelView2.getAdapter()).getItems();
    }

    public void setShowOption2(boolean showOption2) {
        this.showOption2 = showOption2;
    }

    public interface OnItemOptionSelector {
        void onItemSelector(int firstIndex, int secondIndex);

        /**
         * @see com.angcyo.github.pickerview.model.IPickerViewData
         */
        List<Object> getSecondItems(int firstIndex);

        /**
         * @see WheelView#getContentText(Object)
         */
        List<Object> getFirstItems();
    }
}
