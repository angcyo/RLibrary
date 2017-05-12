package com.angcyo.uiview.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Patterns;
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
 * 类的描述：支持显示@显示, 支持显示 带logo的网页链接, 支持显示表情, 支持折叠显示.
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
    public final static Pattern patternUrl = Patterns.WEB_URL;//Pattern.compile("(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.:+#]*[\\w\\-+#])?");
    /**
     * @成员,正则
     */
    public final static Pattern patternMention = Pattern.compile("<m id='(\\d+)'>([^<>]+)</m>");
    /**
     * 数字正则
     */
    public final static Pattern patternNumber = Pattern.compile("^\\d+$");

    /**
     * 电话号码正则
     */
    public final static Pattern patternPhone = Pattern.compile("\\d{3}-\\d{8}|\\d{3}-\\d{7}|\\d{4}-\\d{8}|\\d{4}-\\d{7}|1+[34578]+\\d{9}|\\d{8}|\\d{7}");

    protected ImageTextSpan.OnImageSpanClick mOnImageSpanClick;

    private int maxShowLine = -1;//最大显示多少行, 当超过时, 会显示...全部

    private String foldString;

    private int mImageSpanTextColor = ImageTextSpan.getDefaultColor();

    public RExTextView(Context context) {
        super(context);
    }

    public RExTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RExTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setImageSpanTextColor(int imageSpanTextColor) {
        mImageSpanTextColor = imageSpanTextColor;
    }

    @Override
    protected void initView() {
        super.initView();
        foldString = getResources().getString(R.string.see_all);
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
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return ImageClickMethod.isTouchInSpan;
    }

    /**
     * 设置允许显示的最大行数
     */
    public void setMaxShowLine(int maxShowLine) {
        this.maxShowLine = maxShowLine;
        if (maxShowLine < 0) {
            setMaxLines(Integer.MAX_VALUE);
        } else {
            setEllipsize(TextUtils.TruncateAt.END);
            setMaxLines(maxShowLine);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        Layout layout = getLayout();
//        if (layout != null) {
//            int lines = layout.getLineCount();
//            L.e("call: onDraw([canvas])-> line Count:" + lines);
//            if (lines > 0) {
//                //返回折叠的字符数
//                if (layout.getEllipsisCount(lines - 1) > 0) {
//                    L.e("call: onDraw([canvas])-> getEllipsisCount:");
//                }
//            }
//        }
    }

    public void setOnImageSpanClick(ImageTextSpan.OnImageSpanClick onImageSpanClick) {
        mOnImageSpanClick = onImageSpanClick;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (TextUtils.isEmpty(text)) {
            super.setText(text, type);
        } else {
            SpannableStringBuilder spanBuilder = new SpannableStringBuilder(text);
            patternPhone(spanBuilder, text);
            patternMention(spanBuilder, text);
            patternUrl(spanBuilder, text);
            afterPattern(spanBuilder, text);
            super.setText(spanBuilder, type);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Layout layout = getLayout();
        if (maxShowLine > 0 && layout != null) {
            int lines = layout.getLineCount();
            if (lines > 0) {
                if (lines > maxShowLine) {
                    //需要折叠
                    CharSequence sequence = getText();
                    if (sequence instanceof Spannable) {
                        Spannable spannable = (Spannable) sequence;
                        int lineStart = layout.getLineStart(maxShowLine);

                        String temp = "...";
                        String foldString = getFoldString();
                        int startPosition = lineStart - temp.length() - foldString.length();
                        int start = findStartPosition(spannable, startPosition);

                        int offset = (sequence.length() % 2 == 0) ? 4 : 3;

                        spannable.setSpan(new ImageTextSpan(getContext(), getTextSize(), getCurrentTextColor(), temp),
                                start, start + offset, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spannable.setSpan(new ImageTextSpan(getContext(), getTextSize(), foldString),
                                start + offset, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                }
            }
        }
    }

    private int findStartPosition(Spannable spannable, int startWidthPosition) {
        CharacterStyle[] oldSpans = spannable.getSpans(startWidthPosition, spannable.length(), CharacterStyle.class);
        int position = startWidthPosition;
        for (CharacterStyle oldSpan : oldSpans) {
            int spanStart = spannable.getSpanStart(oldSpan);
            int spanEnd = spannable.getSpanEnd(oldSpan);
            if (spanStart <= startWidthPosition && spanEnd > startWidthPosition) {
                position = spanStart;
            }
            if (spanEnd >= startWidthPosition) {
                spannable.removeSpan(oldSpan);
            }
        }
        return position;
    }

    private String getFoldString() {
        return foldString;
    }

    public void setFoldString(String foldString) {
        this.foldString = foldString;
    }

    /**
     * 子类处理
     */
    protected void afterPattern(SpannableStringBuilder spanBuilder, CharSequence text) {

    }

    /**
     * 匹配Url链接
     */
    protected void patternUrl(SpannableStringBuilder builder, CharSequence input) {
        Matcher matcher = patternUrl.matcher(input);

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            CharSequence text = matcher.group();//input.subSequence(start, end);

            builder.setSpan(new ImageTextSpan(getContext(),
                            ImageTextSpan.initDrawable(getContext(),
                                    R.drawable.base_link_ico, getTextSize()),
                            getContext().getString(R.string.url_link_tip),
                            text.toString())
                            .setOnImageSpanClick(mOnImageSpanClick)
                            .setTextColor(mImageSpanTextColor),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * 匹配@联系人
     */
    protected void patternMention(SpannableStringBuilder builder, CharSequence input) {
        //<m id='60763'>@爱你是一种习惯i<\/m> <m id='61145'>@爱情水深王八多<\/m> <m id='61536'>@爱苦、但亦甜<\/m>

        //String p ;//"<m id='\\d+'>\\w+</m>";
        Matcher matcher = patternMention.matcher(input);

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            builder.setSpan(new ImageTextSpan(getContext(), ImageTextSpan.initDrawable(getTextSize()), matcher.group(2), matcher.group(1))
                            .setOnImageSpanClick(mOnImageSpanClick)
                            .setTextColor(mImageSpanTextColor),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * 匹配 电话号码
     */
    protected void patternPhone(SpannableStringBuilder builder, CharSequence input) {
        Matcher matcher = patternPhone.matcher(input);

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            builder.setSpan(new ImageTextSpan(getContext(), ImageTextSpan.initDrawable(getTextSize()),
                            matcher.group(), matcher.group())
                            .setOnImageSpanClick(mOnImageSpanClick)
                            .setTextColor(mImageSpanTextColor),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * 支持只显示图片, 只显示文本, 支持图片混合显示, 支持点击事件.
     * 需要配合 {@link ImageClickMethod} 才能实现点击
     */
    public static class ImageTextSpan extends ImageSpan {
        static float downX = -1, downY = -1;
        static boolean isTouchDown = false;
        OnImageSpanClick mOnImageSpanClick;
        private String mShowContent = "";//需要绘制的文本
        private Context mContext;
        private int mImageSize;//保存计算出来的图片宽度
        private int space;//文本与图片之间的距离
        private int textColor;//文本颜色
        private Rect tempRect = new Rect();
        private String url;//链接
        private Rect mTextBounds;
        private int mSpanWidth;
        /**
         * 是否可以点击
         */
        private boolean canClick = true;

        /**
         * 是否激活点击效果
         */
        private boolean enableTouchEffect = false;

        /**
         * 构造一个只用来显示文本的ImageSpan
         */
        public ImageTextSpan(Context context, float textSize, String showContent) {
            this(context, textSize, -1, showContent);
        }

        public ImageTextSpan(Context context, float textSize, int textColor, String showContent) {
            super(initDrawable(textSize), ALIGN_BASELINE);
            this.mShowContent = showContent;
            init(context);

            setCanClick(false);
            if (textColor != -1) {
                setTextColor(textColor);
            }
        }

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
            int width = drawable.getIntrinsicWidth();

            TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setTextSize(textSize);
            int textHeight = (int) RTextPaint.getTextHeight(textPaint);

            if (textHeight > height) {
                int offset = textHeight - height + textPaint.getFontMetricsInt().descent / 2;
                InsetDrawable insetDrawable = new InsetDrawable(drawable, 0, offset, 0, 0);
                insetDrawable.setBounds(0, 0, width, textHeight);
                return insetDrawable;
            } else {
                drawable.setBounds(0, 0, width, height);
                return drawable;
            }
        }

        /**
         * 用来只显示文本的ImageSpan
         */
        public static Drawable initDrawable(float textSize) {
            Drawable drawable = new ColorDrawable(Color.WHITE);
            TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setTextSize(textSize);
            int textHeight = (int) RTextPaint.getTextHeight(textPaint);
            drawable.setBounds(0, 1, 0, textHeight);
            return drawable;
        }

        public static int getDefaultColor() {
            return Color.parseColor("#507daf");
        }

        public ImageTextSpan setOnImageSpanClick(OnImageSpanClick onImageSpanClick) {
            mOnImageSpanClick = onImageSpanClick;
            if (mOnImageSpanClick == null) {
                canClick = false;
            } else {
                canClick = true;
            }
            return this;
        }

        private void init(Context context) {
            mContext = context;
            space = (int) (2 * mContext.getResources().getDisplayMetrics().density);

            setDefaultTextColor();
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            if (TextUtils.isEmpty(mShowContent)) {
                mImageSize = super.getSize(paint, text, start, end, fm);
                mSpanWidth = mImageSize;
                return mSpanWidth;
            } else {
                String string = mShowContent;
                mTextBounds = getTextBounds(paint, string);
                mImageSize = super.getSize(paint, text, start, end, fm);
                mSpanWidth = mImageSize + space + mTextBounds.width() + space;
                return mSpanWidth;
            }
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            if (TextUtils.isEmpty(mShowContent)) {
                super.draw(canvas, text, start, end, x, top, y, bottom, paint);
            } else {
                tempRect.set((int) x, top, ((int) (x + mSpanWidth + space + space)), bottom);
                if (isTouchDown && tempRect.contains(((int) downX), (int) downY)) {
                    if (enableTouchEffect) {
                        paint.setColor(SkinHelper.getTranColor(textColor, 0x80));
                        canvas.drawRect(tempRect, paint);
                    } else {
                        paint.setColor(Color.TRANSPARENT);
                        canvas.drawRect(tempRect, paint);
                    }
                }

                super.draw(canvas, text, start, end, x, top, y, bottom, paint);
                paint.setColor(textColor);//默认是黑色

                int height = bottom - top;//绘制区域的高度

                String string = mShowContent;
                int textHeight = (int) RTextPaint.getTextHeight(paint);

                //文本在图片的中间绘制
                float textY;
                if (paint.getFontMetricsInt().descent > 0) {
                    textY = top + textHeight / 2 + height / 2 - paint.getFontMetricsInt().descent / 2;
                } else {
                    textY = top + textHeight / 2 + height / 2 - paint.getFontMetricsInt().descent;
                }
                canvas.drawText(string,
                        x + mImageSize + space,
                        textY,
                        paint);
            }
        }

        public ImageTextSpan setDefaultTextColor() {
            setTextColor(getDefaultColor());//默认的文本颜色
            return this;
        }

        public ImageTextSpan setTextColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public boolean isCanClick() {
            return canClick;
        }

        public ImageTextSpan setCanClick(boolean canClick) {
            this.canClick = canClick;
            return this;
        }

        /**
         * 单击事件
         */
        public void onClick(TextView view) {
            L.e("call: onClick([view])-> " + mShowContent + " : " + url);
            if (mOnImageSpanClick != null) {
                if (!mOnImageSpanClick.onClick(view, mShowContent, url)) {
                    if (patternUrl.matcher(url).matches()) {
                        mOnImageSpanClick.onUrlClick(view, url);
                    } else if (patternPhone.matcher(url).matches()) {
                        mOnImageSpanClick.onPhoneClick(view, url);
                    } else if (patternNumber.matcher(url).matches()) {
                        mOnImageSpanClick.onMentionClick(view, url);
                    }
                }
            }
        }

        public void onTouchUp(final TextView view) {
            isTouchDown = false;
            downX = -1;
            downY = -1;
            view.postInvalidate();//解决在RecyclerView中, 会出现点击背景不消失的BUG
        }

        public void onTouchDown(final TextView view, float x, float y) {
            isTouchDown = true;
            downX = x;
            downY = y;
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onTouchUp(view);
                }
            }, 300);//300毫秒后,自动取消
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

        public static abstract class OnImageSpanClick {
            public void onUrlClick(TextView view, String url) {

            }

            public void onMentionClick(TextView view, String mention) {

            }

            public void onPhoneClick(TextView view, String phone) {

            }

            /**
             * @return 返回false 时, {@link OnImageSpanClick#onUrlClick(TextView, String)}
             * 和{@link OnImageSpanClick#onMentionClick(TextView, String)}才会回调
             */
            public boolean onClick(TextView view, String showContent, String url) {
                return false;
            }
        }
    }

    public static class ImageClickMethod extends LinkMovementMethod {

        /**
         * 是否在Span上点击了
         */
        public static boolean isTouchInSpan = false;
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

                if (link.length != 0 && link[0].isCanClick()) {
                    if (action == MotionEvent.ACTION_UP) {
                        link[0].onTouchUp(widget);
                        link[0].onClick(widget);
                        isTouchInSpan = false;
                    } else if (action == MotionEvent.ACTION_DOWN) {
                        isTouchInSpan = true;
                        link[0].onTouchDown(widget, event.getX(), event.getY());
                        Selection.setSelection(buffer,
                                buffer.getSpanStart(link[0]),
                                buffer.getSpanEnd(link[0]));
                    } else if (action == MotionEvent.ACTION_MOVE) {
                        //link[0].onTouchMove(widget, event.getX(), event.getY());
                        return super.onTouchEvent(widget, buffer, event);
                    } else if (action == MotionEvent.ACTION_CANCEL) {
                        isTouchInSpan = false;
                        link[0].onTouchCancel(widget, event.getX(), event.getY());
                        return super.onTouchEvent(widget, buffer, event);
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
