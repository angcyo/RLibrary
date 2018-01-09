package com.angcyo.uiview.recycler;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.angcyo.uiview.recycler.adapter.RBaseAdapter;
import com.angcyo.uiview.utils.ScreenUtil;

import java.util.Collections;
import java.util.List;

public class RDragCallback extends ItemTouchHelper.SimpleCallback {
    OnDragCallback mDragCallback;
    /**
     * 全屏可视, 拖拽的镜像View
     */
    private boolean drawOverInFullscreen = false;
    private Rect clipBounds = new Rect();
    /**
     * 放手之后, 是否需要动画 (可以模仿微信的滑动到底部删除)
     */
    private boolean noAnimation = false;

    public RDragCallback(OnDragCallback callback) {
        //第一个参数表示,什么方向支持拖拽, 暂不支持滑动
        super(ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0);
        mDragCallback = callback;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return mDragCallback.isLongPressDragEnabled();
    }

    @Override
    public int getDragDirs(RecyclerView recyclerView, ViewHolder viewHolder) {
        if (mDragCallback != null) {
            if (mDragCallback.canDragDirs(recyclerView, viewHolder)) {
                int dirs = 0;
                boolean dragDirs = mDragCallback.canHorizontalDragDirs(recyclerView, viewHolder);
                if (dragDirs) {
                    dirs |= ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                }
                dragDirs = mDragCallback.canVerticalDragDirs(recyclerView, viewHolder);
                if (dragDirs) {
                    dirs |= ItemTouchHelper.DOWN | ItemTouchHelper.UP;
                }
                return dirs;
            } else {
                return 0;
            }
        }
        return super.getDragDirs(recyclerView, viewHolder);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, ViewHolder viewHolder, ViewHolder target) {
        if (mDragCallback != null) {
            if (!mDragCallback.canDragDirs(recyclerView, target)) {
                return false;
            }

            int from = viewHolder.getAdapterPosition();
            int to = target.getAdapterPosition();

            mDragCallback.onMove(recyclerView, from, to);
            return true;
        }
        return false;
    }

    @Override
    public void onSwiped(ViewHolder viewHolder, int direction) {

    }

    @Override
    public void clearView(RecyclerView recyclerView, ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
//        L.e("call: clearView([recyclerView, viewHolder])-> " + " " + viewHolder);
        if (mDragCallback != null) {
            mDragCallback.onClearView(recyclerView, viewHolder);
            //L.e("call: clearView([recyclerView, viewHolder])-> ");
        }
        //recyclerView.invalidate();
    }

    @Override
    public void onSelectedChanged(ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
//        L.e("call: onSelectedChanged([viewHolder, actionState])-> " + " " + viewHolder);
        noAnimation = false;
        if (mDragCallback != null) {
            mDragCallback.onSelectedChanged(viewHolder, actionState);
        }
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
//        //L.e("call: onChildDraw([c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive])-> " + actionState + " " + isCurrentlyActive + " " + viewHolder, 700);
    }

    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView, ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
//        L.e("call: onChildDrawOver([c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive])-> " + actionState + " " + isCurrentlyActive + " " + viewHolder, 300);
        if (drawOverInFullscreen && viewHolder.getAdapterPosition() >= 0) {
            //L.e("call: onChildDrawOver([c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive])-> dx:" + dX + " dy:" + dY + " " + actionState + " " + isCurrentlyActive);
            c.save();
            c.getClipBounds(clipBounds);
            clipBounds.inset(-ScreenUtil.screenWidth, -ScreenUtil.screenHeight);
            c.clipRect(clipBounds, Region.Op.REPLACE);
            c.translate(viewHolder.itemView.getLeft() + dX, viewHolder.itemView.getTop() + dY);
            viewHolder.itemView.draw(c);
            c.restore();
        }
    }

    @Override
    public long getAnimationDuration(RecyclerView recyclerView, int animationType, float animateDx, float animateDy) {
        //L.e("call: getAnimationDuration([recyclerView, animationType, animateDx, animateDy])-> " + duration + " " + animationType + " " + animateDx + " " + animateDy);
        if (noAnimation) {
            return 0;
        }
        long duration = super.getAnimationDuration(recyclerView, animationType, animateDx, animateDy);
        return duration;
    }

    public boolean isDrawOverInFullscreen() {
        return drawOverInFullscreen;
    }

    public void setDrawOverInFullscreen(boolean drawOverInFullscreen) {
        this.drawOverInFullscreen = drawOverInFullscreen;
    }

    public void setNoAnimation(boolean noAnimation) {
        this.noAnimation = noAnimation;
    }

    public interface OnDragCallback {
        boolean canDragDirs(RecyclerView recyclerView, ViewHolder viewHolder);

        boolean canVerticalDragDirs(RecyclerView recyclerView, ViewHolder viewHolder);

        boolean canHorizontalDragDirs(RecyclerView recyclerView, ViewHolder viewHolder);

        void onMove(RecyclerView recyclerView, int fromPosition, int toPosition);

        boolean isLongPressDragEnabled();

        void onClearView(RecyclerView recyclerView, ViewHolder viewHolder);

        void onSelectedChanged(ViewHolder viewHolder, int actionState);
    }

    public static abstract class SingleDragCallback implements OnDragCallback {
        @Override
        public boolean canDragDirs(RecyclerView recyclerView, ViewHolder viewHolder) {
            return true;
        }

        @Override
        public boolean canHorizontalDragDirs(RecyclerView recyclerView, ViewHolder viewHolder) {
            return true;
        }

        @Override
        public boolean canVerticalDragDirs(RecyclerView recyclerView, ViewHolder viewHolder) {
            return true;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public void onClearView(RecyclerView recyclerView, ViewHolder viewHolder) {

        }

        @Override
        public void onSelectedChanged(ViewHolder viewHolder, int actionState) {

        }

        @Override
        public void onMove(RecyclerView recyclerView, int fromPosition, int toPosition) {
            RecyclerView.Adapter adapter = recyclerView.getAdapter();

            if (adapter instanceof RBaseAdapter) {
                List allDatas = ((RBaseAdapter) adapter).getAllDatas();

                int size = allDatas.size();
                if (size > fromPosition && size > toPosition) {
                    Collections.swap(allDatas, fromPosition, toPosition);
                }
                adapter.notifyItemMoved(fromPosition, toPosition);
            }
        }
    }

}
