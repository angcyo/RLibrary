package com.angcyo.uiview.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.OverScroller;

import com.angcyo.library.utils.L;
import com.angcyo.uiview.utils.ScreenUtil;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：让图片在当前可视区域平滑移动显示, 类似QQ资料页背景图片效果
 * 创建人员：Robi
 * 创建时间：2018/05/03 15:40
 * 修改人员：Robi
 * 修改时间：2018/05/03 15:40
 * 修改备注：
 * Version: 1.0.0
 */
public class FlatImageView extends AppCompatImageView {

    private OverScroller mOverScroller = new OverScroller(getContext(), new LinearInterpolator());

    //绘制时的值
    private float drawScrollX = 0f;
    private float drawScrollY = 0f;

    //目标滚动值
    private int targetScrollX = 0;
    private int targetScrollY = 0;

    public FlatImageView(Context context) {
        super(context);
    }

    public FlatImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlatImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mOverScroller.computeScrollOffset()) {
            L.e("call: computeScroll([])-> " + mOverScroller.getCurrX() + " : " + mOverScroller.getCurrY() + " : " + targetScrollX);

            postInvalidate();

            if (targetScrollX > 0) {
                if (mOverScroller.getCurrX() >= 0) {
                    forward();
                }
            } else if (targetScrollX < 0) {
                if (mOverScroller.getCurrX() <= targetScrollX) {
                    //反向滚动
                    backward();
                }
            }

        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        forward();
    }

    public void forward() {
        targetScrollX = getMeasuredWidth() - drawableWidth();
        targetScrollY = getMeasuredWidth() - drawableHeight();
        startScroller();
    }

    public void backward() {
        targetScrollX = drawableWidth() - getMeasuredWidth();
        targetScrollY = drawableHeight() - getMeasuredWidth();
        startScroller();
    }

    private void startScroller() {
        mOverScroller.startScroll(((int) drawScrollX), (int) drawScrollY, targetScrollX, targetScrollY, 5000);
        postInvalidate();
    }

    private int drawableHeight() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return 0;
        }
        return (int) (drawable.getIntrinsicHeight() * ScreenUtil.density());
    }

    private int drawableWidth() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return 0;
        }
        return (int) (drawable.getIntrinsicWidth() * ScreenUtil.density());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        canvas.save();
        canvas.translate(mOverScroller.getCurrX(), mOverScroller.getCurrY());
//        canvas.translate(200, 200);
        //super.onDraw(canvas);
        drawable.setBounds(0, 0,
                drawableWidth(), getMeasuredHeight() /*drawableHeight()*/);
        drawable.draw(canvas);
        canvas.restore();

        drawScrollX = mOverScroller.getCurrX();
        drawScrollY = mOverScroller.getCurrY();
    }
}
