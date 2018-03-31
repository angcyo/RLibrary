package com.angcyo.uiview.viewgroup;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import com.angcyo.uiview.utils.ScreenUtil;

/**
 * Created by angcyo on 2018/03/31 08:39
 */
public class RDrawLine {

    public int drawLine = 0;//不绘制线
    public int drawLineColor = 0;
    public int drawLineOffsetLeft = 0;//左偏移
    public int drawLineOffsetRight = 0;//右偏移
    public float drawLineWidth = 1 * ScreenUtil.density();
    private Paint linePaint;
    private View mView;

    public RDrawLine(View view) {
        mView = view;
    }

    public void draw(Canvas canvas) {
        if (drawLine > 0) {
            if (linePaint == null) {
                linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                linePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            }
            linePaint.setStrokeWidth(drawLineWidth);
            linePaint.setColor(drawLineColor);

            switch (drawLine) {
                case 1://L
                    //暂不支持
                    break;
                case 2://T
                    canvas.drawLine(drawLineOffsetLeft, drawLineWidth / 2,
                            mView.getMeasuredWidth() - drawLineOffsetRight, drawLineWidth / 2,
                            linePaint);
                    break;
                case 3://R
                    //暂不支持
                    break;
                case 4://B
                    canvas.drawLine(drawLineOffsetLeft, mView.getMeasuredHeight() - drawLineWidth / 2,
                            mView.getMeasuredWidth() - drawLineOffsetRight, mView.getMeasuredHeight() - drawLineWidth / 2,
                            linePaint);
                    break;
                case 5://B+T
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
