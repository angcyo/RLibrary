package com.angcyo.uiview.recycler.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by yukuoyuan on 2017/3/10.
 * 这是一个可以侧滑菜单的条目的父布局
 * https://github.com/yukuoyuan/YRecycleview
 */

public class SwipeRecycleViewItemLayout extends FrameLayout {

    private final ViewDragHelper dragHelper;
    private View menu;
    private View content;
    private boolean isOpen;
    private int currentState;
    private ViewDragHelper.Callback rightCallback = new ViewDragHelper.Callback() {

        // 触摸到View的时候就会回调这个方法。
        // return true表示抓取这个View。
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return menu != null && menu.getMeasuredWidth() != 0 && content == child;
        }

        /**
         * 重新处理子view的左侧
         * @param child
         * @param left
         * @param dx
         * @return
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return left > 0 ? 0 : left < -menu.getWidth() ? -menu.getWidth() : left;
        }

        /**
         * 当手指释放的时候回调
         * @param releasedChild
         * @param xvel
         * @param yvel
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {

            // x轴移动速度大于菜单一半，或者已经移动到菜单的一般之后，展开菜单
            if (isOpen) {
                if (xvel > menu.getWidth() || -content.getLeft() < menu.getWidth() / 2) {
                    close();
                } else {
                    open();
                }
            } else {
                if (-xvel > menu.getWidth() || -content.getLeft() > menu.getWidth() / 2) {
                    open();
                } else {
                    close();
                }
            }
        }

        /**
         * view 横向移动的范围
         * @param child
         * @return
         */
        @Override
        public int getViewHorizontalDragRange(View child) {
            return 1;
        }

        /**
         *view纵向移动的范围
         * @param child
         * @return
         */
        @Override
        public int getViewVerticalDragRange(View child) {
            return 1;
        }

        /**
         * 当ViewDragHelper状态发生变化的时候调用（IDLE,DRAGGING,SETTING[自动滚动时]）
         * @param state
         */
        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
            currentState = state;
        }
    };
    private Rect outRect = new Rect();

    public SwipeRecycleViewItemLayout(@NonNull Context context) {
        this(context, null);
    }

    /**
     * 构造方法
     *
     * @param context 上下文
     * @param attrs   属性集合
     */
    public SwipeRecycleViewItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        /**
         * 初始化我们自定义处理触摸事件的方法
         */
        dragHelper = ViewDragHelper.create(this, rightCallback);
    }

    /**
     * 处理触摸事件(交给draghelper去处理)
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dragHelper.processTouchEvent(event);
        return true;
    }

    /**
     * 处理触摸事件
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return dragHelper.shouldInterceptTouchEvent(ev);
    }

    /**
     * 获取当前的状态
     *
     * @return
     */
    public int getState() {
        return currentState;
    }

    public Rect getMenuRect() {
        menu.getHitRect(outRect);
        return outRect;
    }

    /**
     * 当绘制完毕调用的方法
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ensureChildView();
    }

    private void ensureChildView() {
        if (getChildCount() > 0) {
            menu = getChildAt(0);
        }
        if (getChildCount() > 1) {
            content = getChildAt(1);
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        ensureChildView();
    }

    /**
     * 这是一个关闭菜单的方法
     */
    public void close() {
        dragHelper.smoothSlideViewTo(content, 0, 0);
        isOpen = false;
        invalidate();
    }

    /**
     * 这是一个打开菜单的方法
     */
    public void open() {
        dragHelper.smoothSlideViewTo(content, -menu.getWidth(), 0);
        isOpen = true;
        invalidate();
    }

    /**
     * 计算滚动事件
     */
    @Override
    public void computeScroll() {
        super.computeScroll();
        if (dragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    /**
     * 设置点击事件
     *
     * @param l
     */
    @Override
    public void setOnClickListener(OnClickListener l) {
        content.setOnClickListener(l);
    }

    public boolean isOpen() {
        return this.isOpen;
    }

    public static class EmptyView extends View {
        public EmptyView(Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            if (heightMode == MeasureSpec.AT_MOST) {
                heightSize = 0;
            }

            if (widthMode == MeasureSpec.AT_MOST) {
                widthSize = 0;
            }

            setMeasuredDimension(widthSize, heightSize);
        }
    }

}
