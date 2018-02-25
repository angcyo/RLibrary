package com.angcyo.uiview.recycler;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.angcyo.library.utils.L;
import com.angcyo.uiview.recycler.adapter.RBaseSwipeAdapter;
import com.angcyo.uiview.recycler.widget.SwipeRecycleViewItemLayout;

/**
 * 带有侧滑菜单的RecyclerView
 * <p>
 * 请配合
 * {@link com.angcyo.uiview.recycler.adapter.RBaseSwipeAdapter}
 * 使用
 */
public class RSwipeRecycleView extends RRecyclerView {

    /**
     * 侧滑删除的部分常量
     */
    private float startX;
    private float startY;
    private int touchSlop;
    private boolean isChildHandle;
    private View touchView;
    private float distanceX;
    private float distanceY;
    /**
     * 当前手指位置的position(屏幕上显示的第一个Item为0)
     */
    private Rect touchFrame;

    public RSwipeRecycleView(Context context) {
        super(context);
    }

    public RSwipeRecycleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
    }


    public RSwipeRecycleView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    /**
     * @see RBaseSwipeAdapter
     */
    @Override
    public void setAdapter(Adapter adapter) {
        if (adapter instanceof RBaseSwipeAdapter) {

        } else {
            L.e("call: setAdapter([adapter])-> 警告:请使用RBaseSwipeAdapter,否则无法实现侧滑菜单.");
        }
        super.setAdapter(adapter);
    }

    /**
     * 处理横向滑动的事件
     */

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            isChildHandle = false;
            // 记录手指按下的位置
            startY = ev.getY();
            startX = ev.getX();
            distanceX = 0;
            distanceY = 0;
            // 获取按下的那个View
            int position = pointToPosition((int) startX, (int) startY);
            touchView = getChildAt(position);

            if (hasChildOpen()) {
                // 如果触摸的不是打开的那个View, 关闭所有View，并且拦截所有事件
                if (touchView != null && touchView instanceof SwipeRecycleViewItemLayout &&
                        ((SwipeRecycleViewItemLayout) touchView).isOpen()) {
                    isChildHandle = true; // 将事件交给child！
                } else {
                    closeAllSwipeItem();
                    //return false;
                }
            }
        }
        // 禁用多点触控
        if (action == MotionEvent.ACTION_POINTER_DOWN) {
            return false;
        }

        return super.dispatchTouchEvent(ev);
    }

    // 处理和侧滑菜单冲突
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 如果竖向滑动，拦截，否则不拦截。
        int action = ev.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                // 获取当前手指位置
                float endY = ev.getY();
                float endX = ev.getX();
                distanceX = Math.abs(endX - startX);
                distanceY = Math.abs(endY - startY);

                // 如果child已经持有事件，那么不拦截它的事件，直接return false；
                if (isChildHandle) {
                    return false;
                }
                // 如果X轴位移大于Y轴位移，那么将事件交给child处理。
                if (distanceX > touchSlop && distanceX > distanceY) {
                    isChildHandle = true;
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                // state != 1 没有滑动过, 关闭打开的菜单
                if (touchView != null && touchView instanceof SwipeRecycleViewItemLayout) {
                    SwipeRecycleViewItemLayout swipeItem = (SwipeRecycleViewItemLayout) this.touchView;
                    if (swipeItem.isOpen() && swipeItem.getState() != 1) {
                        if (distanceX < touchSlop && distanceY < touchSlop) {
                            swipeItem.close();
                        }
                        Rect rect = swipeItem.getMenuRect();
                        // 如果不是点击在菜单上，拦截点击事件。
                        if (!(startX > rect.left && startX < rect.right &&
                                startY > touchView.getTop() && startY < touchView.getBottom())) {
                            return true;  // return true，拦截Item点击事件, 但是菜单能接收到。
                        }
                    }
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    /******
     *
     *
     * 以下是用来处理侧滑删除的功能的
     *
     */

    private int pointToPosition(int x, int y) {
        Rect frame = touchFrame;
        if (frame == null) {
            touchFrame = new Rect();
            frame = touchFrame;
        }
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == VISIBLE) {
                child.getHitRect(frame);
                if (frame.contains(x, y)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 是否还有条目打开
     *
     * @return
     */
    private boolean hasChildOpen() {
        final int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (child != null && child instanceof SwipeRecycleViewItemLayout) {
                if (((SwipeRecycleViewItemLayout) child).isOpen()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 关闭所有侧滑按钮
     */
    public void closeAllSwipeItem() {
        final int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (child != null && child instanceof SwipeRecycleViewItemLayout) {
                ((SwipeRecycleViewItemLayout) child).close();
            }
        }
    }
}
