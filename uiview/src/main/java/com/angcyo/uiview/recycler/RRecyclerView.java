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
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.angcyo.library.utils.L;
import com.angcyo.uiview.R;
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
public class RRecyclerView extends RecyclerView {
    public static final long AUTO_SCROLL_TIME = 1500;

    protected LayoutManager layoutManager;
    protected int spanCount = 2;
    protected int orientation = LinearLayout.VERTICAL;
    protected Class<? extends AnimationAdapter> animatorAdapter;
    protected RBaseAdapter mAdapterRaw;
    protected AnimationAdapter mAnimationAdapter;
    protected boolean mItemAnim = false;
    protected boolean isFirstAnim = true;//布局动画只执行一次
    protected boolean layoutAnim = false;//是否使用布局动画
    /**
     * 当前自动滚动到的位置
     */
    protected int curScrollPosition = 0;
    /**
     * 激活滚动
     */
    protected boolean enableScroll = false;
    OnTouchListener mInterceptTouchListener;
    OnFlingEndListener mOnFlingEndListener;
    boolean isAutoStart = false;
    private OnScrollListener mScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            Adapter adapter = getAdapterRaw();
            if (adapter != null && adapter instanceof RBaseAdapter) {
                ((RBaseAdapter) adapter).onScrollStateChanged(recyclerView, newState);
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            Adapter adapter = getAdapterRaw();
            if (adapter != null && adapter instanceof RBaseAdapter) {
                ((RBaseAdapter) adapter).onScrolled(recyclerView, dx, dy);
            }
        }
    };
    private float mLastVelocity;
    private int mLastScrollOffset;
    private boolean isFling;
    /**
     * 是否自动滚动
     */
    private boolean isEnableAutoScroll = false;
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
                    scrollTo(curScrollPosition, true);
                }
            }

            if (enableScroll) {
                postDelayed(autoScrollRunnable, AUTO_SCROLL_TIME);
            }
        }
    };

    public RRecyclerView(Context context) {
        this(context, null);
    }

    public RRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RRecyclerView);
        isEnableAutoScroll = typedArray.getBoolean(R.styleable.RRecyclerView_r_enable_auto_scroll, isEnableAutoScroll);
        enableScroll = typedArray.getBoolean(R.styleable.RRecyclerView_r_enable_scroll, enableScroll);
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
        Object mPaint = Reflect.getMember(mEdgeEffect, "mPaint");
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

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        if (TextUtils.equals("aequilate", getContentDescription())) {
            /**自动设置等宽的RecyclerView*/
            setMeasuredDimension(getMeasuredWidth(), Math.min(getMeasuredWidth(), getMeasuredHeight()));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (!isInEditMode()) {
            ensureGlow(RRecyclerView.this, SkinHelper.getSkin().getThemeSubColor());
        }
    }

    //-----------获取 默认的adapter, 获取 RBaseAdapter, 获取 AnimationAdapter----------//

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

    //----------------end--------------------//

    /**
     * 请在{@link RRecyclerView#setAdapter(Adapter)}方法之前调用
     */
    public void setItemAnim(boolean itemAnim) {
        mItemAnim = itemAnim;
        if (mItemAnim) {
            this.setItemAnimator(new FadeInDownAnimator());
        } else {
            this.setItemAnimator(new DefaultItemAnimator());
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
        return super.onTouchEvent(e);
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

            if (enableScroll) {
                stopAutoScroll();
            }
        } else if (actionMasked == MotionEvent.ACTION_UP ||
                actionMasked == MotionEvent.ACTION_CANCEL) {
            if (enableScroll) {
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
        super.onDetachedFromWindow();
        stopAutoScroll();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isEnableAutoScroll) {
            startAutoScroll();
        }
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        //L.e("call: onScrolled([dx, dy])-> " + getLastVelocity());
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
        postDelayed(autoScrollRunnable, AUTO_SCROLL_TIME);
    }

    public void setEnableAutoScroll(boolean enableAutoScroll) {
        isEnableAutoScroll = enableAutoScroll;
        if (enableAutoScroll) {
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
        ScrollerCompat mScroller = (ScrollerCompat) Reflect.getMember(mViewFlinger, "mScroller");
        float currVelocity = mScroller.getCurrVelocity();
        if (Float.isNaN(currVelocity)) {
            currVelocity = mLastVelocity;
        } else {
            mLastVelocity = currVelocity;
        }
        return currVelocity;
    }

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

    public void setCurScrollPosition(int curScrollPosition) {
        this.curScrollPosition = curScrollPosition;
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
}
