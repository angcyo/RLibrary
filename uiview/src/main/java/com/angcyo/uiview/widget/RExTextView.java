package com.angcyo.uiview.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
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
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.angcyo.library.utils.L;
import com.angcyo.uiview.R;
import com.angcyo.uiview.skin.SkinHelper;
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
    /**
     * 网址url正则
     */
    public final static Pattern patternUrl = Pattern.compile("(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.:+#]*[\\w\\-+#])?");
    /**
     * @成员,正则
     */
    public final static Pattern patternMention = Pattern.compile("<m id='(\\d+)'>([^<>]+)</m>");
    /**
     * 数字正则
     */
    public final static Pattern patternNumber = Pattern.compile("^\\d+$");

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
        //setMovementMethod(ImageClickMethod.getInstance());
    }

    @Override
    protected MovementMethod getDefaultMovementMethod() {
        return ImageClickMethod.getInstance();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (TextUtils.isEmpty(text)) {
            super.setText(text, type);
        } else {
            SpannableStringBuilder spanBuilder = new SpannableStringBuilder(text);
            patternUrl(spanBuilder, text);
            patternMention(spanBuilder, text);
            super.setText(spanBuilder, type);
        }
    }

    protected void patternUrl(SpannableStringBuilder builder, CharSequence input) {
        Matcher matcher = patternUrl.matcher(input);

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            CharSequence text = matcher.group();//input.subSequence(start, end);

            builder.setSpan(new ImageTextSpan(getContext(),
                            ImageTextSpan.initDrawable(getContext(), R.drawable.base_link_ico, getTextSize()), "网页链接", text.toString()),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    protected void patternMention(SpannableStringBuilder builder, CharSequence input) {
        //<m id='60763'>@爱你是一种习惯i<\/m> <m id='61145'>@爱情水深王八多<\/m> <m id='61536'>@爱苦、但亦甜<\/m>

        //String p ;//"<m id='\\d+'>\\w+</m>";
        Matcher matcher = patternMention.matcher(input);

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            builder.setSpan(new ImageTextSpan(getContext(), ImageTextSpan.initDrawable(getTextSize()), matcher.group(2), matcher.group(1)),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * 支持显示图片,支持显示文本, 支持点击的Span,
     * 需要配合 {@link ImageClickMethod} 才能实现点击
     */
    public static class ImageTextSpan extends ImageSpan {
        static float downX = -1, downY = -1;
        static boolean isTouchDown = false;
        private String mShowContent = "";//需要绘制的文本
        private Context mContext;
        private int mImageSize;//保存计算出来的图片宽度
        private int space;//文本与图片之间的距离
        private int textColor;//文本颜色
        private Rect tempRect = new Rect();
        private String url;//链接
        private Rect mTextBounds;
        private int mSpanWidth;

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
        public static Drawable initDrawable(Context context, @DrawableRes int resourceId, float textSize) {
            Drawable drawable = ContextCompat.getDrawable(context, resourceId);
            int height = drawable.getIntrinsicHeight();
            TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setTextSize(textSize);
            int textHeight = (int) RTextPaint.getTextHeight(textPaint);

            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), Math.max(height, textHeight));
            return drawable;
        }

        /**
         * 用来只显示文本的ImageSpan
         */
        public static Drawable initDrawable(float textSize) {
            Drawable drawable = new ColorDrawable(Color.WHITE);
            TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setTextSize(textSize);
            int textHeight = (int) RTextPaint.getTextHeight(textPaint);
            drawable.setBounds(0, 0, 0, textHeight);
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
            mSpanWidth = mImageSize + space + mTextBounds.width();
            return mSpanWidth;
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            tempRect.set((int) x, top, ((int) (x + mSpanWidth + space)), bottom);
            if (isTouchDown && tempRect.contains(((int) downX), (int) downY)) {
                paint.setColor(SkinHelper.getTranColor(textColor, 0x80));
                canvas.drawRect(tempRect, paint);
            }

            super.draw(canvas, text, start, end, x, top, y, bottom, paint);
            paint.setColor(textColor);//默认是黑色

            int height = bottom - top;//绘制区域的高度

            String string = mShowContent;
            int textHeight = (int) RTextPaint.getTextHeight(paint);

            //文本在图片的中间绘制
            canvas.drawText(string,
                    x + mImageSize + space,
                    top + textHeight / 4 + height / 2/* + paint.getFontMetricsInt().descent*/,
                    paint);
        }


        /**
         * 单击事件
         */
        public void onClick(TextView view) {
            L.e("call: onClick([view])-> " + url +
                    " isUrl:" + patternUrl.matcher(url).matches() +
                    " isNumber:" + patternNumber.matcher(url).matches());
        }

        public void onTouchUp(TextView view) {
            isTouchDown = false;
            downX = -1;
            downY = -1;
        }

        public void onTouchDown(TextView view, float x, float y) {
            isTouchDown = true;
            downX = x;
            downY = y;
        }

        public void onTouchCancel(TextView view, float x, float y) {
            onTouchUp(view);
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
                    action == MotionEvent.ACTION_DOWN ||
                    action == MotionEvent.ACTION_MOVE ||
                    action == MotionEvent.ACTION_CANCEL) {
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
                        link[0].onTouchUp(widget);
                        link[0].onClick(widget);
                    } else if (action == MotionEvent.ACTION_DOWN) {
                        link[0].onTouchDown(widget, event.getX(), event.getY());
                        Selection.setSelection(buffer,
                                buffer.getSpanStart(link[0]),
                                buffer.getSpanEnd(link[0]));
                    } else if (action == MotionEvent.ACTION_MOVE) {
                        //link[0].onTouchMove(widget, event.getX(), event.getY());
                    } else if (action == MotionEvent.ACTION_CANCEL) {
                        link[0].onTouchCancel(widget, event.getX(), event.getY());
                    }

                    return true;
                } else {
                    Selection.removeSelection(buffer);
                }
            }

            return super.onTouchEvent(widget, buffer, event);
        }
    }

    /**
     * 普通的文本点击span
     */
    @Deprecated
    public static class ClickableTextSpan extends ClickableSpan {

        String show, content;
        private int textColor;//文本颜色

        public ClickableTextSpan(String show, String content) {
            this.show = show;

            textColor = Color.parseColor("#507daf");//默认的文本颜色
        }

        @Override
        public void onClick(View widget) {
            L.i("onClick @: " + content);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            //设置背景色
            ds.bgColor = SkinHelper.getTranColor(textColor, 0x80);
            //设置前景色
            //ds.setColor(getResources().getColor(R.color.theme_color_accent));
            ds.setColor(textColor);
        }
    }
}
