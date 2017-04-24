package com.angcyo.uiview.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import com.angcyo.library.utils.L;
import com.angcyo.uiview.R;
import com.angcyo.uiview.utils.RTextPaint;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：支持显示@显示, 支持显示 带logo的网页链接, 支持显示表情
 * 创建人员：Robi
 * 创建时间：2017/04/24 15:48
 * 修改人员：Robi
 * 修改时间：2017/04/24 15:48
 * 修改备注：
 * Version: 1.0.0
 */
public class RExTextView extends RTextView {
    public RExTextView(Context context) {
        super(context);
    }

    public RExTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RExTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setMovementMethod(ImageClickMethod.getInstance());
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        patternUrl(text, type);
    }

    protected void patternUrl(CharSequence input, BufferType type) {
        String p = "(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.:+#]*[\\w\\-+#])?";
        Pattern pattern = Pattern.compile(p);
        Matcher matcher = pattern.matcher(input);

        SpannableStringBuilder spanBuilder = new SpannableStringBuilder(getText());

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            CharSequence text = input.subSequence(start, end);
            L.e("call: patternUrl([input])-> " + start + " " + end + " :" + text);
//            spanBuilder.setSpan(new ImageTextSpan(getContext(), R.drawable.base_link_ico, "网页链接", text.toString()),
//                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            spanBuilder.setSpan(new ImageTextSpan(getContext(),
                            ImageTextSpan.initDrawable(getContext(), R.drawable.base_link_ico, getTextSize()), "网页链接", text.toString()),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        super.setText(spanBuilder, type);
    }

    /**
     * 支持显示图片,支持显示文本, 支持点击的Span,
     * 需要配合 {@link ImageClickMethod} 才能实现点击
     */
    public static class ImageTextSpan extends ImageSpan {
        private String mShowContent = "";//需要绘制的文本
        private Context mContext;
        private int mImageSize;//保存计算出来的图片宽度
        private int space;//文本与图片之间的距离
        private int textColor;//文本颜色
        private Rect tempRect = new Rect();
        private String url;//链接
        private Rect mTextBounds;

        public ImageTextSpan(Context context, Drawable d, String showContent, String url) {
            super(d, ALIGN_BASELINE);
            this.url = url;
            mShowContent = showContent;
            init(context);
        }

        public ImageTextSpan(Context context, @DrawableRes int resourceId, String show, String url) {
            super(context, resourceId, ALIGN_BASELINE);
            this.mShowContent = show;
            this.url = url;

            init(context);
        }

        /**
         * 根据文本大小, 自动设置图片的高度
         */
        public static BitmapDrawable initDrawable(Context context, @DrawableRes int resourceId, float textSize) {
            BitmapDrawable drawable = (BitmapDrawable) ContextCompat.getDrawable(context, resourceId);
            int height = drawable.getIntrinsicHeight();
            TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setTextSize(textSize);
            int textHeight = (int) RTextPaint.getTextHeight(textPaint);

            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), Math.max(height, textHeight));
            return drawable;
        }

        private void init(Context context) {
            mContext = context;
            space = (int) (2 * mContext.getResources().getDisplayMetrics().density);

            textColor = Color.parseColor("#507daf");//默认的文本颜色
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            String string = mShowContent;
            mTextBounds = getTextBounds(paint, string);

            mImageSize = super.getSize(paint, text, start, end, fm);
            return mImageSize + space + mTextBounds.width();
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            L.e("call: updateDrawState([ds])-> ");
        }

        @Override
        public void updateMeasureState(TextPaint p) {
            super.updateMeasureState(p);
            L.e("call: updateMeasureState([p])-> ");
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            super.draw(canvas, text, start, end, x, top, y, bottom, paint);
            paint.setColor(textColor);//默认是黑色

            int height = bottom - top;//绘制区域的高度

            String string = mShowContent;
            int textHeight = (int) RTextPaint.getTextHeight(paint);

            //文本在图片的中间绘制
            canvas.drawText(string,
                    x + mImageSize + space,
                    top + textHeight / 4 + height / 2/* - paint.getFontMetricsInt().descent*/,
                    paint);
        }

        /**
         * 单击事件
         */
        public void onClick(TextView view) {
            L.e("call: onClick([view])-> " + url);
        }

        public Rect getTextBounds(Paint paint, String text) {
            tempRect.set(0, 0, 0, 0);
            if (TextUtils.isEmpty(text)) {
                return tempRect;
            }
            paint.getTextBounds(text, 0, text.length(), tempRect);
            return tempRect;
        }

        @Override
        public String getSource() {
            return mShowContent;
        }
    }

    public static class ImageClickMethod extends LinkMovementMethod {

        private static ImageClickMethod sInstance;

        public static ImageClickMethod getInstance() {
            if (sInstance == null)
                sInstance = new ImageClickMethod();

            return sInstance;
        }

        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            int action = event.getAction();

            if (action == MotionEvent.ACTION_UP ||
                    action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                ImageTextSpan[] link = buffer.getSpans(off, off, ImageTextSpan.class);

                if (link.length != 0) {
                    if (action == MotionEvent.ACTION_UP) {
                        link[0].onClick(widget);
                    } else if (action == MotionEvent.ACTION_DOWN) {
                        Selection.setSelection(buffer,
                                buffer.getSpanStart(link[0]),
                                buffer.getSpanEnd(link[0]));
                    }

                    return true;
                } else {
                    Selection.removeSelection(buffer);
                }
            }

            return super.onTouchEvent(widget, buffer, event);
        }
    }


}
