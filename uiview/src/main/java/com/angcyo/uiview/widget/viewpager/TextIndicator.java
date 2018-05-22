package com.angcyo.uiview.widget.viewpager;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatTextView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import com.angcyo.github.utilcode.utils.SpannableStringUtils;
import com.angcyo.uiview.utils.Reflect;
import com.angcyo.uiview.widget.ExEditText;


/**
 * 类的描述：1/6 这样的ViewPager 指示器
 * 创建人员：Robi
 * 创建时间：2016/12/17 10:58 ~
 */
public class TextIndicator extends AppCompatTextView implements ViewPager.OnPageChangeListener {

    private ViewPager mViewPager;
    private int maxCount, currentCount;

    private boolean autoHide = true;
    private ExEditText mExEditText;
    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mExEditText != null) {
                if (mExEditText.isUseCharLengthFilter()) {
                    initIndicator(mExEditText.getCharLength() / 2, maxCount);
                } else {
                    setCurrentCount(s.length());
                }
            } else {
                setCurrentCount(s.length());
            }
        }
    };

    public TextIndicator(Context context) {
        super(context);
    }

    public TextIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode() && TextUtils.isEmpty(getText())) {
            initIndicator(1, 10);
        }
    }

    public void setupViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
        mViewPager.addOnPageChangeListener(this);
        initView();
    }

    public void setupEditText(EditText editText) {
        int max = 0;
        if (editText instanceof ExEditText && ((ExEditText) editText).isUseCharLengthFilter()) {
            initIndicator(((ExEditText) editText).getMaxCharLength() / 2, editText);
        } else {
            InputFilter[] filters = editText.getFilters();
            for (int i = 0; i < filters.length; i++) {
                InputFilter filter = filters[i];
                if (filter instanceof InputFilter.LengthFilter) {
                    Object mMax = Reflect.getMember(filter, "mMax");
                    if (mMax != null) {
                        max = (int) mMax;
                    }
                    initIndicator(max, editText);
                    break;
                }
            }
        }
    }

    public TextIndicator setCurrentCount(int currentCount) {
        this.currentCount = currentCount;
        updateText();
        return this;
    }

    private void updateText() {
        if (currentCount > maxCount) {
            setText(SpannableStringUtils.getBuilder(currentCount + "")
                    .setForegroundColor(Color.RED)
                    .append("/" + maxCount).create());
        } else {
            setText(currentCount + "/" + maxCount);
        }
    }

    public TextIndicator setMaxCount(int maxCount) {
        this.maxCount = maxCount;
        updateText();
        return this;
    }

    public TextIndicator initIndicator(int currentCount, int maxCount) {
        this.maxCount = maxCount;
        this.currentCount = currentCount;
        updateText();
        return this;
    }

    public TextIndicator initIndicator(int maxCount, final EditText editText) {
        if (editText instanceof ExEditText) {
            mExEditText = (ExEditText) editText;
            if (mExEditText.isUseCharLengthFilter()) {
                initIndicator(mExEditText.getCharLength() / 2, maxCount);
            } else {
                initIndicator(TextUtils.isEmpty(editText.getText()) ? 0 : editText.length(), maxCount);
            }
        } else {
            mExEditText = null;
            initIndicator(TextUtils.isEmpty(editText.getText()) ? 0 : editText.length(), maxCount);
        }
        editText.removeTextChangedListener(mTextWatcher);
        editText.addTextChangedListener(mTextWatcher);
        return this;
    }

    private void initView() {
        PagerAdapter adapter = mViewPager.getAdapter();
        if (autoHide) {
            if (adapter == null) {
                setVisibility(INVISIBLE);
            } else {
                setVisibility(VISIBLE);
                setText((mViewPager.getCurrentItem() + 1) + "/" + adapter.getCount());
            }
        } else {
            if (adapter == null) {
            } else {
                setText((mViewPager.getCurrentItem() + 1) + "/" + adapter.getCount());
            }
        }
    }

    public void setAutoHide(boolean autoHide) {
        this.autoHide = autoHide;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        initView();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * 合法数量
     */
    public boolean isInputCountValid() {
        return currentCount <= maxCount;
    }
}
