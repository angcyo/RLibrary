package com.angcyo.uiview.viewgroup;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.Pools;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.angcyo.uiview.R;
import com.angcyo.uiview.kotlin.ExKt;
import com.angcyo.uiview.kotlin.ViewExKt;
import com.angcyo.uiview.utils.RUtils;

import java.util.ArrayList;
import java.util.Map;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：轻量级的CoordinatorLayout, 支持三大布局规则, 添加百分比宽高属性
 * 创建人员：Robi
 * 创建时间：2018/04/17 10:33
 * 修改人员：Robi
 * 修改时间：2018/04/17 10:33
 * 修改备注：
 * Version: 1.0.0
 */
public class RViewGroup extends ViewGroup {

    private static final Pools.Pool<Rect> sRectPool = new Pools.SynchronizedPool<>(12);

    //    private ArrayList<ArrayList<View>> mRuleViews = new ArrayList<>();
    private ArrayMap<String, ArrayList<View>> mRuleMap = new ArrayMap<>();

    //保存用来需要计算剩余空间的child
    private ArrayList<View> mWeightViewsH = new ArrayList<>();
    private ArrayList<View> mWeightViewsV = new ArrayList<>();

    public RViewGroup(Context context) {
        super(context);
    }

    public RViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @NonNull
    private static Rect acquireTempRect() {
        Rect rect = sRectPool.acquire();
        if (rect == null) {
            rect = new Rect();
        }
        return rect;
    }

    private static void releaseTempRect(@NonNull Rect rect) {
        rect.setEmpty();
        sRectPool.release(rect);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        prepareChildren();
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        final int paddingRight = getPaddingRight();
        final int paddingBottom = getPaddingBottom();
        final int layoutDirection = ViewCompat.getLayoutDirection(this);
        final boolean isRtl = layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL;//不支持
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        final int widthPadding = paddingLeft + paddingRight;
        final int heightPadding = paddingTop + paddingBottom;
        int widthUsed = getSuggestedMinimumWidth();
        int heightUsed = getSuggestedMinimumHeight();

        int viewWidth = widthSize;
        int viewHeight = heightSize;

        int childMaxWidth = 0;
        int childMaxHeight = 0;

        //布局中包含多少个规则的组合
        for (Map.Entry<String, ArrayList<View>> enter : mRuleMap.entrySet()) {
            //当前布局的规则
            String layoutRule = enter.getKey();
            //规则中, 包含多少个child
            ArrayList<View> ruleChild = enter.getValue();

            int childWidth = 0;
            int childHeight = 0;

            //相同规则child总占用的宽高
            int lineWidth = 0;
            int lineHeight = 0;
            for (int i = 0; i < ruleChild.size(); i++) {
                final View child = ruleChild.get(i);
                if (child.getVisibility() == GONE) {
                    // If the child is GONE, skip...
                    continue;
                }

                final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                //Child宽高测量, 这里做了百分比sw, sh, pw, ph的百分比支持
                int childWidthMeasureSpec;
                int childHeightMeasureSpec;
                int[] calcLayoutWidthHeight = ViewExKt.calcLayoutWidthHeight(child, lp.layoutWidth, lp.layoutHeight,
                        widthSize, heightSize,
                        paddingLeft + paddingRight, paddingTop + paddingBottom);
                if (lp.layoutWidth != null) {
                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(calcLayoutWidthHeight[0], MeasureSpec.EXACTLY);
                } else {
                    childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                            paddingLeft + paddingRight + lp.leftMargin + lp.rightMargin
                                    + widthUsed, lp.width);
                }
                if (lp.layoutHeight != null) {
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(calcLayoutWidthHeight[1], MeasureSpec.EXACTLY);
                } else {
                    childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                            paddingTop + paddingBottom + lp.topMargin + lp.bottomMargin
                                    + heightUsed, lp.height);
                }

                if (layoutRule.contains("f")) {
                    //帧布局规则
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

                    childWidth = child.getMeasuredWidth();
                    childHeight = child.getMeasuredHeight();

                    lineWidth = childWidth + lp.leftMargin + lp.rightMargin;
                    lineHeight = childHeight + lp.topMargin + lp.bottomMargin;
                } else if (layoutRule.contains("l")) {
                    //线性布局规则
                    if (layoutRule.contains("h")) {
                        //水平线性
                        if (lp.layoutWeight >= 0) {
                            mWeightViewsH.add(child);
                            continue;
                        }
                    } else if (layoutRule.contains("v")) {
                        //垂直
                        if (lp.layoutWeight >= 0) {
                            mWeightViewsV.add(child);
                            continue;
                        }
                    }

                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

                    childWidth = child.getMeasuredWidth();
                    childHeight = child.getMeasuredHeight();

                    if (layoutRule.contains("h")) {
                        lineWidth += childWidth + lp.leftMargin + lp.rightMargin;
                        lineHeight = Math.max(lineHeight, childHeight + lp.topMargin + lp.bottomMargin);
                    } else if (layoutRule.contains("v")) {
                        lineWidth = Math.max(lineWidth, childWidth + lp.leftMargin + lp.rightMargin);
                        lineHeight += childHeight + lp.topMargin + lp.bottomMargin;
                    }
                }
            }

            //计算剩余空间分配
            int subLineWidth = 0;
            int subLineHeight = 0;
            if (!RUtils.isListEmpty(mWeightViewsH)) {
                for (int i = 0; i < mWeightViewsH.size(); i++) {
                    View child = mWeightViewsH.get(i);
                    final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                    float scale;
                    if (lp.layoutWeight >= 1) {
                        scale = 1f / mWeightViewsH.size();
                    } else {
                        scale = lp.layoutWeight;
                    }

                    int childWidthMeasureSpec;
                    int childHeightMeasureSpec;
                    int[] calcLayoutWidthHeight = ViewExKt.calcLayoutWidthHeight(child, lp.layoutWidth, lp.layoutHeight,
                            widthSize, heightSize,
                            paddingLeft + paddingRight, paddingTop + paddingBottom);
                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec((int) (widthSize * scale), MeasureSpec.EXACTLY);
                    if (lp.layoutHeight != null) {
                        childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(calcLayoutWidthHeight[1], MeasureSpec.EXACTLY);
                    } else {
                        childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                                paddingTop + paddingBottom + lp.topMargin + lp.bottomMargin
                                        + heightUsed, lp.height);
                    }
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

                    childWidth = child.getMeasuredWidth();
                    childHeight = child.getMeasuredHeight();

                    subLineWidth += childWidth + lp.leftMargin + lp.rightMargin;
                    subLineHeight = Math.max(subLineHeight, childHeight + lp.topMargin + lp.bottomMargin);
                }
            }

            if (!RUtils.isListEmpty(mWeightViewsV)) {
                for (int i = 0; i < mWeightViewsV.size(); i++) {
                    View child = mWeightViewsV.get(i);
                    final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                    float scale;
                    if (lp.layoutWeight >= 1) {
                        scale = 1f / mWeightViewsV.size();
                    } else {
                        scale = lp.layoutWeight;
                    }

                    int childWidthMeasureSpec;
                    int childHeightMeasureSpec;
                    int[] calcLayoutWidthHeight = ViewExKt.calcLayoutWidthHeight(child, lp.layoutWidth, lp.layoutHeight,
                            widthSize, heightSize,
                            paddingLeft + paddingRight, paddingTop + paddingBottom);
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (heightSize * scale), MeasureSpec.EXACTLY);
                    if (lp.layoutWidth != null) {
                        childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(calcLayoutWidthHeight[0], MeasureSpec.EXACTLY);
                    } else {
                        childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                                paddingLeft + paddingRight + lp.leftMargin + lp.rightMargin
                                        + widthUsed, lp.width);
                    }
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

                    childWidth = child.getMeasuredWidth();
                    childHeight = child.getMeasuredHeight();

                    subLineWidth = Math.max(subLineWidth, childWidth + lp.leftMargin + lp.rightMargin);
                    subLineHeight += childHeight + lp.topMargin + lp.bottomMargin;
                }
            }

            mWeightViewsH.clear();
            mWeightViewsV.clear();

            if (layoutRule.contains("h")) {
                lineWidth += subLineWidth;
                lineHeight = Math.max(lineHeight, subLineHeight);
            } else if (layoutRule.contains("v")) {
                lineWidth = Math.max(lineWidth, subLineWidth);
                lineHeight += subLineHeight;
            }

            childMaxWidth = Math.max(lineWidth, childMaxWidth);
            childMaxHeight = Math.max(lineHeight, childMaxHeight);
        }

        if (widthMode != MeasureSpec.EXACTLY) {
            viewWidth = childMaxWidth + paddingLeft + paddingRight;
        }

        if (heightMode != MeasureSpec.EXACTLY) {
            viewHeight = childMaxHeight + paddingTop + paddingBottom;
        }

        setMeasuredDimension(viewWidth, viewHeight);
    }

    /*将具有相同规则child分类到一起*/
    private void prepareChildren() {
//        for (ArrayList<View> rule : mRuleViews) {
//            rule.clear();
//        }
//        mRuleViews.clear();

        mRuleMap.clear();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
            if (TextUtils.isEmpty(layoutParams.layoutRule)) {
                layoutParams.layoutRule = "f";
            }

            if (layoutParams.layoutRule.contains("f")) {
                ArrayList<View> views = new ArrayList<>();
                views.add(child);
                mRuleMap.put("f" + i, views);
            } else if (layoutParams.layoutRule.contains("l")) {
                String[] split = layoutParams.layoutRule.split(" ");
                for (String sp : split) {
                    ArrayList<View> views = mRuleMap.get(sp);
                    if (RUtils.isListEmpty(views)) {
                        views = new ArrayList<>();
                        mRuleMap.put(sp, views);
                    }
                    views.add(child);
                }
            } else {
                throw new IllegalArgumentException("无法识别的布局规则(未包含f l):" + layoutParams.layoutRule);
            }
        }

//        for (Map.Entry<String, ArrayList<View>> enter : mRuleMap.entrySet()) {
//            mRuleViews.add(enter.getValue());
//        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        final Rect childRect = acquireTempRect();

        //用来保存 具有多个 规则child的坐标, 下次布局, 需要从这个坐标开始
        final Rect firstChildRect = acquireTempRect();

        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        final int paddingRight = getPaddingRight();
        final int paddingBottom = getPaddingBottom();

        for (Map.Entry<String, ArrayList<View>> enter : mRuleMap.entrySet()) {
            //当前布局的规则
            String layoutRule = enter.getKey();
            //规则中, 包含多少个child
            ArrayList<View> ruleChild = enter.getValue();

            boolean isFirst = true;

            childRect.setEmpty();
            firstChildRect.setEmpty();

            for (int i = 0; i < ruleChild.size(); i++) {

                final View child = ruleChild.get(i);
                if (child.getVisibility() == GONE) {
                    // If the child is GONE, skip...
                    continue;
                }

                final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                if (layoutRule.contains("f")) {
                    //帧布局规则

                    childRect.left = paddingLeft + lp.leftMargin;
                    childRect.top = paddingTop + lp.topMargin;
                    childRect.right = childRect.left + child.getMeasuredWidth();
                    childRect.bottom = childRect.top + child.getMeasuredHeight();

                    if (ExKt.have(lp.layoutGravity, LayoutParams.RIGHT)) {
                        childRect.right = getMeasuredWidth() - paddingRight - lp.rightMargin;
                        childRect.left = childRect.right - child.getMeasuredWidth();
                    }
                    if (ExKt.have(lp.layoutGravity, LayoutParams.BOTTOM)) {
                        childRect.bottom = getMeasuredHeight() - paddingBottom - lp.bottomMargin;
                        childRect.top = childRect.bottom - child.getMeasuredHeight();
                    }
                    if (ExKt.have(lp.layoutGravity, LayoutParams.CENTER_HORIZONTAL)) {
                        childRect.left = getMeasuredWidth() / 2 - child.getMeasuredWidth() / 2 - lp.leftMargin;//支持leftMargin
                        childRect.right = getMeasuredWidth() / 2 + child.getMeasuredWidth() / 2 - lp.leftMargin;
                    }
                    if (ExKt.have(lp.layoutGravity, LayoutParams.CENTER_VERTICAL)) {
                        childRect.top = getMeasuredHeight() / 2 - child.getMeasuredHeight() / 2 - lp.topMargin;//支持topMargin
                        childRect.bottom = getMeasuredHeight() / 2 + child.getMeasuredHeight() / 2 - lp.topMargin;
                    }
                    if (ExKt.have(lp.layoutGravity, LayoutParams.CENTER)) {
                        childRect.top = getMeasuredHeight() / 2 - child.getMeasuredHeight() / 2;
                        childRect.bottom = getMeasuredHeight() / 2 + child.getMeasuredHeight() / 2;
                        childRect.left = getMeasuredWidth() / 2 - child.getMeasuredWidth() / 2;
                        childRect.right = getMeasuredWidth() / 2 + child.getMeasuredWidth() / 2;
                    }

                    child.layout(childRect.left, childRect.top, childRect.right, childRect.bottom);
                } else if (layoutRule.contains("l")) {
                    if (isFirst) {
                        firstChildRect.set(paddingLeft, paddingTop, paddingRight, paddingBottom);
                    }

                    childRect.left = firstChildRect.left;
                    childRect.top = firstChildRect.top;

                    if (lp.layoutRule.trim().startsWith(layoutRule)) {
                        //线性布局规则
                        if (layoutRule.contains("h")) {
                            //水平线性
                            if (!isFirst) {
                                childRect.left = childRect.right;
                            }
                        } else if (layoutRule.contains("v")) {
                            //垂直
                            if (!isFirst) {
                                childRect.top = childRect.bottom;
                            }
                        }

                        childRect.left += lp.leftMargin;
                        childRect.top += lp.topMargin;

                        childRect.right = childRect.left + child.getMeasuredWidth();
                        childRect.bottom = childRect.top + child.getMeasuredHeight();

                        child.layout(childRect.left, childRect.top, childRect.right, childRect.bottom);

                        childRect.right += lp.rightMargin;
                        childRect.bottom += lp.bottomMargin;

                    } else {
                        //这个View, 同时包含2个以上的规则, 比如: lh1 lv1

                        firstChildRect.set(child.getLeft() - lp.leftMargin, child.getTop() - lp.topMargin,
                                child.getRight() + lp.rightMargin, child.getBottom() + lp.bottomMargin);
                        childRect.set(firstChildRect);

//                        if (layoutRule.contains("h")) {
//                            //水平线性
//                            firstChildRect.left = firstChildRect.right;
//                        } else if (layoutRule.contains("v")) {
//                            //垂直
//                            firstChildRect.top = firstChildRect.bottom;
//                        }
                    }
                }
                isFirst = false;
            }
        }
        releaseTempRect(childRect);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public static class LayoutParams extends MarginLayoutParams {

        public static final int LEFT = 0x01;
        public static final int TOP = 0x02;
        public static final int RIGHT = 0x04;
        public static final int BOTTOM = 0x08;
        public static final int CENTER_VERTICAL = 0x10;
        public static final int CENTER_HORIZONTAL = 0x20;
        public static final int CENTER = 0x30;

        public String layoutRule = "f";//默认采用帧布局规则
        public int layoutGravity = 0;
        public float layoutWeight = -1f;

        public String layoutWidth;
        public String layoutHeight;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            final TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.RViewGroup_Layout);
            String rule = a.getString(R.styleable.RViewGroup_Layout_r_layout_rule);
            layoutGravity = a.getInt(R.styleable.RViewGroup_Layout_r_layout_gravity, layoutGravity);
            layoutWeight = a.getFloat(R.styleable.RViewGroup_Layout_r_layout_weight, layoutWeight);
            layoutWidth = a.getString(R.styleable.RViewGroup_Layout_r_layout_width);
            layoutHeight = a.getString(R.styleable.RViewGroup_Layout_r_layout_height);
            if (!TextUtils.isEmpty(rule)) {
                layoutRule = rule;
            }
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
}
