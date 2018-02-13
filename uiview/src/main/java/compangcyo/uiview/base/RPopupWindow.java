package com.angcyo.uiview.base;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import com.angcyo.uiview.recycler.RBaseViewHolder;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/06/29 15:51
 * 修改人员：Robi
 * 修改时间：2017/06/29 15:51
 * 修改备注：
 * Version: 1.0.0
 */
public class RPopupWindow extends PopupWindow {

    Context mContext;
    FrameLayout mRootLayout;
    RBaseViewHolder mViewHolder;
    OnInitLayout mOnInitLayout;

    public RPopupWindow(Context context) {
        super(context);
        mContext = context;

        setWidth(-2);
        setHeight(-2);

        mRootLayout = new FrameLayout(context);
        mViewHolder = new RBaseViewHolder(mRootLayout);

        mRootLayout.setBackgroundColor(Color.WHITE);
//        setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        setTouchable(true);
        setOutsideTouchable(true);//点击窗口外, 消失

    }

    public static RPopupWindow build(Context context) {
        return new RPopupWindow(context);
    }

    public RPopupWindow layout(@LayoutRes int layoutId, OnInitLayout initLayout) {
        LayoutInflater.from(mContext).inflate(layoutId, mRootLayout);
        mOnInitLayout = initLayout;
        if (initLayout != null) {
            initLayout.onInitLayout(mViewHolder, this);
        }
        return this;
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        setContentView(mRootLayout);
        super.showAsDropDown(anchor, xoff, yoff, gravity);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mOnInitLayout != null) {
            mOnInitLayout.onDismiss();
        }
    }

    public interface OnInitLayout {
        void onInitLayout(RBaseViewHolder viewHolder, PopupWindow window);

        void onDismiss();
    }
}
