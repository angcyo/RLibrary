package com.angcyo.uiview.recycler;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：支持拖拽的RecycleView
 * 创建人员：Robi
 * 创建时间：2017/02/17 17:59
 * 修改人员：Robi
 * 修改时间：2017/02/17 17:59
 * 修改备注：
 * Version: 1.0.0
 */
public class RDragRecyclerView extends RRecyclerView {

    ItemTouchHelper mItemTouchHelper;
    OnDragCallback mDragCallback;
    private RDragCallback mRDragCallback;
    private boolean drawOverInFullscreen = false;
    /**
     * 是否选中了View
     */
    private boolean isSelected = false;
    private int selectedPosition = -1;

    public RDragRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RDragRecyclerView(Context context) {
        super(context);
    }

    public RDragRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void initView(Context context) {
        super.initView(context);
        if (mItemTouchHelper == null) {
            mRDragCallback = new RDragCallback(new RDragCallback.SingleDragCallback() {
                @Override
                public boolean canDragDirs(RecyclerView recyclerView, ViewHolder viewHolder) {
                    if (mDragCallback != null) {
                        return mDragCallback.canDragDirs(recyclerView, viewHolder);
                    } else {
                        return super.canDragDirs(recyclerView, viewHolder);
                    }
                }

                @Override
                public void onSelectedChanged(ViewHolder viewHolder, int actionState) {
                    super.onSelectedChanged(viewHolder, actionState);
//                    if (viewHolder != null) {
//                        L.e("call: onSelectedChanged([viewHolder, actionState])-> " + viewHolder + " " + viewHolder.getAdapterPosition() + " " + viewHolder.getLayoutPosition());
//                    }
                    isSelected = viewHolder != null;
                    if (isSelected) {
                        selectedPosition = viewHolder.getAdapterPosition();
                    }
                    if (mDragCallback != null) {
                        mDragCallback.onSelectedChanged(isSelected, selectedPosition);
                    }
                }

                @Override
                public void onClearView(RecyclerView recyclerView, ViewHolder viewHolder) {
                    super.onClearView(recyclerView, viewHolder);
                    //L.e("call: onClearView([recyclerView, viewHolder])-> " + viewHolder + " " + viewHolder.getAdapterPosition() + " " + viewHolder.getLayoutPosition());
                    isSelected = false;
                    if (mDragCallback != null) {
                        mDragCallback.onClearView();
                    }
                }
            });
            mRDragCallback.setDrawOverInFullscreen(drawOverInFullscreen);
            mItemTouchHelper = new RItemTouchHelper(mRDragCallback);
            mItemTouchHelper.attachToRecyclerView(this);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        //L.e("call: onTouchEvent([e])-> " + e.getActionMasked());
        return super.onTouchEvent(e);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //L.e("call: dispatchTouchEvent([ev])-> " + ev.getActionMasked());
        if (mDragCallback != null) {
            mDragCallback.onTouchMoveTo(ev, isSelected, selectedPosition);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        //L.e("call: onInterceptTouchEvent([e])-> " + e.getActionMasked());
        return super.onInterceptTouchEvent(e);
    }

    public void setDragCallback(OnDragCallback dragCallback) {
        mDragCallback = dragCallback;
    }

    public void setDrawOverInFullscreen(boolean drawOverInFullscreen) {
        this.drawOverInFullscreen = drawOverInFullscreen;
        if (mRDragCallback != null) {
            mRDragCallback.setDrawOverInFullscreen(drawOverInFullscreen);
        }
    }

    public void setNoAnimation(boolean noAnimation) {
        if (mRDragCallback != null) {
            mRDragCallback.setNoAnimation(noAnimation);
        }
    }

    public static class OnDragCallback {
        protected boolean canDragDirs(RecyclerView recyclerView, ViewHolder viewHolder) {
            return true;
        }

        protected void onTouchMoveTo(MotionEvent event, boolean isSelected, int selectedPosition) {
            //L.e("call: onTouchMoveTo([event])-> y:" + event.getY() + " ry:" + event.getRawY());
        }

        protected void onClearView() {
        }

        protected void onSelectedChanged(boolean isSelected, int selectedPosition) {
        }
    }
}
