package com.angcyo.fragment.widget.layout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.angcyo.library.utils.L;
import com.angcyo.uiview.kotlin.ViewExKt;
import com.angcyo.uiview.resources.AnimUtil;
import com.angcyo.uiview.utils.RUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.angcyo.uiview.utils.ScreenUtil.density;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：可以用来像任务管理一样, 查看当前Activity中 有哪些Fragment
 * 创建人员：Robi
 * 创建时间：2018/07/17 11:55
 * 修改人员：Robi
 * 修改时间：2018/07/17 11:55
 * 修改备注：
 * Version: 1.0.0
 */
public class FragmentDebugLayout extends FragmentBackLayout {
    public static boolean showDebugLayout = L.LOG_DEBUG;
    boolean isInDebugLayout = false;
    int hSpace = (int) (30 * getResources().getDisplayMetrics().density);
    int vSpace = (int) (30 * getResources().getDisplayMetrics().density);
    int viewMaxHeight = 0; //debug模式下的成员变量
    Paint debugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private long firstDownTime = 0;
    private StringBuilder logBuilder = new StringBuilder();

    public FragmentDebugLayout(Context context) {
        super(context);
    }

    public FragmentDebugLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected FragmentManager getFragmentManager() {
        Context context = getContext();
        if (context instanceof FragmentActivity) {
            return ((FragmentActivity) context).getSupportFragmentManager();
        }
        return null;
    }

    @Override
    protected boolean canTryCaptureView(View child) {
        if (isInDebugLayout) {
            return false;
        }
        return super.canTryCaptureView(child);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int actionMasked = ev.getActionMasked();

        if (handleDebugLayout(ev)) {
            return true;
        }

        if (isInDebugLayout) {
            return true;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        handleDebugLayout(event);
        if (isInDebugLayout) {
            getOrientationGestureDetector().onTouchEvent(event);
        } else {
            super.onTouchEvent(event);
        }
        return true;
    }

    /**
     * 多点按下, 是否处理
     */
    protected boolean handleDebugLayout(MotionEvent ev) {
        int actionMasked = ev.getActionMasked();
        long downTime = ev.getDownTime();
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            firstDownTime = downTime;
        }

        if (L.LOG_DEBUG &&
                showDebugLayout &&
                actionMasked == MotionEvent.ACTION_POINTER_DOWN &&
                ev.getPointerCount() == 6) {

            if (ev.getEventTime() - firstDownTime < 500) {
                //快速三指按下才受理操作

                //debug模式下, 三指按下
                if (isInDebugLayout) {
                    closeDebugLayout();
                } else {
                    startDebugLayout();
                }
                return true;
            }
        }
        return false;
    }

    public void startDebugLayout() {
        if (!isInDebugLayout) {
            isInDebugLayout = true;
            getOverScroller().abortAnimation();
            requestLayout();
            for (int i = 0; i < getChildCount(); i++) {
                View childAt = getChildAt(i);
                //childAt.startAnimation(AnimationUtils.loadAnimation(mLayoutActivity, R.anim.base_scale_to_min));
                AnimUtil.scaleBounceView(childAt, getDebugWidthSize() * 1f / getMeasuredWidth(), getDebugHeightSize() * 1f / getMeasuredHeight());
            }
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollTo(0, Integer.MAX_VALUE);//滚动到最后一个IView
                }
            }, 16);
        }
    }

    public void closeDebugLayout() {
        if (isInDebugLayout) {
            isInDebugLayout = false;
            getOverScroller().abortAnimation();
            scrollTo(0, 0);//恢复滚动坐标
            requestLayout();
            for (int i = 0; i < getChildCount(); i++) {
                View childAt = getChildAt(i);
                //childAt.startAnimation(AnimationUtils.loadAnimation(mLayoutActivity, R.anim.base_scale_to_max));
                AnimUtil.scaleBounceView(childAt);
            }
        }
    }

    private int getDebugWidthSize() {
        return getMeasuredWidth() - 2 * hSpace;
    }

    private int getDebugHeightSize() {
        return getMeasuredHeight() - 4 * vSpace;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //of java
        int widthSize = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        //of kotlin
//        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
//        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
//        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
//        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        int count = getChildCount();
        if (isInDebugLayout) {
            //int hCount = count > 9 ? 4 : (count > 6 ? 3 : 2);//横向放3个
            //int vCount = (int) Math.max(2, Math.ceil(count * 1f / hCount));//竖向至少2行

            //int wSize = (getMeasuredWidth() - (hCount + 1) * hSpace) / hCount;
            //int hSize = (getMeasuredHeight() - (vCount + 1) * vSpace) / vCount;
            int wSize = widthSize;//getDebugWidthSize();
            int hSize = heightSize;//getDebugHeightSize();

            for (int i = 0; i < count; i++) {
                View childAt = getChildAt(i);
                childAt.setVisibility(VISIBLE);
                childAt.measure(ViewExKt.exactlyMeasure(this, wSize), ViewExKt.exactlyMeasure(this, hSize));
            }

            setMeasuredDimension(widthSize, heightSize);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        //L.e("debug layout 1 " + isInDebugLayout + " " + getScrollX() + " " + getScrollY());
        if (isInDebugLayout) {
            int count = getChildCount();

//            int l = hSpace;
//            int t = vSpace;

            int l = getPaddingLeft();
            int t = -vSpace + getPaddingTop();

            int wSize = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();//getDebugWidthSize();
            int hSize = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();//getDebugHeightSize();

            for (int i = 0; i < count; i++) {
                View childAt = getChildAt(i);
                childAt.layout(l, t, l + wSize, t + hSize);
                t += getDebugHeightSize() + vSpace;
//                t += hSize + vSpace;
            }
//            viewMaxHeight = t;
            viewMaxHeight = t + 2 * vSpace;
            return;
        }

        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public void scrollTo(int x, int y) {
        int maxScrollY = viewMaxHeight - getMeasuredHeight();
        if (y > maxScrollY) {
            y = maxScrollY;
        }
        if (y < 0) {
            y = 0;
        }
        super.scrollTo(x, y);
    }

    @Override
    public void onFlingChange(@NotNull ORIENTATION orientation, float velocity) {
        super.onFlingChange(orientation, velocity);
        if (isInDebugLayout && isVertical(orientation)) {
            if (velocity > 1000) {
                //快速向下滑动
                startFlingY(-(int) velocity, getScrollY());
            } else if (velocity < -1000) {
                //快速向上滑动
                startFlingY(-(int) velocity, viewMaxHeight);
            }
        }
    }

    private void initDebugPaint() {
        debugPaint.setStrokeJoin(Paint.Join.ROUND);
        debugPaint.setStyle(Paint.Style.STROKE);
        debugPaint.setStrokeCap(Paint.Cap.ROUND);
        debugPaint.setTextSize(14 * getResources().getDisplayMetrics().density);
        debugPaint.setColor(Color.WHITE);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        /*调试模式绘制*/
        if (isInDebugLayout) {
            initDebugPaint();
            int childCount = getChildCount();

            int l = hSpace;
            int t = vSpace;

            int wSize = getDebugWidthSize();
            int hSize = getDebugHeightSize();

            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null) {
                List<Fragment> fragments = fragmentManager.getFragments();

                for (int i = 0; i < childCount; i++) {
                    View childAt = getChildAt(i);
                    Fragment fragment = fragments.get(i);

                    logBuilder.replace(0, logBuilder.length(), "");

                    if (childAt == fragment.getView()) {
                        logBuilder.append(RUtils.getClassSimpleName(fragment.getClass()));
                        logBuilder.append(":");
                        logBuilder.append(fragment.hashCode());
                        logBuilder.append(" R:");
                        logBuilder.append(fragment.isResumed());
                        logBuilder.append(" H:");
                        logBuilder.append(fragment.isHidden());
                        logBuilder.append(" V:");
                        logBuilder.append(fragment.isVisible());
                        logBuilder.append(" UV:");
                        logBuilder.append(fragment.getUserVisibleHint());
                    } else {
                        logBuilder.append("未知:");
                        logBuilder.append(RUtils.getClassSimpleName(fragment.getClass()));
                    }
                    float textHeight = ViewExKt.textHeight(this, debugPaint);

                    float dp2 = 2 * density();
                    debugPaint.setShadowLayer(dp2, dp2, dp2, Color.BLACK);

                    canvas.drawText(logBuilder.toString(), hSpace, t + textHeight, debugPaint);

                    t += hSize + vSpace;
                }

            }
        }
    }

    @Override
    public void onScrollChange(@NotNull ORIENTATION orientation, float distance) {
        super.onScrollChange(orientation, distance);
        if (isInDebugLayout && isVertical(orientation)) {
            scrollBy(0, (int) distance);
        }
    }

    public boolean isInDebugLayout() {
        return isInDebugLayout;
    }
}
