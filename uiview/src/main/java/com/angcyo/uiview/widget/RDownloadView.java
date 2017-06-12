package com.angcyo.uiview.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.angcyo.uiview.R;
import com.angcyo.uiview.skin.SkinHelper;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：可以显示3种状态的ImageView (下载, 下载中(包括进度), 下载完成)
 * 创建人员：Robi
 * 创建时间：2017/05/04 17:11
 * 修改人员：Robi
 * 修改时间：2017/05/04 17:11
 * 修改备注：
 * Version: 1.0.0
 */
public class RDownloadView extends AppCompatImageView implements Runnable {
    public static final int DELAY_MILLIS = 40;//24帧绘制
    Drawable normalDrawable, finishDrawable;
    DownloadState mDownloadState = DownloadState.NORMAL;

    Paint mPaint;
    /**
     * 进度模式下, 当前的进度
     */
    int curProgress;
    RectF oval;
    /**
     * 不明确的进度
     */
    boolean indeterminate = true;
    private float mStrokeWidth;

    private int startAngle = -90;

    public RDownloadView(Context context) {
        super(context);
        initView();
    }

    public RDownloadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public RDownloadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        normalDrawable = ContextCompat.getDrawable(getContext(), R.drawable.base_icon_download);
        finishDrawable = ContextCompat.getDrawable(getContext(), R.drawable.base_icon_yixiazai);
        finishDrawable.mutate().setColorFilter(SkinHelper.getSkin().getThemeSubColor(), PorterDuff.Mode.MULTIPLY);

        normalDrawable.setBounds(0, 0, normalDrawable.getIntrinsicWidth(), normalDrawable.getIntrinsicHeight());
        finishDrawable.setBounds(0, 0, normalDrawable.getIntrinsicWidth(), normalDrawable.getIntrinsicHeight());

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStrokeWidth = 2 * getResources().getDisplayMetrics().density;
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);//设置笔触的模式
//        mPaint.setStrokeJoin(Paint.Join.ROUND);//设置连接模式, 在绘制矩形各个角的时候有效果

        oval = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST || widthMode== MeasureSpec.UNSPECIFIED) {
            widthSize = Math.max(normalDrawable.getIntrinsicWidth(),
                    finishDrawable.getIntrinsicWidth()) + getPaddingLeft() + getPaddingRight();
        }

        if (heightMode == MeasureSpec.AT_MOST || heightMode== MeasureSpec.UNSPECIFIED) {
            heightSize = Math.max(normalDrawable.getIntrinsicHeight(),
                    finishDrawable.getIntrinsicHeight()) + getPaddingBottom() + getPaddingTop();
        }

        int size = Math.max(widthSize, heightSize);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        switch (mDownloadState) {
            case FINISH:
                drawDrawable(canvas, finishDrawable);
                break;
            case DOWNING:
                drawProgress(canvas);
                break;
            default:
                drawDrawable(canvas, normalDrawable);
                break;
        }
    }

    private void drawProgress(Canvas canvas) {
        int width2 = getMeasuredWidth() / 2;
        int height2 = getMeasuredHeight() / 2;

        int r = Math.max(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                getMeasuredHeight() - getPaddingBottom() - getPaddingTop()) / 2;

        canvas.save();
        canvas.translate(width2, height2);
        //绘制大圈
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.base_text_color_dark));
        canvas.drawCircle(0, 0, r, mPaint);

        //绘制进度圈
        mPaint.setColor(SkinHelper.getSkin().getThemeSubColor());
        oval.set(-r, -r, r, r);
        if (indeterminate) {
            //进度不确定
            canvas.drawArc(oval, startAngle % 360, 90, false, mPaint);
        } else {
            float angel = calcAngel();
            canvas.drawArc(oval, -90, angel, false, mPaint);
            if (angel >= 360) {
                setDownloadState(DownloadState.FINISH);
            }
        }

        canvas.restore();
    }

    private float calcAngel() {
        return curProgress * 360f / 100;
    }

    private void drawDrawable(Canvas canvas, Drawable drawable) {
        removeCallbacks(this);
        canvas.save();
        canvas.translate(getMeasuredWidth() / 2 - drawable.getIntrinsicWidth() / 2,
                getMeasuredHeight() / 2 - drawable.getIntrinsicHeight() / 2);
        drawable.draw(canvas);
        canvas.restore();
    }

    /**
     * 设置视图显示的状态
     */
    public void setDownloadState(DownloadState downloadState) {
        mDownloadState = downloadState;
        postInvalidate();
        if (indeterminate) {
            removeCallbacks(this);
            postDelayed(this, DELAY_MILLIS);
        }
    }

    @Override
    public void run() {
        if (indeterminate) {
            startAngle += 10;
        } else {
            curProgress++;
        }
        postInvalidate();
        postDelayed(this, DELAY_MILLIS);//60帧绘制
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAngle = -90;
        curProgress = 0;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this);
    }

    /**
     * @param curProgress 从0-100的进度
     */
    public void setCurProgress(int curProgress) {
        this.curProgress = curProgress;
        postInvalidate();
    }

    public enum DownloadState {
        NORMAL, DOWNING, FINISH
    }

}
