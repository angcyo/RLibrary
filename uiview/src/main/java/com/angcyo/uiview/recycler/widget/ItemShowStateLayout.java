package com.angcyo.uiview.recycler.widget;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.angcyo.uiview.R;
import com.angcyo.uiview.view.UIIViewImpl;

/**
 * Created by angcyo on 2016-12-18.
 */

public class ItemShowStateLayout extends FrameLayout implements IShowState {

    int mShowState = IShowState.NORMAL;
    private View loadingView, emptyView, errorView, nonetView;

    public ItemShowStateLayout(Context context) {
        this(context, null);
    }

    public ItemShowStateLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setShowState(mShowState);
    }

    private void initView() {
        for (int i = 0; i < getChildCount(); i++) {
            final View view = getChildAt(i);
            if (TextUtils.equals("error", (CharSequence) view.getTag())) {
                errorView = view;
            } else if (TextUtils.equals("empty", (CharSequence) view.getTag())) {
                emptyView = view;
            } else if (TextUtils.equals("loading", (CharSequence) view.getTag())) {
                loadingView = view;
            } else if (TextUtils.equals("nonet", (CharSequence) view.getTag())) {
                nonetView = view;
            }
        }
    }

    private void showView(View view) {
        if (view != null) {
//            errorView.setVisibility(errorView == view ? View.VISIBLE : View.INVISIBLE);
//            emptyView.setVisibility(emptyView == view ? View.VISIBLE : View.INVISIBLE);
//            loadingView.setVisibility(loadingView == view ? View.VISIBLE : View.INVISIBLE);
//            nonetView.setVisibility(nonetView == view ? View.VISIBLE : View.INVISIBLE);

            safeSetVisibility(errorView, errorView == view ? View.VISIBLE : View.INVISIBLE);
            safeSetVisibility(emptyView, emptyView == view ? View.VISIBLE : View.INVISIBLE);
            safeSetVisibility(loadingView, loadingView == view ? View.VISIBLE : View.INVISIBLE);
            safeSetVisibility(nonetView, nonetView == view ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void safeSetVisibility(final View view, final int visibility) {
        if (view != null) {
            if (view.getVisibility() == View.VISIBLE) {
                ViewCompat.animate(view).scaleX(1.2f).scaleY(1.2f).alpha(0).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        view.setVisibility(visibility);
                    }
                }).setInterpolator(new DecelerateInterpolator()).setDuration(UIIViewImpl.DEFAULT_ANIM_TIME).start();
            } else {
                view.setVisibility(visibility);
            }
        }
    }

    public void animToHide(final Runnable endRunnable) {
        ViewCompat.animate(this).scaleX(1.2f).scaleY(1.2f).alpha(0).withEndAction(new Runnable() {
            @Override
            public void run() {
                endRunnable.run();
                setVisibility(GONE);
            }
        }).setInterpolator(new DecelerateInterpolator()).setDuration(UIIViewImpl.DEFAULT_ANIM_TIME).start();
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        initView();
        initNonetLayout(-1, null, null);
    }

    @Override
    public int getShowState() {
        return mShowState;
    }

    @Override
    public void setShowState(int showState) {
        initView();
        if (showState != mShowState) {
            mShowState = showState;
            if (showState == IShowState.LOADING) {
                showView(loadingView);
            } else if (showState == IShowState.ERROR) {
                showView(errorView);
            } else if (showState == IShowState.EMPTY) {
                showView(emptyView);
            } else if (showState == IShowState.NONET) {
                showView(nonetView);
            } else {
                showView(this);
            }
        }
    }

    /**
     * 初始化空布局数据
     */
    public void initEmptyLayout(@DrawableRes int emptyImage, String emptyText) {
        if (mShowState == EMPTY && emptyView != null) {
            ImageView emptyImageView = emptyView.findViewById(R.id.base_empty_image_view);
            TextView emptyTextView = emptyView.findViewById(R.id.base_empty_tip_view);

            if (emptyImage != -1) {
                emptyImageView.setImageResource(emptyImage);
            }

            if (emptyText != null) {
                emptyTextView.setText(emptyText);
            }
        }
    }

    public void initNonetLayout(@DrawableRes int nonetImage, String nonetText, View.OnClickListener refreshListener) {
        if (mShowState == NONET && nonetView != null) {
            ImageView nonetImageView = nonetView.findViewById(R.id.base_nonet_image_view);
            TextView nonetTextView = nonetView.findViewById(R.id.base_nonet_tip_view);

            if (nonetImage != -1) {
                nonetImageView.setImageResource(nonetImage);
            }

            if (nonetText != null) {
                nonetTextView.setText(nonetText);
            }

            nonetView.findViewById(R.id.base_setting_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Settings.ACTION_SETTINGS);
                    getContext().startActivity(intent);
                }
            });

            nonetView.findViewById(R.id.base_refresh_view).setOnClickListener(refreshListener);
        }
    }
}
