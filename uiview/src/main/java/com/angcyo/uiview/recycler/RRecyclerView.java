package com.angcyo.uiview.recycler;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.OverScroller;

import com.angcyo.library.utils.L;
import com.angcyo.uiview.R;
import com.angcyo.uiview.design.StickLayout;
import com.angcyo.uiview.kotlin.ViewExKt;
import com.angcyo.uiview.recycler.adapter.RBaseAdapter;
import com.angcyo.uiview.recycler.recyclerview.adapters.AnimationAdapter;
import com.angcyo.uiview.recycler.recyclerview.adapters.ScaleInAnimationAdapter;
import com.angcyo.uiview.recycler.recyclerview.animators.BaseItemAnimator;
import com.angcyo.uiview.recycler.recyclerview.animators.FadeInDownAnimator;
import com.angcyo.uiview.resources.AnimUtil;
import com.angcyo.uiview.skin.SkinHelper;
import com.angcyo.uiview.utils.Reflect;
import com.angcyo.uiview.utils.UI;
import com.angcyo.uiview.view.UIIViewImpl;

import java.lang.reflect.Constructor;

/**
 * 简单封装的RecyclerView
 * <p>
 * 动画样式:https://github.com/wasabeef/recyclerview-animators
 * Created by angcyo on 16-03-01-001.
 */
public class RRecyclerView extends RecyclerView implements StickLayout.CanScrollUpCallBack {
    public static final long AUTO_SCROLL_TIME = 1500;

    protected LayoutManager layoutManager;
    protected int spanCount = 2;
    protected int orientation = LinearLayout.VERTICAL;
    protected Class<? extends AnimationAdapter> animatorAdapter;
    protected RBaseAdapter mAdapterRaw;
    protected AnimationAdapter mAnimationAdapter;
    protected boolean mItemAnim = false;
    protected boolean supportsChangeAnimations = false;
    protected boolean isFirstAnim = true;//布局动画只执行一次
    protected boolean layoutAnim = false;//是否使用布局动画
    /**
     * 当前自动滚动到的位置
     */
    protected int curScrollPosition = 0;
    /**
     * 是否激活滚动, 激活滚动是自动滚动的前提
     */
    protected boolean enableScroll = false;
    OnTouchListener mInterceptTouchListener;
    OnFastTouchListener mOnFastTouchListener;
    OnFlingEndListener mOnFlingEndListener;
    boolean isAutoStart = false;
    float fastDownX, fastDownY;
    long fastDownTime = 0L;
    private OnScrollListener mScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            Adapter adapter = getAdapterRaw();
            if (adapter != null && adapter instanceof RBaseAdapter) {
                ((RBaseAdapter) adapter).onScrollStateChanged(RRecyclerView.this, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //滚动状态结束
                    ((RBaseAdapter) adapter).onScrollStateEnd(RRecyclerView.this,
                            isFirstItemVisible(), isLastItemVisible(),
                            UI.canChildScrollUp(recyclerView), UI.canChildScrollDown(recyclerView));
                }
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            Adapter adapter = getAdapterRaw();
            if (adapter != null && adapter instanceof RBaseAdapter) {
                ((RBaseAdapter) adapter).onScrolled(RRecyclerView.this, dx, dy);
                if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                    //滚动结束
                    ((RBaseAdapter) adapter).onScrollStateEnd(RRecyclerView.this,
                            isFirstItemVisible(), isLastItemVisible(),
                            UI.canChildScrollUp(recyclerView), UI.canChildScrollDown(recyclerView));
                }
            }
        }
    };
    private float mLastVelocity;
    private int mLastScrollOffset;
    private boolean isFling;
    /**
     * 当onAttachedToWindow时, 是否自动滚动到onDetachedFromWindow时的位置
     */
    private boolean autoScrollToLastPosition = false;
    /**
     * 是否自动开始滚动
     */
    private boolean isEnableAutoStartScroll = false;

    /**
     * 滚动时间间隔(毫秒)
     */
    private long autoScrollTimeInterval = AUTO_SCROLL_TIME;
    private Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            curScrollPosition++;
            if (getAdapter() != null) {
                int maxItemCount = getAdapter().getItemCount();
                if (curScrollPosition >= maxItemCount) {
                    curScrollPosition = 0;
                    scrollTo(0, false);
                } else {
                    int firstVisibleItemPosition = curScrollPosition;
                    LayoutManager layoutManager = getLayoutManager();
                    if (layoutManager instanceof LinearLayoutManager) {
                        firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                    }
                    //L.e("call: run([])-> " + curScrollPosition + " " + firstVisibleItemPosition);
                    scrollTo(curScrollPosition, Math.abs(firstVisibleItemPosition - curScrollPosition) < 2);
                }
            }

            if (enableScroll) {
                postDelayed(autoScrollRunnable, autoScrollTimeInterval);
            }
        }
    };
    private int lastVisiblePosition = -1;
    private int lastVisibleItemOffset = -1;
    private String widthHeightRatio;
    private boolean equWidth = false;
    private GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Adapter adapter = getAdapterRaw();
            if (adapter != null && adapter instanceof RBaseAdapter) {
                ((RBaseAdapter) adapter).onScrolledInTouch(RRecyclerView.this, e1, e2, distanceX, distanceY);
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    });
    private OnSizeChangedListener mOnSizeChangedListener;

    public RRecyclerView(Context context) {
        this(context, null);
    }

    public RRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RRecyclerView);
        isEnableAutoStartScroll = typedArray.getBoolean(R.styleable.RRecyclerView_r_enable_auto_start_scroll, isEnableAutoStartScroll);
        enableScroll = typedArray.getBoolean(R.styleable.RRecyclerView_r_enable_scroll, enableScroll);
        autoScrollToLastPosition = typedArray.getBoolean(R.styleable.RRecyclerView_r_auto_scroll_to_last_position, autoScrollToLastPosition);
        autoScrollTimeInterval = typedArray.getInt(R.styleable.RRecyclerView_r_auto_scroll_time_interval, (int) autoScrollTimeInterval);
        widthHeightRatio = typedArray.getString(R.styleable.RRecyclerView_r_width_height_ratio);
        equWidth = typedArray.getBoolean(R.styleable.RRecyclerView_r_is_aeq_width, equWidth);
        supportsChangeAnimations = typedArray.getBoolean(R.styleable.RRecyclerView_r_supports_change_animations, supportsChangeAnimations);

        typedArray.recycle();

        initView(context);
    }

    public static void ensureGlow(RecyclerView recyclerView, int color) {
        if (!UIIViewImpl.isLollipop()) {
            if (recyclerView != null) {
                recyclerView.setOverScrollMode(OVER_SCROLL_NEVER);
            }
            return;
        }

        try {
            Reflect.invokeMethod(RecyclerView.class, recyclerView, "ensureTopGlow");
            Reflect.invokeMethod(RecyclerView.class, recyclerView, "ensureBottomGlow");
            Reflect.invokeMethod(RecyclerView.class, recyclerView, "ensureRightGlow");
            Reflect.invokeMethod(RecyclerView.class, recyclerView, "ensureLeftGlow");

            setEdgeEffect(recyclerView, color);
        } catch (Exception e) {
            L.e(e.getMessage());
        }
    }

    //-----------获取 默认的adapter, 获取 RBaseAdapter, 获取 AnimationAdapter----------//

    private static void setEdgeEffect(RecyclerView recyclerView, int color) {
        Object mGlow = Reflect.getMember(RecyclerView.class, recyclerView, "mTopGlow");
        setEdgetEffect(mGlow, color);
        mGlow = Reflect.getMember(RecyclerView.class, recyclerView, "mLeftGlow");
        setEdgetEffect(mGlow, color);
        mGlow = Reflect.getMember(RecyclerView.class, recyclerView, "mRightGlow");
        setEdgetEffect(mGlow, color);
        mGlow = Reflect.getMember(RecyclerView.class, recyclerView, "mBottomGlow");
        setEdgetEffect(mGlow, color);
    }

    public static void setEdgetEffect(Object edgeEffectCompat, @ColorInt int color) {
        Object mEdgeEffect = Reflect.getMember(edgeEffectCompat, "mEdgeEffect");
        Object mPaint;
        if (mEdgeEffect != null) {
            mPaint = Reflect.getMember(mEdgeEffect, "mPaint");
        } else {
            mPaint = Reflect.getMember(edgeEffectCompat, "mPaint");
        }

        if (mPaint instanceof Paint) {
            ((Paint) mPaint).setColor(color);
        }
    }

    protected void initView(Context context) {
        String tag = (String) this.getTag();
        if (TextUtils.isEmpty(tag) || "V".equalsIgnoreCase(tag)) {
            layoutManager = new LinearLayoutManager(context, orientation, false);
        } else {
            //线性布局管理器
            if ("H".equalsIgnoreCase(tag)) {
                orientation = LinearLayoutManager.HORIZONTAL;
                layoutManager = new LinearLayoutManager(context, orientation, false);
            } else {
                //读取其他配置信息(数量和方向)
                final String type = tag.substring(0, 1);
                if (tag.length() >= 3) {
                    try {
                        spanCount = Integer.valueOf(tag.substring(2));//数量
                    } catch (Exception e) {
                    }
                }
                if (tag.length() >= 2) {
                    if ("H".equalsIgnoreCase(tag.substring(1, 2))) {
                        orientation = StaggeredGridLayoutManager.HORIZONTAL;//方向
                    }
                }

                //交错布局管理器
                if ("S".equalsIgnoreCase(type)) {
                    layoutManager = new StaggeredGridLayoutManager(spanCount, orientation);
                }
                //网格布局管理器
                else if ("G".equalsIgnoreCase(type)) {
                    layoutManager = new GridLayoutManager(context, spanCount, orientation, false);
                }
            }
        }

        if (layoutManager instanceof LinearLayoutManager) {
            ((LinearLayoutManager) layoutManager).setRecycleChildrenOnDetach(true);
        }
        this.setLayoutManager(layoutManager);

        setItemAnim(mItemAnim);
        //clearOnScrollListeners();
        removeOnScrollListener(mScrollListener);
        //添加滚动事件监听
        addOnScrollListener(mScrollListener);
    }

    //----------------end--------------------//

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        if (TextUtils.equals("aequilate", getContentDescription()) || equWidth) {
            /**自动设置等宽的RecyclerView*/
            setMeasuredDimension(getMeasuredWidth(), Math.min(getMeasuredWidth(), getMeasuredHeight()));
        } else {
            int[] ints = ViewExKt.calcWidthHeightRatio(this, widthHeightRatio);
            if (ints != null) {
                setMeasuredDimension(ints[0], ints[1]);
            }
        }
    }

//    @Override
//    public RBaseAdapter getAdapter() {
//        return (RBaseAdapter) super.getAdapter();
//    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mOnSizeChangedListener != null) {
            mOnSizeChangedListener.onSizeChanged(w, h, oldw, oldh);
        }
        if (!isInEditMode()) {
            ensureGlow(RRecyclerView.this, SkinHelper.getSkin().getThemeSubColor());
        }
    }

    @Override
    public void setTag(Object tag) {
        super.setTag(tag);
        initView(getContext());
    }

    @Override
    public void startLayoutAnimation() {
        if (isFirstAnim) {
            super.startLayoutAnimation();
        }
        isFirstAnim = false;
    }

    /**
     * 是否设置布局动画
     */
    public void setLayoutAnim(boolean layoutAnim) {
        this.layoutAnim = layoutAnim;
        if (layoutAnim) {
            AnimUtil.applyLayoutAnimation(this);
        } else {
            setLayoutAnimation(null);
        }
    }

    /**
     * 请在{@link RRecyclerView#setAdapter(Adapter)}方法之前调用
     */
    public void setItemAnim(boolean itemAnim) {
        mItemAnim = itemAnim;
        if (mItemAnim) {
            this.setItemAnimator(new FadeInDownAnimator());
        } else {
            this.setItemAnimator(new DefaultItemAnimator());
            setSupportsChangeAnimations(supportsChangeAnimations);
        }
    }

    /**
     * 取消默认动画
     */
    public void setItemNoAnim() {
        setItemAnim(false);
        this.setItemAnimator(null);
    }

    /**
     * @see SimpleItemAnimator#setSupportsChangeAnimations(boolean)
     */
    public void setSupportsChangeAnimations(boolean supportsChangeAnimations) {
        this.supportsChangeAnimations = supportsChangeAnimations;
        ItemAnimator itemAnimator = getItemAnimator();
        if (itemAnimator instanceof DefaultItemAnimator) {
            ((DefaultItemAnimator) itemAnimator).setSupportsChangeAnimations(supportsChangeAnimations);
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (adapter instanceof RBaseAdapter) {
            mAdapterRaw = (RBaseAdapter) adapter;
            addOnChildAttachStateChangeListener(mAdapterRaw);
        }
        mAnimationAdapter = getAnimationAdapter(adapter);

        if (mItemAnim) {
            super.setAdapter(mAnimationAdapter);
        } else {
            super.setAdapter(adapter);
        }
    }

    public RBaseAdapter getAdapterRaw() {
        return mAdapterRaw;
    }

    public AnimationAdapter getAnimationAdapter() {
        return mAnimationAdapter;
    }

    /**
     * 设置Item 动画类, 用于 添加 和 删除 Item时候的动画
     */
    public RRecyclerView setBaseItemAnimator(Class<? extends BaseItemAnimator> animator) {
        try {
            super.setItemAnimator(animator.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * 设置Item滑动时的动画,用于滑动查看时的动画
     */
    public void setAnimatorAdapter(Class<? extends AnimationAdapter> animatorAdapter, Adapter adapter) {
        setAnimatorAdapter(animatorAdapter);
        setAdapter(adapter);
    }

    public RRecyclerView setAnimatorAdapter(Class<? extends AnimationAdapter> animatorAdapter) {
        this.animatorAdapter = animatorAdapter;
        return this;
    }

    /**
     * 将默认的adapter, 包裹一层动画adapter
     */
    private AnimationAdapter getAnimationAdapter(Adapter adapter) {
        AnimationAdapter animationAdapter = new ScaleInAnimationAdapter(adapter);
        if (animatorAdapter != null) {
            try {
                final Constructor<? extends AnimationAdapter> constructor =
                        animatorAdapter.getDeclaredConstructor(Adapter.class);
                animationAdapter = constructor.newInstance(adapter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return animationAdapter;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (!isEnabled()) {
            return false;
        }
        boolean onTouchEvent = super.onTouchEvent(e);
        mGestureDetector.onTouchEvent(e);
        if (getAdapter() == null || getLayoutManager() == null) {
            return false;
        }
        return onTouchEvent;
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        if (Math.abs(velocityY) > 200) {
            isFling = true;
        }
        return super.fling(velocityX, velocityY);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int actionMasked = MotionEventCompat.getActionMasked(ev);
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            isFling = false;

            fastDownX = ev.getX();
            fastDownY = ev.getY();
            fastDownTime = ev.getDownTime();

            if (enableScroll && isEnabled()) {
                stopAutoScroll();
            }
        } else if (actionMasked == MotionEvent.ACTION_UP ||
                actionMasked == MotionEvent.ACTION_CANCEL) {

            if (actionMasked == MotionEvent.ACTION_UP &&
                    mOnFastTouchListener != null) {

                long eventTime = ev.getEventTime();
                int dv = 10;
                if (eventTime - fastDownTime <= OnFastTouchListener.FAST_TIME) {
                    float x = ev.getX();
                    float y = ev.getY();
                    if (Math.abs(x - fastDownX) <= dv && Math.abs(y - fastDownY) <= dv) {
                        mOnFastTouchListener.onFastClick();
                    }
                }
            }

            if (enableScroll && isEnabled()) {
                startAutoScroll();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (mInterceptTouchListener != null) {
            mInterceptTouchListener.onTouch(this, e);
        }

        //项目特殊处理, 可以删除
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(0);
            Rect rect = new Rect();
            childAt.getGlobalVisibleRect(rect);
            if (childAt instanceof RecyclerView && rect.contains(((int) e.getRawX()), (int) e.getRawY())) {
                //如果touch在另一个RecycleView上面, 那么不拦截事件
                return false;
            }
        }
        //--------end--------
        return super.onInterceptTouchEvent(e);
    }

    public void setOnInterceptTouchListener(OnTouchListener l) {
        mInterceptTouchListener = l;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (autoScrollToLastPosition) {
            saveLastPosition();
        }
        //L.e("call: onDetachedFromWindow([]) 1-> " + computeHorizontalScrollRange() + ":" + computeHorizontalScrollExtent() + ":" + computeHorizontalScrollOffset());
        super.onDetachedFromWindow();
        stopAutoScroll();
        //L.e("call: onDetachedFromWindow([]) 2-> " + computeHorizontalScrollRange() + ":" + computeHorizontalScrollExtent() + ":" + computeHorizontalScrollOffset());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (isEnableAutoStartScroll) {
            startAutoScroll();
        }

        resetToLastPosition();
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        //L.e("call: onScrolled([dx, dy])-> " + getLastVelocity());
    }

    /**
     * 恢复滚动信息
     */
    public void resetToLastPosition() {
        if (autoScrollToLastPosition &&
                lastVisiblePosition >= 0) {
            LayoutManager layoutManager = getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(lastVisiblePosition, lastVisibleItemOffset);
            }
        }
    }

    /**
     * 保存滚动的位置信息
     */
    public void saveLastPosition() {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            lastVisiblePosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();

            if (layoutManager.getChildCount() > 0) {
                if (((LinearLayoutManager) layoutManager).getOrientation() == LinearLayoutManager.HORIZONTAL) {
                    lastVisibleItemOffset = getChildAt(0).getLeft();
                } else {
                    lastVisibleItemOffset = getChildAt(0).getTop();
                }
            }
        }
    }

    public void startAutoScroll() {
        LayoutManager layoutManager = getLayoutManager();
        if (enableScroll && getAdapter() != null && getAdapter().getItemCount() > 1 &&
                layoutManager != null && layoutManager instanceof LinearLayoutManager) {
            curScrollPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
            autoScroll();
        }
    }

    protected void autoScroll() {
        if (isAutoStart) {
            return;
        }
        isAutoStart = true;
        postDelayed(autoScrollRunnable, autoScrollTimeInterval);
    }

    public void setEnableAutoStartScroll(boolean enableAutoStartScroll) {
        isEnableAutoStartScroll = enableAutoStartScroll;
        if (enableAutoStartScroll) {
            startAutoScroll();
        } else {
            stopAutoScroll();
        }
    }

    public void setEnableScroll(boolean enableScroll) {
        this.enableScroll = enableScroll;
    }

    public void stopAutoScroll() {
        isAutoStart = false;
        removeCallbacks(autoScrollRunnable);
    }

    @Override
    public void onScrollStateChanged(int state) {
        //L.e("call: onScrollStateChanged([state])-> " + state + " :" + getLastVelocity());
        final int scrollOffset = computeVerticalScrollOffset();
        if (state == SCROLL_STATE_IDLE && isFling && scrollOffset == 0) {
            post(new Runnable() {
                @Override
                public void run() {
                    if (mLastScrollOffset != scrollOffset && mOnFlingEndListener != null) {
                        if (UI.canChildScrollDown(RRecyclerView.this)) {
                            mOnFlingEndListener.onScrollTopEnd(getLastVelocity());
                        }
                    }
                    mLastScrollOffset = -1;
                }
            });
        } else {
            mLastScrollOffset = scrollOffset;
        }
    }

    public void setOnFlingEndListener(OnFlingEndListener onFlingEndListener) {
        mOnFlingEndListener = onFlingEndListener;
    }

    /**
     * 滚动结束后时的速率
     */
    public float getLastVelocity() {
        Object mViewFlinger = Reflect.getMember(RecyclerView.class, this, "mViewFlinger");
        Object mScroller = Reflect.getMember(mViewFlinger, "mScroller");
        float currVelocity = 0f;
        if (mScroller instanceof OverScroller) {
            currVelocity = ((OverScroller) mScroller).getCurrVelocity();
        } else if (mScroller instanceof ScrollerCompat) {
            currVelocity = ((ScrollerCompat) mScroller).getCurrVelocity();
        } else {
            throw new IllegalArgumentException("未兼容的mScroller类型:" + mScroller.getClass().getSimpleName());
        }

        if (Float.isNaN(currVelocity)) {
            currVelocity = mLastVelocity;
        } else {
            mLastVelocity = currVelocity;
        }
        return currVelocity;
    }

    /**
     * 去掉动画, 即可吸顶
     */
    public void scrollTo(int position, boolean anim) {
        LayoutManager manager = getLayoutManager();
        if (manager == null) {
            return;
        }
        stopScroll();
        if (manager instanceof LinearLayoutManager) {
            if (anim) {
                smoothScrollToPosition(position);
            } else {
                ((LinearLayoutManager) manager).scrollToPositionWithOffset(position, 0);
            }
        } else {
            if (anim) {
                smoothScrollToPosition(position);
            } else {
                ((StaggeredGridLayoutManager) manager).scrollToPositionWithOffset(position, 0);
            }
        }
    }

    public void scrollToFirst(int position) {
        scrollTo(position, false);
    }

    /**
     * 滚动到底部
     */
    public void scrollToLastBottom(boolean anim) {
        scrollToLastBottom(anim, true);
    }

    public void scrollToLastBottom(boolean anim, boolean checkScroll) {
        int itemCount = -1;

        if (getAdapter() != null) {
            itemCount = getAdapter().getItemCount();
        }

        if (itemCount > 0 && checkScroll && !ViewCompat.canScrollVertically(this, 1)) {
            //已经是底部
            return;
        }

        final LayoutManager manager = getLayoutManager();
        if (manager == null) {
            return;
        }
        itemCount = manager.getItemCount();
        if (itemCount < 1) {
            return;
        }
        final int position = itemCount - 1;

        if (manager instanceof LinearLayoutManager) {
            if (anim) {
                smoothScrollToPosition(position);
            } else {
                ((LinearLayoutManager) manager).scrollToPositionWithOffset(position, 0);
                post(new Runnable() {
                    @Override
                    public void run() {
                        View target = manager.findViewByPosition(position);//然后才能拿到这个View
                        if (target != null) {
                            ((LinearLayoutManager) manager).scrollToPositionWithOffset(position,
                                    getMeasuredHeight() - target.getMeasuredHeight());//滚动偏移到底部
                        }
                    }
                });
            }
        } else {
            if (anim) {
                smoothScrollToPosition(position);
            } else {
                ((StaggeredGridLayoutManager) manager).scrollToPositionWithOffset(position, 0);
                post(new Runnable() {
                    @Override
                    public void run() {
                        View target = manager.findViewByPosition(position);//然后才能拿到这个View
                        if (target != null) {
                            ((StaggeredGridLayoutManager) manager).scrollToPositionWithOffset(position,
                                    getMeasuredHeight() - target.getMeasuredHeight());//滚动偏移到底部
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.save();
//        try {
//            canvas.translate(getScrollX(), getScrollY());
//            (((ViewGroup) getChildAt(0))).getChildAt(0).draw(canvas);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        canvas.restore();
    }

    public void setAutoScrollToLastPosition(boolean autoScrollToLastPosition) {
        this.autoScrollToLastPosition = autoScrollToLastPosition;
    }

    public int getLastVisiblePosition() {
        return lastVisiblePosition;
    }

    public int getLastVisibleItemOffset() {
        return lastVisibleItemOffset;
    }

    public void setCurScrollPosition(int curScrollPosition) {
        this.curScrollPosition = curScrollPosition;
    }

    public void setLastItemInGridLayoutManager(final GridLayoutManager.SpanSizeLookup spanSizeLookup /*可以为null*/) {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;

            Adapter adapter = getAdapter();
            if (adapter instanceof RBaseAdapter) {
                final RBaseAdapter baseAdapter = (RBaseAdapter) adapter;

                gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        if (baseAdapter.isLast(position) && baseAdapter.isEnableLoadMore()) {
                            return gridLayoutManager.getSpanCount();
                        }
                        if (spanSizeLookup != null && spanSizeLookup.getSpanSize(position) > 0) {
                            return spanSizeLookup.getSpanSize(position);
                        }
                        return 1;
                    }
                });
            }
        }
    }

    /**
     * 是否已经到了顶部
     */
    public boolean isTopEnd() {
        return !UI.canChildScrollUp(this);
    }

    /**
     * 是否已经到了底部
     */
    public boolean isBottomEnd() {
        return !UI.canChildScrollDown(this);
    }

    public void setOnFastTouchListener(OnFastTouchListener onFastTouchListener) {
        mOnFastTouchListener = onFastTouchListener;
    }

    /**
     * 第一个Item是否可见
     */
    public boolean isFirstItemVisible() {
        boolean visible = false;

        Adapter adapter = getAdapter();
        if (adapter != null && adapter.getItemCount() > 0) {
            LayoutManager layoutManager = getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                int firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                visible = firstVisibleItemPosition == 0;
            }
        }
        return visible;
    }

    /**
     * 最后一个Item是否可见
     */
    public boolean isLastItemVisible() {
        boolean visible = false;

        Adapter adapter = getAdapter();
        if (adapter != null && adapter.getItemCount() > 0) {
            LayoutManager layoutManager = getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                int firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                visible = firstVisibleItemPosition == adapter.getItemCount() - 1;
            }
        }
        return visible;
    }

    @Override
    public boolean canChildScrollUp() {
        return UI.canChildScrollUp(this);
    }

    @Override
    public RecyclerView getRecyclerView() {
        return this;
    }

    public OnSizeChangedListener getOnSizeChangedListener() {
        return mOnSizeChangedListener;
    }

    public void setOnSizeChangedListener(OnSizeChangedListener onSizeChangedListener) {
        mOnSizeChangedListener = onSizeChangedListener;
    }

    /**
     * RecyclerView滚动结束后的回调
     */
    public interface OnFlingEndListener {
        /**
         * 突然滚动到顶部, 还剩余的滚动速率
         */
        void onScrollTopEnd(float currVelocity);
    }

    public interface OnFastTouchListener {

        int FAST_TIME = 100;

        /**
         * 快速单击事件监听 (100毫秒内的DOWN UP)
         */
        void onFastClick();
    }

    public interface OnSizeChangedListener {
        void onSizeChanged(int w, int h, int oldw, int oldh);
    }
}
