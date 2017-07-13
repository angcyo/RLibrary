package com.angcyo.uiview.dialog;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.angcyo.uiview.R;
import com.angcyo.uiview.base.UIIDialogImpl;
import com.angcyo.uiview.widget.SimpleProgressBar;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/07/13 09:25
 * 修改人员：Robi
 * 修改时间：2017/07/13 09:25
 * 修改备注：
 * Version: 1.0.0
 */
public class UIProgressDialog extends UIIDialogImpl {

    /**
     * 进度提示文本
     */
    String tipText;

    /**
     * 当前进度
     */
    int progress;

    TextView tipTextView;
    SimpleProgressBar progressBar;

    private UIProgressDialog() {
        setGravity(Gravity.CENTER);
        setCanTouchOnOutside(false);
    }

    public static UIProgressDialog build() {
        return new UIProgressDialog();
    }

    public UIProgressDialog setTipText(String tipText) {
        this.tipText = tipText;
        resetTextView();
        return this;
    }

    public UIProgressDialog setProgress(int progress) {
        this.progress = progress;
        if (progressBar != null) {
            progressBar.setProgress(progress);
        }
        return this;
    }

    @Override
    protected View inflateDialogView(FrameLayout dialogRootLayout, LayoutInflater inflater) {
        return inflate(R.layout.base_progress_dialog_layout);
    }

    @Override
    protected void initDialogContentView() {
        super.initDialogContentView();
        tipTextView = mViewHolder.tv(R.id.base_progress_tip_view);
        resetTextView();
        progressBar = mViewHolder.v(R.id.base_progress_view);
        progressBar.setProgress(progress);
    }

    private void resetTextView() {
        if (tipTextView == null) {
            return;
        }

        if (TextUtils.isEmpty(tipText)) {
            tipTextView.setVisibility(View.GONE);
        } else {
            tipTextView.setVisibility(View.VISIBLE);
            tipTextView.setText(tipText);
        }
    }
}
