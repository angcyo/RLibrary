package com.angcyo.uiview.dialog;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.angcyo.uiview.R;
import com.angcyo.uiview.RApplication;
import com.angcyo.uiview.base.UIIDialogImpl;
import com.angcyo.uiview.base.UIViewConfig;
import com.angcyo.uiview.skin.SkinHelper;
import com.angcyo.uiview.widget.RTextView;
import com.angcyo.uiview.widget.SimpleProgressBar;

/**
 * 标准形式的对话框
 * <p>
 * Created by angcyo on 2016-11-16.
 */

public class UIDialog extends UIIDialogImpl {

    /**
     * 是否使用5.0以上的样式
     */
    boolean isX5Style = true;

    RTextView mBaseDialogTitleView;
    RTextView mBaseDialogTopContentView;
    RTextView mBaseDialogContentView;
    TextView mBaseDialogCancelView;
    TextView mBaseDialogOkView;
    LinearLayout mBaseDialogRootLayout;
    View mLineLayout;

    /**
     * 对话框标题和内容,为空不显示
     */
    String dialogTitle, dialogContent;

    /**
     * 取消文本, 确定文本, 为空不显示
     */
    String cancelText, okText;

    /**
     * 2个监听事件
     */
    View.OnClickListener cancelListener, okListener, contentListener;
    OnDialogClick cancelClick, okClick, contentClick;

    /**
     * 进度
     */
    int progress = 0;
    UIViewConfig mViewConfig;
    private SimpleProgressBar mBaseProgressBar;

    private UIDialog() {
        cancelText = RApplication.getApp().getString(R.string.base_cancel);
        okText = RApplication.getApp().getString(R.string.base_ok);
    }

    public static UIDialog build() {
        return new UIDialog();
    }

    @Override
    protected View inflateDialogView(FrameLayout dialogRootLayout, LayoutInflater inflater) {
        setGravity(Gravity.CENTER_VERTICAL);
        if (isX5Style) {
            return inflater.inflate(R.layout.base_dialog_layout_5x, dialogRootLayout);
        } else {
            return inflater.inflate(R.layout.base_dialog_layout, dialogRootLayout);
        }
    }

    /**
     * 设置对话框的标题
     */
    public UIDialog setDialogTitle(String title) {
        this.dialogTitle = title;
        if (mBaseDialogTitleView != null) {
            mBaseDialogTitleView.setText(dialogTitle);
        }
        return this;
    }

    /**
     * 设置对话框显示的内容
     */
    public UIDialog setDialogContent(String content) {
        this.dialogContent = content;
        if (mBaseDialogContentView != null) {
            mBaseDialogContentView.setText(dialogContent);
        }
        return this;
    }

    public UIDialog setOkText(String text) {
        this.okText = text;
        if (mBaseDialogOkView != null) {
            mBaseDialogOkView.setText(okText);
            mBaseDialogOkView.setVisibility(TextUtils.isEmpty(okText) ? View.GONE : View.VISIBLE);
        }
        return this;
    }

    public UIDialog setCancelText(String text) {
        this.cancelText = text;
        if (mBaseDialogCancelView != null) {
            mBaseDialogCancelView.setText(cancelText);
            mBaseDialogCancelView.setVisibility(TextUtils.isEmpty(cancelText) ? View.GONE : View.VISIBLE);
        }
        return this;
    }

    public UIDialog setCancelListener(View.OnClickListener listener) {
        this.cancelListener = listener;
        return this;
    }

    public UIDialog setOkListener(View.OnClickListener listener) {
        this.okListener = listener;
        return this;
    }

    public UIDialog setContentListener(View.OnClickListener contentListener) {
        this.contentListener = contentListener;
        return this;
    }

    public UIDialog setCancelClick(OnDialogClick cancelClick) {
        this.cancelClick = cancelClick;
        return this;
    }

    public UIDialog setOkClick(OnDialogClick okClick) {
        this.okClick = okClick;
        return this;
    }

    public UIDialog setContentClick(OnDialogClick contentClick) {
        this.contentClick = contentClick;
        return this;
    }

    public UIDialog setViewConfig(UIViewConfig viewConfig) {
        mViewConfig = viewConfig;
        return this;
    }

    @Override
    protected void initDialogContentView() {
        super.initDialogContentView();
    }

    @Override
    public void loadContentView(View rootView) {
        super.loadContentView(rootView);

        mBaseProgressBar = rootView.findViewById(R.id.progress_bar);
        mBaseProgressBar.setProgressColor(SkinHelper.getSkin().getThemeTranColor(0x80));

        mBaseDialogTitleView = rootView.findViewById(R.id.base_dialog_title_view);
        mBaseDialogContentView = rootView.findViewById(R.id.base_dialog_content_view);
        mBaseDialogTopContentView = rootView.findViewById(R.id.base_dialog_top_content_view);
        mBaseDialogRootLayout = rootView.findViewById(R.id.base_dialog_root_layout);
        mBaseDialogOkView = rootView.findViewById(R.id.base_dialog_ok_view);
        mBaseDialogCancelView = rootView.findViewById(R.id.base_dialog_cancel_view);
        if (!isX5Style) {
            mLineLayout = rootView.findViewById(R.id.line_layout);
        }

        //默认文本设置
        //mBaseDialogOkView.setText(mActivity.getResources().getString(R.string.base_cancel));
        //mBaseDialogCancelView.setText(mActivity.getResources().getString(R.string.base_ok));

        click(mBaseDialogOkView, new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (okListener != null) {
                            okListener.onClick(v);
                        }
                        if (okClick != null) {
                            okClick.onDialogClick(UIDialog.this, v);
                        }
                    }
                };
                if (autoFinishDialog) {
                    finishDialog(runnable);
                } else {
                    runnable.run();
                }
            }
        });
        click(mBaseDialogCancelView, new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (cancelListener != null) {
                            cancelListener.onClick(v);
                        }
                        if (cancelClick != null) {
                            cancelClick.onDialogClick(UIDialog.this, v);
                        }
                    }
                };
                if (autoFinishDialog) {
                    finishDialog(runnable);
                } else {
                    runnable.run();
                }
            }
        });

        if (contentListener != null || contentClick != null) {
            click(click(mBaseDialogOkView, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (contentListener != null) {
                        contentListener.onClick(v);
                    }
                    if (contentClick != null) {
                        contentClick.onDialogClick(UIDialog.this, v);
                    }
                }
            });
        }

        mBaseDialogTitleView.setVisibility(TextUtils.isEmpty(dialogTitle) ? View.GONE : View.VISIBLE);
        mBaseDialogContentView.setVisibility(TextUtils.isEmpty(dialogContent) ? View.GONE : View.VISIBLE);
        mBaseDialogCancelView.setVisibility(TextUtils.isEmpty(cancelText) ? View.GONE : View.VISIBLE);
        mBaseDialogOkView.setVisibility(TextUtils.isEmpty(okText) ? View.GONE : View.VISIBLE);

        mBaseDialogTitleView.setText(dialogTitle);
        mBaseDialogContentView.setText(dialogContent);
        mBaseDialogOkView.setText(okText);
        mBaseDialogCancelView.setText(cancelText);

        if (!isX5Style) {
            mLineLayout.setVisibility((TextUtils.isEmpty(dialogTitle) && TextUtils.isEmpty(dialogContent)) ? View.GONE : View.VISIBLE);

            if (TextUtils.isEmpty(okText)) {
                mBaseDialogCancelView.setBackgroundResource(R.drawable.base_bottom_round_bg_selector);
            } else if (TextUtils.isEmpty(cancelText)) {
                mBaseDialogOkView.setBackgroundResource(R.drawable.base_bottom_round_bg_selector);
            }
        }

        resetDialogGravity();

        mBaseDialogOkView.setTextColor(SkinHelper.getSkin().getThemeSubColor());

        if (mViewConfig != null) {
            mViewConfig.initOnShowContentLayout(this, mViewHolder);
        }
    }

    public UIDialog setProgress(int progress) {
        this.progress = progress;
        if (mBaseProgressBar != null) {
            if (progress > 0) {
                mBaseProgressBar.setVisibility(View.VISIBLE);
            }
            mBaseProgressBar.setProgress(progress);
        }
        return this;
    }

    public UIDialog setIncertitudeProgress(boolean incertitudeProgress) {
        if (mBaseProgressBar != null) {
            if (incertitudeProgress) {
                mBaseProgressBar.setVisibility(View.VISIBLE);
            }
            mBaseProgressBar.setIncertitudeProgress(incertitudeProgress);
        }
        return this;
    }

    public interface OnDialogClick {
        void onDialogClick(UIDialog dialog, View clickView);
    }
}
