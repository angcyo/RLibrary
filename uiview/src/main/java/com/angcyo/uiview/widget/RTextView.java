package com.angcyo.uiview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.angcyo.uiview.R;
import com.angcyo.uiview.kotlin.ExKt;
import com.angcyo.uiview.kotlin.ViewExKt;
import com.angcyo.uiview.skin.SkinHelper;
import com.angcyo.uiview.utils.RTextPaint;
import com.angcyo.uiview.utils.Reflect;
import com.angcyo.uiview.utils.ScreenUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通过Tag属性, 得到text格式化模板, 然后重写setText实现格式化
 * Created by angcyo on 2017-01-08.
 */

public class RTextView extends AppCompatTextView {

    /**
     * 滚动文本的方式, 从视图外, 缓缓的向左滚动
     */
    public static final int SCROLL_TYPE_DEFAULT = 1;
    /**
     * 滚动文本的方式, 从0开始, 缓缓的向左滚动
     */
    public static final int SCROLL_TYPE_START = 2;

    /**
     * 左边垂直矩形的颜色
     */
    Rect leftColorRect;
    Paint colorPaint;
    int leftWidth = 0;
    @ColorInt
    int leftColor;
    int leftStringColor;
    int leftStringSize = 14;

    boolean hasUnderline = false;

    boolean isAttached = false;
    /**
     * 由于系统的drawableLeft, 并不会显示在居中文本的左边, 所以自定义此属性
     */
    Drawable textLeftDrawable;
    /**
     * 将textLeftDrawable和TextView的文本居中都居中显示. (默认情况 文本会居中显示, 然后leftDrawable在文本的左边绘制)
     */
    boolean centerLeftDrawable = false;
    float scrollCurX = 0f;
    /**
     * 滚动步长
     */
    int scrollStep = 2;
    /**
     * 暂停滚动
     */
    boolean pauseScroll = false;
    private Drawable mBackgroundDrawable;
    private CharSequence mRawText;
    private int mPaddingLeft;
    private RTextPaint mTextPaint;
    private TextPaint mScrollTextPaint;
    private int mLeftOffset = 0, mTopOffset = 0, mBottomOffset = 0, textLeftOffset = (int) (2 * density());
    private String mLeftString;
    private boolean leftStringBold = false;
    /**
     * 是否显示 未读小红点
     */
    private boolean showNoRead = false;
    /**
     * 小红点半径
     */
    private float noReadRadius = 4 * density();
    private float noReadPaddingTop = 2 * density();
    private float noReadPaddingRight = 2 * density();
    private boolean autoFixTextSize = false;
    /**
     * 宽高是否相等, 取其中最大值计算
     */
    private boolean aeqWidth = false;
    private boolean hideWithEmptyText = false;
    /**
     * 是否显示tip文本 (目前只支持右上角显示)
     */
    private boolean isShowTipText = false;
    private String tipText = "";
    private int tipTextColor = Color.WHITE;
    private int tipTextBgColor = Color.parseColor("#FF3333");
    private int tipTextSize = (int) (9 * density());
    //会根据Gravity, Left会有Right的作用
    private int tipTextTopOffset = 0;
    private int tipTextLeftOffset = 0;
    private RectF tipTextRectF = new RectF();
    /**
     * 是否滚动文本
     * android:ellipsize="none"
     * android:singleLine="true"
     */
    private boolean isScrollText = false;
    /**
     * 是否循环绘制文本, 在文本后面连续绘制文本
     */
    private boolean isScrollTextCircle = false;
    /**
     * 循环绘制文本的间隙
     */
    private int scrollTextCircleOffset = 0;
    private int scrollType = SCROLL_TYPE_DEFAULT;

    public RTextView(Context context) {
        this(context, null);
    }

    public RTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //绘制左边的提示竖线
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RTextView);
        leftColor = typedArray.getColor(R.styleable.RTextView_r_left_color, isInEditMode() ? Color.GREEN : SkinHelper.getSkin().getThemeColor());
        leftWidth = typedArray.getDimensionPixelOffset(R.styleable.RTextView_r_left_width, 0);
        hasUnderline = typedArray.getBoolean(R.styleable.RTextView_r_has_underline, false);
        centerLeftDrawable = typedArray.getBoolean(R.styleable.RTextView_r_center_left_drawable, centerLeftDrawable);
        mBackgroundDrawable = typedArray.getDrawable(R.styleable.RTextView_r_background);

        //绘制左边的提示文本
        mPaddingLeft = getPaddingLeft();
        leftStringColor = typedArray.getColor(R.styleable.RTextView_r_left_text_color, getCurrentTextColor());
        leftStringSize = typedArray.getDimensionPixelOffset(R.styleable.RTextView_r_left_text_size, (int) (leftStringSize * density()));
        ensurePaint();

        mLeftOffset = typedArray.getDimensionPixelOffset(R.styleable.RTextView_r_left_margin, mLeftOffset);
        mBottomOffset = typedArray.getDimensionPixelOffset(R.styleable.RTextView_r_bottom_margin, mBottomOffset);
        mTopOffset = typedArray.getDimensionPixelOffset(R.styleable.RTextView_r_top_margin, mTopOffset);
        textLeftOffset = typedArray.getDimensionPixelOffset(R.styleable.RTextView_r_left_text_offset, textLeftOffset);
        leftStringBold = typedArray.getBoolean(R.styleable.RTextView_r_left_text_bold, leftStringBold);

        String string = typedArray.getString(R.styleable.RTextView_r_left_text);
        setLeftString(string);

        textLeftDrawable = typedArray.getDrawable(R.styleable.RTextView_r_left_drawable);
        if (textLeftDrawable != null) {
            textLeftDrawable.setBounds(0, 0, textLeftDrawable.getIntrinsicWidth(), textLeftDrawable.getIntrinsicHeight());
        }

        noReadRadius = typedArray.getDimensionPixelOffset(R.styleable.RTextView_r_noread_radius, (int) noReadRadius);
        noReadPaddingRight = typedArray.getDimensionPixelOffset(R.styleable.RTextView_r_noread_padding_right, (int) noReadPaddingRight);
        noReadPaddingTop = typedArray.getDimensionPixelOffset(R.styleable.RTextView_r_noread_padding_top, (int) noReadPaddingTop);

        autoFixTextSize = typedArray.getBoolean(R.styleable.RTextView_r_auto_fix_text_size, autoFixTextSize);

        aeqWidth = typedArray.getBoolean(R.styleable.RTextView_r_is_aeq_width, aeqWidth);
        hideWithEmptyText = typedArray.getBoolean(R.styleable.RTextView_r_hide_with_empty_text, hideWithEmptyText);

        isShowTipText = typedArray.getBoolean(R.styleable.RTextView_r_is_show_tip_text, isShowTipText);
        tipText = typedArray.getString(R.styleable.RTextView_r_tip_text);
        if (tipText == null) {
            tipText = "";
        }
        tipTextBgColor = typedArray.getColor(R.styleable.RTextView_r_tip_text_bg, tipTextBgColor);
        tipTextColor = typedArray.getColor(R.styleable.RTextView_r_tip_text_color, tipTextColor);
        tipTextSize = typedArray.getDimensionPixelOffset(R.styleable.RTextView_r_tip_text_size, tipTextSize);
        tipTextTopOffset = typedArray.getDimensionPixelOffset(R.styleable.RTextView_r_tip_text_top_offset, tipTextTopOffset);
        tipTextLeftOffset = typedArray.getDimensionPixelOffset(R.styleable.RTextView_r_tip_text_left_offset, tipTextLeftOffset);

        isScrollText = typedArray.getBoolean(R.styleable.RTextView_r_is_scroll_text, isScrollText);
        if (isScrollText) {
            setSingleLine(true);
            setEllipsize(null);
        }
        isScrollTextCircle = typedArray.getBoolean(R.styleable.RTextView_r_is_scroll_text_circle, isScrollTextCircle);
        scrollStep = typedArray.getInt(R.styleable.RTextView_r_scroll_step, scrollStep);
        scrollType = typedArray.getInt(R.styleable.RTextView_r_scroll_type, scrollType);
        if (!isInEditMode()) {
            scrollTextCircleOffset = (int) (10 * ScreenUtil.density());
        }
        scrollTextCircleOffset = typedArray.getDimensionPixelOffset(R.styleable.RTextView_r_scroll_text_offset, scrollTextCircleOffset);
        typedArray.recycle();

        initView();
    }

    public static void setLeftIco(TextView textView, @DrawableRes int leftIco) {
        Drawable[] compoundDrawables = textView.getCompoundDrawables();
        textView.setCompoundDrawablesWithIntrinsicBounds(ViewExKt.getDrawable(textView, leftIco),
                compoundDrawables[1], compoundDrawables[2], compoundDrawables[3]);
    }

    public static void setRightIco(TextView textView, @DrawableRes int rightIco) {
        Drawable[] compoundDrawables = textView.getCompoundDrawables();
        textView.setCompoundDrawablesWithIntrinsicBounds(compoundDrawables[0],
                compoundDrawables[1], ViewExKt.getDrawable(textView, rightIco), compoundDrawables[3]);
    }

    public static void setTopIco(TextView textView, @DrawableRes int topIco) {
        Drawable[] compoundDrawables = textView.getCompoundDrawables();
        textView.setCompoundDrawablesWithIntrinsicBounds(compoundDrawables[0], ViewExKt.getDrawable(textView, topIco),
                compoundDrawables[2], compoundDrawables[3]);
    }

    public static void setBottomIco(TextView textView, @DrawableRes int bottomIco) {
        Drawable[] compoundDrawables = textView.getCompoundDrawables();
        textView.setCompoundDrawablesWithIntrinsicBounds(
                compoundDrawables[0], compoundDrawables[1],
                compoundDrawables[2], ViewExKt.getDrawable(textView, bottomIco));
    }

    public void setTextLeftDrawable(@DrawableRes int id) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), id);
        if (drawable != null) {
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            textLeftDrawable = drawable;
        }
    }

    public void setLeftString(String leftString) {
        mLeftString = leftString;
        if (!TextUtils.isEmpty(mLeftString)) {
            float textWidth = mTextPaint.getTextWidth(mLeftString);
            setPadding((int) (mPaddingLeft + textWidth + textLeftOffset), getPaddingTop(), getPaddingRight(), getPaddingBottom());
        } else {
            setPadding(mPaddingLeft, getPaddingTop(), getPaddingRight(), getPaddingBottom());
        }
    }

    private void ensurePaint() {
        if (mTextPaint == null) {
            mTextPaint = new RTextPaint(getPaint());
            mTextPaint.setTextSize(leftStringSize);
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

    protected void onDrawScrollText(Canvas canvas) {
        String text = String.valueOf(getText());
        if (text == null) {
            if (isInEditMode()) {
                text = "会滚动的文本";
            } else {
                text = "";
            }
        }
        if (mScrollTextPaint == null) {
            mScrollTextPaint = new TextPaint(getPaint());
        }
        mScrollTextPaint.setColor(getCurrentTextColor());

        float textWidth = mScrollTextPaint.measureText(text);

        float drawTextY = getPaddingTop() - mScrollTextPaint.ascent();//getMeasuredHeight() - getPaddingBottom();

        if (scrollType == SCROLL_TYPE_DEFAULT) {
            canvas.drawText(text, getMeasuredWidth() - scrollCurX, drawTextY, mScrollTextPaint);

            if (isScrollTextCircle) {
                canvas.drawText(text, getMeasuredWidth() - scrollCurX + textWidth + scrollTextCircleOffset, drawTextY, mScrollTextPaint);
            }
        } else if (scrollType == SCROLL_TYPE_START) {
            canvas.drawText(text, -scrollCurX, drawTextY, mScrollTextPaint);

            if (isScrollTextCircle) {
                canvas.drawText(text, -scrollCurX + textWidth + scrollTextCircleOffset, drawTextY, mScrollTextPaint);
            }
        }

        scrollCurX += scrollStep;

        if (scrollType == SCROLL_TYPE_DEFAULT) {
            if (scrollCurX >= (getMeasuredWidth() + textWidth + scrollTextCircleOffset)) {
                scrollCurX = getMeasuredWidth();
            }
        } else if (scrollType == SCROLL_TYPE_START) {
            if (scrollCurX >= textWidth + scrollTextCircleOffset) {
                scrollCurX = 0;
            }
        }

        if (isInEditMode()) {
        } else {
            if (getVisibility() == View.VISIBLE && !pauseScroll) {
                postInvalidateOnAnimation();
            }
        }

        //canvas.drawText(text, );
        //canvas.translate(-100, 0);
        //super.onDraw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isScrollText) {
            onDrawScrollText(canvas);
            return;
        }

        if (hasUnderline) {
            getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
            getPaint().setAntiAlias(true);
        }

        int centerSaveCount = -1;
        if (centerLeftDrawable && textLeftDrawable != null) {
            centerSaveCount = canvas.save();
            canvas.translate((textLeftDrawable.getIntrinsicWidth() + getCompoundDrawablePadding()) / 2, 0);
        }
        super.onDraw(canvas);
        if (leftWidth > 0) {
            canvas.drawRect(leftColorRect, colorPaint);
        }

        ensurePaint();

        if (!TextUtils.isEmpty(mLeftString)) {
            //底部绘制文本
            //mTextPaint.drawOriginText(canvas, mLeftString, getPaddingLeft(), getMeasuredHeight() - getPaddingBottom());
            //居中绘制文本
            canvas.save();
            canvas.translate(-getPaddingLeft() + getScrollX(), 0);
            mTextPaint.setTextColor(leftStringColor);
            mTextPaint.setTextSize(leftStringSize);
//            mTextPaint.drawOriginText(canvas, mLeftString, getPaddingLeft() + mPaddingLeft,
//                    (getMeasuredHeight() - getPaddingBottom() - getPaddingTop()) / 2 +
//                            getPaddingTop() + mTextPaint.getTextHeight() / 2
//            );
            TextPaint textPaint = mTextPaint.getTextPaint();
            addPaintFlags(textPaint, leftStringBold, Paint.FAKE_BOLD_TEXT_FLAG);
            //textPaint.setFakeBoldText(leftStringBold);
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
//            canvas.drawText(mLeftString,
//                    getPaddingLeft() + mPaddingLeft,
//                    getPaddingTop() - fontMetrics.ascent + fontMetrics.descent /*fontMetrics.ascent + fontMetrics.descent*/,
//                    textPaint);
            float textY = 0;
            if (ExKt.have(getGravity(), Gravity.BOTTOM)) {
                if (isInEditMode()) {
                    textPaint.setColor(Color.RED);
                }
                textY = getMeasuredHeight() - getPaddingBottom() - fontMetrics.descent;
            } else if (ExKt.have(getGravity(), Gravity.TOP)) {
                if (isInEditMode()) {
                    textPaint.setColor(Color.GREEN);
                }
                textY = getPaddingTop() - fontMetrics.ascent;
            } else if (ExKt.have(getGravity(), Gravity.CENTER_VERTICAL)) {
                if (isInEditMode()) {
                    textPaint.setColor(Color.BLUE);
                }
                textY = getMeasuredHeight() - getPaddingBottom() - (fontMetrics.descent - fontMetrics.ascent) / 2;
            } else {
                if (isInEditMode()) {
                    textPaint.setColor(Color.YELLOW);
                }
                textY = getPaddingTop() - fontMetrics.ascent + fontMetrics.descent; /*fontMetrics.ascent + fontMetrics.descent*/
            }

            canvas.drawText(mLeftString,
                    getPaddingLeft() + mPaddingLeft,
                    textY,
                    textPaint);

            canvas.restore();
        }

        if (textLeftDrawable != null) {
            canvas.save();
//            Layout layout = getLayout();
            //layout.getLineStart()

//            canvas.translate(rawPaddingLeft + (viewDrawWith - drawWidth) / 2, paddingTop.toFloat() + (viewDrawHeight - drawHeight) / 2)

            canvas.translate(getMeasuredWidth() / 2 -
                            ViewExKt.textWidth(this, String.valueOf(getText())) / 2 -
                            textLeftDrawable.getIntrinsicWidth() -
                            getCompoundDrawablePadding(),
                    getMeasuredHeight() / 2 - textLeftDrawable.getIntrinsicHeight() / 2);

            textLeftDrawable.draw(canvas);

            canvas.restore();
        }

        if (showNoRead /*|| isInEditMode()*/) {
            //未读小红点
            TextPaint textPaint = mTextPaint.getTextPaint();
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setColor(Color.RED);
            //默认位置在右上角
            canvas.drawCircle(getMeasuredWidth() - noReadPaddingRight - noReadRadius, noReadPaddingTop + noReadRadius, noReadRadius, textPaint);
        }

        if (centerSaveCount != -1) {
            canvas.restoreToCount(centerSaveCount);
        }

        if (isShowTipText /*|| isInEditMode()*/) {
            TextPaint textPaint = mTextPaint.getTextPaint();
            textPaint.setStyle(Paint.Style.FILL);

            float textW = ViewExKt.textWidth(this, String.valueOf(getText()));
            float textH = ViewExKt.textHeight(this);

            textPaint.setTextSize(tipTextSize);
            float tipW = ViewExKt.textWidth(this, textPaint, tipText);
            float tipH = ViewExKt.textHeight(this, textPaint);

            tipTextRectF.set(getMeasuredWidth() - tipW,
                    getMeasuredHeight() - getPaddingBottom() - textH - tipH,
                    getMeasuredWidth(),
                    getMeasuredHeight() - getPaddingBottom() - textH);

            float round = 6 * density();
            tipTextRectF.inset(-round / 2, 0);//让背景和文本有点距离
            tipTextRectF.offset(-round, round);//尽量和原有的文本相贴合

            tipTextRectF.offset(-tipTextLeftOffset, -tipTextTopOffset);

            textPaint.setColor(tipTextBgColor);
            canvas.drawRoundRect(tipTextRectF, round, round, textPaint);

            textPaint.setColor(tipTextColor);
            //默认位置在右上角
            canvas.drawText(tipText, tipTextRectF.centerX() - tipW / 2, tipTextRectF.bottom - textPaint.descent(), textPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (aeqWidth) {
            int size = Math.max(getMeasuredWidth(), getMeasuredHeight());
            setMeasuredDimension(size, size);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (autoFixTextSize &&
                (!ExKt.have(getInputType(), EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE) || getMaxLines() == 1)) {
            while (getPaint().getTextSize() > 9 &&
                    ViewExKt.textWidth(this, getText().toString()) > ViewExKt.getViewDrawWith(this)) {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize() - 1 * density());
            }
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
        if (hideWithEmptyText) {
            setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
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
                                    getCurrentTextColor(), more),
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
        setMaxLength(length, false);
    }

    public void setMaxLength(int length, boolean addMoreStringLength) {
        InputFilter[] filters = getFilters();
        boolean have = false;
        InputFilter.LengthFilter lengthFilter = new InputFilter.LengthFilter(length + (addMoreStringLength ? getMoreString().length() : 0));
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

    private Drawable getDrawable(@DrawableRes int icoRes) {
        if (icoRes == -1) {
            return null;
        }
        return ContextCompat.getDrawable(getContext(), icoRes);
    }

    public void setLeftIco(@DrawableRes int leftIco) {
        setLeftIco(this, leftIco);
    }

    public void setRightIco(@DrawableRes int rightIco) {
        setRightIco(this, rightIco);
    }

    public void setTopIco(@DrawableRes int topIco) {
        setTopIco(this, topIco);
    }

    public void setBottomIco(@DrawableRes int bottomIco) {
        setBottomIco(this, bottomIco);
    }

    public void setLeftOffset(int leftOffset) {
        mLeftOffset = leftOffset;
    }

    public void setTopOffset(int topOffset) {
        mTopOffset = topOffset;
    }

    public void setBottomOffset(int bottomOffset) {
        mBottomOffset = bottomOffset;
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
        leftColorRect.set(mLeftOffset, getPaddingTop() + mTopOffset,
                mLeftOffset + leftWidth, viewHeight - getPaddingBottom() - mBottomOffset);
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
     * 判断 需要检测的开始位置,结束位置, 是否已经在其他span中
     */
    protected boolean isInOtherSpan(SpannableStringBuilder builder, int length, int startPosition, int endPosition) {
        return isInOtherSpan(builder, CharacterStyle.class, length, startPosition, endPosition);
    }

    protected <T> boolean isInOtherSpan(SpannableStringBuilder builder, @Nullable Class<T> kind, int length, int startPosition, int endPosition) {
        T[] spans = builder.getSpans(0, length, kind);
        List<int[]> spanRange = new ArrayList<>();
        for (T span : spans) {
            int spanStart = builder.getSpanStart(span);
            int spanEnd = builder.getSpanEnd(span);

            spanRange.add(new int[]{spanStart, spanEnd});
        }

        boolean result = false;
        for (int[] range : spanRange) {
            if (startPosition >= range[0] && startPosition <= range[1]) {
                result = true;
                break;
            }
            if (endPosition >= range[0] && endPosition <= range[1]) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * 设置高亮的关键字
     */
    public void setHighlightWord(String word) {
        setHighlightWord(word, false);
    }

    public void setHighlightWord(String word, boolean first) {
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

            if (first) {
                break;
            }
        }

        setTextEx(builder, BufferType.NORMAL);
    }

    /**
     * 设置是否显示删除线
     */
    public void setDeleteLine(boolean bool) {
        addFlags(bool, Paint.STRIKE_THRU_TEXT_FLAG);
    }

    /**
     * 设置是否显示下划线
     */
    public void setUnderLine(boolean bool) {
        addFlags(bool, Paint.UNDERLINE_TEXT_FLAG);
    }

    /**
     * 设置是否加粗文本
     */
    public void setBoldText(boolean bool) {
        addFlags(bool, Paint.FAKE_BOLD_TEXT_FLAG);
    }

    public void addFlags(boolean add, int flat) {
        TextPaint paint = getPaint();
        addPaintFlags(paint, add, flat);
    }


    public void addPaintFlags(TextPaint paint, boolean add, int flat) {
        if (add) {
            paint.setFlags(paint.getFlags() | flat);
        } else {
            paint.setFlags(paint.getFlags() & ~flat);
        }
    }

    /**
     * 是否显示未读小红点
     */
    public void setShowNoRead(boolean showNoRead) {
        this.showNoRead = showNoRead;
    }

    /**
     * 设置小红点半径
     */
    public void setNoReadRadius(float noReadRadius) {
        this.noReadRadius = noReadRadius;
    }

    public void setNoReadPaddingTop(float noReadPaddingTop) {
        this.noReadPaddingTop = noReadPaddingTop;
    }

    public void setNoReadPaddingRight(float noReadPaddingRight) {
        this.noReadPaddingRight = noReadPaddingRight;
    }

    public void setAutoFixTextSize(boolean autoFixTextSize) {
        this.autoFixTextSize = autoFixTextSize;
    }

    public void setShowTipText(boolean showTipText) {
        isShowTipText = showTipText;
    }

    public void setTipText(String tipText) {
        this.tipText = tipText;
    }

    public void setTipTextColor(int tipTextColor) {
        this.tipTextColor = tipTextColor;
    }

    public void setTipTextBgColor(int tipTextBgColor) {
        this.tipTextBgColor = tipTextBgColor;
    }

    public void setTipTextSize(int tipTextSize) {
        this.tipTextSize = tipTextSize;
    }

    public void setTipTextTopOffset(int tipTextTopOffset) {
        this.tipTextTopOffset = tipTextTopOffset;
    }

    public void setTipTextLeftOffset(int tipTextLeftOffset) {
        this.tipTextLeftOffset = tipTextLeftOffset;
    }

    public boolean isPauseScroll() {
        return pauseScroll;
    }

    public void setPauseScroll(boolean pauseScroll) {
        this.pauseScroll = pauseScroll;
    }
}
