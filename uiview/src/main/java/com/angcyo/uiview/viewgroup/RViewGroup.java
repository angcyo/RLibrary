package com.angcyo.uiview.viewgroup;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.Pools;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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
 * 修改备注：玩不动...日后再说
 * Version: 1.0.0
 */
@Deprecated
public class RViewGroup extends ViewGroup {

    private static final Pools.Pool<Rect> sRectPool = new Pools.SynchronizedPool<>(12);
    private final boolean mAllowInconsistentMeasurement;
    //    private ArrayList<ArrayList<View>> mRuleViews = new ArrayList<>();
    private ArrayMap<String, ArrayList<View>> mRuleMap = new ArrayMap<>();
    //保存用来需要计算剩余空间的child
    private ArrayList<View> mWeightViewsH = new ArrayList<>();
    private ArrayList<View> mWeightViewsV = new ArrayList<>();
    private int mTotalLength;

    public RViewGroup(Context context) {
        this(context, null);
    }

    public RViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final int version = context.getApplicationInfo().targetSdkVersion;
        mAllowInconsistentMeasurement = version <= Build.VERSION_CODES.M;
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

            if (layoutRule.contains("l")) {
                //线性布局规则
                if (layoutRule.contains("h")) {
                    //水平线性
                    measureHorizontal(ruleChild, widthMeasureSpec, heightMeasureSpec, -1f);
                } else if (layoutRule.contains("v")) {
                    //垂直
                    measureVertical(ruleChild, widthMeasureSpec, heightMeasureSpec, -1f);
                }
                continue;
            }


            for (int i = 0; i < ruleChild.size(); i++) {
                final View child = ruleChild.get(i);
                if (child.getVisibility() == GONE) {
                    // If the child is GONE, skip...
                    continue;
                }

                final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                if (!layoutRule.equals(lp.firstLayoutRule)) {
                    childWidth = child.getMeasuredWidth();
                    childHeight = child.getMeasuredHeight();

                    lineWidth += childWidth + lp.leftMargin + lp.rightMargin;
                    lineHeight += childHeight + lp.topMargin + lp.bottomMargin;
                    continue;
                }

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

            //水平, 计算剩余空间分配
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
                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec((int) ((widthSize - lineWidth - widthPadding - lp.leftMargin - lp.rightMargin) * scale), MeasureSpec.EXACTLY);

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

            //垂直, 剩余控件宽高计算
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

                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec((int) ((heightSize - lineHeight - heightPadding - lp.topMargin - lp.bottomMargin) * scale), MeasureSpec.EXACTLY);
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
                for (int j = 0; j < split.length; j++) {
                    String sp = split[j];
                    if (j == 0) {
                        layoutParams.firstLayoutRule = sp;
                    }
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
        releaseTempRect(firstChildRect);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    //测量竖直方向的child
    int[] measureVertical(ArrayList<View> children, int widthMeasureSpec, int heightMeasureSpec, float mWeightSum) {
        mTotalLength = 0;
        int maxWidth = 0;
        int childState = 0;
        int alternativeMaxWidth = 0;
        int weightedMaxWidth = 0;
        boolean allFillParent = true;
        float totalWeight = 0;

        final int count = children.size();

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        boolean matchWidth = false;
        boolean skippedMeasure = false;

        int largestChildHeight = Integer.MIN_VALUE;
        int consumedExcessSpace = 0;

        // See how tall everyone is. Also remember max width.
        for (int i = 0; i < count; ++i) {
            final View child = children.get(i);

            if (child.getVisibility() == View.GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            totalWeight += lp.layoutWeight;

            final boolean useExcessSpace = lp.height == 0 && lp.layoutWeight > 0;
            if (heightMode == MeasureSpec.EXACTLY && useExcessSpace) {
                // Optimization: don't bother measuring children who are only
                // laid out using excess space. These views will get measured
                // later if we have space to distribute.
                final int totalLength = mTotalLength;
                mTotalLength = Math.max(totalLength, totalLength + lp.topMargin + lp.bottomMargin);
                skippedMeasure = true;
            } else {
                if (useExcessSpace) {
                    // The heightMode is either UNSPECIFIED or AT_MOST, and
                    // this child is only laid out using excess space. Measure
                    // using WRAP_CONTENT so that we can find out the view's
                    // optimal height. We'll restore the original height of 0
                    // after measurement.
                    lp.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                }

                // Determine how big this child would like to be. If this or
                // previous children have given a weight, then we allow it to
                // use all available space (and we will shrink things later
                // if needed).
                final int usedHeight = totalWeight == 0 ? mTotalLength : 0;
                measureChildBeforeLayout(child, i, widthMeasureSpec, 0,
                        heightMeasureSpec, usedHeight);

                final int childHeight = child.getMeasuredHeight();
                if (useExcessSpace) {
                    // Restore the original height and record how much space
                    // we've allocated to excess-only children so that we can
                    // match the behavior of EXACTLY measurement.
                    lp.height = 0;
                    consumedExcessSpace += childHeight;
                }

                final int totalLength = mTotalLength;
                mTotalLength = Math.max(totalLength, totalLength + childHeight + lp.topMargin +
                        lp.bottomMargin + getNextLocationOffset(child));
            }

            boolean matchWidthLocally = false;
            if (widthMode != MeasureSpec.EXACTLY && lp.width == LinearLayout.LayoutParams.MATCH_PARENT) {
                // The width of the linear layout will scale, and at least one
                // child said it wanted to match our width. Set a flag
                // indicating that we need to remeasure at least that view when
                // we know our width.
                matchWidth = true;
                matchWidthLocally = true;
            }

            final int margin = lp.leftMargin + lp.rightMargin;
            final int measuredWidth = child.getMeasuredWidth() + margin;
            maxWidth = Math.max(maxWidth, measuredWidth);
            childState = combineMeasuredStates(childState, child.getMeasuredState());

            allFillParent = allFillParent && lp.width == LinearLayout.LayoutParams.MATCH_PARENT;
            if (lp.layoutWeight > 0) {
                /*
                 * Widths of weighted Views are bogus if we end up
                 * remeasuring, so keep them separate.
                 */
                weightedMaxWidth = Math.max(weightedMaxWidth,
                        matchWidthLocally ? margin : measuredWidth);
            } else {
                alternativeMaxWidth = Math.max(alternativeMaxWidth,
                        matchWidthLocally ? margin : measuredWidth);
            }

            i += getChildrenSkipCount(child, i);
        }

        // Add in our padding
        mTotalLength += getPaddingTop() + getPaddingBottom();

        int heightSize = mTotalLength;

        // Check against our minimum height
        heightSize = Math.max(heightSize, getSuggestedMinimumHeight());

        // Reconcile our calculated size with the heightMeasureSpec
        int heightSizeAndState = resolveSizeAndState(heightSize, heightMeasureSpec, 0);
        heightSize = heightSizeAndState & MEASURED_SIZE_MASK;
        // Either expand children with weight to take up available space or
        // shrink them if they extend beyond our current bounds. If we skipped
        // measurement on any children, we need to measure them now.
        int remainingExcess = heightSize - mTotalLength
                + (mAllowInconsistentMeasurement ? 0 : consumedExcessSpace);
        if (skippedMeasure || remainingExcess != 0 && totalWeight > 0.0f) {
            float remainingWeightSum = mWeightSum > 0.0f ? mWeightSum : totalWeight;

            mTotalLength = 0;

            for (int i = 0; i < count; ++i) {
                final View child = children.get(i);
                if (child == null || child.getVisibility() == View.GONE) {
                    continue;
                }

                final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) child.getLayoutParams();
                final float childWeight = lp.weight;
                if (childWeight > 0) {
                    final int share = (int) (childWeight * remainingExcess / remainingWeightSum);
                    remainingExcess -= share;
                    remainingWeightSum -= childWeight;

                    final int childHeight;
                    if (lp.height == 0 && (!mAllowInconsistentMeasurement
                            || heightMode == MeasureSpec.EXACTLY)) {
                        // This child needs to be laid out from scratch using
                        // only its share of excess space.
                        childHeight = share;
                    } else {
                        // This child had some intrinsic height to which we
                        // need to add its share of excess space.
                        childHeight = child.getMeasuredHeight() + share;
                    }

                    final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                            Math.max(0, childHeight), MeasureSpec.EXACTLY);
                    final int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                            getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin,
                            lp.width);
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

                    // Child may now not fit in vertical dimension.
                    childState = combineMeasuredStates(childState, child.getMeasuredState()
                            & (MEASURED_STATE_MASK >> MEASURED_HEIGHT_STATE_SHIFT));
                }

                final int margin = lp.leftMargin + lp.rightMargin;
                final int measuredWidth = child.getMeasuredWidth() + margin;
                maxWidth = Math.max(maxWidth, measuredWidth);

                boolean matchWidthLocally = widthMode != MeasureSpec.EXACTLY &&
                        lp.width == LinearLayout.LayoutParams.MATCH_PARENT;

                alternativeMaxWidth = Math.max(alternativeMaxWidth,
                        matchWidthLocally ? margin : measuredWidth);

                allFillParent = allFillParent && lp.width == LinearLayout.LayoutParams.MATCH_PARENT;

                final int totalLength = mTotalLength;
                mTotalLength = Math.max(totalLength, totalLength + child.getMeasuredHeight() +
                        lp.topMargin + lp.bottomMargin + getNextLocationOffset(child));
            }

            // Add in our padding
            mTotalLength += getPaddingTop() + getPaddingBottom();
            // TODO: Should we recompute the heightSpec based on the new total length?
        } else {
            alternativeMaxWidth = Math.max(alternativeMaxWidth,
                    weightedMaxWidth);
        }

        if (!allFillParent && widthMode != MeasureSpec.EXACTLY) {
            maxWidth = alternativeMaxWidth;
        }

        maxWidth += getPaddingLeft() + getPaddingRight();

        // Check against our minimum width
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        if (matchWidth) {
            forceUniformWidth(children, count, heightMeasureSpec);
        }

        return new int[]{resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                heightSizeAndState};
    }

    int[] measureHorizontal(ArrayList<View> children, int widthMeasureSpec, int heightMeasureSpec, float mWeightSum) {
        mTotalLength = 0;
        int maxHeight = 0;
        int childState = 0;
        int alternativeMaxHeight = 0;
        int weightedMaxHeight = 0;
        boolean allFillParent = true;
        float totalWeight = 0;

        final int count = children.size();

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        boolean matchHeight = false;
        boolean skippedMeasure = false;

        final boolean isExactly = widthMode == MeasureSpec.EXACTLY;

        int largestChildWidth = Integer.MIN_VALUE;
        int usedExcessSpace = 0;

        // See how wide everyone is. Also remember max height.
        for (int i = 0; i < count; ++i) {
            final View child = children.get(i);

            if (child.getVisibility() == GONE) {
                i += getChildrenSkipCount(child, i);
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            totalWeight += lp.layoutWeight;

            final boolean useExcessSpace = lp.width == 0 && lp.layoutWeight > 0;
            if (widthMode == MeasureSpec.EXACTLY && useExcessSpace) {
                // Optimization: don't bother measuring children who are only
                // laid out using excess space. These views will get measured
                // later if we have space to distribute.
                if (isExactly) {
                    mTotalLength += lp.leftMargin + lp.rightMargin;
                } else {
                    final int totalLength = mTotalLength;
                    mTotalLength = Math.max(totalLength, totalLength +
                            lp.leftMargin + lp.rightMargin);
                }

                skippedMeasure = true;
            } else {
                if (useExcessSpace) {
                    // The widthMode is either UNSPECIFIED or AT_MOST, and
                    // this child is only laid out using excess space. Measure
                    // using WRAP_CONTENT so that we can find out the view's
                    // optimal width. We'll restore the original width of 0
                    // after measurement.
                    lp.width = LayoutParams.WRAP_CONTENT;
                }

                // Determine how big this child would like to be. If this or
                // previous children have given a weight, then we allow it to
                // use all available space (and we will shrink things later
                // if needed).
                final int usedWidth = totalWeight == 0 ? mTotalLength : 0;
                measureChildBeforeLayout(child, i, widthMeasureSpec, usedWidth,
                        heightMeasureSpec, 0);

                final int childWidth = child.getMeasuredWidth();
                if (useExcessSpace) {
                    // Restore the original width and record how much space
                    // we've allocated to excess-only children so that we can
                    // match the behavior of EXACTLY measurement.
                    lp.width = 0;
                    usedExcessSpace += childWidth;
                }

                if (isExactly) {
                    mTotalLength += childWidth + lp.leftMargin + lp.rightMargin
                            + getNextLocationOffset(child);
                } else {
                    final int totalLength = mTotalLength;
                    mTotalLength = Math.max(totalLength, totalLength + childWidth + lp.leftMargin
                            + lp.rightMargin + getNextLocationOffset(child));
                }

            }

            boolean matchHeightLocally = false;
            if (heightMode != MeasureSpec.EXACTLY && lp.height == LayoutParams.MATCH_PARENT) {
                // The height of the linear layout will scale, and at least one
                // child said it wanted to match our height. Set a flag indicating that
                // we need to remeasure at least that view when we know our height.
                matchHeight = true;
                matchHeightLocally = true;
            }

            final int margin = lp.topMargin + lp.bottomMargin;
            final int childHeight = child.getMeasuredHeight() + margin;
            childState = combineMeasuredStates(childState, child.getMeasuredState());

            maxHeight = Math.max(maxHeight, childHeight);

            allFillParent = allFillParent && lp.height == LayoutParams.MATCH_PARENT;
            if (lp.layoutWeight > 0) {
                /*
                 * Heights of weighted Views are bogus if we end up
                 * remeasuring, so keep them separate.
                 */
                weightedMaxHeight = Math.max(weightedMaxHeight,
                        matchHeightLocally ? margin : childHeight);
            } else {
                alternativeMaxHeight = Math.max(alternativeMaxHeight,
                        matchHeightLocally ? margin : childHeight);
            }

            i += getChildrenSkipCount(child, i);
        }

        // Add in our padding
        mTotalLength += getPaddingLeft() + getPaddingRight();

        int widthSize = mTotalLength;

        // Check against our minimum width
        widthSize = Math.max(widthSize, getSuggestedMinimumWidth());

        // Reconcile our calculated size with the widthMeasureSpec
        int widthSizeAndState = resolveSizeAndState(widthSize, widthMeasureSpec, 0);
        widthSize = widthSizeAndState & MEASURED_SIZE_MASK;

        // Either expand children with weight to take up available space or
        // shrink them if they extend beyond our current bounds. If we skipped
        // measurement on any children, we need to measure them now.
        int remainingExcess = widthSize - mTotalLength
                + (mAllowInconsistentMeasurement ? 0 : usedExcessSpace);
        if (skippedMeasure || remainingExcess != 0 && totalWeight > 0.0f) {
            float remainingWeightSum = mWeightSum > 0.0f ? mWeightSum : totalWeight;

            maxHeight = -1;

            mTotalLength = 0;

            for (int i = 0; i < count; ++i) {
                final View child = children.get(i);
                if (child == null || child.getVisibility() == View.GONE) {
                    continue;
                }

                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                final float childWeight = lp.layoutWeight;
                if (childWeight > 0) {
                    final int share = (int) (childWeight * remainingExcess / remainingWeightSum);
                    remainingExcess -= share;
                    remainingWeightSum -= childWeight;

                    final int childWidth;
                    if (lp.width == 0 && (!mAllowInconsistentMeasurement
                            || widthMode == MeasureSpec.EXACTLY)) {
                        // This child needs to be laid out from scratch using
                        // only its share of excess space.
                        childWidth = share;
                    } else {
                        // This child had some intrinsic width to which we
                        // need to add its share of excess space.
                        childWidth = child.getMeasuredWidth() + share;
                    }

                    final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                            Math.max(0, childWidth), MeasureSpec.EXACTLY);
                    final int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                            getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin,
                            lp.height);
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

                    // Child may now not fit in horizontal dimension.
                    childState = combineMeasuredStates(childState,
                            child.getMeasuredState() & MEASURED_STATE_MASK);
                }

                if (isExactly) {
                    mTotalLength += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin +
                            getNextLocationOffset(child);
                } else {
                    final int totalLength = mTotalLength;
                    mTotalLength = Math.max(totalLength, totalLength + child.getMeasuredWidth() +
                            lp.leftMargin + lp.rightMargin + getNextLocationOffset(child));
                }

                boolean matchHeightLocally = heightMode != MeasureSpec.EXACTLY &&
                        lp.height == LayoutParams.MATCH_PARENT;

                final int margin = lp.topMargin + lp.bottomMargin;
                int childHeight = child.getMeasuredHeight() + margin;
                maxHeight = Math.max(maxHeight, childHeight);
                alternativeMaxHeight = Math.max(alternativeMaxHeight,
                        matchHeightLocally ? margin : childHeight);

                allFillParent = allFillParent && lp.height == LayoutParams.MATCH_PARENT;
            }

            // Add in our padding
            mTotalLength += getPaddingLeft() + getPaddingRight();
            // TODO: Should we update widthSize with the new total length?

        } else {
            alternativeMaxHeight = Math.max(alternativeMaxHeight, weightedMaxHeight);
        }

        if (!allFillParent && heightMode != MeasureSpec.EXACTLY) {
            maxHeight = alternativeMaxHeight;
        }

        maxHeight += getPaddingTop() + getPaddingBottom();

        // Check against our minimum height
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());

        if (matchHeight) {
            forceUniformHeight(children, count, widthMeasureSpec);
        }

        return new int[]{widthSizeAndState | (childState & MEASURED_STATE_MASK),
                resolveSizeAndState(maxHeight, heightMeasureSpec,
                        (childState << MEASURED_HEIGHT_STATE_SHIFT))};
    }

    int getChildrenSkipCount(View child, int index) {
        return 0;
    }

    void measureChildBeforeLayout(View child, int childIndex,
                                  int widthMeasureSpec, int totalWidth, int heightMeasureSpec,
                                  int totalHeight) {
        measureChildWithMargins(child, widthMeasureSpec, totalWidth,
                heightMeasureSpec, totalHeight);
    }

    int getNextLocationOffset(View child) {
        return 0;
    }

    private void forceUniformWidth(ArrayList<View> children, int count, int heightMeasureSpec) {
        // Pretend that the linear layout has an exact size.
        int uniformMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(),
                MeasureSpec.EXACTLY);
        for (int i = 0; i < count; ++i) {
            final View child = children.get(i);
            if (child != null && child.getVisibility() != GONE) {
                LayoutParams lp = ((LayoutParams) child.getLayoutParams());

                if (lp.width == LayoutParams.MATCH_PARENT) {
                    // Temporarily force children to reuse their old measured height
                    // FIXME: this may not be right for something like wrapping text?
                    int oldHeight = lp.height;
                    lp.height = child.getMeasuredHeight();

                    // Remeasue with new dimensions
                    measureChildWithMargins(child, uniformMeasureSpec, 0, heightMeasureSpec, 0);
                    lp.height = oldHeight;
                }
            }
        }
    }

    private void forceUniformHeight(ArrayList<View> children, int count, int widthMeasureSpec) {
        // Pretend that the linear layout has an exact size. This is the measured height of
        // ourselves. The measured height should be the max height of the children, changed
        // to accommodate the heightMeasureSpec from the parent
        int uniformMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(),
                MeasureSpec.EXACTLY);
        for (int i = 0; i < count; ++i) {
            final View child = children.get(i);
            if (child != null && child.getVisibility() != GONE) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();

                if (lp.height == LayoutParams.MATCH_PARENT) {
                    // Temporarily force children to reuse their old measured width
                    // FIXME: this may not be right for something like wrapping text?
                    int oldWidth = lp.width;
                    lp.width = child.getMeasuredWidth();

                    // Remeasure with new dimensions
                    measureChildWithMargins(child, widthMeasureSpec, 0, uniformMeasureSpec, 0);
                    lp.width = oldWidth;
                }
            }
        }
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

        String firstLayoutRule; //在测量时赋值, 当有多个规则时, 某些属性只能作用在第一个规则上

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
