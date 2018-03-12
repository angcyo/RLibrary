package com.angcyo.uiview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Px;
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
import com.angcyo.uiview.kotlin.ExKt;
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
    //public final static Pattern patternUrl = Pattern.compile("(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.:+#]*[\\w\\-+#])?");//Patterns.WEB_URL;//
    public final static Pattern patternUrlSys = Patterns.WEB_URL;
    public final static Pattern patternUrl = Pattern.compile("^(http|https|ftp)\\://([a-zA-Z0-9\\.\\-]+(\\:[a-zA-Z0-9\\.&%\\$\\-]+)*@)?((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|([a-zA-Z0-9\\-]+\\.)*[a-zA-Z0-9\\-]+\\.[a-zA-Z]{2,4})(\\:[0-9]+)?(/[^/][a-zA-Z0-9\\.\\,\\?\\'\\\\/\\+&%\\$#\\=~_\\-@]*)*$");//Patterns.WEB_URL;//
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
    public final static Pattern patternPhone = Pattern.compile("\\d{3}-\\d{8}|\\d{3}-\\d{7}|\\d{4}-\\d{8}|\\d{3}-\\d{4}-\\d{4}|\\d{4}-\\d{7}|1+[34578]+\\d{9}|\\d{8}|\\d{7}");

    /**
     * 5位以上数字帐号匹配
     */
    public final static Pattern patternNumberAccount = Pattern.compile("\\d{5,}");

    /**
     * 座机号码正则
     */
    public final static Pattern patternTel = Pattern.compile("^([0-9]{3,4}-)?[0-9]{4,8}$");

    //    public final static Pattern patternTel2 = Pattern.compile("(\\d{2,4}[-_－—]?)?\\d{3,8}([-_－—]?\\d{3,8})?([-_－—]?\\d{1,7})?|0?1[34578]\\d{9}");
    public final static Pattern patternTel2 = Pattern.compile("\\d{3,4}[-_－—]\\d{3,4}[-_－—]\\d{3,8}");

    protected ImageTextSpan.OnImageSpanClick mOnImageSpanClick;

    private int maxShowLine = -1;//最大显示多少行, 当超过时, 会显示...全部

    private String more = "...";
    private String foldString = "";

    private int mImageSpanTextColor = ImageTextSpan.getDefaultColor();
    /**
     * 是否匹配网址
     */
    private boolean needPatternUrl = true;
    /**
     * 匹配Url时, 是否检查是Http开头
     */
    private boolean needPatternUrlCheckHttp = true;
    /**
     * 匹配到URL, 是否显示Link Ico图标
     */
    private boolean showPatternUrlIco = true;
    /**
     * 是否显示url的原始文本, 否则会被 '网页链接' 替换
     */
    private boolean showUrlRawText = false;
    /**
     * 是否匹配@的人
     */
    private boolean needPatternMention = true;
    /**
     * 是否匹配手机号码
     */
    private boolean needPatternPhone = true;
    /**
     * 是否匹配 座机, 联系方式
     */
    private boolean needPatternTel = true;
    /**
     * 是否匹配纯数据帐号
     */
    private boolean needPatternNumberAccount = true;

    public RExTextView(Context context) {
        this(context, null);
    }

    public RExTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RExTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initExTextView(context, attrs);
    }

    /**
     * 判断 str是否是数字
     */
//    public static boolean isNumber(String str) {
//        Pattern compile = Pattern.compile("^\\d+$");
//        Matcher matcher = compile.matcher(str);
//        //matcher.group(matcher.groupCount())
//        return matcher.find();
//    }
    public static boolean isWebUrl(String url) {
//        if (TextUtils.isEmpty(url)) {
//            return false;
//        }
//        return patternUrl.matcher(url).matches();
        return isWebUrlSys(url, true);
    }

    public static boolean isWebUrlSys(String url, boolean checkHttp) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        if (checkHttp) {
            if (isStartWidthUrl(url)) {

            } else {
                return false;
            }
        }
        return patternUrlSys.matcher(url).matches();
    }

    /**
     * url 是否包含了 http请求协议
     */
    public static boolean isStartWidthUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        if (url.startsWith("http") || url.startsWith("HTTP")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断 str是否是数字
     */
    public static boolean isNumber(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        return patternNumber.matcher(str).matches();
    }

    /**
     * 是否是手机号码
     */
    public static boolean isPhone(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        return patternPhone.matcher(str).matches();
    }

    /**
     * 是否是座机号码
     */
    public static boolean isTel(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        return patternTel.matcher(str).matches();
    }

    public static boolean isTel2(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        return patternTel2.matcher(str).matches();
    }

    private void initExTextView(Context context, AttributeSet attrs) {
        //foldString = getResources().getString(R.string.see_all);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RExTextView);
        maxShowLine = typedArray.getInt(R.styleable.RExTextView_r_max_show_line, maxShowLine);
        foldString = typedArray.getString(R.styleable.RExTextView_r_show_fold_text);
        more = typedArray.getString(R.styleable.RExTextView_r_show_more_text);
        if (more == null) {
            more = "...";
        }
        if (foldString == null) {
            foldString = "";
        }
        if (maxShowLine > 0) {
            setMaxShowLine(maxShowLine);
        }
        typedArray.recycle();

        setMovementMethod(getDefaultMovementMethod());
    }

    public RExTextView setImageSpanTextColor(int imageSpanTextColor) {
        mImageSpanTextColor = imageSpanTextColor;
        return this;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //setMovementMethod(ImageClickMethod.getInstance());
    }

    @Override
    protected MovementMethod getDefaultMovementMethod() {
        if (isNeedPattern()) {
            return ImageClickMethod.getInstance();
        }
        return super.getDefaultMovementMethod();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean touchEvent = super.onTouchEvent(event);
        if (isNeedPattern()) {
            if (hasOnClickListeners()) {
                return touchEvent || ImageClickMethod.isTouchInSpan;
            } else {
                return ImageClickMethod.isTouchInSpan;
            }
        } else {
            return touchEvent;
        }
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
        if (isInEditMode()) {
            super.setText(text, type);
            return;
        }
        if (TextUtils.isEmpty(text)) {
            super.setText(text, type);
        } else {
            SpannableStringBuilder spanBuilder = new SpannableStringBuilder(text);
            if (needPatternUrl) {
                patternUrl(spanBuilder, text);//优先匹配
            }
            if (needPatternMention) {
                patternMention(spanBuilder, text);
            }
            if (needPatternNumberAccount) {
                patternNumberAccount(spanBuilder, text);
            }
            if (needPatternTel) {
                patternTel(spanBuilder, text);
            }
            if (needPatternPhone) {
                patternPhone(spanBuilder, text);
            }
            afterPattern(spanBuilder, text);
            super.setText(spanBuilder, type);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);


        //int lastLineHeight = getLastLineHeight();
        //float descent = getPaint().descent();

        //setMeasuredDimension(getMeasuredWidth(), (int) (getMeasuredHeight() + density() * 40));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        checkMaxShowLine();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        checkMaxShowLine();
    }

    private void checkMaxShowLine() {
        Layout layout = getLayout();
        if (maxShowLine > 0 && layout != null) {
            int lines = layout.getLineCount();
            if (lines > 0) {
                if (lines > maxShowLine) {
                    //需要折叠
                    CharSequence sequence = getText();
                    if (sequence instanceof Spannable) {
                        Spannable spannable = (Spannable) sequence;
                        int textLength = spannable.length();

                        String foldString = getFoldString();

                        if (textLength <= more.length() + foldString.length()) {
                            setMaxShowLine(-1);//换行字符太多的情况
                            return;
                        }

                        int lineStart = layout.getLineStart(maxShowLine);//返回第几行的第一个字符, 在字符串中的index

                        float needWidth = getPaint().measureText(more + foldString)
                                /*+ 20 * ImageTextSpan.getSpace(getContext())*/;//需要预留绘制文件的空间宽度

                        //找出需要剔除多少个字符,才够空间绘制
                        int startPosition = -1;
                        for (int i = 0; i < textLength && lineStart > 0; i++) {
                            int start = lineStart - i;
                            if (start >= 0 && start < lineStart) {
                                CharSequence charSequence = sequence.subSequence(start, lineStart);
                                float textWidth = getPaint().measureText(String.valueOf(charSequence));
                                if (textWidth > needWidth) {
                                    startPosition = lineStart - i - 1;//多预留一个位置, 防止不够宽度无法绘制
                                    break;
                                }
                            }
                        }

                        //int startPosition = lineStart - more.length() - foldString.length();

                        if (startPosition < 0) {
                            //L.e("call: onMeasure([widthMeasureSpec, heightMeasureSpec])-> Set Span 1");
                            spannable.setSpan(new ImageTextSpan(getContext(), getTextSize(), getCurrentTextColor(), more),
                                    lineStart - 1, lineStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            return;
                        }

                        int start = findStartPosition(spannable, startPosition);

                        int offset = more.length();//(sequence.length() % 2 == 0) ? 4 : 3;

                        if (TextUtils.isEmpty(foldString)) {
                            if (!TextUtils.isEmpty(more)) {
                                //L.e("call: onMeasure([widthMeasureSpec, heightMeasureSpec])-> Set Span 2:" + start);
                                spannable.setSpan(new ImageTextSpan(getContext(), getTextSize(), getCurrentTextColor(), more),
                                        start, textLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        } else {
                            if (!TextUtils.isEmpty(more)) {
                                //L.e("call: onMeasure([widthMeasureSpec, heightMeasureSpec])-> Set Span 3:" + start);
                                spannable.setSpan(new ImageTextSpan(getContext(), getTextSize(), getCurrentTextColor(), more),
                                        start, start + offset, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                            //L.e("call: onMeasure([widthMeasureSpec, heightMeasureSpec])-> Set Span 4:" + start);
                            spannable.setSpan(new ImageTextSpan(getContext(), getTextSize(), foldString),
                                    start + offset, textLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

                        //setMeasuredDimension(getMeasuredWidth(), (int) (getMeasuredHeight() + density() * 140));
                    }

                }
            }
        }
    }

    private int getLastLineHeight() {
        Layout layout = getLayout();
        if (layout != null) {
            int lineCount = layout.getLineCount();
            if (lineCount > 0) {
                //行的底部距离view顶部的高度, 最后一行的LineTop通常会等于View的height
                return layout.getLineTop(lineCount) - layout.getLineTop(lineCount - 1);
            }
        }
        return 0;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        if (maxShowLine > 0) {
            return false;
        }
        return super.canScrollVertically(direction);
    }

    @Override
    public void scrollTo(@Px int x, @Px int y) {
        if (maxShowLine > 0) {
            return;
        }
        super.scrollTo(x, y);
    }

    /**
     * 检查当前位置是否命中在spannable上, 如果是, 返回spannable的start position
     */
    private int findStartPosition(Spannable spannable, int startWidthPosition) {
        CharacterStyle[] oldSpans = spannable.getSpans(startWidthPosition, spannable.length(), CharacterStyle.class);
        int position = startWidthPosition;
        for (CharacterStyle oldSpan : oldSpans) {
            int spanStart = spannable.getSpanStart(oldSpan);
            int spanEnd = spannable.getSpanEnd(oldSpan);

            if (spanStart <= startWidthPosition && spanEnd > startWidthPosition) {
                position = spanStart;
            }

            if (spanStart >= startWidthPosition) {
                spannable.removeSpan(oldSpan);
            }
        }
        //L.e("call: findStartPosition([spannable, startWidthPosition]) " + startWidthPosition + " -> " + position);
        return position;
    }

    private String getFoldString() {
        if (TextUtils.isEmpty(foldString)) {
            return "";
        }
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
        Matcher matcher = patternUrlSys.matcher(input);

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String text = matcher.group();//input.subSequence(start, end);

            if (needPatternUrlCheckHttp && !isStartWidthUrl(text)) {
                continue;
            }

            if (showUrlRawText) {
                builder.setSpan(new ImageTextSpan(getContext(),
                                ImageTextSpan.initDrawable(getTextSize()),
                                text,
                                text)
                                .setOnImageSpanClick(mOnImageSpanClick)
                                .setTextColor(mImageSpanTextColor),
                        start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (showPatternUrlIco) {
                builder.setSpan(new ImageTextSpan(getContext(),
                                ImageTextSpan.initDrawable(getContext(),
                                        R.drawable.base_link_ico, getTextSize()),
                                getContext().getString(R.string.url_link_tip),
                                text)
                                .setOnImageSpanClick(mOnImageSpanClick)
                                .setTextColor(mImageSpanTextColor),
                        start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                builder.setSpan(new ImageTextSpan(getContext(),
                                ImageTextSpan.initDrawable(getTextSize()),
                                getContext().getString(R.string.url_link_tip),
                                text)
                                .setOnImageSpanClick(mOnImageSpanClick)
                                .setTextColor(mImageSpanTextColor),
                        start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

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

            if (!isInOtherSpan(builder, input.length(), start, end)) {
                builder.setSpan(new ImageTextSpan(getContext(), ImageTextSpan.initDrawable(getTextSize()), matcher.group(2), matcher.group(1))
                                .setOnImageSpanClick(mOnImageSpanClick)
                                .setTextColor(mImageSpanTextColor),
                        start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    /**
     * 匹配 电话号码
     */
    protected void patternPhone(SpannableStringBuilder builder, CharSequence input) {
        pattern(patternPhone.matcher(input), builder, input);
    }

    /**
     * 匹配座机
     */
    protected void patternTel(SpannableStringBuilder builder, CharSequence input) {
        pattern(patternTel2.matcher(input), builder, input);
    }

    /**
     * 匹配数字帐号
     */
    protected void patternNumberAccount(SpannableStringBuilder builder, CharSequence input) {
        pattern(patternNumberAccount.matcher(input), builder, input);
    }

    protected void pattern(Matcher matcher, SpannableStringBuilder builder, CharSequence input) {
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String group = matcher.group();

            if (!isInOtherSpan(builder, input.length(), start, end)) {
                builder.setSpan(new ImageTextSpan(getContext(), ImageTextSpan.initDrawable(getTextSize()),
                                group, group)
                                .setOnImageSpanClick(mOnImageSpanClick)
                                .setTextColor(mImageSpanTextColor),
                        start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    /**
     * 判断 需要检测的开始位置,结束位置, 是否已经在其他span中
     */
    @Override
    protected boolean isInOtherSpan(SpannableStringBuilder builder, int length, int startPosition, int endPosition) {
        return isInOtherSpan(builder, ImageTextSpan.class, length, startPosition, endPosition);
    }

    /**
     * 恢复到默认匹配状态
     */
    public void resetPatternState() {
        needPatternMention = true;
        needPatternPhone = true;

        needPatternNumberAccount = true;
        needPatternUrl = true;
        needPatternUrlCheckHttp = true;
        showPatternUrlIco = true;
        showUrlRawText = false;
    }

    public boolean isNeedPattern() {
        return needPatternPhone || needPatternMention || needPatternUrl || needPatternNumberAccount;
    }

    /**
     * 是否激活匹配功能
     */
    public RExTextView setNeedPattern(boolean needPattern) {
        this.needPatternUrl = needPattern;
        this.needPatternMention = needPattern;
        this.needPatternPhone = needPattern;
        this.needPatternNumberAccount = needPattern;
        setMovementMethod(getDefaultMovementMethod());
        return this;
    }

    public RExTextView setNeedPatternUrl(boolean needPatternUrl) {
        this.needPatternUrl = needPatternUrl;
        setMovementMethod(getDefaultMovementMethod());
        return this;
    }

    public RExTextView setNeedPatternUrlCheckHttp(boolean needPatternUrlCheckHttp) {
        this.needPatternUrlCheckHttp = needPatternUrlCheckHttp;
        return this;
    }

    public RExTextView setNeedPatternNumberAccount(boolean needPatternNumberAccount) {
        this.needPatternNumberAccount = needPatternNumberAccount;
        return this;
    }

    public RExTextView setShowPatternUrlIco(boolean showPatternUrlIco) {
        this.showPatternUrlIco = showPatternUrlIco;
        return this;
    }

    public RExTextView setShowUrlRawText(boolean showUrlRawText) {
        this.showUrlRawText = showUrlRawText;
        return this;
    }

    public RExTextView setNeedPatternMention(boolean needPatternMention) {
        this.needPatternMention = needPatternMention;
        setMovementMethod(getDefaultMovementMethod());
        return this;
    }

    public RExTextView setNeedPatternPhone(boolean needPatternPhone) {
        this.needPatternPhone = needPatternPhone;
        setMovementMethod(getDefaultMovementMethod());
        return this;
    }

    /**
     * 设置是否当点击span时, 显示背景颜色
     */
    public RExTextView setShowSelectionSpanBgColor(boolean showSelectionSpanBgColor) {
        ImageClickMethod.getInstance().showSelectionSpanBgColor = showSelectionSpanBgColor;
        return this;
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
            this(context, textSize, getDefaultColor(), showContent);
        }

        public ImageTextSpan(Context context, float textSize, int textColor, String showContent) {
            super(initDrawable(textSize), ALIGN_BASELINE);
            this.mShowContent = showContent;
            init(context);

            setCanClick(false);
            setTextColor(textColor);
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
            float textHeight = textPaint.descent() - textPaint.ascent(); //(int) RTextPaint.getTextHeight(textPaint);

//            if (textHeight > height) {
//                int offset = textHeight - height + textPaint.getFontMetricsInt().descent / 2;
//                InsetDrawable insetDrawable = new InsetDrawable(drawable, 0, offset, 0, 0);
//                insetDrawable.setBounds(0, 0, width, textHeight);
//                return insetDrawable;
//            } else {
//                drawable.setBounds(0, 0, width, height);
//                return drawable;
//            }

            //drawable.setBounds(0, 0, width, (int) Math.max(height, textHeight));
            drawable.setBounds(0, 0, width, (int) -textPaint.ascent()/*(int) Math.max(height, textHeight)*/);
            return drawable;
        }

        /**
         * 用来只显示文本的ImageSpan
         */
        public static Drawable initDrawable(float textSize) {
            Drawable drawable = new ColorDrawable(Color.WHITE);
            TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setTextSize(textSize);
            //int textHeight = (int) (textPaint.descent());// - textPaint.ascent());//(int) RTextPaint.getTextHeight(textPaint);
            //drawable.setBounds(0, 0, 1, textHeight);
            drawable.setBounds(0, 0, 0, (int) -textPaint.ascent() /*(int) ExKt.textHeight(textPaint)*/);
            return drawable;
        }

        public static int getDefaultColor() {
            return Color.parseColor("#507daf");
        }

        private static int getSpace(Context context) {
            return (int) (2 * context.getResources().getDisplayMetrics().density);
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
            space = getSpace(context);

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
                mSpanWidth = mImageSize + mTextBounds.width();
                if (haveIco()) {
                    mSpanWidth += space;
                }
                return mSpanWidth;
            }
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            if (TextUtils.isEmpty(mShowContent)) {
                super.draw(canvas, text, start, end, x, top, y, bottom, paint);
            } else {
                tempRect.set((int) x, top, ((int) (x + mSpanWidth)), bottom);
                canvas.save();
                if (enableTouchEffect) {
                    if (isTouchDown && tempRect.contains(((int) downX), (int) downY)) {
                        paint.setColor(SkinHelper.getTranColor(textColor, 0x80));
                        canvas.drawRect(tempRect, paint);
                    } else {
                        paint.setColor(Color.TRANSPARENT);
                        canvas.drawRect(tempRect, paint);
                    }
                } else {
                    paint.setColor(Color.TRANSPARENT);
                    canvas.drawRect(tempRect, paint);
                }
//                paint.setColor(Color.WHITE);
//                canvas.drawRect(tempRect, paint);
//                canvas.drawColor(Color.TRANSPARENT);
                canvas.restore();

                super.draw(canvas, text, start, end, x, top, y, bottom, paint);
                paint.setColor(textColor);//默认是黑色

                int height = bottom - top;//绘制区域的高度

                String string = mShowContent;
                int textHeight = (int) RTextPaint.getTextHeight(paint);

                //文本在图片的中间绘制
                float textY;
                textY = y /*+ textHeight / 2 + height / 2 */ /*- paint.getFontMetricsInt().descent*/;
                if (y == bottom) {
                    textY = y - paint.descent();
                }
//                if (paint.getFontMetricsInt().descent > 0) {
//                    textY = top + textHeight / 2 + height / 2 - paint.getFontMetricsInt().descent / 2;
//                } else {
//                    textY = top + textHeight / 2 + height / 2 - paint.getFontMetricsInt().descent;
//                }

                if (top != y) {
                    canvas.save();
//                    canvas.saveLayer(x, top, x + x, bottom, null, Canvas.ALL_SAVE_FLAG);
//                    paint.setStyle(Paint.Style.FILL_AND_STROKE);
//                    paint.setColor(Color.RED);
//                    canvas.drawRect(x, top, x + x, bottom, paint);
//                    paint.setColor(textColor);
                    int sp = 0;
                    if (haveIco()) {
                        sp = space;
                    }
                    canvas.drawText(string,
                            x + mImageSize + sp,
                            textY,
                            paint);
                    canvas.restore();
                }

//                paint.setColor(Color.RED);
            }
        }

        public ImageTextSpan setDefaultTextColor() {
            setTextColor(getDefaultColor());//默认的文本颜色
            return this;
        }

        private boolean haveIco() {
            Drawable drawable = getDrawable();
            if (drawable == null) {
                return false;
            }
            if (drawable instanceof ColorDrawable) {
                return false;
            }
            return true;
        }

        public ImageTextSpan setTextColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public int getShowTextLength() {
            return mShowContent.length();
        }

        public String getShowContent() {
            return mShowContent;
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
            L.e("ImageTextSpan Click-> " + mShowContent + ":" + url);
            if (mOnImageSpanClick != null) {
                if (!mOnImageSpanClick.onClick(view, mShowContent, url)) {
                    if (isWebUrl(url)) {
                        mOnImageSpanClick.onUrlClick(view, url);
                    } else if (isPhone(url) || isTel(url) || isTel2(url)) {
                        mOnImageSpanClick.onPhoneClick(view, url);
                    } else if (isNumber(url)) {
                        mOnImageSpanClick.onMentionClick(view, url);
                    }
                }
            }
        }


        public void onTouchUp(final TextView view) {
            isTouchDown = false;
            downX = -1;
            downY = -1;
            if (enableTouchEffect) {
                view.postInvalidate();//解决在RecyclerView中, 会出现点击背景不消失的BUG
            }
        }

        public void onTouchDown(final TextView view, float x, float y) {
            isTouchDown = true;
            downX = x;
            downY = y;
            if (enableTouchEffect) {
                view.postInvalidate();
            }
//            view.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    onTouchUp(view);
//                }
//            }, 300);//300毫秒后,自动取消
        }

        public void onTouchCancel(TextView view, float x, float y) {
            onTouchUp(view);
        }

        public Rect getTextBounds(Paint paint, String text) {
            tempRect.set(0, 0, 0, 0);
            if (TextUtils.isEmpty(text)) {
                return tempRect;
            }
            //paint.getTextBounds(text, 0, text.length(), tempRect);
            tempRect.set(0, 0, (int) paint.measureText(text), (int) ExKt.textHeight(paint));
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

        /**
         * 设置是否当点击span时, 显示背景颜色
         */
        private boolean showSelectionSpanBgColor = false;

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

                if (link.length > 0) {
                    ImageTextSpan imageTextSpan = link[0];
                    int spanStart = buffer.getSpanStart(imageTextSpan);
                    int spanEnd = buffer.getSpanEnd(imageTextSpan);
                    int showTextLength = imageTextSpan.getShowTextLength();

                    int top = layout.getLineTop(line);
                    int bottom = layout.getLineTop(line + 1);
                    float left = layout.getPrimaryHorizontal(spanStart);
                    float right = layout.getPrimaryHorizontal(spanStart + showTextLength);

                    if (imageTextSpan.isCanClick() && (x >= left && x <= right)   /*(off >= spanStart && off <= spanStart + showTextLength)*/) {
                        if (action == MotionEvent.ACTION_UP) {
                            imageTextSpan.onTouchUp(widget);
                            imageTextSpan.onClick(widget);
                            isTouchInSpan = false;
                        } else if (action == MotionEvent.ACTION_DOWN) {
                            isTouchInSpan = true;
                            imageTextSpan.onTouchDown(widget, event.getX(), event.getY());
                            if (showSelectionSpanBgColor) {
                                Selection.setSelection(buffer,
                                        spanStart,
                                        spanEnd);
                            }
                        } else if (action == MotionEvent.ACTION_MOVE) {
                            //link[0].onTouchMove(widget, event.getX(), event.getY());
                            //return super.onTouchEvent(widget, buffer, event);
                        } else {
                            isTouchInSpan = false;
                            imageTextSpan.onTouchCancel(widget, event.getX(), event.getY());
                            //return super.onTouchEvent(widget, buffer, event);
                        }

                    } else {
                        L.e("onTouchEvent " + action + " -> " + imageTextSpan.getShowContent());
                        Selection.removeSelection(buffer);
                    }
                    return isTouchInSpan;
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
