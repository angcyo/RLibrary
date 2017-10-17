package com.angcyo.uiview.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;

import com.angcyo.uiview.R;
import com.angcyo.uiview.container.IWindowInsetsListener;
import com.angcyo.uiview.utils.ClipHelper;
import com.angcyo.uiview.view.ILifecycle;
import com.angcyo.uiview.viewgroup.TouchBackLayout;

import java.util.ArrayList;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：针对键盘 弹出隐藏的问题, 进行修复
 * 创建人员：Robi
 * 创建时间：2016/12/02 11:30
 * 修改人员：Robi
 * 修改时间：2016/12/02 11:30
 * 修改备注：
 * Version: 1.0.0
 */
public class SoftRelativeLayout extends TouchBackLayout implements ILifecycle {
    boolean isViewShow = false;
    boolean mFitSystemWindow = false;
    ClipHelper mClipHelper;
    OnInterceptTouchListener mOnInterceptTouchListener;
    private ArrayList<IWindowInsetsListener> mOnWindowInsetsListeners;
    private int[] mInsets = new int[4];
    /**
     * 锁定高度, 当键盘弹出的时候, 可以不改变size
     */
    private boolean lockHeight = false;//自动根据页面隐藏显示, 决定是否固定高度
    private boolean fixHeight = false;//固定高度
    private boolean mInterceptKeyboard;//拦截touch down事件, 自动隐藏键盘
    private boolean isTouchDown;

    /**
     * 悬浮标题View, 不悬浮采用上下竖直布局
     */
    private boolean floatingTitleView = true;

    public SoftRelativeLayout(Context context) {
        this(context, null);
    }

    public SoftRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        setWillNotDraw(false);
//        setClickable(true);
        setEnabled(true);
//        setFocusable(true);
//        setFocusableInTouchMode(true);
//        setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

        mClipHelper = new ClipHelper(this);
    }

    public void fitsSystemWindows(boolean fit) {
        mFitSystemWindow = fit;
        setFitsSystemWindows(mFitSystemWindow);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setFitsSystemWindows(mFitSystemWindow);
        setClickable(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
//        requestFocus();
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            hideSoftInput();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (floatingTitleView) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            View titleView = findViewById(R.id.base_root_title_id);
            if (titleView == null) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            } else {
                int widthMode = MeasureSpec.getMode(widthMeasureSpec);
                int heightMode = MeasureSpec.getMode(heightMeasureSpec);
                int widthSize = MeasureSpec.getSize(widthMeasureSpec);
                int heightSize = MeasureSpec.getSize(heightMeasureSpec);

                titleView.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST));

                View contentView = findViewById(R.id.base_root_content_id);
                if (contentView != null) {
                    contentView.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(heightSize - titleView.getMeasuredHeight(), MeasureSpec.EXACTLY));
                }

                setMeasuredDimension(widthSize, heightSize);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (floatingTitleView) {
            super.onLayout(changed, l, t, r, b);
        } else {
            View titleView = findViewById(R.id.base_root_title_id);
            if (titleView == null) {
                super.onLayout(changed, l, t, r, b);
            } else {

                titleView.layout(0, 0, r, titleView.getMeasuredHeight());

                View contentView = findViewById(R.id.base_root_content_id);
                if (contentView != null) {
                    contentView.layout(0, titleView.getMeasuredHeight(), r, b);
                }
            }
        }

//        final int count = getChildCount();
//        for (int i = 0; i < count; i++) {
//            View child = getChildAt(i);
//            if (child.getVisibility() != GONE) {
//                LayoutParams st =
//                        (LayoutParams) child.getLayoutParams();
//                int offset = mInsets[3];
//                int left = (int) Reflect.getMember(st, "mLeft");
//                int top = (int) Reflect.getMember(st, "mTop");
//                int right = (int) Reflect.getMember(st, "mRight");
//                int bottom = (int) Reflect.getMember(st, "mBottom");
//
//                int height = getMeasuredHeight() - offset;
//
//                int offsetTop = top;
////                if (getPaddingBottom() > 200) {
////                    offsetTop = t;
////                }
//
//                /*修复对话框中,包含输入控件,键盘弹出时, 无法居中的BUG*/
//                if (st.getRules()[RelativeLayout.CENTER_IN_PARENT] == RelativeLayout.TRUE ||
//                        st.getRules()[RelativeLayout.CENTER_VERTICAL] == RelativeLayout.TRUE) {
//                    //child.layout(left, , right, height / 2 + child.getMeasuredHeight() / 2);
//                    animateView(child, offsetTop - offset / 2);
//                } else if (st.getRules()[RelativeLayout.ALIGN_PARENT_BOTTOM] == RelativeLayout.TRUE) {
//                    animateView(child, offsetTop - offset);
//                } else {
//                    final int gravity = getGravity() & (~Gravity.START);
//                    if (gravity == Gravity.CENTER || gravity == Gravity.CENTER_VERTICAL) {
//                        animateView(child, top);
//                    } else if (gravity == Gravity.BOTTOM) {
//                        animateView(child, top);
//                    }
//                }
//            }
//        }
    }

    private void animateView(View view, float y) {
        ViewCompat.animate(view)
                .y(y)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(300)
                .start();
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        return super.fitSystemWindows(insets);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mInsets[0] = insets.getSystemWindowInsetLeft();
            mInsets[1] = insets.getSystemWindowInsetTop();
            mInsets[2] = insets.getSystemWindowInsetRight();
            mInsets[3] = insets.getSystemWindowInsetBottom();

            if (isViewShow) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        notifyListener();
                    }
                });
                return super.onApplyWindowInsets(insets.replaceSystemWindowInsets(insets.getSystemWindowInsetLeft(), 0,
                        insets.getSystemWindowInsetRight(), lockHeight || fixHeight ? 0 : insets.getSystemWindowInsetBottom()));
            } else {
                setPadding(getPaddingLeft(), 0, getPaddingRight(), 0);
//                return super.onApplyWindowInsets(insets.replaceSystemWindowInsets(insets.getSystemWindowInsetLeft(), 0,
//                        insets.getSystemWindowInsetRight(), lockHeight ? 0 : insets.getSystemWindowInsetBottom()));
                return insets.replaceSystemWindowInsets(insets.getSystemWindowInsetLeft(), 0,
                        insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            }

        } else {
            return super.onApplyWindowInsets(insets);
        }
    }

    private void notifyListener() {
         /*键盘弹出监听事件*/
        if (mOnWindowInsetsListeners != null) {
            for (IWindowInsetsListener listener : mOnWindowInsetsListeners) {
                listener.onWindowInsets(mInsets[0], mInsets[1], mInsets[2], mInsets[3]);
            }
        }
    }

    public SoftRelativeLayout addOnWindowInsetsListener(IWindowInsetsListener listener) {
        if (listener == null) {
            return this;
        }
        if (mOnWindowInsetsListeners == null) {
            mOnWindowInsetsListeners = new ArrayList<>();
        }
        this.mOnWindowInsetsListeners.add(listener);
        return this;
    }

    public SoftRelativeLayout removeOnWindowInsetsListener(IWindowInsetsListener listener) {
        if (listener == null || mOnWindowInsetsListeners == null) {
            return this;
        }
        this.mOnWindowInsetsListeners.remove(listener);
        return this;
    }

    @Deprecated
    public void setLockHeight(boolean lockHeight) {
        this.lockHeight = lockHeight;
    }

    public void setFixHeight(boolean fixHeight) {
        this.fixHeight = fixHeight;
    }

    /**
     * 修复状态栏的高度
     */
    public void fixInsertsTop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setPadding(getPaddingLeft(), getResources().getDimensionPixelSize(R.dimen.status_bar_height),
                    getPaddingRight(), getPaddingBottom());
        }
    }

    public void fixInsertsTitleTop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setPadding(getPaddingLeft(),
                    getResources().getDimensionPixelSize(R.dimen.status_bar_height)
                            + getResources().getDimensionPixelSize(R.dimen.action_bar_height),
                    getPaddingRight(), getPaddingBottom());
        } else {
            setPadding(getPaddingLeft(), getResources().getDimensionPixelSize(R.dimen.action_bar_height),
                    getPaddingRight(), getPaddingBottom());
        }
    }

    /**
     * 获取底部装饰物的高度 , 通常是键盘的高度
     */
    public int getInsertsBottom() {
        return mInsets[3];
    }


    /**
     * 判断键盘是否显示
     */
    public boolean isSoftKeyboardShow() {
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        Rect rect = new Rect();
        getWindowVisibleDisplayFrame(rect);
        int visibleBottom = rect.bottom;
        int keyboardHeight = screenHeight - visibleBottom;
        return screenHeight != keyboardHeight && keyboardHeight > 100;
    }

    public void hideSoftInput() {
        if (isSoftKeyboardShow()) {
            InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    public void showSoftInput(View view) {
        view.requestFocus();
        InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInput(view, 0);
    }

    @Override
    public void onLifeViewShow() {
        isViewShow = true;
        lockHeight = false;
        setFitsSystemWindows(mFitSystemWindow);
    }

    @Override
    public void onLifeViewHide() {
        isViewShow = false;
        lockHeight = true;
        setFitsSystemWindows(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //super.onTouchEvent(event);
//        Object mListenerInfo = Reflect.getMember(View.class, this, "mListenerInfo");
//        if (mListenerInfo != null) {
//            Object mOnTouchListener = Reflect.getMember(mListenerInfo, "mOnTouchListener");
//            if (mOnTouchListener != null) {
//                ((OnTouchListener) mOnTouchListener).onTouch(this, event);
//            }
//        }

        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            isTouchDown = true;
        } else if (action == MotionEvent.ACTION_UP) {
            if (isTouchDown) {
                performClick();
            }
            isTouchDown = false;
        } else if (action == MotionEvent.ACTION_CANCEL) {
            isTouchDown = false;
        }

        if (mOnInterceptTouchListener == null) {
            hideSoftInput();
        } else {
            if (action == MotionEvent.ACTION_DOWN) {
                mOnInterceptTouchListener.onTouchDown(event);
            }
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return true;
        }

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (mInterceptKeyboard) {
                hideSoftInput();
            }
            if (mOnInterceptTouchListener != null) {
                mOnInterceptTouchListener.onInterceptTouchDown(ev);
            }
        }
        boolean touchEvent = super.onInterceptTouchEvent(ev);
        return touchEvent;
    }

    @Override
    public void draw(Canvas canvas) {
        mClipHelper.draw(canvas);
        super.draw(canvas);
    }

    public ClipHelper getClipHelper() {
        return mClipHelper;
    }

    public void setEnableClip(boolean enableClip) {
        mClipHelper.setEnableClip(enableClip);
    }

    public boolean isClipEnd() {
        return mClipHelper.isClipEnd();
    }

    public void startEnterClip(View view, ClipHelper.OnEndListener listener) {
        mClipHelper.startEnterClip(view, listener);
    }

    public void startEnterClip(float startX, float startY, float startR, ClipHelper.OnEndListener listener) {
        mClipHelper.startEnterClip(startX, startY, startR, listener);
    }

    public void startExitClip(ClipHelper.OnEndListener listener) {
        mClipHelper.startExitClip(listener);
    }

    public void setFloatingTitleView(boolean floatingTitleView) {
        this.floatingTitleView = floatingTitleView;
    }

    public void setAutoInterceptKeyboard(boolean interceptKeyboard) {
        mInterceptKeyboard = interceptKeyboard;
    }

    public void setOnInterceptTouchListener(OnInterceptTouchListener onInterceptTouchListener) {
        mOnInterceptTouchListener = onInterceptTouchListener;
    }

    public interface OnInterceptTouchListener {
        void onInterceptTouchDown(MotionEvent event);

        void onTouchDown(MotionEvent event);
    }
}
