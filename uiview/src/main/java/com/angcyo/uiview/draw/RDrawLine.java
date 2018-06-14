package com.angcyo.uiview.draw;

import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.angcyo.uiview.R;
import com.angcyo.uiview.utils.ScreenUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by angcyo on 2018/03/31 08:39
 */
public class RDrawLine extends BaseDraw {

    public static final int DRAW_NONE = 0;
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
    /**
     * 是否是虚线, 蚂蚁线
     */
    public boolean isDashLine = false;
    private Paint linePaint;

    public RDrawLine(View view, AttributeSet attr) {
        super(view, attr);
        initAttribute(attr);
    }

    @Override
    protected void initAttribute(AttributeSet attr) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attr, R.styleable.RDrawLine);

        drawLine = typedArray.getInt(R.styleable.RDrawLine_r_draw_line, drawLine);
        drawLineOffsetLeft = typedArray.getDimensionPixelOffset(R.styleable.RDrawLine_r_draw_line_offset_left, drawLineOffsetLeft);
        drawLineOffsetRight = typedArray.getDimensionPixelOffset(R.styleable.RDrawLine_r_draw_line_offset_right, drawLineOffsetRight);
        drawLineColor = typedArray.getColor(R.styleable.RDrawLine_r_draw_line_color, ContextCompat.getColor(getContext(), R.color.base_chat_bg_color));
        drawLineWidth = typedArray.getDimensionPixelOffset(R.styleable.RDrawLine_r_draw_line_width, (int) drawLineWidth);
        isDashLine = typedArray.getBoolean(R.styleable.RDrawLine_r_draw_dash_line, isDashLine);

        typedArray.recycle();
    }

    @Override
    public void onDraw(Canvas canvas) {
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

    public void setDrawLine(@DrawLine int drawLine) {
        this.drawLine = drawLine;
        mView.postInvalidate();
    }

    @IntDef({DRAW_NONE, DRAW_LINE_LEFT, DRAW_LINE_TOP, DRAW_LINE_RIGHT, DRAW_LINE_BOTTOM, DRAW_LINE_BOTTOM_TOP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DrawLine {

    }
}
