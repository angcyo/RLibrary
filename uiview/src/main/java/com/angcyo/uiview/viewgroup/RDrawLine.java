package com.angcyo.uiview.viewgroup;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.view.View;

import com.angcyo.uiview.utils.ScreenUtil;

/**
 * Created by angcyo on 2018/03/31 08:39
 */
public class RDrawLine {

    public static final int DRAW_LINE_LEFT = 1;
    public static final int DRAW_LINE_TOP = 2;
    public static final int DRAW_LINE_RIGHT = 3;
    public static final int DRAW_LINE_BOTTOM = 4;
    public static final int DRAW_LINE_BOTTOM_TOP = 5;

    public int drawLine = 0;//不绘制线
    public int drawLineColor = 0;
    public int drawLineOffsetLeft = 0;//左偏移
    public int drawLineOffsetRight = 0;//右偏移
    public float drawLineWidth = 1 * ScreenUtil.density();
    private Paint linePaint;
    private View mView;

    /**
     * 是否是虚线, 蚂蚁线
     */
    public boolean isDashLine = false;

    public RDrawLine(View view) {
        mView = view;
    }

    public void draw(Canvas canvas) {
        if (drawLine > 0) {
            if (linePaint == null) {
                linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                linePaint.setStyle(Paint.Style.FILL_AND_STROKE);

                if (isDashLine) {
                    mView.setLayerType(View.LAYER_TYPE_SOFTWARE, linePaint);
                    linePaint.setPathEffect(new DashPathEffect(new float[]{4 * ScreenUtil.density, 5 * ScreenUtil.density}, 0));
                }
            }

            linePaint.setStrokeWidth(drawLineWidth);
            linePaint.setColor(drawLineColor);

            switch (drawLine) {
                case DRAW_LINE_LEFT://L
                    //暂不支持
                    break;
                case DRAW_LINE_TOP://T
                    canvas.drawLine(drawLineOffsetLeft, drawLineWidth / 2,
                            mView.getMeasuredWidth() - drawLineOffsetRight, drawLineWidth / 2,
                            linePaint);
                    break;
                case DRAW_LINE_RIGHT://R
                    //暂不支持
                    break;
                case DRAW_LINE_BOTTOM://B
                    canvas.drawLine(drawLineOffsetLeft, mView.getMeasuredHeight() - drawLineWidth / 2,
                            mView.getMeasuredWidth() - drawLineOffsetRight, mView.getMeasuredHeight() - drawLineWidth / 2,
                            linePaint);
                    break;
                case DRAW_LINE_BOTTOM_TOP://B+T
                    canvas.drawLine(drawLineOffsetLeft, drawLineWidth / 2,
                            mView.getMeasuredWidth() - drawLineOffsetRight, drawLineWidth / 2,
                            linePaint);
                    canvas.drawLine(drawLineOffsetLeft, mView.getMeasuredHeight() - drawLineWidth / 2,
                            mView.getMeasuredWidth() - drawLineOffsetRight, mView.getMeasuredHeight() - drawLineWidth / 2,
                            linePaint);
                    break;
            }
        }
    }


}
