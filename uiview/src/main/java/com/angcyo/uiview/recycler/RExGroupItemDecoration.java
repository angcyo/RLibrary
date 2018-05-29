package com.angcyo.uiview.recycler;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;

import com.angcyo.uiview.R;
import com.angcyo.uiview.RApplication;

import static com.angcyo.uiview.utils.ScreenUtil.density;

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

    private GroupInfo getGroupInfo(int position) {
        GroupInfo groupInfo = groupInfoArrayMap.get(position);
        if (groupInfo == null) {
            groupInfo = new GroupInfo();
            groupInfoArrayMap.put(position, groupInfo);
        }
        return groupInfo;
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

            GroupInfo groupInfo = getGroupInfo(adapterPosition);
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

        int startOffset = -1;
        String lastGroupText = "";
        for (int i = 0; i < parent.getChildCount(); i++) {
            final View view = parent.getChildAt(i);
            final RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
            final int adapterPosition = layoutParams.getViewAdapterPosition();

            GroupInfo groupInfo = getGroupInfo(adapterPosition);

            //分组开头的第一个View的left值
            if (groupInfo.groupStartPosition == adapterPosition) {
                if (groupInfo.isHorizontal()) {
                    startOffset = Math.max(view.getLeft(), 0);
                } else {
                    startOffset = Math.max(view.getTop(), groupInfo.outRect.top);
                }
            } else if (groupInfo.groupEndPosition == groupInfo.firstVisibleItemPosition) {
                if (groupInfo.isHorizontal()) {
                    startOffset = (int) Math.min(0, view.getRight() - mGroupCallBack.getGroupTextWidth(adapterPosition, groupInfo));
                } else {
                    startOffset = Math.min(groupInfo.outRect.top, view.getBottom());
                }
            } else {
                if (startOffset == -1) {
                    if (groupInfo.isHorizontal()) {
                        startOffset = 0;
                    } else {
                        startOffset = groupInfo.outRect.top;
                    }
                }
            }

            groupInfo.groupStartOffset = startOffset;

            if (!TextUtils.isEmpty(groupInfo.groupText) && !TextUtils.equals(lastGroupText, groupInfo.groupText)) {
                mGroupCallBack.onGroupOverDraw(c, view, getGroupInfo(adapterPosition));
                lastGroupText = groupInfo.groupText;
                startOffset = -1;
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

        GroupInfo groupInfo = getGroupInfo(adapterPosition);

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
                if (!TextUtils.isEmpty(tempGroupText) /*如果分组前面出现空字符, 则视为不同分组*/ &&
                        TextUtils.equals(tempGroupText, groupText)) {
                    groupInfo.groupStartPosition = i;
                } else {
                    break;
                }
            }

            for (int i = adapterPosition; i < itemCount; i++) {
                String tempGroupText = mGroupCallBack.getGroupText(i);
                if (TextUtils.isEmpty(tempGroupText) /*如果分组后面出现了空字符的分组信息, 则视为相同相同分组*/ ||
                        TextUtils.equals(tempGroupText, groupText)) {
                    groupInfo.groupEndPosition = i;
                } else {
                    break;
                }
            }
        }

        groupInfo.outRect.set(0, 0, 0, 0);
        mGroupCallBack.onGetItemOffsets(groupInfo.outRect, groupInfo);
        outRect.set(groupInfo.outRect);
    }

    public static abstract class GroupCallBack {

        protected TextPaint mTextPaint;
        protected float leftOffset = 0f, bottomOffset = 0f;//偏移距离

        public GroupCallBack() {
            mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            mTextPaint.setTextSize(12 * density());
            mTextPaint.setColor(RApplication.getApp().getResources().getColor(R.color.base_text_color_dark));

            bottomOffset = 4 * density();
            leftOffset = 10 * density();
        }
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
        public float getGroupTextWidth(int position, @NonNull GroupInfo groupInfo) {
            return mTextPaint.measureText(groupInfo.groupText);
        }

        /**
         * 绘制分组信息
         */
        public void onGroupDraw(@NonNull Canvas canvas, @NonNull View view, @NonNull GroupInfo groupInfo) {

        }

        /**
         * 绘制悬浮信息, 相同分组, 只会绘制一次
         */
        public void onGroupOverDraw(@NonNull Canvas canvas, @NonNull View view, @NonNull GroupInfo groupInfo) {

        }

        /**
         * 预留位置, 用来绘制分组信息
         */
        public void onGetItemOffsets(@NonNull Rect outRect, @NonNull GroupInfo groupInfo) {

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
