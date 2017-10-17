package com.angcyo.uiview.base;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.angcyo.library.utils.L;
import com.angcyo.uiview.R;
import com.angcyo.uiview.container.ContentLayout;
import com.angcyo.uiview.container.UILayoutImpl;
import com.angcyo.uiview.container.UIParam;
import com.angcyo.uiview.container.UITitleBarContainer;
import com.angcyo.uiview.model.TitleBarPattern;
import com.angcyo.uiview.resources.ResUtil;
import com.angcyo.uiview.skin.ISkin;
import com.angcyo.uiview.skin.SkinHelper;
import com.angcyo.uiview.utils.ClipHelper;
import com.angcyo.uiview.utils.ScreenUtil;
import com.angcyo.uiview.view.UIIViewImpl;
import com.angcyo.uiview.widget.EmptyView;
import com.angcyo.uiview.widget.SoftRelativeLayout;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * 实现了 空布局, 无网络布局, 数据加载布局, 内容布局之间的切换
 * <p>
 * 内容布局会在 {@link #showContentLayout()} 之后才显示
 * <p>
 * Created by angcyo on 2016-11-27.
 */

public abstract class UIBaseView extends UIIViewImpl {

    public static int mUITitleBarId = View.NO_ID;
    public static int mBaseRootId = View.NO_ID;
    public static int mBaseContentRootId = View.NO_ID;
    /**
     * 布局切换动画
     */
    public static boolean ENABLE_LAYOUT_CHANGE_ANIM = true;
    /**
     * 根布局,和父类中的 {@link #mRootView} 相同, 包含标题栏
     */
    protected SoftRelativeLayout mBaseRootLayout;
    /**
     * 所有内容的根布局, 不包含标题栏
     */
    protected FrameLayout mBaseContentRootLayout;
    /**
     * 空布局
     */
    protected View mBaseEmptyLayout;
    /**
     * 无网络布局
     */
    protected View mBaseNonetLayout;
    /**
     * 加载数据的布局
     */
    protected View mBaseLoadLayout;

    protected View mBaseErrorLayout;
    /**
     * 内容布局
     */
    protected ContentLayout mBaseContentLayout;
    /**
     * 标题
     */
    protected UITitleBarContainer mUITitleBarContainer;
    protected LayoutState mLayoutState = LayoutState.NORMAL;
    protected View.OnClickListener mNonetSettingClickListener, mNonetRefreshClickListener;
    protected OnViewLoadListener mOnViewLoadListener;

    protected boolean haveTitleBar = true;
    private Animation mLoadingAnimation;
    private ClipMode mClipMode;
    private boolean mEnableClip = false;
    private int[] clipXYR = null;//clip 开始的坐标

    public static void safeSetVisibility(final View view, final int visibility, final boolean anim) {
        if (view != null) {
            if (view.getVisibility() == View.VISIBLE) {
                if (anim) {
                    ViewCompat.animate(view).scaleX(1.2f).scaleY(1.2f).alpha(0).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            view.setVisibility(visibility);
                        }
                    }).setInterpolator(new DecelerateInterpolator()).setDuration(UIIViewImpl.DEFAULT_ANIM_TIME).start();
                } else {
                    view.setVisibility(visibility);
                }
            } else {
                view.setVisibility(visibility);
            }
        }
    }

    public static Animation createClipEnterAnim(float fromAlpha) {
        AlphaAnimation animation = new AlphaAnimation(fromAlpha, 1f);
        setDefaultConfig(animation, false);
        return animation;
    }

    public static Animation createClipExitAnim(float toAlpha) {
        AlphaAnimation animation = new AlphaAnimation(1f, toAlpha);
        setDefaultConfig(animation, true);
        return animation;
    }

    @Override
    protected View inflateBaseView(FrameLayout container, LayoutInflater inflater) {
        //包含标题栏的根布局
        mBaseRootLayout = new SoftRelativeLayout(mActivity);
        mBaseRootLayout.setEnableClip(mEnableClip && enableEnterClip());

        mBaseRootId = R.id.base_root_layout_id;//View.generateViewId();
        mBaseRootLayout.setId(mBaseRootId);

        //mBaseRootLayout.setBackgroundColor(getDefaultBackgroundColor());
        mBaseRootLayout.setBackground(getDefaultDrawable());

        TitleBarPattern titleBarPattern = getTitleBar();
        if (titleBarPattern != null && isHaveTitleBar()) {
            if (mOnUIViewListener != null) {
                mOnUIViewListener.onCreateTitleBar(titleBarPattern);
            }

            mUITitleBarContainer = new UITitleBarContainer(mActivity);
            mUITitleBarId = R.id.base_root_title_id;//View.generateViewId();
            mUITitleBarContainer.setId(mUITitleBarId);
            mUITitleBarContainer.setTitleBarPattern(titleBarPattern);
            mUITitleBarContainer.onAttachToLayout(mILayout);//note
            mUITitleBarContainer.setOnBackListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UIBaseView.this.onTitleBackListener();
//                    mILayout.requestBackPressed();
                }
            });

            //内容根布局, 包含空布局,加载布局等
            mBaseContentRootLayout = new FrameLayout(mActivity);
            mBaseContentRootId = R.id.base_root_content_id;//View.generateViewId();
            mBaseContentRootLayout.setId(mBaseContentRootId);

            //内容包裹布局
            mBaseContentLayout = new ContentLayout(mActivity);
            mBaseContentRootLayout.addView(mBaseContentLayout, new ViewGroup.LayoutParams(-1, -1));

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, -1);
            mBaseRootLayout.addView(mBaseContentRootLayout, params);

            if (!titleBarPattern.isFixStatusHeight) {
                mUITitleBarContainer.setPadding(0, 0, 0, 0);
            }

            if (titleBarPattern.titleBarPaddingTop > 0) {
                mUITitleBarContainer.setPadding(mUITitleBarContainer.getPaddingLeft(),
                        mUITitleBarContainer.getPaddingTop() + titleBarPattern.titleBarPaddingTop,
                        mUITitleBarContainer.getPaddingRight(), mUITitleBarContainer.getPaddingBottom());
            }

            mBaseRootLayout.addView(mUITitleBarContainer, new ViewGroup.LayoutParams(-1, -2));

            mBaseRootLayout.setFloatingTitleView(titleBarPattern.isFloating);

            if (titleBarPattern.isFloating) {
                if (titleBarPattern.isFixContentHeight) {
                    mBaseContentRootLayout.setPadding(
                            mBaseContentRootLayout.getPaddingLeft(),
                            (mBaseContentRootLayout.getPaddingTop() + (titleBarPattern.isFixStatusHeight ? mActivity.getResources().getDimensionPixelOffset(R.dimen.title_bar_height) : mActivity.getResources().getDimensionPixelOffset(R.dimen.action_bar_height))) + titleBarPattern.titleBarPaddingTop,
                            mBaseContentRootLayout.getPaddingRight(),
                            mBaseContentRootLayout.getPaddingBottom());
                }
            } else {
            }

        } else {
            //没有标题的情况, 减少布局层级 星期二 2017-7-11
            mBaseContentRootLayout = mBaseRootLayout;

            mBaseContentLayout = new ContentLayout(mActivity);
            mBaseContentRootId = R.id.base_root_content_id;//View.generateViewId();
            mBaseContentLayout.setId(mBaseContentRootId);

            mBaseContentRootLayout.addView(mBaseContentLayout, new ViewGroup.LayoutParams(-1, -1));
        }

        // 2016-12-18 使用懒加载的方式 加载.
//        mBaseEmptyLayout = UILayoutImpl.safeAssignView(mBaseContentRootLayout,
//                inflateEmptyLayout(mBaseContentRootLayout, inflater));//填充空布局
//        mBaseNonetLayout = UILayoutImpl.safeAssignView(mBaseContentRootLayout,
//                inflateNonetLayout(mBaseContentRootLayout, inflater));//填充无网络布局
//        mBaseLoadLayout = UILayoutImpl.safeAssignView(mBaseContentRootLayout,
//                inflateLoadLayout(mBaseContentRootLayout, inflater));//填充加载布局
//
//        safeSetView(mBaseContentLayout);
//        safeSetView(mBaseEmptyLayout);
//        safeSetView(mBaseNonetLayout);
//        safeSetView(mBaseLoadLayout);


        container.addView(mBaseRootLayout, new ViewGroup.LayoutParams(-1, -1));
        return mBaseRootLayout;
    }

    @Override
    public void loadContentView(View rootView) {
        super.loadContentView(rootView);
        LayoutState state = getDefaultLayoutState();
        if (state == LayoutState.CONTENT) {
            showContentLayout();
            startLayoutAnim(mBaseContentLayout);
        } else if (state == LayoutState.LOAD) {
            showLoadLayout();
        } else if (state == LayoutState.EMPTY) {
            showEmptyLayout();
        } else if (state == LayoutState.NONET) {
            showNonetLayout();
        } else if (state == LayoutState.ERROR) {
            showErrorLayout();
        }
    }

    @NonNull
    protected LayoutState getDefaultLayoutState() {
        return LayoutState.LOAD;
    }

    /**
     * 请不要在此方法中初始化内容, 因为ButterKnife.bind(this, mBaseContentLayout);还么有执行
     */
    protected abstract void inflateContentLayout(ContentLayout baseContentLayout, LayoutInflater inflater);

    /**
     * 初始化内容, 当你的 默认布局状态不等于 {@link LayoutState#CONTENT} 时,请使用以下方法初始化View
     */
    protected void initOnShowContentLayout() {
        L.d("call: initOnShowContentLayout([])-> ");
        if (mOnUIViewListener != null) {
            mOnUIViewListener.initOnShowContentLayout(this, mViewHolder);
        }
    }

    /**
     * 只要显示了内容, 就会调用此方法, 请在此方法中设置布局内容
     */
    protected void onShowContentLayout() {
        L.d("call: onShowContentLayout([])-> ");
    }

    /**
     * 追加在内容层级, 不包含附加布局层(空布局, 无网络,等)
     */
    protected View inflate(@LayoutRes int layoutId) {
        return LayoutInflater.from(mActivity).inflate(layoutId, mBaseContentLayout);
    }

    /**
     * 追加在顶层, 包含附加布局层(空布局, 无网络,等)
     */
    protected View inflateRoot(@LayoutRes int layoutId) {
        return LayoutInflater.from(mActivity).inflate(layoutId, mBaseContentRootLayout);
    }

    /**
     * 布局inflate
     */
    protected View inflateLoadLayout(FrameLayout baseRootLayout, LayoutInflater inflater) {
//        return inflater.inflate(R.layout.base_load_layout, baseRootLayout);
        EmptyView emptyView = new EmptyView(mActivity);
        int padding = (int) ResUtil.dpToPx(mActivity, 20);
        emptyView.setPadding(padding, padding, padding, padding);
        baseRootLayout.addView(emptyView, new ViewGroup.LayoutParams(-1, -1));
        return emptyView;
    }

    protected View inflateNonetLayout(FrameLayout baseRootLayout, LayoutInflater inflater) {
        View view = inflater.inflate(getBaseNonetLayoutId(), baseRootLayout);
        initBaseNonetLayout(view);
        return view;
    }

    protected int getBaseNonetLayoutId() {
        return R.layout.base_nonet_layout;
    }

    protected void initBaseNonetLayout(View view) {
        Drawable drawable = getTipButtonSelector();
        ResUtil.setBgDrawable(view.findViewById(R.id.base_refresh_view), drawable);
        drawable = getTipButtonSelector();
        ResUtil.setBgDrawable(view.findViewById(R.id.base_setting_view), drawable);
    }

    protected Drawable getTipButtonSelector() {
        int borderColor = getColor(R.color.default_base_border);
        int borderSize = getDimensionPixelOffset(R.dimen.base_border_size);
        int radii = getDimensionPixelOffset(R.dimen.base_40dpi);
        return ResUtil.selector(
                ResUtil.createDrawable(borderColor, borderSize, radii),
                ResUtil.createDrawable(SkinHelper.getTranColor(SkinHelper.getSkin().getThemeSubColor(), 0x80), radii)
        );
    }

    protected View inflateEmptyLayout(FrameLayout baseRootLayout, LayoutInflater inflater) {
        View view = inflater.inflate(getBaseEmptyLayoutId(), baseRootLayout);
        initBaseEmptyLayout(view);
        return view;
    }

    protected void initBaseEmptyLayout(View view) {

    }

    protected void initBaseErrorLayout(View view) {

    }

    protected int getBaseEmptyLayoutId() {
        return R.layout.base_empty_layout;
    }

    protected View inflateErrorLayout(FrameLayout baseRootLayout, LayoutInflater inflater) {
        View view = inflater.inflate(getBaseErrorLayoutId(), baseRootLayout);
        initBaseErrorLayout(view);
        return view;
    }

    protected int getBaseErrorLayoutId() {
        return R.layout.base_error_layout;
    }

    protected TitleBarPattern getTitleBar() {
        return createTitleBarPattern();
    }

    public TitleBarPattern createTitleBarPattern() {
        return TitleBarPattern.build(getTitleResource() == View.NO_ID ?
                getTitleString() : mActivity.getResources().getString(getTitleResource()))
                .setTitleBarBGColor(getTitleBarBGColor())
                .setShowBackImageView(!haveParentILayout() && ((UILayoutImpl) mILayout).getAttachViewSize() >= 1);
    }

    protected int getTitleBarBGColor() {
        return SkinHelper.getSkin().getThemeColor();//mActivity.getResources().getColor(R.color.theme_color_primary);
    }

    protected String getTitleString() {
        return mActivity.getTitle().toString();
    }

    /**
     * 设置标题文本
     */
    public void setTitleString(String title) {
        if (mUITitleBarContainer != null) {
            mUITitleBarContainer.getTitleView().setText(title);
        }
    }

    protected int getTitleResource() {
        return View.NO_ID;
    }

    /**
     * 当布局的显示状态发生了改变
     */
    protected void onLayoutStateChanged(LayoutState fromState, LayoutState toState) {
        if (fromState == LayoutState.LOAD && mBaseLoadLayout != null) {
        }
        if (toState == LayoutState.LOAD && mBaseLoadLayout != null) {

        } else if (toState == LayoutState.EMPTY && mBaseEmptyLayout != null) {
            String tip = getBaseEmptyTip();
            if (tip != null) {
                ((TextView) mBaseEmptyLayout.findViewById(R.id.base_empty_tip_view)).setText(tip);
            }
        } else if (toState == LayoutState.NONET && mBaseNonetLayout != null) {
            mBaseNonetLayout.findViewById(R.id.base_setting_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mNonetSettingClickListener != null) {
                        mNonetSettingClickListener.onClick(v);
                    }
                }
            });
            mBaseNonetLayout.findViewById(R.id.base_refresh_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mNonetRefreshClickListener != null) {
                        showLoadLayout();
                        mNonetRefreshClickListener.onClick(v);
                    }
                }
            });
        }
    }

    /**
     * 空页面的时候, 提示的字符串
     */
    protected String getBaseEmptyTip() {
        return null;
    }

    /**
     * 显示装载布局
     */
    public void showLoadLayout() {
        removeOtherView(LayoutState.LOAD);
        if (mBaseLoadLayout == null) {
            mBaseLoadLayout = UILayoutImpl.safeAssignView(mBaseContentRootLayout,
                    inflateLoadLayout(mBaseContentRootLayout, LayoutInflater.from(mActivity)));//填充加载布局
        }
        changeState(mLayoutState, LayoutState.LOAD);
    }

    /**
     * 显示空布局
     */
    public void showEmptyLayout() {
        removeOtherView(LayoutState.EMPTY);
        if (mBaseEmptyLayout == null) {
            mBaseEmptyLayout = UILayoutImpl.safeAssignView(mBaseContentRootLayout,
                    inflateEmptyLayout(mBaseContentRootLayout, LayoutInflater.from(mActivity)));//填充空布局
        }
        changeState(mLayoutState, LayoutState.EMPTY);
    }

    public void showErrorLayout() {
        removeOtherView(LayoutState.ERROR);
        if (mBaseErrorLayout == null) {
            mBaseErrorLayout = UILayoutImpl.safeAssignView(mBaseContentRootLayout,
                    inflateErrorLayout(mBaseContentRootLayout, LayoutInflater.from(mActivity)));//填充错误布局
        }
        changeState(mLayoutState, LayoutState.ERROR);
    }

    public void showNonetLayout() {
        showNonetLayout(null, null);
    }


    //-----------------以下私有方法------------------//

    /**
     * 显示无网络布局
     */
    public void showNonetLayout(View.OnClickListener settingListener, View.OnClickListener refreshListener) {
        mNonetSettingClickListener = settingListener;
        mNonetRefreshClickListener = refreshListener;
        removeOtherView(LayoutState.NONET);
        if (mBaseNonetLayout == null) {
            mBaseNonetLayout = UILayoutImpl.safeAssignView(mBaseContentRootLayout,
                    inflateNonetLayout(mBaseContentRootLayout, LayoutInflater.from(mActivity)));//填充无网络布局
        }
        changeState(mLayoutState, LayoutState.NONET);
    }

    protected void removeOtherView(LayoutState needShowState) {
        mViewHolder.clear();
        if (needShowState == LayoutState.CONTENT) {
            removeLoadLayout();
            removeEmptyLayout();
            removeNonetLayout();
            removeErrorLayout();
        } else {
            if (mBaseContentLayout != null) {
                mBaseContentLayout.removeAllViews();
            }
            if (needShowState == LayoutState.EMPTY) {
                removeLoadLayout();
                removeNonetLayout();
                removeErrorLayout();
            } else if (needShowState == LayoutState.LOAD) {
                removeEmptyLayout();
                removeNonetLayout();
                removeErrorLayout();
            } else if (needShowState == LayoutState.NONET) {
                removeLoadLayout();
                removeEmptyLayout();
                removeErrorLayout();
            } else if (needShowState == LayoutState.ERROR) {
                removeLoadLayout();
                removeEmptyLayout();
                removeNonetLayout();
            }
        }
    }

    private void removeErrorLayout() {
        if (mBaseErrorLayout != null) {
            mBaseContentRootLayout.removeView(mBaseErrorLayout);
            mBaseErrorLayout = null;
        }
    }

    private void removeNonetLayout() {
        if (mBaseNonetLayout != null) {
            mBaseContentRootLayout.removeView(mBaseNonetLayout);
            mBaseNonetLayout = null;
        }
    }

    private void removeEmptyLayout() {
        if (mBaseEmptyLayout != null) {
            mBaseContentRootLayout.removeView(mBaseEmptyLayout);
            mBaseEmptyLayout = null;
        }
    }

    private void removeLoadLayout() {
        if (mBaseLoadLayout != null) {
            mBaseContentRootLayout.removeView(mBaseLoadLayout);
            mBaseLoadLayout = null;
        }
    }

    public void showNonetLayout(View.OnClickListener refreshListener) {
        final View.OnClickListener settingListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
//                // 判断手机系统的版本 即API大于10 就是3.0或以上版本及魅族手机
//                if (Build.VERSION.SDK_INT > 10 && !Build.MANUFACTURER.equals("Meizu")) {
//                    intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
//                } else if (Build.VERSION.SDK_INT > 17 && Build.MANUFACTURER.equals("Meizu")) {
//                    //魅族更高版本调转的方式与其它手机型号一致  可能之前的版本有些一样  所以另加条件(tsp)
//                    intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
//                } else {
//                    intent = new Intent(Settings.ACTION_SETTINGS);
////                    intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
////                    intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
////                    ComponentName component = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
////                    intent.setComponent(component);
////                    intent.setAction("android.intent.action.VIEW");
//                }
                mActivity.startActivity(intent);
            }
        };
        showNonetLayout(settingListener, refreshListener);
    }

    /**
     * 显示内容布局
     */
    public void showContentLayout() {
        removeOtherView(LayoutState.CONTENT);
        if (mBaseContentLayout.getChildCount() == 0) {
            inflateContentLayout(mBaseContentLayout, LayoutInflater.from(mActivity));
            if (mOnUIViewListener != null) {
                mOnUIViewListener.inflateContentLayout(this, mBaseContentLayout, LayoutInflater.from(mActivity));
            }
            //不使用 butterknife
            //ButterKnife.bind(this, mBaseContentLayout);
            initOnShowContentLayout();
        }
        onShowContentLayout();
        changeState(mLayoutState, LayoutState.CONTENT);
    }

    protected Animation loadLoadingAnimation() {
        RotateAnimation rotateAnimation = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        setDefaultConfig(rotateAnimation, false);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setRepeatMode(Animation.RESTART);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setDuration(1000);
        return rotateAnimation;
    }

    protected void fixInsertsTop() {
        mBaseRootLayout.fixInsertsTop();
    }

    private void safeSetView(View view) {
        if (view != null) {
            view.setVisibility(GONE);
            //mBaseRootLayout.addView(view, new ViewGroup.LayoutParams(-1, -1));
        }
    }

    /**
     * 改变布局的状态
     */
    protected void changeState(LayoutState from, LayoutState to) {
        if (from == to) {
            return;
        }
        mLayoutState = to;
        updateLayoutState();
        onLayoutStateChanged(from, to);
    }

    /**
     * 布局改变时, 是否激活动画
     */
    protected boolean isEnableLayoutChangeAnim() {
        return ENABLE_LAYOUT_CHANGE_ANIM;
    }

    private void updateLayoutState() {
        if (mLayoutState == LayoutState.LOAD) {
            safeSetVisibility(mBaseContentLayout, View.GONE, isEnableLayoutChangeAnim());
            safeSetVisibility(mBaseEmptyLayout, View.GONE, isEnableLayoutChangeAnim());
            safeSetVisibility(mBaseNonetLayout, View.GONE, isEnableLayoutChangeAnim());
            safeUpdateLayoutState(mBaseLoadLayout, View.VISIBLE);
        } else if (mLayoutState == LayoutState.EMPTY) {
            safeSetVisibility(mBaseContentLayout, View.GONE, isEnableLayoutChangeAnim());
            safeSetVisibility(mBaseLoadLayout, View.GONE, isEnableLayoutChangeAnim());
            safeSetVisibility(mBaseNonetLayout, View.GONE, isEnableLayoutChangeAnim());
            safeUpdateLayoutState(mBaseEmptyLayout, View.VISIBLE);
        } else if (mLayoutState == LayoutState.NONET) {
            safeSetVisibility(mBaseContentLayout, View.GONE, isEnableLayoutChangeAnim());
            safeSetVisibility(mBaseEmptyLayout, View.GONE, isEnableLayoutChangeAnim());
            safeSetVisibility(mBaseLoadLayout, View.GONE, isEnableLayoutChangeAnim());
            safeUpdateLayoutState(mBaseNonetLayout, View.VISIBLE);
        } else if (mLayoutState == LayoutState.CONTENT) {
            safeSetVisibility(mBaseLoadLayout, View.GONE, isEnableLayoutChangeAnim());
            safeSetVisibility(mBaseEmptyLayout, View.GONE, isEnableLayoutChangeAnim());
            safeSetVisibility(mBaseNonetLayout, View.GONE, isEnableLayoutChangeAnim());
            safeUpdateLayoutState(mBaseContentLayout, View.VISIBLE);
        }
    }

    private void safeUpdateLayoutState(View view, int visibility) {
        if (view == null) {
            showContentLayout();
            return;
        }
        if (visibility == VISIBLE) {
            ViewCompat.animate(view).alpha(1).scaleX(1).scaleY(1).start();
        }
        view.setVisibility(visibility);
    }

    public UITitleBarContainer getUITitleBarContainer() {
        return mUITitleBarContainer;
    }

    public UIBaseView showLoadView() {
        if (mUITitleBarContainer != null) {
            mUITitleBarContainer.showLoadView();
        }
        if (mOnViewLoadListener != null) {
            mOnViewLoadListener.onShowLoadView();
        }
        return this;
    }

    public UIBaseView hideLoadView() {
        if (mUITitleBarContainer != null) {
            mUITitleBarContainer.hideLoadView();
        }
        if (mOnViewLoadListener != null) {
            mOnViewLoadListener.onHideLoadView();
        }
        return this;
    }

    /**
     * 显示键盘
     */
    public void showSoftInput() {
        if (isSoftKeyboardShow()) {
            return;
        }
        InputMethodManager manager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInputFromInputMethod(mActivity.getWindow().getDecorView().getWindowToken(), 0);
    }

    public void showSoftInput(View view) {
        view.requestFocus();
        InputMethodManager manager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInput(view, 0);
    }

    /**
     * 隐藏键盘
     */
    public void hideSoftInput() {
        if (isSoftKeyboardShow()) {
            InputMethodManager manager = (InputMethodManager) mActivity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(mActivity.getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    /**
     * 判断键盘是否显示
     */
    public boolean isSoftKeyboardShow() {
        int screenHeight = mActivity.getResources().getDisplayMetrics().heightPixels;
        int keyboardHeight = getSoftKeyboardHeight();
        return screenHeight != keyboardHeight && keyboardHeight > 100;
    }

    /**
     * 获取键盘的高度
     */
    public int getSoftKeyboardHeight() {
        int screenHeight = mActivity.getResources().getDisplayMetrics().heightPixels;
        Rect rect = new Rect();
        mActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        int visibleBottom = rect.bottom;
        return screenHeight - visibleBottom;
    }

    @ColorInt
    public int getDefaultBackgroundColor() {
        return Color.TRANSPARENT;
    }

    public Drawable getDefaultDrawable() {
        return new ColorDrawable(getDefaultBackgroundColor());
    }

    public void updateSkin() {
        onSkinChanged(SkinHelper.getSkin());
    }

    @Override
    public void onSkinChanged(ISkin skin) {
        super.onSkinChanged(skin);
        if (mUITitleBarContainer != null) {
            mUITitleBarContainer.setBackgroundColor(getTitleBarBGColor());
        }
    }

    @Override
    public Animation loadStartAnimation() {
        if (mEnableClip && enableEnterClip()) {
            //为了不影响之前的动画逻辑, 这里使用一个效果不明显的动画
            Animation animation = createClipEnterAnim(0.8f);
            animation.setDuration(initClipTime());
            return animation;
        }
        return super.loadStartAnimation();
    }

    @Override
    public Animation loadFinishAnimation() {
        if (mEnableClip && enableExitClip()) {
            //为了不影响之前的动画逻辑, 这里使用一个效果不明显的动画
            Animation animation = createClipExitAnim(0.6f);
            animation.setDuration(initClipTime());
            return animation;
        }
        return super.loadFinishAnimation();
    }

    @Override
    public Animation loadOtherExitAnimation() {
        if (mEnableClip && enableEnterClip()) {
            Animation animation = createClipExitAnim(1f);
            animation.setDuration(initClipTime());
            return animation;
        }
        return super.loadOtherExitAnimation();
    }

    @Override
    public Animation loadOtherEnterAnimation() {
        if (mEnableClip && enableExitClip()) {
            //为了不影响之前的动画逻辑, 这里使用一个效果不明显的动画
            Animation animation = createClipEnterAnim(1f);
            animation.setDuration(initClipTime());
            return animation;
        }
        return super.loadOtherEnterAnimation();
    }

    private boolean enableEnterClip() {
        return mClipMode == ClipMode.CLIP_BOTH || mClipMode == ClipMode.CLIP_START;
    }

    private boolean enableExitClip() {
        return mClipMode == ClipMode.CLIP_BOTH || mClipMode == ClipMode.CLIP_EXIT;
    }

    @Override
    public boolean canTryCaptureView() {
        return mBaseRootLayout.isClipEnd();
    }

    @Override
    public boolean canSwipeBackPressed() {
        return super.canSwipeBackPressed();
    }

    /**
     * 标题栏中, 点击了返回按钮
     */
    protected boolean onTitleBackListener() {
        if (mEnableClip && enableExitClip()) {
            if (mClipMode == ClipMode.CLIP_EXIT) {
                mBaseRootLayout.setEnableClip(true);
                mBaseRootLayout.getClipHelper().initXYR(null);
            }
            mBaseRootLayout.startExitClip(new ClipHelper.OnEndListener() {
                @Override
                public void onEnd() {
                    //backPressed();
                    mEnableClip = false;
                    onClipEnd(ClipMode.CLIP_EXIT);
                }
            });
            if (mILayout != null) {
                mILayout.requestBackPressed(new UIParam(true, true, false).setClickOnTitleBack(true));
            }
            return false;
        } else {
            mEnableClip = false;
            if (mILayout != null) {
                mILayout.requestBackPressed(new UIParam(true, false, false).setClickOnTitleBack(true));
            }
            return true;
        }
    }

    @Override
    public boolean onBackPressed() {
        if (mEnableClip) {
            return onTitleBackListener();
        }
        return super.onBackPressed();
    }

    @Override
    public void onViewLoad() {
        super.onViewLoad();
        if (enableEnterClip()) {
            Runnable runnable;
            if (clipXYR == null) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        mBaseRootLayout.startEnterClip(null,
                                new ClipHelper.OnEndListener() {
                                    @Override
                                    public void onEnd() {
                                        onClipEnd(ClipMode.CLIP_START);
                                    }
                                });
                    }
                };
            } else {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        mBaseRootLayout.startEnterClip(clipXYR[0], clipXYR[1], clipXYR[2],
                                new ClipHelper.OnEndListener() {
                                    @Override
                                    public void onEnd() {
                                        onClipEnd(ClipMode.CLIP_START);
                                    }
                                });
                    }
                };
            }

            checkDelayClip(runnable);
        }
    }

    private void checkDelayClip(Runnable runnable) {
        if (mBaseRootLayout == null ||
                mBaseRootLayout.getMeasuredWidth() == 0 ||
                mBaseRootLayout.getMeasuredHeight() == 0) {
            post(runnable);
        } else {
            runnable.run();
        }
    }

    /**
     * Clip结束之后的回调
     *
     * @param clipEndMode 是开始之后的回调, 还是结束之后的回调
     */
    protected void onClipEnd(ClipMode clipEndMode) {

    }

    /**
     * 是否使用clip效果
     */
    public UIBaseView setEnableClipMode(ClipMode mode) {
        return setEnableClipMode(mode, null);
    }

    public UIBaseView setEnableClipMode(ClipMode mode, View view) {
        this.mClipMode = mode;
        this.mEnableClip = true;
        clipXYR = ClipHelper.init(view);
        initClipTime();
        return this;
    }

    public UIBaseView setEnableClipMode(ClipMode mode, int x, int y, int r) {
        this.mClipMode = mode;
        this.mEnableClip = true;
        clipXYR = new int[]{x, y, r};
        initClipTime();
        return this;
    }

    /**
     * 计算clip需要的时间
     */
    private int initClipTime() {
        float endRadius;
        int maxWidth = ScreenUtil.screenWidth;
        int maxHeight = ScreenUtil.screenHeight;
        if (clipXYR == null) {
            endRadius = ClipHelper.calcEndRadius(maxWidth,
                    maxHeight,
                    maxWidth / 2,
                    maxHeight / 2);
        } else {
            endRadius = ClipHelper.calcEndRadius(maxWidth,
                    maxHeight, clipXYR[0], clipXYR[1]);
        }

        int value = Math.max(maxWidth, maxHeight);
        int time;
        if (clipXYR != null && endRadius - clipXYR[2] >= value / 2) {
            time = 300;
        } else {
            time = 200;
        }
        ClipHelper.ANIM_TIME = time;
        return time;
//        return ClipHelper.ANIM_TIME;
    }

    @Override
    public UILayoutImpl getILayout() {
        return (UILayoutImpl) super.getILayout();
    }

    /**
     * 抖动IView
     */
    public void shakeView() {
        if (mRootView == null) {
            return;
        }
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, .02f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f);
        translateAnimation.setInterpolator(new CycleInterpolator(1f));
        translateAnimation.setRepeatCount(1);
        translateAnimation.setRepeatMode(Animation.REVERSE);
        translateAnimation.setDuration(300);
        translateAnimation.setFillAfter(false);
        //translateAnimation.start();
        mRootView.startAnimation(translateAnimation);
    }

    public LayoutState getLayoutState() {
        return mLayoutState;
    }

    /**
     * 需要显示加载View的监听
     */
    public void setOnViewLoadListener(OnViewLoadListener onViewLoadListener) {
        mOnViewLoadListener = onViewLoadListener;
    }

    public boolean isHaveTitleBar() {
        return haveTitleBar;
    }

    public UIBaseView setHaveTitleBar(boolean haveTitleBar) {
        this.haveTitleBar = haveTitleBar;
        return this;
    }

    /**
     * 指示当前布局的显示状态, 当前那个布局在显示
     */
    public enum LayoutState {
        NORMAL,//正常
        EMPTY,//空布局
        LOAD,//装载布局
        NONET,//无网络
        CONTENT, //内容
        ERROR //异常
    }

    /**
     * clip模式
     */
    public enum ClipMode {
        /**
         * 启动的时候, 使用clip
         */
        CLIP_START,
        /**
         * 退出的时候, 使用clip
         */
        CLIP_EXIT,
        /**
         * 2者都使用
         */
        CLIP_BOTH
    }

    public interface OnViewLoadListener {
        void onShowLoadView();

        void onHideLoadView();
    }
}
