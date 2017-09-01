package com.angcyo.uiview.container;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;

import com.angcyo.library.utils.L;
import com.angcyo.uiview.R;
import com.angcyo.uiview.RApplication;
import com.angcyo.uiview.RCrashHandler;
import com.angcyo.uiview.Root;
import com.angcyo.uiview.base.UILayoutActivity;
import com.angcyo.uiview.model.ViewPattern;
import com.angcyo.uiview.resources.AnimUtil;
import com.angcyo.uiview.rsen.RGestureDetector;
import com.angcyo.uiview.skin.ISkin;
import com.angcyo.uiview.view.ILifecycle;
import com.angcyo.uiview.view.IView;
import com.angcyo.uiview.view.UIIViewImpl;
import com.angcyo.uiview.widget.viewpager.UIViewPager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import static com.angcyo.uiview.view.UIIViewImpl.DEFAULT_ANIM_TIME;

/**
 * 可以用来显示IView的布局, 每一层的管理
 * Created by angcyo on 2016-11-12.
 */

public class UILayoutImpl extends SwipeBackLayout implements ILayout<UIParam>, UIViewPager.OnPagerShowListener {

    private static final String TAG = "UILayoutImpl";
    public static String LAYOUT_INFO = "";
    /**
     * 已经追加到内容层的View
     */
    protected Stack<ViewPattern> mAttachViews = new Stack<>();
    /**
     * 最前显示的View
     */
    protected ViewPattern mLastShowViewPattern;
    protected boolean isAttachedToWindow = false;
    protected UILayoutActivity mLayoutActivity;
    /**
     * 是否正在退出界面
     */
    private boolean isFinishing = false;
    /**
     * 是否正在启动新界面
     */
    private boolean isStarting = false;
    Application.ActivityLifecycleCallbacks mCallbacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            L.w("call: onActivityCreated([activity, savedInstanceState])-> " + activity.getClass().getSimpleName());
        }

        @Override
        public void onActivityStarted(Activity activity) {
            L.w("call: onActivityStarted([activity])-> " + activity.getClass().getSimpleName());
            if (activity == mLayoutActivity) {
                if (mLastShowViewPattern != null &&
                        !mLastShowViewPattern.mIView.haveParentILayout() /*&&
                        mLastShowViewPattern.mView.getVisibility() != VISIBLE*/) {
                    viewShow(mLastShowViewPattern, null);
                }
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {
            L.w("call: onActivityResumed([activity])-> " + activity.getClass().getSimpleName());
//            if (activity == mLayoutActivity) {
//                viewShow(mLastShowViewPattern, null);
//            }
        }

        @Override
        public void onActivityPaused(Activity activity) {
            L.w("call: onActivityPaused([activity])-> " + activity.getClass().getSimpleName());
//            if (activity == mLayoutActivity) {
//                viewHide(mLastShowViewPattern);
//            }
        }

        @Override
        public void onActivityStopped(Activity activity) {
            L.w("call: onActivityStopped([activity])-> " + activity.getClass().getSimpleName());
            if (activity == mLayoutActivity) {
                if (mLastShowViewPattern != null &&
                        !mLastShowViewPattern.mIView.haveParentILayout() /*&&
                        mLastShowViewPattern.mView.getVisibility() == VISIBLE*/) {
                    viewHide(mLastShowViewPattern);
                }
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            if (activity == mLayoutActivity) {
                while (!mAttachViews.empty()) {
                    try {
                        ViewPattern pattern = mAttachViews.pop();
                        pattern.interrupt = true;
                        pattern.isAnimToEnd = true;
                        pattern.mIView.onViewHide();
                        pattern.mIView.onViewUnload();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };
    /**
     * 已经按下返回键
     */
    private boolean isBackPress = false;
    private ArrayList<IWindowInsetsListener> mIWindowInsetsListeners;
    private ArrayList<OnIViewChangedListener> mOnIViewChangedListeners = new ArrayList<>();
    private int[] mInsets = new int[4];
    /**
     * 锁定高度, 当键盘弹出的时候, 可以不改变size
     */
    private boolean lockHeight = false;
    private float mTranslationOffsetX;
    /**
     * 记录有多少个Runnable任务在执行
     */
    private int runnableCount = 0;
    /**
     * 如果只剩下最后一个View, 是否激活滑动删除
     */
    private boolean enableRootSwipe = false;
    /**
     * 是否正在拖拽返回.
     */
    private boolean isSwipeDrag = false;
    /**
     * 是否需要滑动返回, 如果正在滑动返回,则阻止onLayout的进行
     */
    private boolean isWantSwipeBack = false;
    /**
     * 需要中断IView启动的的列表
     */
    private Set<IView> interruptSet;
    /**
     * 滑动关闭标志
     */
    private boolean needDragClose = false;
    /**
     * 管理子的ILayout, 用于在滑动的过程中控制显示和隐藏最后一个IView
     */
    private ILayout mChildILayout;

    public UILayoutImpl(Context context) {
        super(context);
        initLayout();
    }

    public UILayoutImpl(Context context, IView iView) {
        super(context);
        initLayout();

        UIParam uiParam = new UIParam(false, false);
        final ViewPattern newViewPattern = startIViewInternal(iView, uiParam);
        startIViewAnim(mLastShowViewPattern, newViewPattern, uiParam, false);
    }

    public UILayoutImpl(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout();
    }

    public UILayoutImpl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayout();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public UILayoutImpl(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initLayout();
    }

    /**
     * inflate之后, 有时会返回 父布局, 这个时候需要处理一下, 才能拿到真实的RootView.
     */
    public static View safeAssignView(final View parentView, final View childView) {
        if (parentView == childView) {
            if (parentView instanceof ViewGroup) {
                final ViewGroup viewGroup = (ViewGroup) parentView;
                return viewGroup.getChildAt(viewGroup.getChildCount() - 1);
            }
            return childView;
        } else {
            return childView;
        }
    }

    public static void saveToSDCard(String data) {
        try {
            String saveFolder = Environment.getExternalStorageDirectory().getAbsoluteFile() +
                    File.separator + Root.APP_FOLDER + File.separator + "log";
            File folder = new File(saveFolder);
            if (!folder.exists()) {
                if (!folder.mkdirs()) {
                    return;
                }
            }
            String dataTime = RCrashHandler.getDataTime("yyyy-MM-dd_HH-mm-ss-SSS");
            File file = new File(saveFolder, /*dataTime + */"ILayout.log");
            boolean append = true;
            if (file.length() > 1024 * 1024 * 10 /*大于10MB重写*/) {
                append = false;
            }
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, append)));
            pw.println(dataTime);
            pw.println(data);
            // 导出手机信息
            pw.println();
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Class<? extends IView>> getIViewList(Class<? extends IView>... iViews) {
        return Arrays.asList(iViews);
    }

    @Override
    public void onLastViewReShow(Bundle bundle) {
        if (mLastShowViewPattern != null) {
            mLastShowViewPattern.mView.setVisibility(VISIBLE);
            mLastShowViewPattern.mIView.onViewReShow(bundle);
        }
    }

    @Override
    public void onLastViewShow(Bundle bundle) {
        viewShow(mLastShowViewPattern, bundle);
    }

    @Override
    public void onLastViewHide() {
        viewHide(mLastShowViewPattern, false);
    }

    /**
     * 滑动返回处理
     */
    @Override
    protected boolean canTryCaptureView(View child) {
        if (isBackPress || mLastShowViewPattern == null ||
                mLastShowViewPattern.isAnimToStart || isFinishing ||
                mViewDragState != ViewDragHelper.STATE_IDLE ||
                needDragClose) {
            return false;
        }

        if (getAttachViewSize() > 1) {
            if (/*!mLastShowViewPattern.mIView.isDialog()//最前的不是对话框
                    &&*/ mLastShowViewPattern.mIView.canTryCaptureView()//激活滑动关闭
                    && mLastShowViewPattern.mView == child) {
                hideSoftInput();
                return true;
            } else {
                return false;
            }
        } else if (enableRootSwipe) {
            hideSoftInput();
            return true;
        }
        return false;
    }

    public void setEnableRootSwipe(boolean enableRootSwipe) {
        this.enableRootSwipe = enableRootSwipe;
    }

    private void initLayout() {
        if (!isInEditMode()) {
            mLayoutActivity = (UILayoutActivity) getContext();
        }
        interruptSet = new HashSet<>();
        setTag(TAG);
        //setPadding(-2, 0, -2, 0);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            mLayoutActivity.getApplication().registerActivityLifecycleCallbacks(mCallbacks);
        }
        setFocusable(true);
        setFocusableInTouchMode(true);
        post(new Runnable() {
            @Override
            public void run() {
                isAttachedToWindow = true;
                loadViewInternal();
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mLayoutActivity.getApplication().unregisterActivityLifecycleCallbacks(mCallbacks);
        isAttachedToWindow = false;
        unloadViewInternal();
/*        if (mIWindowInsetsListeners != null) {
            mIWindowInsetsListeners.clear();
        }*/
        //mOnIViewChangedListeners.clear();
        //interruptSet.clear();
        //mChildILayout = null;
        //mLayoutActivity = null;
    }

    /**
     * 卸载IView
     */
    protected void unloadViewInternal() {
        for (; !mAttachViews.isEmpty(); ) {
            final ViewPattern viewPattern = mAttachViews.pop();
            viewHide(viewPattern);
            viewPattern.mIView.onViewUnload();
            try {
                removeView(viewPattern.mView);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                viewPattern.clear();
            }
        }
    }

    @Override
    public void startIView(final IView iView, final UIParam param) {
        String log = this.getClass().getSimpleName() + " 请求启动:" + iView.getClass().getSimpleName();
        L.i(log);
        saveToSDCard(log);

        iView.onAttachedToILayout(this);
        if (checkInterruptAndRemove(iView)) return;

        runnableCount++;
        final Runnable endRunnable = new Runnable() {
            @Override
            public void run() {
                startInner(iView, param);
                runnableCount--;
            }
        };
        if (isFinishing || (mLastShowViewPattern != null
                && mLastShowViewPattern.mIView.isDialog()
                && !iView.showOnDialog())) {
            //如果在对话框上,启动一个IView的时候, 切启动的iView不是对话框
            runnableCount--;
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    isFinishing = false;
                    startIView(iView, param);
                }
            }, UIIViewImpl.DEFAULT_ANIM_TIME);
        } else {
            if (param.mAsync) {
                post(endRunnable);
            } else {
                endRunnable.run();
            }
        }
    }

    /**
     * 检查是否需要中断启动
     */
    private boolean checkInterrupt(IView iView) {
        /**已经被中断启动了*/
        if (interruptSet.contains(iView)) {
            //interruptSet.remove(iView);
            L.i("请求启动:" + iView.getClass().getSimpleName() + " --启动被中断!");
            return true;
        }
        return false;
    }

    private boolean checkInterruptAndRemove(IView iView) {
        /**已经被中断启动了*/
        if (interruptSet.contains(iView)) {
            interruptSet.remove(iView);
            L.i("请求启动:" + iView.getClass().getSimpleName() + " --启动被中断!");
            return true;
        }
        return false;
    }

    private void startInner(final IView iView, final UIParam param) {
        if (!isAttachedToWindow) {
            post(new Runnable() {
                @Override
                public void run() {
                    startInner(iView, param);
                }
            });
            return;
        }

        if (checkInterruptAndRemove(iView)) return;

        final ViewPattern oldViewPattern = getLastViewPattern();
        isStarting = true;

        if (param.start_mode == UIParam.SINGLE_TOP) {
            if (oldViewPattern != null && oldViewPattern.mIView == iView) {
                //如果已经是最前显示, 调用onViewShow方法
                oldViewPattern.mIView.onViewShow(param.getBundle());
                isStarting = false;
            } else {
                ViewPattern viewPatternByIView = findViewPatternByClass(iView.getClass());
                if (viewPatternByIView == null) {
                    //这个IView 还不存在
                    final ViewPattern newViewPattern = startIViewInternal(iView, param);
//                    startIViewAnim(oldViewPattern, newViewPattern, param);
                    viewPatternByIView = newViewPattern;
                    startIViewAnim(oldViewPattern, viewPatternByIView, param, false);

                } else {
                    //这个IView 存在, 但是不在最前显示
//                    bottomViewFinish(oldViewPattern, viewPatternByIView, param);
//                    topViewStart(viewPatternByIView, param);

                    mAttachViews.remove(viewPatternByIView);
                    mAttachViews.push(viewPatternByIView);
                    startIViewAnim(oldViewPattern, viewPatternByIView, param, true);
                }
            }
        } else {
            //正常的启动模式
            final ViewPattern newViewPattern = startIViewInternal(iView, param);
            startIViewAnim(oldViewPattern, newViewPattern, param, false);
        }
    }

    @Override
    public void startIView(IView iView) {
        startIView(iView, new UIParam());
    }

    private ViewPattern startIViewInternal(final IView iView, UIParam param) {

        hideSoftInput();

        iView.onAttachedToILayout(this);

        //1:inflateContentView, 会返回对应IView的RootLayout
        View rawView = loadViewInternal(iView, param);
        //2:loadContentView
        iView.loadContentView(rawView);

        final ViewPattern newViewPattern = new ViewPattern(iView, rawView);
        mAttachViews.push(newViewPattern);

        for (OnIViewChangedListener listener : mOnIViewChangedListeners) {
            listener.onIViewAdd(this, newViewPattern);
        }

        return newViewPattern;
    }

    /**
     * [add] 星期三 2017-1-11
     */
    private ViewPattern startIViewInternal(final ViewPattern viewPattern) {
        hideSoftInput();

        IView iView = viewPattern.mIView;

        iView.onAttachedToILayout(this);

        //1:inflateContentView, 会返回对应IView的RootLayout
        View rawView = loadViewInternal(iView, null);
        //2:loadContentView
        iView.loadContentView(rawView);

        viewPattern.setView(rawView);
        mAttachViews.push(viewPattern);

        for (OnIViewChangedListener listener : mOnIViewChangedListeners) {
            listener.onIViewAdd(this, viewPattern);
        }

        return viewPattern;
    }

    /**
     * 加载所有添加的IView
     */
    protected void loadViewInternal() {
        ViewPattern lastViewPattern = null;
        for (ViewPattern viewPattern : mAttachViews) {
            if (lastViewPattern != null) {
                viewHide(lastViewPattern);
            }
            lastViewPattern = viewPattern;
            lastViewPattern.mIView.onViewLoad();//1:
        }

        if (lastViewPattern != null) {
            viewShow(lastViewPattern, null);
        }
        mLastShowViewPattern = lastViewPattern;
    }

    /**
     * 加载IView
     */
    private View loadViewInternal(IView iView, UIParam uiParam) {

        //首先调用IView接口的inflateContentView方法,(inflateContentView请不要初始化View)
        //其次会调用loadContentView方法,用来初始化View.(此方法调用之后, 就支持ButterKnife了)
        //1:
        final View view = iView.inflateContentView(mLayoutActivity, this, this, LayoutInflater.from(mLayoutActivity));

        View rawView;

        //返回真实的RootView, 防止连续追加2个相同的IView之后, id重叠的情况
        if (this == view) {
            rawView = getChildAt(getChildCount() - 1);
        } else {
            rawView = view;
        }

        //2:
        iView.onViewCreate(rawView);
        iView.onViewCreate(rawView, uiParam);

        L.e("call: loadViewInternal()-> 加载页面:" + iView.getClass().getSimpleName());

        return rawView;
    }

    @Override
    public void finishIView(final View view, final boolean needAnim) {
        if (view == null) {
            return;
        }
        final ViewPattern viewPatternByView = findViewPatternByView(view);
        if (viewPatternByView == null) {
//            post(new Runnable() {
//                @Override
//                public void run() {
//                    finishIView(view, needAnim);
//                }
//            });
            return;
        }
        finishIView(viewPatternByView.mIView, new UIParam(needAnim, false, false));
    }

    /**
     * @param param isQuiet 如果为true, 上层的视图,将取消生命周期 {@link IView#onViewShow()}  的回调
     */
    private void finishIViewInner(final ViewPattern viewPattern, final UIParam param) {
        isFinishing = true;

        if (viewPattern == null || viewPattern.isAnimToEnd) {
            finishEnd();
            return;
        }

        viewPattern.mView.setEnabled(false);

        String log = this.getClass().getSimpleName() + " 请求关闭2:" + viewPattern.toString() + " isFinishing:" + isFinishing;
        L.i(log);
        saveToSDCard(log);

        ViewPattern lastViewPattern = findLastShowViewPattern(viewPattern);

        if (!viewPattern.interrupt) {
            if (viewPattern.isAnimToStart || isFinishing) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        finishIViewInner(viewPattern, param);
                    }
                });
                return;
            }
        }

        if (param.isSwipeBack) {

        } else {
            /*对话框的处理*/
            if (viewPattern.mIView.isDialog() &&
                    !viewPattern.mIView.canCancel()) {
                finishEnd();
                return;
            }
        }

        boolean isOnTop = viewPattern == mLastShowViewPattern;

//        if (!param.isSwipeBack && !viewPattern.mIView.onBackPressed()) {
//            //如果不是滑动返回, 并且不能退出
//            isFinishing = false;
//            return;
//        }

        topViewFinish(lastViewPattern, viewPattern, param);
        if (isOnTop) {
            bottomViewStart(lastViewPattern, viewPattern, param.mAnim, param.isQuiet);
        } else {
            //如果要关闭的IView不是顶上的,比如是下层的
        }

//        if (param.mAnim) {
//            viewPattern.isAnimToEnd = true;
//            //startViewPatternAnim(viewPattern, lastViewPattern, true, true);
//            //startViewPatternAnim(viewPattern, lastViewPattern, false, false);
//            topViewFinish(viewPattern, true);
//            bottomViewStart(lastViewPattern, viewPattern, param.mAnim, param.isQuiet);
//        } else {
//            if (lastViewPattern != null) {
//
//                if (!param.isQuiet) {
//                    viewShow(lastViewPattern, null);
//                }
//
//                if (lastViewPattern.mView instanceof ILifecycle) {
//                    ((ILifecycle) lastViewPattern.mView).onLifeViewShow();
//                }
//            }
//            removeViewPattern(viewPattern);
//        }

        //mLastShowViewPattern = lastViewPattern;//在top view 完全remove 之后, 再赋值
    }

    @Override
    public void finishIView(Class<?> clz) {
        ViewPattern pattern = findViewPatternByClass(clz);
        if (pattern != null) {
            pattern.interrupt = true;
            finishIViewInner(pattern, new UIParam(false, true, true));
        }
    }

    @Override
    public void finishIView(View view) {
        finishIView(view, true);
    }

    @Override
    public void finishIView(IView iview) {
        finishIView(iview, true);
    }

    @Override
    public void finishIView(IView iview, boolean needAnim) {
        finishIView(iview, needAnim, false);
    }

    @Override
    public void finishIView(IView iview, boolean needAnim, boolean quiet) {
        finishIView(iview, new UIParam(needAnim, false, quiet));
    }

    @Override
    public void finishIView(final IView iview, final UIParam param) {
        finishIView(iview, param, true);
    }

    protected void finishIView(final IView iview, final UIParam param, boolean checkInterrupt) {
        if (iview == null) {
            finishEnd();
            return;
        }

        final ViewPattern viewPattern = findViewPatternByIView(iview);
        if (viewPattern == null) {
            interruptSet.add(iview);
        }
        if (viewPattern == null || viewPattern.mIView == null) {
            return;
        }

        viewPattern.mView.setEnabled(false);

        //            if (viewPattern.interrupt) {
//                log = iview.getClass().getSimpleName() + " 已在中断";
//                L.i(log);
//                saveToSDCard(log);
//                return;
//            }
        viewPattern.interrupt = true;//中断启动

        if (checkInterrupt && checkInterrupt(iview)) {
            String log = this.getClass().getSimpleName() + " 已在中断列表中:" + iview.getClass().getSimpleName();
            L.i(log);
            saveToSDCard(log);
            return;
        } else {
            String log = this.getClass().getSimpleName() + " 请求关闭/中断:" + iview.getClass().getSimpleName();
            L.i(log);
            saveToSDCard(log);

            if (mLastShowViewPattern != null &&
                    mLastShowViewPattern != viewPattern &&
                    mLastShowViewPattern.mIView.isDialog()) {
                L.i("等待对话框:" + mLastShowViewPattern.mIView.getClass().getSimpleName() + " 的关闭");
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finishIView(iview, param, false);
                    }
                }, DEFAULT_ANIM_TIME);
                return;
            }

            final Runnable endRunnable = new Runnable() {
                @Override
                public void run() {
                    interruptSet.add(iview);
                    finishIViewInner(viewPattern, param);
                }
            };

            if (param.mAsync) {
                post(endRunnable);
                return;
            }
            endRunnable.run();
        }
    }


    /**
     * 关闭操作被中断/取消, 需要恢复一些变量
     */
    void finishEnd() {
        needDragClose = false;
        isBackPress = false;
        isFinishing = false;
        isSwipeDrag = false;
        isStarting = false;
    }

    @Override
    public void showIView(View view) {
        showIView(view, true);
    }

    @Override
    public void showIView(final View view, final boolean needAnim) {
        showIView(view, new UIParam(needAnim));
    }

    @Override
    public void showIView(final View view, final UIParam param) {
        if (view == null) {
            return;
        }

        post(new Runnable() {
            @Override
            public void run() {
                final ViewPattern viewPattern = findViewPatternByView(view);
                showIViewInternal(viewPattern, param);
            }
        });
    }

    @Override
    public void showIView(IView iview, boolean needAnim) {
        showIView(iview, new UIParam(needAnim));
    }

    @Override
    public void showIView(IView iview) {
        showIView(iview, true);
    }

    @Override
    public void showIView(final IView iview, final UIParam param) {
        post(new Runnable() {
            @Override
            public void run() {
                final ViewPattern viewPattern = findViewPatternByIView(iview);
                showIViewInternal(viewPattern, param);
            }
        });
    }

    private void showIViewInternal(final ViewPattern viewPattern, final UIParam param) {
        if (viewPattern == null) {
            return;
        }

        if (isFinishing || !isAttachedToWindow) {
            post(new Runnable() {
                @Override
                public void run() {
                    showIViewInternal(viewPattern, param);
                }
            });
            return;
        }

        viewPattern.mView.setVisibility(VISIBLE);
        viewPattern.mView.bringToFront();

        if (viewPattern == mLastShowViewPattern) {
            viewReShow(viewPattern, param == null ? null : param.getBundle());
            return;
        }

        final ViewPattern lastShowViewPattern = mLastShowViewPattern;
        mLastShowViewPattern = viewPattern;

        mAttachViews.remove(viewPattern);
        mAttachViews.push(viewPattern);

        final Runnable endRunnable = new Runnable() {
            @Override
            public void run() {
                viewShow(mLastShowViewPattern, param == null ? null : param.getBundle());
                printLog();
            }
        };

        isStarting = true;
        if (mLastShowViewPattern.mIView.isDialog()) {
            startDialogAnim(mLastShowViewPattern, param.mAnim ? mLastShowViewPattern.mIView.loadShowAnimation() : null, endRunnable);
        } else {
            if (lastShowViewPattern != null) {
                safeStartAnim(lastShowViewPattern.mView, needAnim(param, false) ?
                        mLastShowViewPattern.mIView.loadOtherHideAnimation() : null, new Runnable() {
                    @Override
                    public void run() {
                        viewHide(lastShowViewPattern);
                    }
                });
            }
            safeStartAnim(mLastShowViewPattern.mView, (param.needTransitionStartAnim ||
                    mLastShowViewPattern.mIView.needTransitionStartAnim() ||
                    needAnim(param, false)) ?
                    mLastShowViewPattern.mIView.loadShowAnimation() : null, endRunnable);
        }
    }

    @Override
    public void hideIView(final View view, final boolean needAnim) {
        if (isFinishing) {
            post(new Runnable() {
                @Override
                public void run() {
                    hideIView(view, needAnim);
                }
            });
            return;
        }

        post(new Runnable() {
            @Override
            public void run() {
                final ViewPattern viewPattern = findViewPatternByView(view);
                final Runnable endRunnable = new Runnable() {
                    @Override
                    public void run() {
                        viewHide(viewPattern);
                    }
                };
                if (viewPattern.mIView.isDialog()) {
                    finishDialogAnim(viewPattern, needAnim ? viewPattern.mIView.loadHideAnimation() : null, endRunnable);
                } else {
                    //隐藏的时候, 会错乱!!! 找不到之前显示的View, 慎用隐藏...
                    safeStartAnim(view, needAnim ? viewPattern.mIView.loadHideAnimation() : null, endRunnable);
                }
            }
        });
    }

    @Override
    public void hideIView(View view) {
        hideIView(view, true);
    }

    @Override
    public View getLayout() {
        return this;
    }

    @Override
    public boolean requestBackPressed() {
        return requestBackPressed(new UIParam());
    }

    @Override
    public boolean requestBackPressed(final UIParam param) {
        if (isSwipeDrag) {
            return false;
        }

        if (isBackPress) {
            return false;
        }
        if (getAttachViewSize() <= 0) {
            return true;
        }
        if (getAttachViewSize() == 1) {
            if (mLastShowViewPattern == null) {
                return true;
            } else {
                return mLastShowViewPattern.mIView.onBackPressed();
            }
        }

        if (param.isSwipeBack) {
            if (!mLastShowViewPattern.mIView.canSwipeBackPressed()) {
                //不能滑动返回
                return false;
            }
        } else {
            if (!mLastShowViewPattern.mIView.onBackPressed()) {
                //不能退出
                return false;
            }
        }

        isBackPress = true;
        finishIView(mLastShowViewPattern.mIView, param);
        return false;
    }

    /**
     * 返回追加了多个iView
     */
    public int getAttachViewSize() {
        return mAttachViews.size();
    }

    @Override
    public void replaceIView(final IView iView, final UIParam param) {
        if (iView == null) {
            return;
        }

        iView.onAttachedToILayout(this);

        if (isFinishing) {
            post(new Runnable() {
                @Override
                public void run() {
                    replaceIView(iView, param);
                }
            });
            return;
        }

        if (mLastShowViewPattern != null && mLastShowViewPattern.mIView.isDialog() &&
                (!iView.isDialog() || !iView.showOnDialog())) {
            L.i("等待对话框:" + mLastShowViewPattern.mIView.getClass().getSimpleName() + " 的关闭");
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    replaceIView(iView, param);
                }
            }, DEFAULT_ANIM_TIME);
            return;
        }

        runnableCount++;

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final ViewPattern oldViewPattern = getLastViewPattern();
                final ViewPattern newViewPattern = startIViewInternal(iView, param);

                //3:
                newViewPattern.mIView.onViewLoad();

                topViewStart(newViewPattern, param);

                if (param.isReplaceIViewEmpty() || oldViewPattern.mIView == param.replaceIView) {
                    final Runnable endRunnable = new Runnable() {
                        @Override
                        public void run() {
                            removeViewPattern(oldViewPattern, param);
                            param.clear();
                        }
                    };
                    viewHide(oldViewPattern);
                    bottomViewRemove(oldViewPattern, newViewPattern, endRunnable, true, param);
                }

                mLastShowViewPattern = newViewPattern;
                runnableCount--;
            }
        };

        isStarting = true;
        if (param.mAsync) {
            post(runnable);
        } else {
            runnable.run();
        }
    }

    @Override
    public void replaceIView(IView iView) {
        replaceIView(iView, new UIParam());
    }

    /**
     * 获取最前显示的视图信息
     */
    public ViewPattern getLastViewPattern() {
//        if (mAttachViews.isEmpty()) {
//            return null;
//        }
//        return mAttachViews.lastElement();
        return mLastShowViewPattern;
    }

    private void startIViewAnim(final ViewPattern oldViewPattern, final ViewPattern newViewPattern,
                                final UIParam param, boolean reLoad) {
//        if (isAttachedToWindow) {
        mLastShowViewPattern = newViewPattern;

        if (!reLoad) {
            newViewPattern.mIView.onViewLoad();
        }

        clearOldViewFocus(oldViewPattern);

        //startViewPatternAnim(newViewPattern, oldViewPattern, false, true);
        //startViewPatternAnim(newViewPattern, oldViewPattern, true, false);
        bottomViewFinish(oldViewPattern, newViewPattern, param);//先执行Bottom
        topViewStart(newViewPattern, param);//后执行Top
//        } else {
//            for (ViewPattern viewPattern : mAttachViews) {
//                viewPattern.mView.setVisibility(INVISIBLE);
//            }
//        }
    }

    /**
     * 清除焦点
     */
    private void clearOldViewFocus(ViewPattern oldViewPattern) {
        if (oldViewPattern != null) {
            oldViewPattern.mView.clearFocus();
            View focus = oldViewPattern.mView.findFocus();
            if (focus != null) {
                focus.clearFocus();
            }
        }
    }

    /**
     * 顶上视图进入的动画
     */

    private void topViewStart(final ViewPattern topViewPattern, final UIParam param) {
        final Animation animation = topViewPattern.mIView.loadStartAnimation();
        animation.setFillAfter(false);

        final Runnable endRunnable = new Runnable() {
            @Override
            public void run() {
                L.i(topViewPattern.mIView.getClass().getSimpleName() + " 启动完毕.");
                viewShow(topViewPattern, param.getBundle());
                topViewPattern.isAnimToStart = false;
                finishEnd();
                printLog();
            }
        };

        if (topViewPattern.mView instanceof ILifecycle) {
            ((ILifecycle) topViewPattern.mView).onLifeViewShow();
        }

        topViewPattern.mView.bringToFront();
        topViewPattern.isAnimToStart = true;
        topViewPattern.isAnimToEnd = false;

        if (param.needTransitionStartAnim ||
                topViewPattern.mIView.needTransitionStartAnim() ||
                needAnim(param, topViewPattern.mIView.isDialog())) {
            if (topViewPattern.mIView.isDialog()) {
                //对话框的启动动画,作用在第一个子View上
                startDialogAnim(topViewPattern, animation, endRunnable);
            } else {
                safeStartAnim(topViewPattern.mIView.getAnimView(), animation, endRunnable);
            }
        } else {
            if (param.mAsync) {
                post(endRunnable);
            } else {
                endRunnable.run();
            }
        }
    }

    /**
     * 顶上视图退出的动画
     */
    private void topViewFinish(final ViewPattern bottomViewPattern, final ViewPattern topViewPattern, final UIParam param) {
        final Animation animation = topViewPattern.mIView.loadFinishAnimation();
        animation.setFillAfter(true);//2017-9-1
        final Runnable endRunnable = new Runnable() {
            @Override
            public void run() {
                L.i(topViewPattern.mIView.getClass().getSimpleName() + " 关闭完成.");
                mLastShowViewPattern = bottomViewPattern;//星期一 2017-1-16

                topViewPattern.isAnimToEnd = false;
                viewHide(topViewPattern);
                removeViewPattern(topViewPattern, param);

                finishEnd();
                printLog();
            }
        };


        if (topViewPattern.mView instanceof ILifecycle) {
            ((ILifecycle) topViewPattern.mView).onLifeViewHide();
        }

        if (!param.needTransitionExitAnim && !needAnim(param, topViewPattern.mIView.isDialog())) {
            endRunnable.run();
            return;
        }

        isFinishing = true;

        topViewPattern.isAnimToEnd = true;
        topViewPattern.isAnimToStart = false;

        if (topViewPattern.mIView.isDialog()) {
            //对话框的启动动画,作用在第一个子View上
            finishDialogAnim(topViewPattern, animation, endRunnable);
        } else {
            safeStartAnim(topViewPattern.mIView.getAnimView(), animation, endRunnable, true);
        }
    }

    private boolean needAnim(final UIParam param, boolean isDialog) {
        if (isDialog) {
            return param.mAnim;
        }
        return (!RApplication.isLowDevice && param.mAnim);
    }

    @Deprecated
    private boolean needTransitionAnim(ViewPattern viewPattern, boolean isSwipeBack, boolean isExit) {
        if (isSwipeBack) {
            return false;
        }
        if (isExit) {
            return viewPattern.mIView.needTransitionExitAnim();
        } else {
            return viewPattern.mIView.needTransitionStartAnim();
        }
    }

    /**
     * 为了确保任务都行执行完了, 延迟打印堆栈信息
     */
    private void printLog() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                logLayoutInfo();
            }
        }, 16);
    }

    /**
     * 底部视图进入动画
     */
    private void bottomViewStart(final ViewPattern bottomViewPattern, final ViewPattern topViewPattern,
                                 boolean anim, final boolean quiet) {
        if (bottomViewPattern == null) {
            return;
        }
        final Runnable endRunnable = new Runnable() {
            @Override
            public void run() {
                if (!quiet) {
                    viewShow(bottomViewPattern, null);
                }
                bottomViewPattern.mIView.onViewReShow(null);
            }
        };
        bottomViewPattern.mView.setVisibility(VISIBLE);
        showChildLayoutLastView();

        if (bottomViewPattern.mView instanceof ILifecycle) {
            ((ILifecycle) bottomViewPattern.mView).onLifeViewShow();
        }


        if (topViewPattern.mIView.isDialog()) {
            //对话框结束时, 不执行生命周期
        } else {
            if (RApplication.isLowDevice || !anim || quiet) {
                endRunnable.run();
            } else {
                final Animation animation = topViewPattern.mIView.loadOtherEnterAnimation();
                animation.setFillAfter(false);
                safeStartAnim(bottomViewPattern.mIView.getAnimView(), animation, endRunnable);
            }
        }
    }

    /**
     * 底部视图退出动画
     */
    private void bottomViewFinish(final ViewPattern bottomViewPattern,
                                  final ViewPattern topViewPattern,
                                  final UIParam param) {
        final Runnable endRunnable = new Runnable() {
            @Override
            public void run() {
                viewHide(bottomViewPattern, param.hideLastIView);
                finishEnd();
            }
        };
        bottomViewRemove(bottomViewPattern, topViewPattern, endRunnable, false, param);
    }

    /**
     * 底部视图 销毁
     */
    private void bottomViewRemove(final ViewPattern bottomViewPattern,
                                  final ViewPattern topViewPattern,
                                  final Runnable endRunnable,
                                  boolean isRemove,/*是否需要移除bottomViewPattern*/
                                  final UIParam param) {
        if (bottomViewPattern == null) {
            return;
        }

        bottomViewPattern.isAnimToStart = false;

        if (bottomViewPattern.mView instanceof ILifecycle) {
            ((ILifecycle) bottomViewPattern.mView).onLifeViewHide();
        }

        if (topViewPattern.mIView.isDialog() && !isRemove) {
            //对话框弹出的时候, 底部IView 不执行周期
        } else {
            if (!RApplication.isLowDevice || param.mAnim) {
                final Animation animation = topViewPattern.mIView.loadOtherExitAnimation();
                animation.setFillAfter(false);
                safeStartAnim(bottomViewPattern.mIView.getAnimView(), animation, endRunnable, true);
            } else {
                endRunnable.run();
            }
        }
    }

    /**
     * 执行IView生命周期onViewHide
     */
    private void viewHide(final ViewPattern viewPattern, boolean hide) {
        if (viewPattern == null ||
                viewPattern.mIView.getIViewShowState() == IView.IViewShowState.STATE_VIEW_HIDE) {
            return;
        }
        saveToSDCard(viewPattern.mIView.getClass().getSimpleName() + " onViewHide()");
        viewPattern.mIView.onViewHide();
        if (hide && !viewPattern.mIView.isDialog()) {
            viewPattern.mView.setVisibility(GONE);
        }
    }

    private void viewHide(final ViewPattern viewPattern) {
        viewHide(viewPattern, false);
    }

    /**
     * 执行IView生命周期onViewShow
     */
    private void viewShow(final ViewPattern viewPattern, final Bundle bundle) {
        isStarting = false;
        if (viewPattern == null ||
                viewPattern.mIView.getIViewShowState() == IView.IViewShowState.STATE_VIEW_SHOW) {
            return;
        }
        saveToSDCard(viewPattern.mIView.getClass().getSimpleName() + " onViewShow()" + bundle);
//        viewPattern.mView.setVisibility(VISIBLE);
//        viewPattern.mView.bringToFront();
        viewPattern.mIView.onViewShow(bundle);
    }

    /**
     * 执行IView生命周期onViewReShow
     */
    private void viewReShow(final ViewPattern viewPattern, final Bundle bundle) {
        isStarting = false;
        saveToSDCard(viewPattern.mIView.getClass().getSimpleName() + " onViewReShow()" + bundle);
        viewPattern.mIView.onViewReShow(bundle);
    }

    /**
     * 启动之前, 开始View的动画
     * 新界面, 播放启动动画;
     * 旧界面, 播放伴随退出动画
     * 2016-12-2 太复杂了, 拆成4个方法
     */
    @Deprecated
    private void startViewPatternAnim(final ViewPattern newViewPattern, final ViewPattern lastViewPattern, boolean isStart, boolean withOther) {
        if (newViewPattern == null) {
            return;
        }

        if (withOther) {
            if (newViewPattern.mIView.isDialog()) {
                //对话框不产生伴随动画.
                if (isStart) {
                    if (lastViewPattern != null) {
                        lastViewPattern.mIView.onViewShow(null);
                    }
                } else {
                    if (lastViewPattern != null) {
                        lastViewPattern.mIView.onViewHide();
                    }
                }
            } else {
                //伴随, 需要产生的动画
                if (isStart) {
                    //上层的View退出, 下层的View进入的动画
                    final Animation animation = newViewPattern.mIView.loadOtherEnterAnimation();

                    safeStartAnim(lastViewPattern.mView, animation, new Runnable() {
                        @Override
                        public void run() {
                            lastViewPattern.mIView.onViewShow(null);

                            if (lastViewPattern.mView instanceof ILifecycle) {
                                ((ILifecycle) lastViewPattern.mView).onLifeViewShow();
                            }
                        }
                    });
                } else if (lastViewPattern != null) {
                    //上层的View进入, 下层的View退出的动画
                    final Animation animation = newViewPattern.mIView.loadOtherExitAnimation();
                    safeStartAnim(lastViewPattern.mView, animation, new Runnable() {
                        @Override
                        public void run() {
                            lastViewPattern.mIView.onViewHide();

                            if (lastViewPattern.mView instanceof ILifecycle) {
                                ((ILifecycle) lastViewPattern.mView).onLifeViewHide();
                            }
                        }
                    });
                }
            }
        } else {
            if (newViewPattern.mIView.isDialog()) {
                if (isStart) {
                    //对话框的启动动画,作用在第一个子View上
                    startDialogAnim(newViewPattern, newViewPattern.mIView.loadStartAnimation(), new Runnable() {
                        @Override
                        public void run() {
                            newViewPattern.mIView.onViewShow(null);
                        }
                    });
                } else {
                    //finish View的动画
                    finishDialogAnim(newViewPattern, newViewPattern.mIView.loadFinishAnimation(), new Runnable() {
                        @Override
                        public void run() {
                            removeViewPattern(newViewPattern, null);
                        }
                    });
                }
            } else {
                //自己的动画
                if (isStart) {
                    //启动View的动画
                    final Animation animation = newViewPattern.mIView.loadStartAnimation();
                    safeStartAnim(newViewPattern.mView, animation, new Runnable() {
                        @Override
                        public void run() {
                            newViewPattern.mIView.onViewShow(null);
                        }
                    });
                } else {
                    //finish View的动画
                    final Animation animation = newViewPattern.mIView.loadFinishAnimation();
                    safeStartAnim(newViewPattern.mView, animation, new Runnable() {
                        @Override
                        public void run() {
                            removeViewPattern(newViewPattern, null);
                        }
                    });
                }
            }
        }
    }

    /**
     * 对话框的启动动画
     */
    private void startDialogAnim(final ViewPattern dialogPattern, final Animation animation, final Runnable endRunnable) {
        //对话框的启动动画,作用在第一个子View上

        /*是否变暗*/
        if (dialogPattern.mIView.isDimBehind()) {
            AnimUtil.startArgb(dialogPattern.mIView.getDialogDimView(),
                    Color.TRANSPARENT, dialogPattern.mIView.getDimColor(), DEFAULT_ANIM_TIME);
        }

        if (dialogPattern.mIView.canTouchOnOutside()) {
            dialogPattern.mView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dialogPattern.mIView.canCanceledOnOutside()) {
                        finishIView(dialogPattern.mView);
                    }
                }
            });
        }

        if (dialogPattern.mIView.canDoubleCancel()) {
            RGestureDetector.onDoubleTap(dialogPattern.mView, new RGestureDetector.OnDoubleTapListener() {
                @Override
                public void onDoubleTap() {
                    if (dialogPattern.mIView.canCanceledOnOutside()) {
                        finishIView(dialogPattern.mView);
                    }
                }
            });
        }

        safeStartAnim(dialogPattern.mIView.getAnimView(), animation, endRunnable);
    }

    /**
     * 销毁对话框的动画
     */
    private void finishDialogAnim(final ViewPattern dialogPattern, final Animation animation, final Runnable end) {
          /*是否变暗*/
        if (dialogPattern.mIView.isDimBehind()) {
            AnimUtil.startArgb(dialogPattern.mIView.getDialogDimView(),
                    dialogPattern.mIView.getDimColor(), Color.TRANSPARENT, DEFAULT_ANIM_TIME);
        }

        final View animView = dialogPattern.mIView.getAnimView();

        final Runnable endRunnable = new Runnable() {
            @Override
            public void run() {
                dialogPattern.mView.setAlpha(0);
                dialogPattern.mView.setVisibility(INVISIBLE);
                end.run();
            }
        };

        safeStartAnim(animView, animation, endRunnable, true);
    }

    /**
     * 安全的启动一个动画
     */
    private boolean safeStartAnim(final View view, final Animation animation,
                                  final Runnable endRunnable) {
        return safeStartAnim(view, animation, endRunnable, false);
    }

    private boolean safeStartAnim(final View view, final Animation animation,
                                  final Runnable endRunnable, boolean isFinish) {
        if (view == null) {
            if (endRunnable != null) {
                endRunnable.run();
            }
            return false;
        }

        if (animation == null) {
            if (endRunnable != null) {
                endRunnable.run();
            }
            return false;
        }

        animation.setAnimationListener(new AnimRunnable(view, endRunnable, isFinish));

        view.startAnimation(animation);

        return true;
    }

    public ViewPattern findViewPatternByView(View view) {
        for (ViewPattern viewPattern : mAttachViews) {
            if (viewPattern.mView == view) {
                return viewPattern;
            }
        }
        return null;
    }

    public ViewPattern findViewPatternByIView(IView iview) {
        for (ViewPattern viewPattern : mAttachViews) {
//            if (TextUtils.equals(viewPattern.mIView.getClass().getSimpleName(), iview.getClass().getSimpleName())) {
//                return viewPattern;
//            }

            if (viewPattern.mIView == iview) {
                return viewPattern;
            }
        }
        return null;
    }

    public ViewPattern findViewPatternByClass(Class<?> clz) {
        for (ViewPattern viewPattern : mAttachViews) {
            if (isViewPatternEquals(clz, viewPattern)) {
                return viewPattern;
            }
        }
        return null;
    }

    /**
     * 判断ViewPattern的类名是否相等
     */
    private boolean isViewPatternEquals(Class<?> clz, ViewPattern viewPattern) {
        return TextUtils.equals(viewPattern.mIView.getClass().getSimpleName(), clz.getSimpleName());
    }

    public ViewPattern findLastShowViewPattern() {
        return findViewPattern(getAttachViewSize() - 2);
    }

    public ViewPattern findLastShowViewPattern(final ViewPattern anchor) {
        if (anchor == mLastShowViewPattern) {
            return findViewPattern(getAttachViewSize() - 2);
        } else {
            return mLastShowViewPattern;
        }
    }

    public ViewPattern findLastViewPattern() {
        return findViewPattern(getAttachViewSize() - 1);
    }

    public ViewPattern findViewPattern(int position) {
        if (getAttachViewSize() > position && position >= 0) {
            return mAttachViews.get(position);
        }
        return null;
    }

    public void removeViewPattern(final ViewPattern viewPattern, final UIParam param) {
        hideSoftInput();
        final View view = viewPattern.mView;
        //ViewCompat.setAlpha(view, 0);
        viewPattern.mIView.onViewUnload();

        view.setEnabled(false);
        view.setVisibility(GONE);
        ViewCompat.setAlpha(view, 0);

        post(new Runnable() {
            @Override
            public void run() {
                try {
                    removeView(view);
                } catch (Exception e) {

                }
                try {
                    //UI.setView(view, 0, 0);
                    isFinishing = false;
                    isBackPress = false;
                    viewPattern.mIView.release();
                    interruptSet.remove(viewPattern.mIView);
                    mAttachViews.remove(viewPattern);
                    L.e("call: removeViewPattern()-> 关闭界面结束:" + viewPattern.mIView.getClass().getSimpleName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (param != null && param.getUnloadRunnable() != null) {
                    if (param.mAsync) {
                        post(param.getUnloadRunnable());
                    } else {
                        param.getUnloadRunnable().run();
                    }
                    param.clear();
                }
            }
        });

        for (OnIViewChangedListener listener : mOnIViewChangedListeners) {
            listener.onIViewRemove(this, viewPattern);
        }
    }

    @Override
    public void onShowInPager(final UIViewPager viewPager) {
        if (mLastShowViewPattern == null) {
            return;
        }
        if (runnableCount > 0) {
            post(new Runnable() {
                @Override
                public void run() {
                    onShowInPager(viewPager);
                }
            });
            return;
        }
        mLastShowViewPattern.mIView.onShowInPager(viewPager);
    }

    @Override
    public void onHideInPager(final UIViewPager viewPager) {
        if (mLastShowViewPattern == null) {
            return;
        }
        if (runnableCount > 0) {
            post(new Runnable() {
                @Override
                public void run() {
                    onShowInPager(viewPager);
                }
            });
            return;
        }
        mLastShowViewPattern.mIView.onHideInPager(viewPager);
    }

    private boolean needDelay() {
        if (!isAttachedToWindow) {
            return true;
        }
        if (getAttachViewSize() > 0 && mLastShowViewPattern == null) {
            return true;
        }
        return false;
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mInsets[0] = insets.getSystemWindowInsetLeft();
            mInsets[1] = insets.getSystemWindowInsetTop();
            mInsets[2] = insets.getSystemWindowInsetRight();
            mInsets[3] = insets.getSystemWindowInsetBottom();

            post(new Runnable() {
                @Override
                public void run() {
                    notifyListener();
                }
            });
            return super.onApplyWindowInsets(insets.replaceSystemWindowInsets(insets.getSystemWindowInsetLeft(), 0,
                    insets.getSystemWindowInsetRight(), lockHeight ? 0 : insets.getSystemWindowInsetBottom()));
        } else {
            return super.onApplyWindowInsets(insets);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int l = 0;
        if (isWantSwipeBack /*&& !requestLayout*/) {
            if (getChildCount() > 0) {
                View childAt = getChildAt(getChildCount() - 1);

                l = childAt.getLeft();
            }
        }
//        super.onLayout(changed, left, top, right, bottom);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View childAt = getChildAt(i);
//            if (i == count - 1 || i == count - 2) {
            childAt.layout(0, 0, right, bottom);
            if (childAt.getMeasuredHeight() == getMeasuredHeight() &&
                    childAt.getMeasuredWidth() == getMeasuredWidth()) {
                childAt.setTag(R.id.tag_layout, "true");
            } else {
                childAt.setTag(R.id.tag_layout, "false");
            }
//            } else {
//                childAt.setTag(R.id.tag_layout, "false");
//            }
        }
//        for (int i = 0; i < getChildCount(); i++) {
//            View childAt = getChildAt(i);
//            childAt.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
//        }
        if (isWantSwipeBack /*&& !requestLayout*/) {
            if (getChildCount() > 0) {
                View childAt = getChildAt(getChildCount() - 1);

                if (childAt.getVisibility() == VISIBLE && childAt.getAlpha() == 1) {
                    childAt.layout(l, childAt.getTop(),
                            l + childAt.getMeasuredWidth(), childAt.getTop() + childAt.getMeasuredHeight());
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //of java
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        //of kotlin
//        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
//        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
//        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
//        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View childAt = getChildAt(i);

            ViewPattern viewPatternByView = findViewPatternByView(childAt);
            int iViewSize = getIViewSize();

            if (viewPatternByView == null) {
                continue;
            }

            int indexFromIViews = getIndexFromIViews(viewPatternByView);

            //-----------------只有部分界面需要测量, 优化性能---------
            boolean needMeasure = false;
            if (!"true".equalsIgnoreCase(String.valueOf(childAt.getTag(R.id.tag_layout)))) {
                //如果还没有layout过
                needMeasure = true;
            } else if (viewPatternByView == mLastShowViewPattern) {
                //最后一个页面
                needMeasure = true;
            } else if (i == count - 1 /*|| i == count - 2*/) {
                //倒数第一个, 第二个view
                needMeasure = true;
            } else if (indexFromIViews >= 0 && (indexFromIViews == iViewSize - 1 /*|| indexFromIViews == iViewSize - 2*/)) {
                //倒数第一个, 第二个iview
                needMeasure = true;
            } else {
                if (viewPatternByView.mIView.needForceMeasure() ||
                        viewPatternByView.mIView.haveParentILayout() ||
                        viewPatternByView.mIView.haveChildILayout()) {
                    //需要强制测量
                    needMeasure = true;
                } else {
                    IView iView = viewPatternByView.mIView;
                    for (int j = mAttachViews.size() - 1; j >= 0; j--) {
                        ViewPattern viewPattern = mAttachViews.get(j);
                        if (viewPattern.mIView.isDialog() || viewPattern.mIView.showOnDialog()) {
                            //界面上面全是对话框
                            needMeasure = true;
                            if (viewPattern.mIView == iView) {
                                break;
                            }
                        } else if (viewPattern.mIView == iView) {
                            break;
                        } else {
                            needMeasure = false;
                            break;
                        }
                    }
                }

//                if (!"true".equalsIgnoreCase(String.valueOf(childAt.getTag(R.id.tag_layout)))) {
                //childAt.measure(exactlyMeasure(widthSize), exactlyMeasure(heightSize));
//                } else {
//                    ViewPattern viewPatternByView = findViewPatternByView(childAt);
//                    if (viewPatternByView != null) {
//                        IView iView = viewPatternByView.mIView;
//                        if (iView != null && iView.haveChildILayout()) {
//                            childAt.measure(exactlyMeasure(widthSize), exactlyMeasure(heightSize));
//                        }
//                    }
//                }
            }
            //needMeasure = true;
            if (needMeasure) {
                childAt.measure(exactlyMeasure(widthSize), exactlyMeasure(heightSize));
            }
            //-----------------------------
//            if (i == count - 1 || i == count - 2) {
//                childAt.measure(exactlyMeasure(widthSize), exactlyMeasure(heightSize));
//            }
//            if (i == count - 2) {
////                if (mAttachViews.get(i).mIView.showOnDialog() || mAttachViews.get(i).mIView.isDialog()) {
////                    childAt.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY),
////                            MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
////                } else
//                if (childNeedMeasure(mAttachViews.get(i).mIView) || childNeedMeasure(childAt, widthSize, heightSize)) {
//                    childAt.measure(exactlyMeasure(widthSize), exactlyMeasure(heightSize));
//                }
//            }
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    private int getIndexFromIViews(ViewPattern viewPattern) {
        int result = -1;
        for (int i = 0; i < mAttachViews.size(); i++) {
            ViewPattern pattern = mAttachViews.get(i);
            if (pattern == viewPattern) {
                result = i;
                break;
            }
        }
        return result;
    }

    private boolean lastIsDialog() {
        if (mLastShowViewPattern != null) {
            if (mLastShowViewPattern.mIView.isDialog() ||
                    mLastShowViewPattern.mIView.showOnDialog()) {
                return true;
            }
        }
        return false;
    }

    private int exactlyMeasure(int size) {
        return MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
    }

    private boolean childNeedMeasure(IView iView) {
        if (iView.getIViewShowState() == IView.IViewShowState.STATE_NORMAL ||
                iView.getIViewShowState() == IView.IViewShowState.STATE_VIEW_SHOW) {
            return true;
        }
        return false;
    }


    private boolean childNeedMeasure(View child, int viewWidth, int viewHeight) {
        if (child == null) {
            return false;
        }
        return child.getMeasuredHeight() != viewHeight || child.getMeasuredWidth() != viewWidth;
    }

    private void notifyListener() {
         /*键盘弹出监听事件*/
        if (mIWindowInsetsListeners != null) {
            for (IWindowInsetsListener listener : mIWindowInsetsListeners) {
                listener.onWindowInsets(mInsets[0], mInsets[1], mInsets[2], mInsets[3]);
            }
        }
    }

    public UILayoutImpl addIWindowInsetsListener(IWindowInsetsListener listener) {
        if (listener == null) {
            return this;
        }
        if (mIWindowInsetsListeners == null) {
            mIWindowInsetsListeners = new ArrayList<>();
        }
        this.mIWindowInsetsListeners.add(listener);
        return this;
    }

    public UILayoutImpl removeIWindowInsetsListener(IWindowInsetsListener listener) {
        if (listener == null || mIWindowInsetsListeners == null) {
            return this;
        }
        this.mIWindowInsetsListeners.remove(listener);
        return this;
    }

    public UILayoutImpl addOnIViewChangeListener(OnIViewChangedListener listener) {
        if (listener == null) {
            return this;
        }
        this.mOnIViewChangedListeners.add(listener);
        return this;
    }

    public UILayoutImpl removeOnIViewChangeListener(OnIViewChangedListener listener) {
        if (listener == null || mOnIViewChangedListeners == null) {
            return this;
        }
        this.mOnIViewChangedListeners.remove(listener);
        return this;
    }

    public void setLockHeight(boolean lockHeight) {
        this.lockHeight = lockHeight;
    }

    /**
     * 获取底部装饰物的高度 , 通常是键盘的高度
     */
    public int getInsersBottom() {
        return mInsets[3];
    }

    public void hideSoftInput() {
        if (isSoftKeyboardShow()) {
            InputMethodManager manager = (InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    /**
     * 判断键盘是否显示
     */
    private boolean isSoftKeyboardShow() {
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int keyboardHeight = getSoftKeyboardHeight();
        return screenHeight != keyboardHeight && keyboardHeight > 100;
    }

    /**
     * 获取键盘的高度
     */
    private int getSoftKeyboardHeight() {
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        Rect rect = new Rect();
        getWindowVisibleDisplayFrame(rect);
        int visibleBottom = rect.bottom;
        return screenHeight - visibleBottom;
    }

    public void showSoftInput() {
        InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInputFromInputMethod(getWindowToken(), 0);
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (mLastShowViewPattern != null) {
            if (mLastShowViewPattern.isAnimToEnd) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        onActivityResult(requestCode, resultCode, data);
                    }
                });
            } else {
                mLastShowViewPattern.mIView.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    /**
     * 根据位置, 返回IView
     */
    public ViewPattern getViewPattern(int position) {
        if (position < 0 || position >= getAttachViewSize()) {
            return null;
        }
        return mAttachViews.get(position);
    }

    @Override
    public ViewPattern getViewPatternAtLast(int lastCount) {
        return getViewPattern(getAttachViewSize() - 1 - lastCount);
    }

    /**
     * 通过类名, 返回最早添加的IView
     */
    public ViewPattern getViewPatternWithClass(Class<?> cls) {
        for (ViewPattern pattern : mAttachViews) {
            if (pattern.mIView.getClass().getSimpleName().equalsIgnoreCase(cls.getSimpleName())) {
                return pattern;
            }
        }
        return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isFinishing || isStarting) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public <IV extends IView> IV getIViewWith(Class<IV> cls) {
        IView result = null;
        for (ViewPattern pattern : mAttachViews) {
            if (isViewPatternEquals(cls, pattern)) {
                result = pattern.mIView;
                break;
            }
        }
        return (IV) result;
    }

    @Override
    public void finishAll() {
        finishAll(false);
    }

    @Override
    public void finishAllWithKeep(List<Class<? extends IView>> keepList, boolean keepLast, final UIParam lastFinishParam) {
        List<ViewPattern> needFinishPattern = new ArrayList<>();

        //循环拿到需要finish的IView
        for (ViewPattern pattern : mAttachViews) {
            boolean keep = false;
            for (Class cls : keepList) {
                if (isViewPatternEquals(cls, pattern)) {
                    keep = true;
                    break;
                }
            }

            if (!keep) {
                if (keepLast && pattern == getLastViewPattern()) {

                } else {
                    needFinishPattern.add(pattern);
                }
            }
        }

        for (ViewPattern pattern : needFinishPattern) {
            if (pattern == getLastViewPattern() && lastFinishParam != null) {
                //最后一个页面关闭时, 正常执行
                //finishIViewInner(pattern, lastFinishParam);
                finishIView(pattern.mIView, lastFinishParam);
            } else {
                //看不见的页面关闭时, 安静执行
                finishIViewInner(pattern, new UIParam(false, false, true));
            }
        }
    }

    @Override
    public void finishAll(boolean keepLast) {
        while (!mAttachViews.empty()) {
            ViewPattern pattern = mAttachViews.pop();
            if (keepLast && pattern == getLastViewPattern()) {
                return;
            } else {
                pattern.interrupt = true;
                finishIViewInner(pattern, new UIParam(false, false, true));
            }
        }
    }

    @Override
    public void finish() {
        finishAll();
        mLayoutActivity.onBackPressed();
    }

    @Override
    public void onSkinChanged(ISkin skin) {
        for (ViewPattern pattern : mAttachViews) {
            pattern.mIView.onSkinChanged(skin);
        }
    }

    /**
     * 滚动到关闭状态
     */
    @Override
    protected void onRequestClose() {
        super.onRequestClose();
        if (enableRootSwipe && getIViewSize() == 1) {
            mLayoutActivity.finish();
            mLayoutActivity.overridePendingTransition(0, 0);
        } else {
            needDragClose = true;
            mLastShowViewPattern.mView.setVisibility(GONE);
            mLastShowViewPattern.mView.setAlpha(0f);
            finishIView(mLastShowViewPattern.mIView, new UIParam(false, true, false));
        }
        printLog();
    }

    /**
     * 默认状态
     */
    @Override
    protected void onRequestOpened() {
        super.onRequestOpened();
        isSwipeDrag = false;
        needDragClose = false;
        translation(0);
        final ViewPattern viewPattern = findLastShowViewPattern(mLastShowViewPattern);
        if (viewPattern != null) {
            viewPattern.mView.setVisibility(GONE);
        }
        //hideChildLayoutLastView();
        printLog();
    }

    @Override
    protected void onSlideChange(float percent) {
        super.onSlideChange(percent);
        isSwipeDrag = true;
        translation(percent);
    }

    @Override
    protected void onStateIdle() {
        super.onStateIdle();
        isWantSwipeBack = false;
    }

    /**
     * 滑动中
     */
    @Override
    protected void onStateDragging() {
        super.onStateDragging();
        isWantSwipeBack = true;
        isSwipeDrag = true;

        //开始偏移时, 偏移的距离
        final ViewPattern viewPattern = findLastShowViewPattern(mLastShowViewPattern);
        if (viewPattern != null && !viewPattern.mIView.isDialog()) {
            mTranslationOffsetX = getMeasuredWidth() * 0.3f;
            viewPattern.mView.setVisibility(VISIBLE);
            viewPattern.mView.setTranslationX(-mTranslationOffsetX);
        }
        showChildLayoutLastView();
    }

    /**
     * child layout
     */
    private void showChildLayoutLastView() {
        if (!isChildILayoutEmpty()) {
            ViewPattern patternAtLast = mChildILayout.getViewPatternAtLast(0);
            if (patternAtLast != null) {
                patternAtLast.mView.setVisibility(VISIBLE);
            }
        }
    }

    private boolean isChildILayoutEmpty() {
        return mChildILayout == null || mChildILayout == this;
    }

    private void hideChildLayoutLastView() {
        if (!isChildILayoutEmpty()) {
            ViewPattern patternAtLast = mChildILayout.getViewPatternAtLast(0);
            if (patternAtLast != null) {
                patternAtLast.mView.setVisibility(GONE);
            }
        }
    }

    private void translation(float percent) {
        final ViewPattern viewPattern = findLastShowViewPattern(mLastShowViewPattern);
        if (viewPattern != null && !viewPattern.mIView.isDialog()) {
            viewPattern.mView.setTranslationX(-mTranslationOffsetX * percent);
        }
    }

    /**
     * 移动最后一个可见视图
     */
    public void translationLastView(int x) {
        if (mLastShowViewPattern != null) {
            mLastShowViewPattern.mView.setTranslationX(x);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        try {
            super.dispatchDraw(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取已经添加IView的数量
     */
    public int getIViewSize() {
        if (mAttachViews == null || mAttachViews.isEmpty()) {
            return 0;
        }
        return getAttachViewSize();
    }

    public boolean isSwipeDrag() {
        return isSwipeDrag;
    }

    @Override
    public void setChildILayout(ILayout childILayout) {
        mChildILayout = childILayout;
    }

    /**
     * 打印堆栈信息
     */
    public String logLayoutInfo() {
        StringBuilder stringBuilder = new StringBuilder(this.getClass().getSimpleName() + " IViews:\n");
        for (int i = 0; i < getAttachViewSize(); i++) {
            ViewPattern viewPattern = mAttachViews.get(i);
            stringBuilder.append(i);
            stringBuilder.append("-->");
            stringBuilder.append(viewPattern.mIView.getClass().getSimpleName());
            stringBuilder.append("");
            int visibility = viewPattern.mView.getVisibility();
            String vis;
            if (visibility == View.GONE) {
                vis = "GONE";
            } else if (visibility == View.VISIBLE) {
                vis = "VISIBLE";
            } else if (visibility == View.INVISIBLE) {
                vis = "INVISIBLE";
            } else {
                vis = "NONE";
            }
            stringBuilder.append(" visibility-->");
            stringBuilder.append(vis);
            stringBuilder.append(" alpha-->");
            stringBuilder.append(viewPattern.mView.getAlpha());
            stringBuilder.append(" W:");
            stringBuilder.append(this.getMeasuredWidth());
            stringBuilder.append("-");
            stringBuilder.append(viewPattern.mView.getMeasuredWidth());
            stringBuilder.append(" H:");
            stringBuilder.append(this.getMeasuredHeight());
            stringBuilder.append("-");
            stringBuilder.append(viewPattern.mView.getMeasuredHeight());
            stringBuilder.append(" R:");
            stringBuilder.append(viewPattern.mView.getRight());
            stringBuilder.append(" B:");
            stringBuilder.append(viewPattern.mView.getBottom());
            stringBuilder.append(" layout:");
            stringBuilder.append(viewPattern.mView.getTag(R.id.tag_layout));
            stringBuilder.append("\n");
        }
        LAYOUT_INFO = stringBuilder.toString();
        L.e(LAYOUT_INFO);
        saveToSDCard(LAYOUT_INFO);
        return LAYOUT_INFO;
    }

    /**
     * IView 添加,移除监听
     */
    public interface OnIViewChangedListener {
        void onIViewAdd(final UILayoutImpl uiLayout, final ViewPattern viewPattern);

        void onIViewRemove(final UILayoutImpl uiLayout, final ViewPattern viewPattern);
    }

    static class AnimRunnable implements Animation.AnimationListener {

        private Runnable mRunnable;
        private View mView;
        private boolean isFinish;

        public AnimRunnable(View view, Runnable runnable, boolean isFinish) {
            mRunnable = runnable;
            mView = view;
            this.isFinish = isFinish;
        }

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (mRunnable != null) {
                if (mView != null && !isFinish) {
                    mView.post(mRunnable);
                } else {
                    mRunnable.run();
                }
                mRunnable = null;
            }
            if (mView != null) {
                mView.clearAnimation();
                mView = null;
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }
}
