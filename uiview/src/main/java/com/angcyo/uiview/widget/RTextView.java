package com.angcyo.uiview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

import com.angcyo.uiview.R;
import com.angcyo.uiview.skin.SkinHelper;
import com.angcyo.uiview.utils.RTextPaint;
import com.angcyo.uiview.utils.Reflect;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通过Tag属性, 得到text格式化模板, 然后重写setText实现格式化
 * Created by angcyo on 2017-01-08.
 */

public class RTextView extends AppCompatTextView {

    /**
     * 左边垂直矩形的颜色
     */
    Rect leftColorRect;
    Paint colorPaint;
    int leftWidth = 0;
    @ColorInt
    int leftColor;
    int leftMargin = 0;

    boolean hasUnderline = false;

    boolean isAttached = false;
    private Drawable mBackgroundDrawable;
    private CharSequence mRawText;
    private int mPaddingLeft;
    private RTextPaint mTextPaint;
    private int mLeftOffset;
    private String mLeftString;

    public RTextView(Context context) {
        this(context, null);
    }


    public RTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (isInEditMode()) {
            return;
        }

        //绘制左边的提示竖线
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RTextView);
        leftColor = typedArray.getColor(R.styleable.RTextView_r_left_color, SkinHelper.getSkin().getThemeColor());
        leftWidth = typedArray.getDimensionPixelOffset(R.styleable.RTextView_r_left_width, 0);
        hasUnderline = typedArray.getBoolean(R.styleable.RTextView_r_has_underline, false);
        mBackgroundDrawable = typedArray.getDrawable(R.styleable.RTextView_r_background);

        //绘制左边的提示文本
        mPaddingLeft = getPaddingLeft();
        ensurePaint();
        int color = typedArray.getColor(R.styleable.RTextView_r_left_text_color, getCurrentTextColor());
        mTextPaint.setTextColor(color);

        mLeftOffset = typedArray.getDimensionPixelOffset(R.styleable.RTextView_r_left_text_offset,
                getResources().getDimensionPixelOffset(R.dimen.base_ldpi));

        String string = typedArray.getString(R.styleable.RTextView_r_left_text);
        setLeftString(string);
        typedArray.recycle();

        initView();
    }

    public void setLeftString(String leftString) {
        mLeftString = leftString;
        if (!TextUtils.isEmpty(mLeftString)) {
            float textWidth = mTextPaint.getTextWidth(mLeftString);
            setPadding((int) (mPaddingLeft + textWidth + mLeftOffset), getPaddingTop(), getPaddingRight(), getPaddingBottom());
        } else {
            setPadding(mPaddingLeft, getPaddingTop(), getPaddingRight(), getPaddingBottom());
        }
    }

    private void ensurePaint() {
        if (mTextPaint == null) {
            mTextPaint = new RTextPaint(getPaint());
        }
    }

    public float density() {
        return getResources().getDisplayMetrics().density;
    }

    @Override
    public void draw(Canvas canvas) {
        if (mBackgroundDrawable != null) {
            mBackgroundDrawable.setBounds(canvas.getClipBounds());
            mBackgroundDrawable.draw(canvas);
        }
        super.draw(canvas);
    }

    protected void initView() {
        initLeftRes();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initLeftRes();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            super.onDraw(canvas);
            return;
        }

        if (hasUnderline) {
            getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
            getPaint().setAntiAlias(true);
        }
        super.onDraw(canvas);
        if (leftWidth > 0) {
            canvas.drawRect(leftColorRect, colorPaint);
        }

        if (!TextUtils.isEmpty(mLeftString)) {
            //底部绘制文本
            //mTextPaint.drawOriginText(canvas, mLeftString, getPaddingLeft(), getMeasuredHeight() - getPaddingBottom());
            //居中绘制文本
            canvas.save();
            canvas.translate(-getPaddingLeft() + getScrollX(), 0);
            mTextPaint.drawOriginText(canvas, mLeftString, getPaddingLeft() + mPaddingLeft,
                    (getMeasuredHeight() - getPaddingBottom() - getPaddingTop()) / 2 +
                            getPaddingTop() + mTextPaint.getTextHeight() / 2);
            canvas.restore();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttached = false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttached = true;
//        if (getContentDescription() != null) {
//            LinkifyCompat.addLinks(this, Linkify.ALL);
//        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (isInEditMode()) {
            super.setText(text, type);
            return;
        }
        if (getTag() != null &&
                getTag().toString().contains("%") &&
                !"title".equalsIgnoreCase(getTag().toString()) /**当出现在TitleBar中, 会有这个标志*/ &&
                !TextUtils.isEmpty(text)) {
            try {
                final String format = String.format(Locale.CHINA, getTag().toString(), text);
                setTextEx(format, type);
            } catch (Exception e) {
                setTextEx(text, type);
            }
        } else {
            setTextEx(text, type);
        }
    }

    /**
     * 系统的省略号, 有时不会显示, 采用Span手动处理
     */
    private void setTextEx(CharSequence text, BufferType type) {
        if (TextUtils.isEmpty(text)) {
            setSuperText(text, type);
            return;
        }

        SpannableStringBuilder spanBuilder;
        if (text instanceof SpannableStringBuilder) {
            spanBuilder = (SpannableStringBuilder) text;
        } else {
            spanBuilder = new SpannableStringBuilder(text);
        }

        if (getMaxLines() == 1 && getEllipsize() == TextUtils.TruncateAt.END) {

            int maxLength = Integer.MAX_VALUE;
            InputFilter[] filters = getFilters();
            for (InputFilter filter : filters) {
                if (filter instanceof InputFilter.LengthFilter) {
                    maxLength = (int) Reflect.getMember(InputFilter.LengthFilter.class, filter, "mMax");
                }
            }

            /*请在设置 InputLengthFilter的时候, 预加上3个字符*/
            String more = getMoreString();
            int offset = more.length();
            int lastIndex = maxLength - offset;
            if (lastIndex >= 0) {
                if (text.length() > lastIndex) {
//                    if (EmojiFilter.isEmojiCharacter(spanBuilder.charAt(lastIndex))) {
//                        offset = 4;/*兼容末尾是emoji表情*/
//                    }
                    spanBuilder.setSpan(new RExTextView.ImageTextSpan(getContext(), getTextSize(),
                                    getTextColors().getColorForState(new int[]{}, getTextColors().getDefaultColor()), more),
                            maxLength - offset, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }

        setSuperText(spanBuilder, type);
    }

    @NonNull
    private String getMoreString() {
        return "...";
    }

    private void setSuperText(CharSequence text, BufferType type) {
        mRawText = text;
        super.setText(text, type);
    }

    /**
     * 没有被折叠的文本
     */
    public CharSequence getRawText() {
        return mRawText;
    }

    public void addFilter(InputFilter filter) {
        final InputFilter[] filters = getFilters();
        final InputFilter[] newFilters = new InputFilter[filters.length + 1];
        System.arraycopy(filters, 0, newFilters, 0, filters.length);
        newFilters[filters.length] = filter;
        setFilters(newFilters);
    }

    /**
     * 需要预留3个'...'字符的数量, <em></>已自动处理</em>
     */
    public void setMaxLength(int length) {
        InputFilter[] filters = getFilters();
        boolean have = false;
        InputFilter.LengthFilter lengthFilter = new InputFilter.LengthFilter(length + getMoreString().length());
        for (int i = 0; i < filters.length; i++) {
            InputFilter filter = filters[i];
            if (filter instanceof InputFilter.LengthFilter) {
                have = true;
                filters[i] = lengthFilter;
                setFilters(filters);
                break;
            }
        }
        if (!have) {
            addFilter(lengthFilter);
        }

        setMaxLines(1);
        //setSingleLine();
        setEllipsize(TextUtils.TruncateAt.END);
    }

    public void setText(Object... args) {
        if (getTag() != null && args != null && args.length > 0) {
            try {
                final String format = String.format(Locale.CHINA, getTag().toString(), args);
                super.setText(format);
            } catch (Exception e) {
                super.setText("");
            }
        } else {
            super.setText("");
        }
    }

    public void setLeftIco(@DrawableRes int leftIco) {
        final Drawable[] compoundDrawables = getCompoundDrawables();
        setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(leftIco),
                compoundDrawables[1], compoundDrawables[2], compoundDrawables[3]);
    }

    public void setRightIco(@DrawableRes int rightIco) {
        final Drawable[] compoundDrawables = getCompoundDrawables();
        setCompoundDrawablesWithIntrinsicBounds(compoundDrawables[0],
                compoundDrawables[1], getResources().getDrawable(rightIco), compoundDrawables[3]);
    }

    public void setTopIco(@DrawableRes int topIco) {
        final Drawable[] compoundDrawables = getCompoundDrawables();
        setCompoundDrawablesWithIntrinsicBounds(compoundDrawables[0], getResources().getDrawable(topIco),
                compoundDrawables[2], compoundDrawables[3]);
    }

    public void setBottomIco(@DrawableRes int bottomIco) {
        final Drawable[] compoundDrawables = getCompoundDrawables();
        setCompoundDrawablesWithIntrinsicBounds(
                compoundDrawables[0], compoundDrawables[1],
                compoundDrawables[2], getResources().getDrawable(bottomIco));
    }

    private void initLeftRes() {
        if (leftWidth <= 0) {
            return;
        }

        int viewHeight = getMeasuredHeight();
        if (leftColorRect == null) {
            leftColorRect = new Rect();
        }
        if (colorPaint == null) {
            colorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        colorPaint.setColor(leftColor);
        leftColorRect.set(leftMargin, getPaddingTop(),
                leftMargin + leftWidth, viewHeight - getPaddingBottom());
    }

    public RTextView setLeftWidth(int leftWidth) {
        this.leftWidth = leftWidth;
        initLeftRes();
        return this;
    }

    public RTextView setLeftColor(int leftColor) {
        this.leftColor = leftColor;
        initLeftRes();
        return this;
    }

    public RTextView setLeftColor(int leftColor, int leftWidth) {
        this.leftColor = leftColor;
        this.leftWidth = leftWidth;
        initLeftRes();
        return this;
    }

    public void setDefaultSKin(int resId) {
        setDefaultSKin(getResources().getString(resId));
    }

    public void setDefaultSKin(String text) {
        setPadding(getResources().getDimensionPixelOffset(R.dimen.base_hdpi), getPaddingTop(),
                getPaddingRight(), getPaddingBottom());
        setLeftColor(SkinHelper.getSkin().getThemeSubColor(), getResources().getDimensionPixelOffset(R.dimen.base_mdpi));
        setText(text);
//        if (isAttached) {
//            postInvalidate();
//        }
    }

    public void setRBackgroundColor(int color) {
        mBackgroundDrawable = new ColorDrawable(color);
    }

    /**
     * 设置高亮的关键字
     */
    public void setHighlightWord(String word) {
        CharSequence text = getText();
        if (TextUtils.isEmpty(text) || TextUtils.isEmpty(word)) {
            return;
        }

        SpannableStringBuilder builder;
        if (text instanceof SpannableStringBuilder) {
            builder = (SpannableStringBuilder) text;
        } else {
            builder = new SpannableStringBuilder(text);
        }

        Matcher matcher = Pattern.compile(word).matcher(text);

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            //CharSequence text = matcher.group();//input.subSequence(start, end);

            builder.setSpan(new ForegroundColorSpan(SkinHelper.getSkin().getThemeSubColor()),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        setTextEx(builder, BufferType.NORMAL);
    }
}
