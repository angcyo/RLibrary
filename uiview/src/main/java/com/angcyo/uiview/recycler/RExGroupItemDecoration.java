package com.angcyo.uiview.recycler;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

/**
 * 支持分组的ItemDecoration, 暂且只支持LinearLayoutManager
 * Created by angcyo on 2017-06-07.
 */

public class RExGroupItemDecoration extends RecyclerView.ItemDecoration {

    private GroupCallBack mGroupCallBack;

    private ArrayMap<Integer, GroupInfo> groupInfoArrayMap = new ArrayMap<>();

    public RExGroupItemDecoration(GroupCallBack groupCallBack) {
        mGroupCallBack = groupCallBack;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mGroupCallBack == null || parent.getChildCount() <= 0) {
            return;
        }
        for (int i = 0; i < parent.getChildCount(); i++) {
            final View view = parent.getChildAt(i);
            final RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
            final int adapterPosition = layoutParams.getViewAdapterPosition();

            GroupInfo groupInfo = groupInfoArrayMap.get(adapterPosition);
            if (i == 0) {
                groupInfo.firstVisibleItemPosition = adapterPosition;
            }
            mGroupCallBack.onGroupDraw(c, view, groupInfo);
        }
    }

    /**
     * 重写此方法, 可以实现您自己的分组上推效果.
     */
    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mGroupCallBack == null || parent.getChildCount() <= 0) {
            return;
        }

        int startOffset;
        String lastGroupText = "";
        for (int i = 0; i < parent.getChildCount(); i++) {
            final View view = parent.getChildAt(i);
            final RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
            final int adapterPosition = layoutParams.getViewAdapterPosition();

            GroupInfo groupInfo = groupInfoArrayMap.get(adapterPosition);

            if (groupInfo.isHorizontal()) {
                startOffset = 0;
            } else {
                startOffset = groupInfo.outRect.top;
            }

            //分组开头的第一个View的left值
            if (groupInfo.groupStartPosition == adapterPosition) {
                if (groupInfo.isHorizontal()) {
                    startOffset = Math.max(view.getLeft(), 0);
                } else {
                    startOffset = Math.max(view.getTop(), groupInfo.outRect.top);
                }
            }
            if (groupInfo.groupEndPosition == groupInfo.firstVisibleItemPosition) {
                if (groupInfo.isHorizontal()) {
                    startOffset = (int) Math.min(0, view.getRight() - mGroupCallBack.getGroupTextSize(adapterPosition, groupInfo.layoutOrientation));
                } else {
                    startOffset = Math.min(startOffset, view.getBottom());
                }
            }

            groupInfo.groupStartOffset = startOffset;

            if (!TextUtils.isEmpty(groupInfo.groupText) && !TextUtils.equals(lastGroupText, groupInfo.groupText)) {
                mGroupCallBack.onGroupOverDraw(c, view, groupInfoArrayMap.get(adapterPosition));
                lastGroupText = groupInfo.groupText;
            }
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        final RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();//布局管理器
        if (!(layoutManager instanceof LinearLayoutManager)) {
            throw new IllegalArgumentException("暂不支持 " + layoutManager.getClass().getSimpleName());
        }

        if (mGroupCallBack == null) {
            return;
        }

        final RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
        final int adapterPosition = layoutParams.getViewAdapterPosition();
        String groupText = mGroupCallBack.getGroupText(adapterPosition);

        GroupInfo groupInfo = new GroupInfo();

        groupInfo.groupText = groupText;
        groupInfo.adapterPosition = adapterPosition;
        groupInfo.layoutOrientation = ((LinearLayoutManager) layoutManager).getOrientation();

        //2017-6-7
        int itemCount = parent.getAdapter().getItemCount();
        groupInfo.groupStartPosition = adapterPosition;//当前分组开始的位置
        groupInfo.groupEndPosition = adapterPosition;//当前分组结束的位置
        if (TextUtils.isEmpty(groupText)) {
            groupInfo.groupStartPosition = -1;
            groupInfo.groupEndPosition = -1;
        } else {
            for (int i = adapterPosition - 1; i >= 0; i--) {
                String tempGroupText = mGroupCallBack.getGroupText(i);
                if (TextUtils.isEmpty(tempGroupText) /*注意此判断, 我也不知道有啥用...*/ ||
                        TextUtils.equals(tempGroupText, groupText)) {
                    groupInfo.groupStartPosition = i;
                } else {
                    break;
                }
            }

            for (int i = adapterPosition; i < itemCount; i++) {
                String tempGroupText = mGroupCallBack.getGroupText(i);
                if (TextUtils.isEmpty(tempGroupText) || TextUtils.equals(tempGroupText, groupText)) {
                    groupInfo.groupEndPosition = i;
                } else {
                    break;
                }
            }
        }

        groupInfoArrayMap.put(adapterPosition, groupInfo);
        groupInfo.outRect.set(0, 0, 0, 0);
        mGroupCallBack.onGetItemOffsets(groupInfo.outRect, groupInfo);
        outRect.set(groupInfo.outRect);
    }

    public static abstract class GroupCallBack {
        /**
         * 返回分组的高度, 请使用onGetItemOffsets代替
         */

        /**
         * 返回分组的文本
         */
        public String getGroupText(int position) {
            return null;
        }

        /**
         * 分组文本的宽度, 如果layoutOrientation是横向, 则返回文本的宽度, 如果是 竖向, 则返回文本的高度.
         * <p>
         * 实现此方法, 可以在2个分组相近是, 出现上一个分组上推的效果
         */
        public float getGroupTextSize(int position, int layoutOrientation) {
            return 0;
        }

        /**
         * 绘制分组信息
         */
        public void onGroupDraw(Canvas canvas, View view, GroupInfo groupInfo) {

        }

        /**
         * 绘制悬浮信息, 相同分组, 只会绘制一次
         */
        public void onGroupOverDraw(Canvas canvas, View view, GroupInfo groupInfo) {

        }

        /**
         * 预留位置, 用来绘制分组信息
         */
        public void onGetItemOffsets(Rect outRect, GroupInfo groupInfo) {

        }
    }

    public static class GroupInfo {
        public int layoutOrientation = -1;
        public int adapterPosition = -1;
        public int groupStartPosition = -1;
        public int groupEndPosition = -1;
        public String groupText = null;
        /**
         * 保存itemOffsets
         */
        public Rect outRect = new Rect();

        /**
         * 此字段在onDraw中赋值
         */
        public int firstVisibleItemPosition = -1;

        /**
         * 此字段在onDrawOver中赋值, 分组信息开始的坐标. 支持横竖方向
         */
        public int groupStartOffset = 0;
        /**
         * 此字段在onDrawOver中赋值, 分组信息结束时最后一个View的剩余有效值
         * 这个属性用于, 分组的文本大小, 大于RecyclerView中ItemView的大小...
         */
        @Deprecated
        public int groupEndOffset = 0;

        /**
         * 返回是否是横向
         */
        public boolean isHorizontal() {
            return layoutOrientation == LinearLayoutManager.HORIZONTAL;
        }
    }

}
