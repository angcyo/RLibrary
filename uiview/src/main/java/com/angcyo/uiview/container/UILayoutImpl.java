package com.angcyo.uiview.container;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
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
import android.widget.EditText;
import android.widget.TextView;

import com.angcyo.library.utils.L;
import com.angcyo.uiview.R;
import com.angcyo.uiview.RApplication;
import com.angcyo.uiview.RCrashHandler;
import com.angcyo.uiview.Root;
import com.angcyo.uiview.base.UIBaseView;
import com.angcyo.uiview.base.UILayoutActivity;
import com.angcyo.uiview.kotlin.ViewExKt;
import com.angcyo.uiview.kotlin.ViewGroupExKt;
import com.angcyo.uiview.model.ViewPattern;
import com.angcyo.uiview.resources.AnimUtil;
import com.angcyo.uiview.rsen.RGestureDetector;
import com.angcyo.uiview.skin.ISkin;
import com.angcyo.uiview.utils.Debug;
import com.angcyo.uiview.utils.RUtils;
import com.angcyo.uiview.view.ILifecycle;
import com.angcyo.uiview.view.IView;
import com.angcyo.uiview.view.UIIViewImpl;
import com.angcyo.uiview.widget.viewpager.UIViewPager;

import org.jetbrains.annotations.NotNull;

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

import static com.angcyo.uiview.utils.ScreenUtil.density;
import static com.angcyo.uiview.view.UIIViewImpl.DEFAULT_ANIM_TIME;
import static com.angcyo.uiview.view.UIIViewImpl.DEFAULT_DELAY_ANIM_TIME;

/**
 * 可以用来显示IView的布局, 每一层的管理
 * Created by angcyo on 2016-11-12.
 */

public class UILayoutImpl extends SwipeBackLayout implements ILayout<UIParam>, UIViewPager.OnPagerShowListener {

    public static final String TAG_MAIN = "main";
    private static final String TAG = "UILayoutImpl";
    public static String LAYOUT_INFO = "";
    /**
     * 多指是否显示debug layout
     */
    public static boolean showDebugLayout = true;
    public static boolean showDebugInfo = false;
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
    int hSpace = (int) (30 * getResources().getDisplayMetrics().density);
    int vSpace = (int) (30 * getResources().getDisplayMetrics().density);
    int viewMaxHeight = 0;
    boolean isInDebugLayout = false;
    Paint debugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    StringBuilder measureLogBuilder = new StringBuilder();
    /**
     * 启动界面时的过渡背景颜色
     */
    int transitionColor = Color.WHITE;
    Rect viewVisibleRectTemp = new Rect();
    /**
     * 是否正在退出界面
     */
    private boolean isFinishing = false;
    /**
     * 是否正在启动新界面
     */
    private boolean isStarting = false;
    /**
     * 已经按下返回键
     */
    private boolean isBackPress = false;
    private ArrayList<IWindowInsetsListener> mIWindowInsetsListeners;
    private ArrayList<OnIViewChangedListener> mOnIViewChangedListeners = new ArrayList<>();
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
    /**
     * 三指首次按下的时间
     */
    private long firstDownTime = 0;

    /**
     * 拦截所有touch事件
     */
    private boolean interceptTouchEvent = false;

    /**
     * 覆盖在的所有IView上的Drawable
     */
    private Drawable overlayDrawable;

    /**
     * 高度使用DecorView的高度, 否则使用View的高度
     */
    private boolean isFullOverlayDrawable = false;

    public UILayoutImpl(Context context) {
        super(context);
        initLayout();
    }

    public UILayoutImpl(Context context, IView iView) {
        super(context);
        initLayout();

        UIParam uiParam = new UIParam(false, false);
        final ViewPattern newViewPattern = startIViewInternal(iView, uiParam);
        if (newViewPattern != null) {
            startIViewAnim(mLastShowViewPattern, newViewPattern, uiParam, false);
        }
    }

    public UILayoutImpl(Context context, AttributeSet attrs) {
        super(context, attrs);
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
            if (mLastShowViewPattern.mView != null) {
                mLastShowViewPattern.mView.setVisibility(VISIBLE);
            }
            if (mLastShowViewPattern.mIView != null) {
                mLastShowViewPattern.mIView.onViewReShow(bundle);
            }
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
     * 获取屏幕方向
     *
     * @see android.content.res.Configuration#ORIENTATION_LANDSCAPE
     * @see android.content.res.Configuration#ORIENTATION_PORTRAIT
     */
    public int getScreenOrientation() {
        return getResources().getConfiguration().orientation;
    }

    /**
     * 滑动返回处理
     */
    @Override
    protected boolean canTryCaptureView(View child) {
        if (!isMainLayout()) {
            return false;
        }

        if (isBackPress ||
                mLastShowViewPattern == null ||
                mLastShowViewPattern.interrupt ||
                mLastShowViewPattern.isAnimToStart ||
                isFinishing ||
                mViewDragState != ViewDragHelper.STATE_IDLE ||
                needDragClose) {
//            if (mLastShowViewPattern != null) {
//                checkInterruptAndRemove(mLastShowViewPattern.mIView);
//            }
            return false;
        }

        if (getScreenOrientation() != Configuration.ORIENTATION_PORTRAIT) {
            //非竖屏, 禁用滑动返回
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
                viewPattern.mIView.release();
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
        if (isFinishing ||
                (mLastShowViewPattern != null && mLastShowViewPattern.interrupt) ||
                (mLastShowViewPattern != null && mLastShowViewPattern.mIView.isDialog() && !iView.showOnDialog())
                ) {
            ensureLastViewPattern();

            L.i(mLastShowViewPattern);
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

    private void addInterrupt(IView iView) {
        interruptSet.add(iView);
    }

    private void removeInterrupt(IView iView) {
        interruptSet.remove(iView);
    }

    /**
     * 检查是否需要中断启动
     */
    private boolean checkInterrupt(IView iView) {
        /**已经被中断启动了*/
        if (interruptSet.contains(iView)) {
            //removeInterrupt(iView);
            L.i("中断:" + iView.getClass().getSimpleName());
            return true;
        }
        return false;
    }

    private boolean checkInterruptAndRemove(IView iView) {
        /**已经被中断启动了*/
        if (interruptSet.contains(iView)) {
            removeInterrupt(iView);
            L.i("中断并移除:" + iView.getClass().getSimpleName());
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

        if (isSwipeDrag()) {
            restoreCaptureView();
        }

        if (checkInterruptAndRemove(iView)) {
            finishEnd();
            return;
        }

        final ViewPattern oldViewPattern = getLastViewPattern();
        isStarting = true;

        if (param.start_mode == UIParam.SINGLE_TOP) {
            if (oldViewPattern != null && oldViewPattern.mIView == iView) {
                //如果已经是最前显示, 调用onViewShow方法
                setIViewNeedLayout(oldViewPattern.mView, true);
                oldViewPattern.mIView.onViewShow(param.getBundle());
                oldViewPattern.mIView.onViewReShow(param.getBundle());
                isStarting = false;
            } else {
                ViewPattern viewPatternByIView = findViewPatternByClass(iView.getClass());
                if (viewPatternByIView == null) {
                    //这个IView 还不存在
                    final ViewPattern newViewPattern = startIViewInternal(iView, param);
//                    startIViewAnim(oldViewPattern, newViewPattern, param);
                    if (newViewPattern != null) {
                        viewPatternByIView = newViewPattern;
                        startIViewAnim(oldViewPattern, viewPatternByIView, param, false);
                    }
                } else {
                    //这个IView 存在, 但是不在最前显示
//                    bottomViewFinish(oldViewPattern, viewPatternByIView, param);
//                    topViewStart(viewPatternByIView, param);

                    mAttachViews.remove(viewPatternByIView);
                    mAttachViews.push(viewPatternByIView);
                    startIViewAnim(oldViewPattern, viewPatternByIView, param, true);
                    final ViewPattern finalViewPatternByIView = viewPatternByIView;
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finalViewPatternByIView.mIView.onViewReShow(param.getBundle());
                        }
                    }, DEFAULT_DELAY_ANIM_TIME);
                }
            }
        } else {
            //正常的启动模式
            final ViewPattern newViewPattern = startIViewInternal(iView, param);
            if (newViewPattern != null) {
                startIViewAnim(oldViewPattern, newViewPattern, param, false);
            }
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
        if (checkInterrupt(iView)) {
            L.e("loadContentView()-> 已被中断:" + iView.getClass().getSimpleName());
            removeView(rawView);
            removeInterrupt(iView);
            finishEnd();
            return null;
        } else {
            final ViewPattern newViewPattern = new ViewPattern(iView, rawView);

            iView.loadContentView(rawView);

            mAttachViews.push(newViewPattern);

            for (OnIViewChangedListener listener : mOnIViewChangedListeners) {
                try {
                    listener.onIViewAdd(this, newViewPattern);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return newViewPattern;
        }
    }

    /**
     * [add] 星期三 2017-1-11
     */
    @Deprecated
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
            try {
                listener.onIViewAdd(this, viewPattern);
            } catch (Exception e) {
                e.printStackTrace();
            }
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

        if (!checkInterrupt(iView)) {
            //2:
            iView.onViewCreate(rawView);
            iView.onViewCreate(rawView, uiParam);

            for (OnIViewChangedListener listener : mOnIViewChangedListeners) {
                try {
                    listener.onIViewCreate(this, iView, rawView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            L.e("loadViewInternal()-> 加载页面:" + iView.getClass().getSimpleName());
        } else {
            L.e("onViewCreate()-> 已被中断:" + iView.getClass().getSimpleName());
        }
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
        if (viewPattern == null || viewPattern.isAnimToEnd) {
            finishEnd();
            return;
        }

        if (isSwipeDrag()) {
            restoreCaptureView();
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
                removeInterrupt(viewPattern.mIView);
                viewPattern.interrupt = false;
                viewPattern.mView.setEnabled(true);
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

        isFinishing = true;

        if (lastViewPattern != null && lastViewPattern.mView != null) {
            setIViewNeedLayout(lastViewPattern.mView, true);
        }
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
            //finishEnd();
            return;
        }

        final ViewPattern viewPattern = findViewPatternByIView(iview);
        if (viewPattern == null) {
            L.i("中断启动:" + iview.getClass().getSimpleName());
            addInterrupt(iview);
        }
        if (viewPattern == null || viewPattern.mIView == null) {
            //finishEnd();
            runUnloadRunnable(param);
            return;
        }

        L.i("关闭：" + iview.getClass().getSimpleName() + " -- " + viewPattern.mIView.getClass().getSimpleName());

        viewPattern.mView.setEnabled(false);
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

//            L.i(mLastShowViewPattern);
//            L.i(viewPattern);
//            L.i(mLastShowViewPattern != viewPattern);

            if (mLastShowViewPattern != null &&
                    mLastShowViewPattern != viewPattern &&
                    mLastShowViewPattern.mIView.isDialog()) {
                ensureLastViewPattern();

                logLayoutInfo();
                L.i(iview.getClass().getSimpleName() + " 等待对话框:" + mLastShowViewPattern.mIView.getClass().getSimpleName() + " 的关闭");
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
                    addInterrupt(iview);
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

        setIViewNeedLayout(viewPattern.mView, true);
        viewPattern.mView.setVisibility(VISIBLE);
        viewPattern.mView.bringToFront();

        if (viewPattern.mView.getMeasuredWidth() == 0 ||
                viewPattern.mView.getMeasuredHeight() == 0) {
            requestLayout();
        }

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
        return requestBackPressed(new UIParam().setClickOnTitleBack(true));
    }

    @Override
    public boolean requestBackPressed(final UIParam param) {
        if (isInDebugLayout) {
            closeDebugLayout();
            return false;
        }

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
                boolean backPressed = mLastShowViewPattern.mIView.onBackPressed();
                if (param.clickOnTitleBack) {
                    if (backPressed) {
                        mLayoutActivity.finishSelf();
                    }
                }
                return backPressed;
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

    /**
     * 判断界面是否还在
     */
    private boolean isIViewExist(final IView iView) {
        boolean exist = false;
        if (iView != null) {
            for (ViewPattern viewPattern : mAttachViews) {
                if (viewPattern.mIView == iView) {
                    exist = true;
                    break;
                }
            }
        }
        return exist;
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

        if (mLastShowViewPattern != null &&
                mLastShowViewPattern.mIView.isDialog() &&
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

        if (mLastShowViewPattern != null &&
                !param.isReplaceIViewEmpty() &&
                mLastShowViewPattern.mIView != param.replaceIView &&
                isIViewExist(param.replaceIView)) {
            L.i(param.replaceIView.getClass().getSimpleName() +
                    " 请求替换 " + iView.getClass().getSimpleName() +
                    " LastIs:" + mLastShowViewPattern.mIView.getClass().getSimpleName());
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
                ViewPattern newViewPattern = null;

                if (param.start_mode == UIParam.SINGLE_TOP) {
                    if (oldViewPattern != null && oldViewPattern.mIView == iView) {
                        //如果已经是最前显示, 调用onViewShow方法
                        setIViewNeedLayout(oldViewPattern.mView, true);
                        oldViewPattern.mIView.onViewShow(param.getBundle());
                        oldViewPattern.mIView.onViewReShow(param.getBundle());
                        isStarting = false;

                        newViewPattern = oldViewPattern;
                    } else {
                        ViewPattern viewPatternByIView = findViewPatternByClass(iView.getClass());
                        if (viewPatternByIView == null) {
                            //这个IView 还不存在
                            newViewPattern = startIViewInternal(iView, param);
//                    startIViewAnim(oldViewPattern, newViewPattern, param);
                            if (newViewPattern != null) {
                                viewPatternByIView = newViewPattern;
                                startIViewAnim(oldViewPattern, viewPatternByIView, param, false);
                            }
                        } else {
                            //这个IView 存在, 但是不在最前显示
//                    bottomViewFinish(oldViewPattern, viewPatternByIView, param);
//                    topViewStart(viewPatternByIView, param);
                            newViewPattern = viewPatternByIView;
                            mAttachViews.remove(viewPatternByIView);
                            mAttachViews.push(viewPatternByIView);
                            startIViewAnim(oldViewPattern, viewPatternByIView, param, true);
                            final ViewPattern finalViewPatternByIView = viewPatternByIView;
                            postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finalViewPatternByIView.mIView.onViewReShow(param.getBundle());
                                }
                            }, DEFAULT_DELAY_ANIM_TIME);
                        }
                    }
                } else {
                    newViewPattern = startIViewInternal(iView, param);
                    if (newViewPattern != null) {
                        if (checkInterrupt(newViewPattern.mIView)) {
                            finishIView(newViewPattern.mIView.getClass());
                            L.e("replaceIView()-> 已被中断:" + iView.getClass().getSimpleName());
                        } else {
                            //3:
                            newViewPattern.mIView.onViewLoad();
                            topViewStart(newViewPattern, param);
                        }
                    }
                }

                //需要替换的IView和最后一个界面的IView相等. 如果 isReplaceIViewEmpty, 那么强制替换最后一个IView
                if (param.isReplaceIViewEmpty() || (oldViewPattern != null && oldViewPattern.mIView == param.replaceIView)) {
                    final Runnable endRunnable = new Runnable() {
                        @Override
                        public void run() {
                            removeViewPattern(oldViewPattern, param, null);
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
        return mLastShowViewPattern;
    }

    private void ensureLastViewPattern() {
        if (mLastShowViewPattern != null) {
            if (mAttachViews.isEmpty() || mAttachViews.contains(mLastShowViewPattern)) {
            } else {
                if (isStarting || isFinishing) {

                } else {
                    mLastShowViewPattern = mAttachViews.lastElement();
                }
            }
        }
    }

    public ViewPattern getLastViewPatternInAttach() {
        if (mAttachViews.isEmpty()) {
            return null;
        }
        return mAttachViews.lastElement();
    }

    private void startIViewAnim(final ViewPattern oldViewPattern, final ViewPattern newViewPattern,
                                final UIParam param, boolean reLoad) {
//        if (isAttachedToWindow) {
        mLastShowViewPattern = newViewPattern;

        if (!reLoad) {
            newViewPattern.mIView.onViewLoad();
        }

        clearOldViewFocus(oldViewPattern);

        if (checkInterrupt(newViewPattern.mIView)) {
            finishIView(newViewPattern.mIView.getClass());
            L.e("startIViewAnim()-> 已被中断:" + newViewPattern.mIView.getClass().getSimpleName());
        } else {
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
        if (animation != null) {
            animation.setFillAfter(false);
        }

        final Runnable endRunnable = new Runnable() {
            @Override
            public void run() {
                L.i(topViewPattern.mIView.getClass().getSimpleName() + " 启动完毕.");
                viewShow(topViewPattern, param.getBundle());
                topViewPattern.isAnimToStart = false;
                isStarting = false;
                onStartIViewEnd(topViewPattern);
                printLog();
            }
        };

        if (topViewPattern.mView instanceof ILifecycle) {
            ((ILifecycle) topViewPattern.mView).onLifeViewShow();
        }

        setIViewNeedLayout(topViewPattern.mView, true);
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
        if (animation != null) {
            animation.setFillAfter(true);//2017-9-1
        }
        final Runnable endRunnable = new Runnable() {
            @Override
            public void run() {
                L.i(topViewPattern.mIView.getClass().getSimpleName() + " 关闭完成.");

                topViewPattern.isAnimToEnd = false;
                viewHide(topViewPattern);
                removeViewPattern(topViewPattern, param, new Runnable() {
                    @Override
                    public void run() {
                        mLastShowViewPattern = bottomViewPattern;//星期一 2017-1-16
                    }
                });

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
                finishEnd();
            }
        };
        setIViewNeedLayout(bottomViewPattern.mView, true);
        bottomViewPattern.mView.setVisibility(VISIBLE);
        showChildLayoutLastView();

        if (bottomViewPattern.mView instanceof ILifecycle) {
            ((ILifecycle) bottomViewPattern.mView).onLifeViewShow();
        }

        if (topViewPattern.mIView.isDialog()) {
            //对话框结束时, 不执行生命周期
            bottomViewPattern.mIView.onViewShowOnDialogFinish();
        } else {
            if (RApplication.isLowDevice || !anim || quiet) {
                endRunnable.run();
            } else {
                final Animation animation = topViewPattern.mIView.loadOtherEnterAnimation();
                if (animation != null) {
                    animation.setFillAfter(false);
                }
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
                if (animation != null) {
                    animation.setFillAfter(false);
                }
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
                viewPattern.mIView == null ||
                viewPattern.mIView.getIViewShowState() == IView.IViewShowState.STATE_VIEW_HIDE) {
            return;
        }
        saveToSDCard(viewPattern.mIView.getClass().getSimpleName() + " onViewHide()");
        setIViewNeedLayout(viewPattern.mView, false);
        viewPattern.mIView.onViewHide();
        if (hide && !viewPattern.mIView.isDialog()) {
            viewPattern.mView.setVisibility(GONE);
        }
    }

    private void viewHide(final ViewPattern viewPattern) {
        viewHide(viewPattern, false);

        for (OnIViewChangedListener listener : mOnIViewChangedListeners) {
            try {
                listener.onIViewHide(UILayoutImpl.this, viewPattern);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 执行IView生命周期onViewShow
     */
    private void viewShow(final ViewPattern viewPattern, final Bundle bundle) {
        isStarting = false;
        if (viewPattern == null ||
                viewPattern.mIView == null ||
                viewPattern.mIView.getIViewShowState() == IView.IViewShowState.STATE_VIEW_SHOW) {
            return;
        }
        saveToSDCard(viewPattern.mIView.getClass().getSimpleName() + " onViewShow()" + bundle);
//        viewPattern.mView.setVisibility(VISIBLE);
//        viewPattern.mView.bringToFront();
        viewPattern.mIView.onViewShow(bundle);

        for (OnIViewChangedListener listener : mOnIViewChangedListeners) {
            try {
                listener.onIViewShow(UILayoutImpl.this, viewPattern);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
                            removeViewPattern(newViewPattern, null, null);
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
                            removeViewPattern(newViewPattern, null, null);
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
                    L.i("点击在对话框外-> " + dialogPattern.mIView.getClass().getSimpleName());
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
                    L.i("双击在对话框外-> " + dialogPattern.mIView.getClass().getSimpleName());

                    //if (dialogPattern.mIView.canCanceledOnOutside()) {//2017-12-19
                    finishIView(dialogPattern.mView);
                    //}
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

    public void removeViewPattern(final ViewPattern viewPattern, final UIParam param, final Runnable runnable) {
        hideSoftInput();
        final View view = viewPattern.mView;
        //ViewCompat.setAlpha(view, 0);
        viewPattern.mIView.onViewUnload();

        view.setEnabled(false);
        ViewCompat.setAlpha(view, 0);
        //view.setVisibility(GONE);

        post(new Runnable() {
            @Override
            public void run() {
                try {
                    //UI.setView(view, 0, 0);
                    viewPattern.interrupt = false;
                    mAttachViews.remove(viewPattern);

                    if (mTargetView == view) {
                        mTargetView = null;
                    }

                    try {
                        removeView(view);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    isFinishing = false;
                    isBackPress = false;
                    viewPattern.mIView.release();
                    removeInterrupt(viewPattern.mIView);

                    if (runnable != null) {
                        runnable.run();
                    }

                    ensureLastViewPattern();

                    onFinishIViewEnd(viewPattern);

                    viewPattern.mIView.onViewUnload();

                    L.e("call: removeViewPattern()-> 关闭界面结束:" + viewPattern.mIView.getClass().getSimpleName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                runUnloadRunnable(param);
            }
        });

        for (OnIViewChangedListener listener : mOnIViewChangedListeners) {
            try {
                listener.onIViewRemove(this, viewPattern);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void runUnloadRunnable(final UIParam param) {
        if (param != null && param.getUnloadRunnable() != null) {
            if (param.mAsync) {
                post(param.getUnloadRunnable());
            } else {
                param.getUnloadRunnable().run();
            }
            param.clear();
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
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private int getDebugWidthSize() {
        return getMeasuredWidth() - 2 * hSpace;
    }

    private int getDebugHeightSize() {
        return getMeasuredHeight() - 4 * vSpace;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //of java
        int widthSize = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        //of kotlin
//        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
//        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
//        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
//        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        int count = getChildCount();
        if (isInDebugLayout) {
            //int hCount = count > 9 ? 4 : (count > 6 ? 3 : 2);//横向放3个
            //int vCount = (int) Math.max(2, Math.ceil(count * 1f / hCount));//竖向至少2行

            //int wSize = (getMeasuredWidth() - (hCount + 1) * hSpace) / hCount;
            //int hSize = (getMeasuredHeight() - (vCount + 1) * vSpace) / vCount;
            int wSize = widthSize;//getDebugWidthSize();
            int hSize = heightSize;//getDebugHeightSize();

            for (int i = 0; i < count; i++) {
                View childAt = getChildAt(i);
                childAt.setVisibility(VISIBLE);
                childAt.measure(exactlyMeasure(wSize), exactlyMeasure(hSize));
            }
        } else {
            if (showDebugInfo) {
                Debug.logTimeStartD("\n开始测量, 共:" + getAttachViewSize());
            }
            measureLogBuilder.delete(0, measureLogBuilder.length());

            for (int i = 0; i < count; i++) {
                View childAt = getChildAt(i);

                boolean[] childState = checkChildState(i);

                if (childState == null) {
                    continue;
                }
                ViewPattern viewPatternByView = findViewPatternByView(childAt);

                boolean needMeasure = childState[0];//是否需要测量
                boolean needVisible = childState[1];//是否需要显示

                boolean isTransition = isTransition();

                //needMeasure = true;
                if (needMeasure) {
                    if (needVisible) {
                        childAt.setVisibility(VISIBLE);
                    } else {
                        childAt.setVisibility(INVISIBLE);
                    }
                    childAt.measure(exactlyMeasure(widthSize), exactlyMeasure(heightSize));
                    //L.d("测量 needVisible " + needVisible + ": " + viewPatternByView.mIView.getClass().getSimpleName());
                    measureLogBuilder.append("\n测量->");
                    measureLogBuilder.append(viewPatternByView.mIView.getClass().getSimpleName());
                    measureLogBuilder.append(" needVisible:");
                    measureLogBuilder.append(needVisible);
                    measureLogBuilder.append(" W:");
                    measureLogBuilder.append(childAt.getMeasuredWidth());
                    measureLogBuilder.append(" H:");
                    measureLogBuilder.append(childAt.getMeasuredHeight());
                } else {
                    if (i == count - 2) {
                        View lastView = getChildAt(i + 1);
                        ViewPattern lastViewPattern = findViewPatternByView(lastView);

                        if (isTransition) {
                            childAt.setVisibility(VISIBLE);
                        } else if (lastViewPattern != null &&
                                lastViewPattern.mIView != null &&
                                (lastViewPattern.mIView.needTransitionExitAnim() ||
                                        lastViewPattern.mIView.needTransitionStartAnim() ||
                                        lastViewPattern.interrupt)
                                ) {
                            childAt.setVisibility(VISIBLE);
                        } else {
                            childAt.setVisibility(GONE);
                        }
                    } else {
                        childAt.setVisibility(GONE);
                    }
                }
            }
            if (showDebugInfo) {
                L.i(measureLogBuilder.toString());
                Debug.logTimeEndD("\n测量结束");
            }
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    private boolean isTransition() {
        return isSwipeDrag || isStarting || isFinishing;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        //L.e("debug layout 1 " + isInDebugLayout + " " + getScrollX() + " " + getScrollY());
        if (isInDebugLayout) {
            int count = getChildCount();

//            int l = hSpace;
//            int t = vSpace;

            int l = getPaddingLeft();
            int t = -vSpace + getPaddingTop();

            int wSize = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();//getDebugWidthSize();
            int hSize = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();//getDebugHeightSize();

            for (int i = 0; i < count; i++) {
                View childAt = getChildAt(i);
                childAt.layout(l, t, l + wSize, t + hSize);
                t += getDebugHeightSize() + vSpace;
//                t += hSize + vSpace;
            }
//            viewMaxHeight = t;
            viewMaxHeight = t + 2 * vSpace;
            return;
        }

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
            if (childAt.getVisibility() == VISIBLE) {
                childAt.layout(getPaddingLeft(), getPaddingTop(), getMeasuredWidth() - getPaddingRight(), getMeasuredHeight() - getPaddingBottom());
            }

//            if (i == count - 1 || i == count - 2) {
            //childAt.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
            //L.e("debug layout 2 " + right + ' ' + bottom + ' ' + childAt.getMeasuredHeight());
            if (getMeasuredWidth() == 0 || getMeasuredHeight() == 0) {
                setIViewNeedLayout(childAt, true);
            } else if (childAt.getMeasuredWidth() == 0 || childAt.getMeasuredHeight() == 0) {
                setIViewNeedLayout(childAt, true);
            } else {
                if (childAt.getMeasuredHeight() == getMeasuredHeight() &&
                        childAt.getMeasuredWidth() == getMeasuredWidth()) {
                    setIViewNeedLayout(childAt, false);
                } else {
                    setIViewNeedLayout(childAt, true);
                }
            }
            if (i == count - 1) {
                //最后一个界面
                ViewPattern viewPatternByView = findViewPatternByView(childAt);
                UIBaseView.LayoutState layoutState = UIBaseView.LayoutState.NONE;
                if (viewPatternByView.mIView != null) {
                    if (viewPatternByView.mIView instanceof UIBaseView) {
                        layoutState = ((UIBaseView) viewPatternByView.mIView).getLayoutState();
                    }
                    IView.IViewShowState viewShowState = viewPatternByView.mIView.getIViewShowState();
                    viewPatternByView.mIView.onIViewLayout(viewPatternByView, layoutState, viewShowState, childAt);
                }

//                if (showDebugInfo) {
//                    L.i("布局->" + viewPatternByView.mIView + " "
//                            + layoutState + " "
//                            + viewShowState + " "
//                            + viewPatternByView.mView + " ");
//                }

            }
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

    /**
     * 清除已经布局的flag
     */
    public void setIViewNeedLayout(View view, boolean layout) {
        view.setTag(R.id.tag_need_layout, layout ? "true" : "false");
        if (layout) {
            view.forceLayout();
        }
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

    /**
     * child的测量和显示状态
     */
    private boolean[] checkChildState(int childIndex) {
        View childAt = getChildAt(childIndex);
        int childCount = getChildCount();

        ViewPattern viewPatternByView = findViewPatternByView(childAt);
        int iViewSize = getIViewSize();

        if (viewPatternByView == null) {
            return null;
        }

        int indexFromIViews = getIndexFromIViews(viewPatternByView);

        //-----------------只有部分界面需要测量, 优化性能---------
        boolean needMeasure = false;//是否需要测量
        boolean needVisible = false;//是否需要显示

//                if (i == count - 1) {
//                    //needMeasure = true;
//                    needVisible = true;
//                }

        if ("true".equalsIgnoreCase(String.valueOf(childAt.getTag(R.id.tag_need_layout)))) {
            //如果还没有layout过
            //needMeasure = true;
            needVisible = true;
        } else if (viewPatternByView == mLastShowViewPattern) {
            //最后一个页面
            //needMeasure = true;
            needVisible = true;
        } else if (childIndex == childCount - 1 /*|| i == count - 2*/) {
            //倒数第一个, 第二个view
            //needMeasure = true;
            needVisible = true;
        } else if (indexFromIViews >= 0 && (indexFromIViews == iViewSize - 1 /*|| indexFromIViews == iViewSize - 2*/)) {
            //倒数第一个, 第二个iview
            //needMeasure = true;
            needVisible = true;
        } else if ((childIndex == childCount - 2 || indexFromIViews == iViewSize - 2) &&
                viewPatternByView.mIView.needForceVisible()) {
            needVisible = true;
        } else {
//            if (viewPatternByView.mIView.needForceMeasure() ||
//                    viewPatternByView.mIView.haveParentILayout() /*||
//                        viewPatternByView.mIView.haveChildILayout()*/) {
//                //需要强制测量
//                needMeasure = true;
//            } else {
            boolean isLastAllDialog = false;//界面上面全是对话框
            IView iView = viewPatternByView.mIView;
            for (int j = mAttachViews.size() - 1; j >= 0; j--) {
                ViewPattern viewPattern = mAttachViews.get(j);
                if (viewPattern.mIView.isDialog() ||
                        viewPattern.mIView.showOnDialog() ||
                        viewPattern.isAnimToEnd ||
                        viewPattern.mIView.needTransitionExitAnim() ||
                        viewPattern.mIView.needTransitionStartAnim()) {
                    //界面上面全是对话框
                    //needMeasure = true;
                    isLastAllDialog = true;
                    if (viewPattern.mIView == iView) {
                        break;
                    }
                } else if (viewPattern.mIView == iView) {
                    break;
                } else {
                    isLastAllDialog = false;
                    break;
                }
            }
            if (isLastAllDialog) {
                needVisible = true;
            }
//            }
        }

        if (needVisible ||
                (viewPatternByView.mIView.needForceMeasure() ||
                        viewPatternByView.mIView.haveParentILayout())) {
            needMeasure = true;
        }

        return new boolean[]{needMeasure, needVisible};
    }

    /**
     * 重新设置View的显示状态
     */
    private void resetChildState() {
        int childCount = getChildCount();

        for (int i = 0; i < childCount; i++) {
            boolean[] childState = checkChildState(i);

            if (childState == null) {
                continue;
            }

            View childAt = getChildAt(i);

            boolean needMeasure = childState[0];//是否需要测量
            boolean needVisible = childState[1];//是否需要显示

            if (needVisible) {
                childAt.setVisibility(View.VISIBLE);
            } else {
                if (needMeasure) {
                    childAt.setVisibility(View.INVISIBLE);
                } else {
                    childAt.setVisibility(View.GONE);
                }
            }

            //2017-11-14
            if (i == childCount - 1) {
                ViewPattern topViewPattern = findViewPatternByView(childAt);
                if (topViewPattern != null) {
                    int width = topViewPattern.mView.getMeasuredWidth();
                    int height = topViewPattern.mView.getMeasuredHeight();
                    if (width == 0 || height == 0) {
                        StringBuilder log = new StringBuilder();
                        log.append("请注意:界面出现异常.");
                        log.append(topViewPattern.mIView.getClass().getSimpleName());
                        log.append(" w:");
                        log.append(width);
                        log.append(" h:");
                        log.append(height);
                        log.append(" ");
                        log.append(topViewPattern.mIView.getIViewShowState());
                        L.e(log.toString());
                        saveToSDCard(log.toString());

                        int vw = getMeasuredWidth();
                        int vh = getMeasuredHeight();
                        topViewPattern.mView.measure(exactlyMeasure(vw), exactlyMeasure(vh));
                        topViewPattern.mView.layout(0, 0, vw, vh);

//                        topViewPattern.mIView.onViewCreate(topViewPattern.mView);
//                        topViewPattern.mIView.loadContentView(topViewPattern.mView);
                        topViewPattern.mIView.onViewLoad();
                        topViewPattern.mIView.onViewShow(null);

                        setIViewNeedLayout(topViewPattern.mView, true);
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                requestLayout();
                            }
                        }, 60);
                    }
                }
            }
        }
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

    /**
     * 是否是主要的layout
     */
    public boolean isMainLayout() {
        return TextUtils.equals(String.valueOf(getTag(R.id.tag)), TAG_MAIN);
    }

    /**
     * 设置
     */
    public void setMainLayout(boolean main) {
        if (main) {
            setTag(R.id.tag, UILayoutImpl.TAG_MAIN);
        } else {
            setTag(R.id.tag, "");
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int actionMasked = ev.getActionMasked();
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            if (mLastShowViewPattern != null) {
                View view = null;
                if (L.LOG_DEBUG && isMainLayout()) {
                    view = ViewGroupExKt.findView((ViewGroup) mLastShowViewPattern.mView, ev.getRawX(), ev.getRawY());
                    StringBuilder builder = new StringBuilder("touch on->");
                    if (view == null) {
                        builder.append("null");
                    } else {
                        view.getGlobalVisibleRect(viewVisibleRectTemp);
                        builder.append(viewVisibleRectTemp);
                        builder.append("#");
                        if (view instanceof TextView) {
                            builder.append(((TextView) view).getText());
                            builder.append("#");
                        }
                        if (view.hasOnClickListeners()) {
                            builder.append("$");
                        }
                        builder.append(view);
                    }
                    L.d(builder.toString());
                }

                if (mLastShowViewPattern.mIView.hideSoftInputOnTouchDown()) {
                    if (view == null && isMainLayout()) {
                        view = ViewGroupExKt.findView((ViewGroup) mLastShowViewPattern.mView, ev.getRawX(), ev.getRawY());
                    }
                    //L.e("call: onInterceptTouchEvent([ev])-> " + RSoftInputLayout.getSoftKeyboardHeight(this));
                    //L.e("call: onInterceptTouchEvent([ev])-> " + view);
                    if (view != null) {
                        if (view instanceof EditText || view.getTag() != null) {
                            L.w("touch on EditText or tag not null");

                        } else {
                            hideSoftInput();
                        }
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int actionMasked = ev.getActionMasked();

        if (handleDebugLayout(ev)) {
            return true;
        }

        if (isInDebugLayout) {
            return true;
        }

        if (isFinishing || isStarting || interceptTouchEvent) {
            L.i("拦截事件：" + actionMasked + " isFinishing:" + isFinishing + " isStarting:" + isStarting + " interceptTouchEvent:" + interceptTouchEvent);
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        handleDebugLayout(event);
        if (isInDebugLayout) {
            getOrientationGestureDetector().onTouchEvent(event);
        } else {
            super.onTouchEvent(event);
        }
        return true;
    }

    /**
     * 多点按下, 是否处理
     */
    protected boolean handleDebugLayout(MotionEvent ev) {
        int actionMasked = ev.getActionMasked();
        long downTime = ev.getDownTime();
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            firstDownTime = downTime;
        }

        if (L.LOG_DEBUG &&
                showDebugLayout &&
                actionMasked == MotionEvent.ACTION_POINTER_DOWN &&
                ev.getPointerCount() == 6) {

            if (ev.getEventTime() - firstDownTime < 500) {
                //快速三指按下才受理操作

                //debug模式下, 三指按下
                if (isInDebugLayout) {
                    closeDebugLayout();
                } else {
                    startDebugLayout();
                }
                return true;
            }
        }
        return false;
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
            pattern.interrupt = true;
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
//        while (!mAttachViews.empty()) {
//            ViewPattern pattern = mAttachViews.pop();
//            if (keepLast && pattern == getLastViewPattern()) {
//                return;
//            } else {
//                pattern.interrupt = true;
//                finishIViewInner(pattern, new UIParam(false, false, true));
//            }
//        }
        finishAllWithKeep(new ArrayList<Class<? extends IView>>(), true, null);
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
        translation(0);
        if (enableRootSwipe && getIViewSize() == 1) {
            mLayoutActivity.finish();
            mLayoutActivity.overridePendingTransition(0, 0);
        } else {
            needDragClose = true;
            //mLastShowViewPattern.mView.setVisibility(GONE);
            mLastShowViewPattern.mView.setAlpha(0f);
            swipeBackIView(mLastShowViewPattern.mIView);
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
        resetChildState();
//        final ViewPattern viewPattern = findLastShowViewPattern(mLastShowViewPattern);
//        if (viewPattern != null) {
//            viewPattern.mView.setVisibility(GONE);
//        }
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
            if (viewPattern.mView.getVisibility() == View.GONE) {
                viewPattern.mView.setVisibility(VISIBLE);
            }
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
            float tx = -mTranslationOffsetX * percent;
            viewPattern.mView.setVisibility(View.VISIBLE);
            if (viewPattern.mView.getTranslationX() != tx) {
                viewPattern.mView.setTranslationX(tx);
            }
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
            stringBuilder.append(" need layout:");
            stringBuilder.append(viewPattern.mView.getTag(R.id.tag_need_layout));
            stringBuilder.append("\n");
        }
        LAYOUT_INFO = stringBuilder.toString();
        L.e(LAYOUT_INFO);
        saveToSDCard(LAYOUT_INFO);
        return LAYOUT_INFO;
    }

    /**
     * 滑动返回的形式, 关闭一个IView
     */
    public void swipeBackIView(IView iView) {
        finishIView(iView, new UIParam(false, true, false));
    }

    public void startDebugLayout() {
        if (!isInDebugLayout) {
            isInDebugLayout = true;
            getOverScroller().abortAnimation();
            requestLayout();
            for (int i = 0; i < getChildCount(); i++) {
                View childAt = getChildAt(i);
                //childAt.startAnimation(AnimationUtils.loadAnimation(mLayoutActivity, R.anim.base_scale_to_min));
                AnimUtil.scaleBounceView(childAt, getDebugWidthSize() * 1f / getMeasuredWidth(), getDebugHeightSize() * 1f / getMeasuredHeight());
            }
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollTo(0, Integer.MAX_VALUE);//滚动到最后一个IView
                }
            }, 16);
        }
    }

    public void closeDebugLayout() {
        if (isInDebugLayout) {
            isInDebugLayout = false;
            getOverScroller().abortAnimation();
            scrollTo(0, 0);//恢复滚动坐标
            requestLayout();
            for (int i = 0; i < getChildCount(); i++) {
                View childAt = getChildAt(i);
                //childAt.startAnimation(AnimationUtils.loadAnimation(mLayoutActivity, R.anim.base_scale_to_max));
                AnimUtil.scaleBounceView(childAt);
            }
        }
    }

    @Override
    protected void drawSwipeLine(Canvas canvas) {
        if (!isInDebugLayout) {
            super.drawSwipeLine(canvas);
        }
    }

    @Override
    protected void drawDimStatusBar(Canvas canvas) {
        if (!isInDebugLayout) {
            super.drawDimStatusBar(canvas);
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        int maxScrollY = viewMaxHeight - getMeasuredHeight();
        if (y > maxScrollY) {
            y = maxScrollY;
        }
        if (y < 0) {
            y = 0;
        }
        super.scrollTo(x, y);
    }

    @Override
    public void onFlingChange(@NotNull ORIENTATION orientation, float velocity) {
        super.onFlingChange(orientation, velocity);
        if (isInDebugLayout && isVertical(orientation)) {
            if (velocity > 1000) {
                //快速向下滑动
                startFlingY(-(int) velocity, getScrollY());
            } else if (velocity < -1000) {
                //快速向上滑动
                startFlingY(-(int) velocity, viewMaxHeight);
            }
        }
    }

    private void initDebugPaint() {
        debugPaint.setStrokeJoin(Paint.Join.ROUND);
        debugPaint.setStyle(Paint.Style.STROKE);
        debugPaint.setStrokeCap(Paint.Cap.ROUND);
        debugPaint.setTextSize(14 * getResources().getDisplayMetrics().density);
        debugPaint.setColor(Color.WHITE);
    }

    @Override
    public void draw(Canvas canvas) {
        if (isTransition()) {
            canvas.drawColor(transitionColor);
        }

        super.draw(canvas);
        if (isInDebugLayout) {
            initDebugPaint();
            int childCount = getChildCount();

            int l = hSpace;
            int t = vSpace;

            int wSize = getDebugWidthSize();
            int hSize = getDebugHeightSize();

            for (int i = 0; i < childCount; i++) {
                View childAt = getChildAt(i);

                ViewPattern viewPatternByView = findViewPatternByView(childAt);

                if (viewPatternByView == null) {
                    continue;
                }

                float textHeight = ViewExKt.textHeight(this, debugPaint);

                float dp2 = 2 * density();
                debugPaint.setShadowLayer(dp2, dp2, dp2, Color.BLACK);

                canvas.drawText(RUtils.getClassSimpleName(viewPatternByView.mIView.getClass()),
                        hSpace, t + textHeight, debugPaint);

                t += hSize + vSpace;
            }
        }

        if (overlayDrawable != null) {
            Context context = getContext();
            int screenHeight = getMeasuredHeight();

            if (isFullOverlayDrawable) {
                if (context instanceof Activity) {
                    screenHeight = ((Activity) context).getWindow().getDecorView().getMeasuredHeight();
                }
            }

            overlayDrawable.setBounds(0, 0, getMeasuredWidth(), screenHeight);
            overlayDrawable.draw(canvas);
        }
    }

    @Override
    public void onScrollChange(@NotNull ORIENTATION orientation, float distance) {
        super.onScrollChange(orientation, distance);
        if (isInDebugLayout && isVertical(orientation)) {
            scrollBy(0, (int) distance);
        }
    }

    /**
     * 启动一个IView完成后, ReplaceIView 也会执行此方法
     */
    protected void onStartIViewEnd(ViewPattern viewPattern) {
        resetChildState();
    }

    /**
     * 当结束一个界面, Remove后, 执行此方法
     */
    protected void onFinishIViewEnd(ViewPattern viewPattern) {

    }

    public void setInterceptTouchEvent(boolean interceptTouchEvent) {
        this.interceptTouchEvent = interceptTouchEvent;
    }

    public void setOverlayDrawable(Drawable overlayDrawable) {
        this.overlayDrawable = overlayDrawable;
        postInvalidate();
    }

    public void setFullOverlayDrawable(boolean fullOverlayDrawable) {
        isFullOverlayDrawable = fullOverlayDrawable;
    }

    /**
     * IView 添加,移除监听
     */
    public interface OnIViewChangedListener {
        void onIViewAdd(final UILayoutImpl uiLayout, final ViewPattern viewPattern);

        void onIViewShow(final UILayoutImpl uiLayout, final ViewPattern viewPattern);

        void onIViewHide(final UILayoutImpl uiLayout, final ViewPattern viewPattern);

        void onIViewCreate(final UILayoutImpl uiLayout, final IView iView, final View rootView);

        void onIViewRemove(final UILayoutImpl uiLayout, final ViewPattern viewPattern);
    }

    public static class SimpleOnIViewChangedListener implements OnIViewChangedListener {

        @Override
        public void onIViewAdd(UILayoutImpl uiLayout, ViewPattern viewPattern) {

        }

        @Override
        public void onIViewShow(UILayoutImpl uiLayout, ViewPattern viewPattern) {

        }

        @Override
        public void onIViewHide(UILayoutImpl uiLayout, ViewPattern viewPattern) {

        }

        @Override
        public void onIViewCreate(UILayoutImpl uiLayout, IView iView, View rootView) {

        }

        @Override
        public void onIViewRemove(UILayoutImpl uiLayout, ViewPattern viewPattern) {

        }
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
