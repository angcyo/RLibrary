package com.angcyo.uiview.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.angcyo.library.utils.Anim;
import com.angcyo.library.utils.L;
import com.angcyo.uiview.R;
import com.angcyo.uiview.RApplication;
import com.angcyo.uiview.kotlin.ExKt;
import com.angcyo.uiview.resources.RAnimListener;
import com.angcyo.uiview.skin.SkinHelper;
import com.angcyo.uiview.utils.RTextPaint;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by angcyo on 2016-11-20.
 */

public class ExEditText extends AppCompatEditText {

    Rect clearRect = new Rect();//删除按钮区域
    boolean isDownIn = false;//是否在按钮区域按下
    Drawable clearDrawable;

    boolean showClear = true;//是否显示删除按钮

    boolean isPassword = false;//隐藏显示密码

    boolean handleTouch = false;

    long downTime = 0;//按下的时间

    /**
     * 是否当键盘弹出的时候, touch down事件隐藏键盘
     */
    boolean autoHideSoftInput = false;
    /**
     * 保存需要@的成员昵称
     */
    List<String> mAllMention = new ArrayList<>(5);
    /**
     * 用来限制输入的最大值, 需要inputType  包含 EditorInfo.TYPE_CLASS_NUMBER
     */
    float mMaxNumber = Float.MAX_VALUE;
    /**
     * 小数点后几位, 需要inputType  包含 EditorInfo.TYPE_NUMBER_FLAG_DECIMAL
     */
    int mDecimalCount = Integer.MAX_VALUE;
    private List<Range> mRangeArrayList = new ArrayList<>(5);
    private OnMentionInputListener mOnMentionInputListener;
    private boolean mIsSelected = false;
    private Range mLastSelectedRange;
    /**
     * 是否激活@功能,会检查文本 ,当调用{@link #setOnMentionInputListener(OnMentionInputListener)}后, 自动激活
     */
    private boolean enableMention = false;

    /**
     * 是否监听@字符输入
     */
    private boolean enableCallback = true;

    private RTextPaint mTextPaint;
    private String mLeftString;
    /**
     * 自动提示的文本
     */
    private String mInputTipText = "";
    /**
     * 模拟实现系统hint的效果, 好处在于可以使光标在hint字符串的右边
     */
    private String mRHintText;
    private List<String> mInputTipTextList = new ArrayList<>();
    private int mLeftOffset;
    private int mDrawLeftOffset;
    private int mPaddingLeft;
    private ValueAnimator rollAnim;
    private int lastRollAnimValue = -100;

    private boolean showContentMenu = true;

    public ExEditText(Context context) {
        super(context);
    }

    public ExEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public ExEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    /**
     * 为TextView设置显示@效果
     */
    public static void checkMentionSpannable(TextView textView, String content, List<String> allMention) {
        if (textView == null) {
            return;
        }
        if (allMention.isEmpty() || TextUtils.isEmpty(content)) {
            textView.setText(content);
            textView.setMovementMethod(ArrowKeyMovementMethod.getInstance());
            return;
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(content);
        int lastMentionIndex = -1;

        for (String mention : allMention) {
            Matcher matcher = Pattern.compile("@" + mention).matcher(content);

            while (matcher.find()) {
                String mentionText = matcher.group();
                int start;
                if (lastMentionIndex != -1) {
                    start = content.indexOf(mentionText, lastMentionIndex);
                } else {
                    start = content.indexOf(mentionText);
                }
                int end = start + mentionText.length();
                spannableStringBuilder.setSpan(new MentionSpan(mentionText), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        textView.setText(spannableStringBuilder);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public static boolean canVerticalScroll(EditText editText) {
        //滚动的距离
        int scrollY = editText.getScrollY();
        //控件内容的总高度
        int scrollRange = editText.getLayout().getHeight();
        //控件实际显示的高度
        int scrollExtent = editText.getHeight() - editText.getCompoundPaddingTop() - editText.getCompoundPaddingBottom();
        //控件内容总高度与实际显示高度的差值
        int scrollDifference = scrollRange - scrollExtent;

        if (scrollDifference == 0) {
            return false;
        }

        return (scrollY > 0) || (scrollY < scrollDifference - 1);
    }

    /**
     * 判断string是否是手机号码
     */
    public static boolean isPhone(String string) {
        if (TextUtils.isEmpty(string)) {
            return false;
        }
        final String phone = string.trim();
        return !TextUtils.isEmpty(phone) && phone.matches("^1[3-8]\\d{9}$");
    }

    public static float getInputNumber(Editable editable) {
        Float value;
        try {
            value = Float.valueOf(editable.toString());
        } catch (Exception e) {
            value = 0f;
        }
        return value;
    }

    public boolean unableCallback() {
        enableCallback = false;
        return enableCallback;
    }

    public boolean enableCallback() {
        enableCallback = true;
        return enableCallback;
    }

    public void setEnableMention(boolean enableMention) {
        this.enableMention = enableMention;
    }

    public void setMaxNumber(float mMaxNumber) {
        this.mMaxNumber = mMaxNumber;
    }

    public void setDecimalCount(int mDecimalCount) {
        this.mDecimalCount = mDecimalCount;
    }

    protected void initView(Context context, AttributeSet attrs) {
        mPaddingLeft = getPaddingLeft();

        ensurePaint();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExEditText);
        int color = typedArray.getColor(R.styleable.ExEditText_r_left_text_color, getCurrentTextColor());
        mLeftOffset = typedArray.getDimensionPixelOffset(R.styleable.ExEditText_r_left_text_offset,
                getResources().getDimensionPixelOffset(R.dimen.base_ldpi));
        String string = typedArray.getString(R.styleable.ExEditText_r_left_text);
        mRHintText = typedArray.getString(R.styleable.ExEditText_r_hint_text);

        mMaxNumber = typedArray.getFloat(R.styleable.ExEditText_r_max_number, mMaxNumber);
        mDecimalCount = typedArray.getInteger(R.styleable.ExEditText_r_decimal_count, mDecimalCount);

        setShowContentMenu(typedArray.getBoolean(R.styleable.ExEditText_r_show_content_menu, showContentMenu));

        typedArray.recycle();

        setLeftString(string);

        mTextPaint.setTextColor(color);
    }

    public boolean canVerticalScroll() {
        return canVerticalScroll(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

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

        if (isFocused()) {
            if (isInputTipPattern()) {
                //只处理了竖直居中的情况
                canvas.save();
                final TextPaint textPaint = getPaint();
                textPaint.setColor(SkinHelper.getTranColor(getCurrentTextColor(), 0x40));

                int lineHeight = getLayout().getLineDescent(0) - getLayout().getLineAscent(0);
                int top = getMeasuredHeight() / 2 - lineHeight / 2;
                int bottom = getPaddingTop() + (getMeasuredHeight() - getPaddingTop() - getPaddingBottom()) / 2 + lineHeight / 2;

                //只绘制末尾的文本区域
                canvas.clipRect(textPaint.measureText(String.valueOf(getText()), 0, getText().length()) + getInputTipDrawLeft(),
                        0, getMeasuredWidth(), getMeasuredHeight());
                canvas.drawText(mInputTipText, getInputTipDrawLeft(),
                        bottom - getLayout().getLineDescent(0),
                        textPaint);

                canvas.restore();
            }
        }

        //绘制RHint
        if (TextUtils.isEmpty(getText()) && TextUtils.isEmpty(getHint()) && !TextUtils.isEmpty(mRHintText)) {
            canvas.save();
            final TextPaint textPaint = getPaint();
            textPaint.setColor(getCurrentHintTextColor());
//            textPaint.setColor(Color.RED);

            int lineHeight = getLayout().getLineDescent(0) - getLayout().getLineAscent(0);
            int bottom = getPaddingTop() + (getMeasuredHeight() - getPaddingTop() - getPaddingBottom()) / 2 + lineHeight / 2;

            float x = 0, y = 0;
            y = bottom - getLayout().getLineDescent(0);

            if (ExKt.have(getGravity(), Gravity.LEFT)) {
                x = getInputTipDrawLeft();
            } else if (ExKt.have(getGravity(), Gravity.RIGHT)) {
                //right属性和inputType属性 同时存在,会产生无效果的BUG.请注意
                x = getMeasuredWidth() - getInputTipDrawRight() - textPaint.measureText(mRHintText);
            }

            canvas.drawText(mRHintText, x, y, textPaint);
            canvas.restore();
        }
    }

    private int getInputTipDrawLeft() {
        int left = getPaddingLeft();
        Drawable[] drawables = getCompoundDrawables();
        if (drawables[0] != null) {
            left += drawables[0].getIntrinsicWidth();
            left += getCompoundDrawablePadding();
        }
        return left;
    }

    private int getInputTipDrawRight() {
        int right = getPaddingRight();
        Drawable[] drawables = getCompoundDrawables();
        if (drawables[2] != null) {
            right += drawables[2].getIntrinsicWidth();
            right += getCompoundDrawablePadding();
        }
        return right;
    }

    private boolean isCenterVertical() {
        return Gravity.CENTER_VERTICAL == (getGravity() & Gravity.CENTER_VERTICAL);
    }

    private boolean isInputTipPattern() {

        if (mInputTipTextList.isEmpty() /*需要自动匹配的文本不能为空*/) {
            return false;
        }

        String text = getText().toString();

        if (TextUtils.isEmpty(text) /*当前文本框内容不能为空*/) {
            return false;
        }

        if (!isCenterVertical() /*必须是Gravity.CENTER_VERTICAL*/) {
            return false;
        }

//
////        return isCenterVertical() /*必须是Gravity.CENTER_VERTICAL*/ &&
////                !TextUtils.isEmpty(mInputTipText) /*需要自动匹配的文本不能为空*/ &&
////                !TextUtils.isEmpty(text) /*当前文本框内容不能为空*/ &&
////                mInputTipText.startsWith(text) &&
////                !TextUtils.equals(mInputTipText, text) /*匹配的内容如果已经一致了, 就没必要了.*/;

        mInputTipText = "";

        if (TextUtils.equals(Build.MODEL, "SCH-I545") /*三星这台手机执行下面的for循环会死机.*/) {
            return false;
        }

        for (int i = 0; i < mInputTipTextList.size(); i++) {
            String tipString = mInputTipTextList.get(i);
            if (!TextUtils.isEmpty(tipString) && tipString.startsWith(text)) {
                mInputTipText = tipString;
                break;
            }
        }

        if (TextUtils.isEmpty(mInputTipText) /*没有匹配到*/) {
            return false;
        }

        if (TextUtils.equals(mInputTipText, text) /*匹配的内容如果已经一致了, 就没必要了.*/) {
            return false;
        }

        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initView();
        ensurePaint();
    }

    @Override
    public void setTag(Object tag) {
        super.setTag(tag);
        initView();
    }

    private void ensurePaint() {
        if (mTextPaint == null) {
            mTextPaint = new RTextPaint(getPaint());
        }
    }

    public void setInputText(String text) {
        setText(text);
        setSelection(TextUtils.isEmpty(text) ? 0 : text.length());
        //checkEdit(true);
    }

    /**
     * 光标定位在文本的最后面
     */
    public void setSelectionLast() {
        setSelection(TextUtils.isEmpty(getText()) ? 0 : getText().length());
    }

    private void initView() {
        Object tag = getTag();
        if (tag != null) {
            String tagString = String.valueOf(tag);
            if (tagString.contains("emoji")) {
                //激活emoji表情过滤
                addFilter(new EmojiFilter());
            }

            if (tagString.contains("password")) {
                //隐藏显示密码
                isPassword = true;
            }

            if (tagString.contains("hide")) {
                //隐藏删除按钮
                showClear = false;
            } else if (tagString.contains("show")) {
                //显示删除按钮
                showClear = true;
            }
        }
        getClearDrawable();
    }

    private Drawable getClearDrawable() {
        if (showClear && clearDrawable == null) {
            clearDrawable = ResourcesCompat.getDrawable(
                    getResources(),
                    R.drawable.base_edit_delete_selector,
                    getContext().getTheme());

            if (getCompoundDrawablePadding() == 0) {
                setCompoundDrawablePadding((int) (4 * getResources().getDisplayMetrics().density));
            }
        }
        return clearDrawable;
    }

    private void addFilter(InputFilter filter) {
        final InputFilter[] filters = getFilters();
        final InputFilter[] newFilters = new InputFilter[filters.length + 1];
        System.arraycopy(filters, 0, newFilters, 0, filters.length);
        newFilters[filters.length] = filter;
        setFilters(newFilters);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        checkEdit(focused);

        if (!focused) {
            //没有焦点的时候, 检查自动匹配输入
            if (isInputTipPattern()) {
                setText(mInputTipText);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (showClear) {
            clearRect.set(w - getPaddingRight() - getClearDrawable().getIntrinsicWidth(),
                    getPaddingTop(), w - getPaddingRight(), Math.min(w, h) - getPaddingBottom());
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        if (showClear && isFocused()) {
            if (action == MotionEvent.ACTION_DOWN) {
                isDownIn = checkClear(event.getX(), event.getY());
                updateState(isDownIn);
            } else if (action == MotionEvent.ACTION_MOVE) {
                updateState(checkClear(event.getX(), event.getY()));
            } else if (action == MotionEvent.ACTION_UP) {
                updateState(false);
                if (isDownIn && checkClear(event.getX(), event.getY())) {
                    if (!TextUtils.isEmpty(getText())) {
                        setText("");
                        return true;
                    }
                }
                isDownIn = false;

                if (autoHideSoftInput && isSoftKeyboardShow()) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            hideSoftInput();
                        }
                    });
                }
            } else if (action == MotionEvent.ACTION_CANCEL) {
                updateState(false);
                isDownIn = false;
            }
        }

        //L.e("call: onTouchEvent([event])-> canVerticalScroll:" + canVerticalScroll(this));

//        if (isPassword) {
//            if (action == MotionEvent.ACTION_DOWN) {
//                downTime = System.currentTimeMillis();
//            } else if (action == MotionEvent.ACTION_MOVE) {
//                if ((System.currentTimeMillis() - downTime) > 100) {
//                    if (isDownIn) {
//                        hidePassword();
//                    } else {
//                        showPassword();
//                    }
//                }
//            } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
//                hidePassword();
//            }
//        }
        return super.onTouchEvent(event);
    }

    private void updateState(boolean isDownIn) {
        final Drawable clearDrawable = getCompoundDrawables()[2];
        if (clearDrawable == null) {
            return;
        }
        if (isDownIn) {
            clearDrawable.setState(new int[]{android.R.attr.state_checked});
        } else {
            clearDrawable.setState(new int[]{});
        }
    }

    public void checkEdit(boolean focused) {
        if (showClear) {
            final Drawable[] compoundDrawables = getCompoundDrawables();
            if (TextUtils.isEmpty(getText()) || !focused) {
                if (compoundDrawables[2] != null) {
                    setCompoundDrawablesWithIntrinsicBounds(compoundDrawables[0], compoundDrawables[1],
                            null, compoundDrawables[3]);
                }
            } else {
                setError(null);
                setCompoundDrawablesWithIntrinsicBounds(compoundDrawables[0], compoundDrawables[1],
                        getClearDrawable(), compoundDrawables[3]);
            }
        }
    }

    private boolean checkClear(float x, float y) {
        return clearRect.contains(((int) x), (int) y);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        checkEdit(isFocused());
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        checkDebugCmd(text);
        checkEdit(isFocused());
        if (enableMention) {
            checkMentionString();
        }

        if (isInputTypeNumber() &&
                mMaxNumber != Float.MAX_VALUE &&
                mDecimalCount != Integer.MAX_VALUE) {

            if (!TextUtils.isEmpty(text)) {
                Float value;
                try {
                    value = Float.valueOf(String.valueOf(text));
                } catch (Exception e) {
                    value = 0f;
                }

                //限制最大数值
                if (value > mMaxNumber) {
                    String maxValue;
                    if (isInputTypeDecimal()) {
                        maxValue = Float.valueOf(mMaxNumber).toString();
                    } else {
                        maxValue = String.valueOf(Float.valueOf(mMaxNumber).intValue());
                    }
                    resetSelectionText(maxValue, 0);
                    setSelection(maxValue.length());
                }

                if (isInputTypeDecimal()) {
                    //显示小数点后几位
                    String string = String.valueOf(text);
                    int lastIndexOf = string.lastIndexOf(".");
                    if (lastIndexOf != -1 && string.length() - lastIndexOf - 1 > mDecimalCount) {
                        resetSelectionText(string.substring(0, lastIndexOf + mDecimalCount + 1), 0);
                    }
                }

                //剔除前面的0
                if (text.length() > 1) {
                    if (text.charAt(0) == '0' && text.charAt(1) != '.') {
                        resetSelectionText(String.valueOf(text.subSequence(1, text.length())), 0);
                    }
                    //如果是小数点开头,补齐0
                    if (text.charAt(0) == '.') {
                        resetSelectionText("0" + text, 1);
                    }
                }
            }
        }
    }

    /**
     * 输入类型是否是数字
     */
    private boolean isInputTypeNumber() {
        return (getInputType() & EditorInfo.TYPE_CLASS_NUMBER) == EditorInfo.TYPE_CLASS_NUMBER;
    }

    /**
     * 输入类型是否是小数
     */
    private boolean isInputTypeDecimal() {
        return (getInputType() & EditorInfo.TYPE_NUMBER_FLAG_DECIMAL) == EditorInfo.TYPE_NUMBER_FLAG_DECIMAL;
    }

    private void resetSelectionText(String text, int startOffset) {
        int start = getSelectionStart();
        setText(text);
        setSelection(Math.min(start + startOffset, text.length()));
    }

    /**
     * 获取输入的数字
     */
    public float getInputNumber() {
        return getInputNumber(getText());
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);

        if (!enableMention) {
            return;
        }

        //avoid infinite recursion after calling setSelection()
        if (mLastSelectedRange != null && mLastSelectedRange.isEqual(selStart, selEnd)) {
            return;
        }

        //if user cancel a selection of show string, reset the state of 'mIsSelected'
        Range closestRange = getRangeOfClosestMentionString(selStart, selEnd);
        if (closestRange != null && closestRange.to == selEnd) {
            mIsSelected = false;
        }

        Range nearbyRange = getRangeOfNearbyMentionString(selStart, selEnd);
        //if there is no show string nearby the cursor, just skip
        if (nearbyRange == null) {
            return;
        }

        //forbid cursor located in the show string.
        if (selStart == selEnd) {
            setSelection(nearbyRange.getAnchorPosition(selStart));
        } else {
            if (selEnd < nearbyRange.to) {
                setSelection(selStart, nearbyRange.to);
            }
            if (selStart > nearbyRange.from) {
                setSelection(nearbyRange.from, selEnd);
            }
        }
    }

    private boolean hasPasswordTransformation() {
        return this.getTransformationMethod() instanceof PasswordTransformationMethod;
    }

    public void showPassword() {
        final int selection = getSelectionEnd();
        setTransformationMethod(null);
        setSelection(selection);
    }

    public void hidePassword() {
        final int selection = getSelectionEnd();
        setTransformationMethod(PasswordTransformationMethod.getInstance());
        setSelection(selection);
    }

    void passwordVisibilityToggleRequested() {
        final int selection = getSelectionEnd();

        if (hasPasswordTransformation()) {
            setTransformationMethod(null);
        } else {
            setTransformationMethod(PasswordTransformationMethod.getInstance());
        }

        // And restore the cursor position
        setSelection(selection);
    }

    /**
     * 判断是否是手机号码
     */
    public boolean isPhone() {
        return isPhone(string());
    }

    //------------------------------------@功能支持-----------------------------------------//

    /**
     * 判断是否是有效
     */
    public boolean isPassword() {
        final String string = string().trim();
        return !TextUtils.isEmpty(string) && string.matches("^[a-zA-Z0-9_-]{6,12}$");
    }

    /**
     * 判断键盘是否显示
     */
    public boolean isSoftKeyboardShow() {
        int screenHeight = getScreenHeightPixels();
        int keyboardHeight = getSoftKeyboardHeight();
        return screenHeight != keyboardHeight && keyboardHeight > 100;
    }

    public void hideSoftInput() {
        InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    public void setAutoHideSoftInput(boolean autoHideSoftInput) {
        this.autoHideSoftInput = autoHideSoftInput;
    }

    /**
     * 获取键盘的高度
     */
    public int getSoftKeyboardHeight() {
        int screenHeight = getScreenHeightPixels();
        Rect rect = new Rect();
        getWindowVisibleDisplayFrame(rect);
        int visibleBottom = rect.bottom;
        return screenHeight - visibleBottom;
    }

    /**
     * 屏幕高度(不包含虚拟导航键盘的高度)
     */
    private int getScreenHeightPixels() {
        return getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 长度
     */
    public int length() {
        return string().length();
    }

    public String string() {
        String rawText = getText().toString().trim();
        String tipText = mInputTipText.trim();

        if (TextUtils.isEmpty(rawText)) {
            return rawText;
        } else if (TextUtils.isEmpty(tipText)) {
            return rawText;
        } else {
            return tipText;
        }
    }

    public boolean isEmpty() {
        return TextUtils.isEmpty(string());
    }

    public void setMaxLength(int length) {
        InputFilter[] filters = getFilters();
        boolean have = false;
        InputFilter.LengthFilter lengthFilter = new InputFilter.LengthFilter(length);
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
    }

    /**
     * 监听键盘按键
     */
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new HackInputConnection(super.onCreateInputConnection(outAttrs), true, this);
    }

    /**
     * 添加需要@的人
     */
    public void addMention(String mention) {
        addMention(mention, true);
    }

    /**
     * @param auto 如果为true时, 自动上屏和去重
     */
    public void addMention(String mention, boolean auto) {
        int start = getSelectionStart();
        boolean insetAt = false;
        if (start == 0 || (start > 0 && '@' != getText().charAt(start - 1))) {
            // mention = "@" + mention;//同时@多个人时, 自动补齐@字符
            insetAt = true;
        }

        if (auto) {
            if (isContains(mention)) {
                if ('@' == getText().charAt(start - 1)) {
                    deleteLast(start);
                }
            } else {
                mAllMention.add(mention);
                insert((insetAt ? '@' : "") + mention + ' ');
            }
        } else {
            mAllMention.add(mention);
        }
    }

    /**
     * 在指定的位置插入text
     */
    public void insert(int where, CharSequence text) {
        if (text == null) {
            return;
        }
        int start = getSelectionStart();
        int end = getSelectionEnd();
        int length = text.length();

        if (start == end) {
            getText().insert(where, text);
            setSelection(where + length, where + length);
        } else {
            getText().replace(start, end, text, 0, length);
            setSelection(start + length, start + length);
        }
    }

    public void insert(CharSequence text) {
        insert(getSelectionStart(), text);
    }

    /**
     * 是否已经添加了
     */
    public boolean isContains(String mention) {
        return mAllMention.contains(mention);
    }

    public void removeMention(String mention) {
        mAllMention.remove(mention);
    }

    /**
     * 删除最后一个字符,用来当@的人, 已经存在时,调用
     */
    public void deleteLast(int position) {
        Editable text = getText();
        if (text != null && text.length() >= position) {
            text.delete(position - 1, position);
        }
    }

    public List<String> getAllMention() {
        return mAllMention;
    }

    public void setOnMentionInputListener(OnMentionInputListener onMentionInputListener) {
        enableMention = true;
        if (mOnMentionInputListener == null) {
            //监听@字符输入
            addTextChangedListener(new MentionTextWatcher());
            //为@span添加点击事件支持
            //setMovementMethod(LinkMovementMethod.getInstance());
        }
        mOnMentionInputListener = onMentionInputListener;
    }

    private Range getRangeOfClosestMentionString(int selStart, int selEnd) {
        if (mRangeArrayList == null) {
            return null;
        }
        for (Range range : mRangeArrayList) {
            if (range.contains(selStart, selEnd)) {
                return range;
            }
        }
        return null;
    }

    private Range getRangeOfNearbyMentionString(int selStart, int selEnd) {
        if (mRangeArrayList == null) {
            return null;
        }
        for (Range range : mRangeArrayList) {
            if (range.isWrappedBy(selStart, selEnd)) {
                return range;
            }
        }
        return null;
    }

    //检查@span
    private void checkMentionString() {
        //reset state
        mIsSelected = false;
        if (mRangeArrayList != null) {
            mRangeArrayList.clear();
        }

        Editable spannableText = getText();
        if (mAllMention.isEmpty() || spannableText == null || TextUtils.isEmpty(spannableText.toString())) {
            mAllMention.clear();
            return;
        }

        //remove previous spans
        MentionSpan[] oldSpans = spannableText.getSpans(0, spannableText.length(), MentionSpan.class);
        for (MentionSpan oldSpan : oldSpans) {
            spannableText.removeSpan(oldSpan);
        }

        //find show string and color it
        int lastMentionIndex = -1;
        String text = spannableText.toString();

        //筛选一下, 防止被删除了, 缺还在List中
        List<String> mentions = new ArrayList<>();
        for (String mention : mAllMention) {
            Matcher matcher = Pattern.compile("@" + mention).matcher(text);

            boolean isFind = false;
            while (matcher.find()) {
                isFind = true;
                String mentionText = matcher.group();
                int start;
                if (lastMentionIndex != -1) {
                    start = text.indexOf(mentionText, lastMentionIndex);
                } else {
                    start = text.indexOf(mentionText);
                }
                int end = start + mentionText.length();
                spannableText.setSpan(new MentionSpan(text.substring(start, end)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                lastMentionIndex = end;
                //recordRunnable all show-string's position
                mRangeArrayList.add(new Range(start, end));
            }
            if (!isFind) {
                //有特殊字符比如:()会匹配不到
                String temp = "@" + mention;
                if (text.contains(temp)) {
                    int start = text.indexOf(temp);
                    int end = start + temp.length();
                    spannableText.setSpan(new MentionSpan(text.substring(start, end)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mRangeArrayList.add(new Range(start, end));
                    isFind = true;
                }
            }

            if (isFind) {
                mentions.add(mention);
            }
        }
        mAllMention.clear();
        mAllMention.addAll(mentions);

        if (mOnMentionInputListener != null) {
            mOnMentionInputListener.onMentionTextChanged(mAllMention);
        }
    }

    /**
     * 替换@功能的文本信息
     */
    public String fixMentionString(getIdFromUserName getIdFromUserName) {
        String string = string();
        List<String> allMention = getAllMention();
        for (String s : allMention) {
            string = string.replaceAll("@" + s.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)"),
                    createStringWithUserName(getIdFromUserName.userId(s)));
        }
        return string;
    }

    public String fixShowMentionString(getIdFromUserName getIdFromUserName) {
        String string = string();
        List<String> allMention = getAllMention();
        for (String s : allMention) {
            string = string.replaceAll("@" + s.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)"),
                    createShowString(getIdFromUserName.userId(s), "@" + s));
        }
        return string;
    }

    private String createStringWithUserName(String id) {
        return "<m>" + id + "</m>";
    }

    private String createShowString(String id, String name) {
        return String.format(Locale.CHINA, "<m id='%s'>%s</m>", id, name);//  "<m>" + id + "</m>";//<m id='60763'>@爱你是一种习惯i<\/m>
    }

    @Override
    public void setError(CharSequence error) {
        super.setError(error);
    }

    @Override
    public void setError(CharSequence error, Drawable icon) {
        requestFocus();
        super.setError(error, icon);
    }

    /**
     * 设置文本, 并且自动获取焦点和自动选择到最后
     */
    public void setExText(CharSequence text) {
        requestFocus();
        setText(text);
        setSelection(TextUtils.isEmpty(text) ? 0 : text.length());
    }

    /**
     * 错误提示
     */
    public void error() {
        Anim.band(this);
    }

    /**
     * 如果为空, 自动执行动画提示.
     * 返回结果表示是否为空
     */
    public boolean checkEmpty() {
        return checkEmpty(false);
    }

    /**
     * 返回结果表示是否为空
     */
    public boolean checkEmpty(boolean checkPhone) {
        if (isEmpty()) {
            error();
            requestFocus();
            if (!isSoftKeyboardShow()) {
                if (getParent() instanceof FrameLayout &&
                        getParent().getParent() instanceof TextInputLayout) {
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            RSoftInputLayout.showSoftInput(ExEditText.this);
                        }
                    }, 200);
                } else {
                    RSoftInputLayout.showSoftInput(ExEditText.this);
                }
            }
            return true;
        }
        if (checkPhone) {
            if (isPhone()) {

            } else {
                error();
                requestFocus();
                return true;
            }
        }
        return false;
    }

    public boolean checkMinLength(int minLength) {
        if (checkEmpty()) {
            return true;
        }
        if (string().length() < minLength) {
            error();
            requestFocus();
            return true;
        }
        return false;
    }

    public void setShowContentMenu(boolean showContentMenu) {
        this.showContentMenu = showContentMenu;
        if (showContentMenu) {
        } else {
            setLongClickable(false);
        }
    }

    /**
     * 设置是否是密码类型
     */
    public void setIsPassword(boolean isPassword) {
        int inputType = getInputType();
        if (isPassword) {
            setShowContentMenu(false);
            setInputType(inputType | EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            setInputType(inputType & ~EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

//    public void setIsWebEmail(boolean isWebEmail) {
//        int inputType = getInputType();
//        if (isWebEmail) {
//            setInputType(inputType | EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
//        } else {
//            setInputType(inputType & ~EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
//        }
//    }

    public void setIsText(boolean isText) {
        int inputType = getInputType();
        if (isText) {
            setIsPassword(false);
            setInputType(EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        } else {
            setInputType(inputType & ~EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        }
    }

    public void setIsPhone(boolean isPhone, int maxLength) {
        setIsNumber(isPhone, false, maxLength);
    }

    public void setIsNumber(boolean isNumber, boolean decimal, int maxLength) {
        int inputType = getInputType();
        if (isNumber) {
            if (decimal) {
                setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
            } else {
                setInputType(EditorInfo.TYPE_CLASS_NUMBER);
            }
        } else {
            setInputType(inputType & ~EditorInfo.TYPE_CLASS_NUMBER & ~EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
        }

        if (maxLength < 0) {
            maxLength = Integer.MAX_VALUE;
        }

        setMaxLength(maxLength);
    }

    public void setLeftString(String leftString) {
        mLeftString = leftString;
        if (!TextUtils.isEmpty(mLeftString)) {
            float textWidth = mTextPaint.getTextWidth(mLeftString);
            mDrawLeftOffset = (int) textWidth + mLeftOffset;
            setPadding(mPaddingLeft + mDrawLeftOffset, getPaddingTop(), getPaddingRight(), getPaddingBottom());
        } else {
            setPadding(mPaddingLeft, getPaddingTop(), getPaddingRight(), getPaddingBottom());
        }
    }

    /**
     * 设置自动匹配提示文本
     */
    public void setInputTipText(String inputTipText) {
        mInputTipTextList.clear();
        mInputTipTextList.add(inputTipText);
    }

    public void setInputTipTextList(List<String> list) {
        mInputTipTextList.clear();
        mInputTipTextList.addAll(list);
    }

    /**
     * 随机滚动数值至
     *
     * @param toValue   滚动结束后的数值
     * @param maxValue  滚动过程中最大的数值
     * @param minValue  滚动过程中最小的数值
     * @param threshold 控制阈值, 阈值越大, 跳动的越慢
     */
    public void rollTo(final float toValue, final float minValue, final float maxValue, final int threshold) {
        if (rollAnim != null) {
            rollAnim.cancel();
        }
        lastRollAnimValue = -100;
        final Random random = new Random(System.nanoTime());
        rollAnim = ValueAnimator.ofInt(0, 100);
        rollAnim.setInterpolator(new LinearInterpolator());
        rollAnim.setDuration(300);
        rollAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                if ((value - lastRollAnimValue) >= threshold) {
                    lastRollAnimValue = value;
                    setInputText(String.format(Locale.CHINA, "%.2f", minValue + (maxValue - minValue) * random.nextFloat()));
                }
            }
        });
        rollAnim.addListener(new RAnimListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setInputText(String.format(Locale.CHINA, "%.2f", toValue));
            }
        });
        rollAnim.start();
    }

    @Override
    public boolean showContextMenu() {
        //L.e("call: showContextMenu([])-> ");
        if (showContentMenu) {
            return super.showContextMenu();
        } else {
            setLongClickable(false);
            return false;
        }
    }

    private void checkDebugCmd(CharSequence text) {
        if ("_cmd:open_debug".equalsIgnoreCase(String.valueOf(text))) {
            L.LOG_DEBUG = true;
            GlideImageView.Companion.setDEBUG_SHOW(true);
        }
    }

    public interface getIdFromUserName {
        String userId(String userName);
    }

    /**
     * Listener for '@' character
     */
    public interface OnMentionInputListener {
        /**
         * call when '@' character is inserted into EditText, 当输入@字符之后, 会回调
         */
        void onMentionCharacterInput();

        /**
         * 所有@的成员
         */
        void onMentionTextChanged(List<String> allMention);
    }

    /**
     * {@code @}文本样式Span
     */
    public static class MentionSpan extends ClickableSpan {

        String mention;

        public MentionSpan(String mention) {
            this.mention = mention;
        }

        @Override
        public void onClick(View widget) {
            L.i("onClick @: " + mention);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            //设置背景色
            ds.bgColor = RApplication.getApp().getResources().getColor(R.color.theme_color_primary_dark_tran3);
            //设置前景色
            //ds.setColor(getResources().getColor(R.color.theme_color_accent));
        }
    }

    /**
     * 用来监听@字符输入
     */
    private class MentionTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int index, int i1, int count) {
            if (!enableCallback) {
                return;
            }
            if (count == 1 && !TextUtils.isEmpty(charSequence)) {
                char mentionChar = charSequence.toString().charAt(index);
                if ('@' == mentionChar && mOnMentionInputListener != null) {
                    mOnMentionInputListener.onMentionCharacterInput();
                }
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            //文本内容为空时, 解决光标不显示的BUG
            if (editable.length() > 0) {
                setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                setMovementMethod(getDefaultMovementMethod());
            }
        }
    }

//    @Override
//    protected void onCreateContextMenu(ContextMenu menu) {
//        L.e("call: onCreateContextMenu([menu])-> ");
//        super.onCreateContextMenu(menu);
//    }

    /**
     * 用来处理按下删除键的时候,删除整个@文本内容
     */
    private class HackInputConnection extends InputConnectionWrapper {
        private ExEditText editText;

        public HackInputConnection(InputConnection target, boolean mutable, ExEditText editText) {
            super(target, mutable);
            this.editText = editText;
        }

        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            //editText.getText();
            return super.commitText(text, newCursorPosition);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                int selectionStart = editText.getSelectionStart();
                int selectionEnd = editText.getSelectionEnd();
                Range closestRange = getRangeOfClosestMentionString(selectionStart, selectionEnd);
                if (closestRange == null) {
                    mIsSelected = false;
                    return super.sendKeyEvent(event);
                }
                //if show string has been selected or the cursor is at the beginning of show string, just use default action(delete)
                if (mIsSelected || selectionStart == closestRange.from) {
                    mIsSelected = false;
                    return super.sendKeyEvent(event);
                } else {
                    //select the show string
                    mIsSelected = true;
                    mLastSelectedRange = closestRange;
                    setSelection(closestRange.to, closestRange.from);
                    return super.sendKeyEvent(event);//调用此方法, 删除整个@文本内容
                }
                //return true;
            }
            return super.sendKeyEvent(event);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            if (beforeLength == 1 && afterLength == 0) {
                return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }
            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }

//    @Override
//    public boolean showContextMenu(float x, float y) {
//        L.e("call: showContextMenu([x, y])-> ");
//        return super.showContextMenu(x, y);
//    }
//
//    @Override
//    public boolean onTextContextMenuItem(int id) {
//        L.e("call: onTextContextMenuItem([id])-> ");
//        return super.onTextContextMenuItem(id);
//    }
//
//    @Override
//    public void setOnCreateContextMenuListener(OnCreateContextMenuListener l) {
//        L.e("call: setOnCreateContextMenuListener([l])-> ");
//        super.setOnCreateContextMenuListener(l);
//    }
//
//    @Override
//    public void createContextMenu(ContextMenu menu) {
//        L.e("call: createContextMenu([menu])-> ");
//        super.createContextMenu(menu);
//    }
//
//
//    @Override
//    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
//        L.e("call: getContextMenuInfo([])-> ");
//        return super.getContextMenuInfo();
//    }

    /**
     * {@code @}文本的开始位置和结束位置
     */
    private class Range {
        int from;
        int to;

        public Range(int from, int to) {
            this.from = from;
            this.to = to;
        }

        public boolean isWrappedBy(int start, int end) {
            return (start > from && start < to) || (end > from && end < to);
        }

        public boolean contains(int start, int end) {
            return from <= start && to >= end;
        }

        public boolean isEqual(int start, int end) {
            return (from == start && to == end) || (from == end && to == start);
        }

        public int getAnchorPosition(int value) {
            if ((value - from) - (to - value) > 0) {
                return to;
            } else {
                return from;
            }
        }
    }
}
