package com.angcyo.uiview.draw;

import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.angcyo.uiview.R;


/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/10/20
 */
public class RDrawText extends BaseDraw {
    public static final int GRAVITY_TOP = 1;
    public static final int GRAVITY_LEFT = 2;
    public static final int GRAVITY_RIGHT = 4;
    public static final int GRAVITY_BOTTOM = 8;
    public static final int GRAVITY_CENTER = 16;
    public static final int GRAVITY_CENTER_H = 32;
    public static final int GRAVITY_CENTER_V = 64;

    protected ColorStateList textColor;
    protected String drawText;
    protected int textSize, drawTextSize;

    protected int textGravity = GRAVITY_TOP | GRAVITY_LEFT;

    protected int textOffsetX, textOffsetY;

    public static boolean haveInt(int src, int i) {
        int maskSrc = src & 0xff;
        int maskI = i & 0xff;
        return (maskSrc & maskI) == maskI;
    }

    public RDrawText(View view, AttributeSet attr) {
        super(view, attr);
        initAttribute(attr);
    }

    /**
     * @return 返回文本 绘制 在 top 和 bottom 中间的 y坐标
     */
    public static int textDrawCenterY(Paint paint, int top, int bottom) {
        int height = bottom - top;
        int textHeight = (int) (paint.descent() - paint.ascent());
        return (int) (top + height / 2 + textHeight / 2 - paint.descent());
    }

    @Override
    protected void initAttribute(AttributeSet attr) {
        TypedArray array = obtainStyledAttributes(attr, R.styleable.RDrawText);
        textColor = array.getColorStateList(R.styleable.RDrawText_r_draw_text_color);
        drawText = array.getString(R.styleable.RDrawText_r_draw_text_string);
        textSize = array.getDimensionPixelOffset(R.styleable.RDrawText_r_draw_text_size, getResources().getDimensionPixelOffset(R.dimen.base_xhdpi_15));
        textOffsetX = array.getDimensionPixelOffset(R.styleable.RDrawText_r_draw_text_offset_x, textOffsetX);
        textOffsetY = array.getDimensionPixelOffset(R.styleable.RDrawText_r_draw_text_offset_y, textOffsetY);
        drawTextSize = array.getDimensionPixelOffset(R.styleable.RDrawText_r_draw_text_draw_size, textSize);
        textGravity = array.getInt(R.styleable.RDrawText_r_draw_text_gravity, textGravity);

        if (textColor == null) {
            textColor = ColorStateList.valueOf(getBaseColor());
        }

        array.recycle();

        mBasePaint.setTextSize(textSize);
    }

//    @Override
//    protected int getBaseColor() {
//        return getColor(R.color.common_color_333333);
//    }

    private int[] getCurState() {
        return new int[]{};
    }

    @Override
    public int[] measureDraw(int widthMeasureSpec, int heightMeasureSpec) {
        mBasePaint.setTextSize(textSize);
        return super.measureDraw(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public int measureDrawWidth() {
        return (int) mBasePaint.measureText(drawText);
    }

    public int getTextSize() {
        return textSize;
    }

    protected boolean isCenter() {
        return haveInt(textGravity, GRAVITY_CENTER);
    }

    protected int getTextDrawX() {
        if (haveInt(textGravity, GRAVITY_RIGHT)) {
            return getViewWidth() - getPaddingRight() - measureDrawWidth() - textOffsetX;
        }
        return getPaddingLeft() + textOffsetX;
    }

    protected int getTextDrawY() {
        if (haveInt(textGravity, GRAVITY_BOTTOM)) {
            return (int) (getViewHeight() - getPaddingBottom() - mBasePaint.descent() - textOffsetY);
        }
        return (int) (getPaddingTop() - mBasePaint.ascent()) + textOffsetY;
    }

    protected int getDrawTextColor() {
        return textColor.getColorForState(getCurState(), getBaseColor());
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (!TextUtils.isEmpty(drawText)) {
            mBasePaint.setTextSize(drawTextSize);
            mBasePaint.setColor(getDrawTextColor());
            canvas.drawText(drawText, getTextDrawX(), getTextDrawY(), mBasePaint);
        }
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        this.drawTextSize = textSize;
        postInvalidate();
    }

    public void setDrawTextSize(int drawTextSize) {
        this.drawTextSize = drawTextSize;
        postInvalidate();
    }

    public int getDrawTextSize() {
        return drawTextSize;
    }

    public void setTextColor(ColorStateList textColor) {
        this.textColor = textColor;
        postInvalidate();
    }

    public void setDrawText(String drawText) {
        this.drawText = drawText;
        postInvalidate();
    }

    public void setTextGravity(int textGravity) {
        this.textGravity = textGravity;
        postInvalidate();
    }

    public void setTextOffsetX(int textOffsetX) {
        this.textOffsetX = textOffsetX;
        postInvalidate();
    }

    public void setTextOffsetY(int textOffsetY) {
        this.textOffsetY = textOffsetY;
        postInvalidate();
    }
}
