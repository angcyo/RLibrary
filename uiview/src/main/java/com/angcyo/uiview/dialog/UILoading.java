package com.angcyo.uiview.dialog;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.angcyo.uiview.R;
import com.angcyo.uiview.base.UIIDialogImpl;
import com.angcyo.uiview.container.ILayout;
import com.angcyo.uiview.container.UIParam;
import com.angcyo.uiview.view.IViewAnimationType;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：显示一个装载形式的对话框
 * 创建人员：Robi
 * 创建时间：2016/12/14 19:09
 * 修改人员：Robi
 * 修改时间：2016/12/14 19:09
 * 修改备注：
 * Version: 1.0.0
 */
public class UILoading extends UIIDialogImpl {

    /**
     * 对话框显示的样式
     */
    public static final int DIALOG_TYPE_DEFAULT = 1; //默认的横条样式
    public static final int DIALOG_TYPE_PROGRESS = 2;//居中的进度圈圈提示
    public static final int DIALOG_TYPE_FLOW = 3;//模仿QQ支付验证对话框
    protected static boolean isShowing = false;
    private static UILoading mUILoading;
    protected ViewGroup mBaseLoadingRootLayout;
    protected View mBaseLoadingView;
    protected TextView mBaseLoadingTipView;
    String mLoadingTipText = "别怕, 马上就好...";
    private int dialogType = DIALOG_TYPE_DEFAULT;

    private int flowImageRes = -1;

    protected UILoading() {
        setGravity(Gravity.TOP);
    }

    @Deprecated
    public static UILoading build() {
        if (mUILoading == null) {
            mUILoading = new UILoading();
        }
        return mUILoading;
    }

    /**
     * 显示
     */
    @Deprecated
    public static void hide(ILayout layout) {
        if (isShowing) {
            layout.finishIView(mUILoading);
        }
    }

    public static void hide() {
        if (isShowing && mUILoading != null) {
            mUILoading.setCanCancel(true);
            mUILoading.finishIView(mUILoading, new UIParam(true, true, false));
            isShowing = false;
        }
    }

    public static UILoading show2(ILayout layout) {
        if (isShowing) {
            mUILoading.initLoadingUI();
        } else {
            if (mUILoading == null) {
                mUILoading = new UILoading();
            }
            layout.startIView(mUILoading);
            isShowing = true;
        }

        return mUILoading;
    }

    public static UILoading progress(ILayout layout) {
        if (isShowing) {
            mUILoading.initLoadingUI();
        } else {
            if (mUILoading == null) {
                mUILoading = new UILoading();
                mUILoading.mLoadingTipText = "加载中...";
                mUILoading.setGravity(Gravity.CENTER);
                mUILoading.dialogType = DIALOG_TYPE_PROGRESS;
            }
            layout.startIView(mUILoading);
            isShowing = true;
        }

        return mUILoading;
    }

    public static UILoading flow(ILayout layout) {
        if (isShowing) {
            mUILoading.initLoadingUI();
        } else {
            if (mUILoading == null) {
                mUILoading = new UILoading();
                mUILoading.mLoadingTipText = "请稍等...";
                mUILoading.setGravity(Gravity.CENTER);
                mUILoading.dialogType = DIALOG_TYPE_FLOW;
            }
            mUILoading.setAnimationType(IViewAnimationType.SCALE_TO_MAX_AND_END);
            layout.startIView(mUILoading);
            isShowing = true;
        }
        return mUILoading;
    }

    /**
     * 显示
     */
    @Deprecated
    public UILoading show(ILayout layout) {
        if (isShowing) {
            mUILoading.initLoadingUI();
        } else {
            layout.startIView(mUILoading);
        }
        return this;
    }

    /**
     * 设置提示语
     */
    public UILoading setLoadingTipText(String loadingTipText) {
        if (mLoadingTipText != null) {
            mLoadingTipText = loadingTipText;
        }
        return this;
    }

    public UILoading setFlowImageRes(int flowImageRes) {
        this.flowImageRes = flowImageRes;
        if (flowImageRes != -1 && dialogType == DIALOG_TYPE_FLOW) {
            if (mViewHolder != null) {
                ImageView imageView = mViewHolder.imageView(R.id.base_load_image_view);
                if (imageView != null) {
                    imageView.setImageResource(flowImageRes);
                }
            }
        }
        return this;
    }

    @Override
    protected View inflateDialogView(FrameLayout dialogRootLayout, LayoutInflater inflater) {
        if (dialogType == DIALOG_TYPE_PROGRESS) {
            return inflate(R.layout.base_progress_loading_layout);
        } else if (dialogType == DIALOG_TYPE_FLOW) {
            return inflate(R.layout.base_flow_loading_layout);
        } else {
            return inflate(R.layout.base_loading_layout);
        }
    }

    @Override
    public void loadContentView(View rootView) {
        super.loadContentView(rootView);
        mBaseLoadingRootLayout = mViewHolder.v(R.id.base_load_root_layout);
        mBaseLoadingView = mViewHolder.v(R.id.base_load_view);
        mBaseLoadingTipView = mViewHolder.v(R.id.base_load_tip_view);
        initLoadingUI();
    }

    protected void initLoadingUI() {
        if (mBaseLoadingTipView != null) {
            mBaseLoadingTipView.setText(mLoadingTipText);
        }
        setFlowImageRes(flowImageRes);
    }

    @Override
    public void onViewLoad() {
        super.onViewLoad();
        isShowing = true;
    }

    @Override
    public void onViewUnload() {
        super.onViewUnload();
        mUILoading = null;
        isShowing = false;
    }

    @Override
    public boolean canCanceledOnOutside() {
        return false;
    }
}
