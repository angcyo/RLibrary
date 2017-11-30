package com.angcyo.uiview.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.angcyo.uiview.R;
import com.angcyo.uiview.kotlin.ViewGroupExKt;
import com.angcyo.uiview.resources.ResUtil;
import com.angcyo.uiview.skin.SkinHelper;

import java.util.ArrayList;
import java.util.List;

import kotlin.jvm.functions.Function0;

/**
 * Created by angcyo on 15-10-22-022.
 */
public class RFlowLayout extends LinearLayout {
    /**
     * The M all views.
     */
    List<List<View>> mAllViews = new ArrayList<>();//保存所有行的所有View
    /**
     * The M line height.
     */
    List<Integer> mLineHeight = new ArrayList<>();//保存每一行的行高

    List<View> lineViews = new ArrayList<>();

    /**
     * Instantiates a new Flow radio group.
     *
     * @param context the context
     */
    public RFlowLayout(Context context) {
        this(context, null);
    }

    /**
     * Instantiates a new Flow radio group.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public RFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(LinearLayout.HORIZONTAL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        int width = 0, height = 0;
        int lineWidth = 0, lineHeight = 0;
        int childWidth = 0, childHeight = 0;

        mAllViews.clear();
        mLineHeight.clear();
        lineViews.clear();

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }

            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) child.getLayoutParams();

            childWidth = child.getMeasuredWidth() + params.leftMargin + params.rightMargin;
            childHeight = child.getMeasuredHeight() + params.topMargin + params.bottomMargin;

            if (lineWidth + childWidth > sizeWidth - getPaddingLeft() - getPaddingRight()) {
                width = Math.max(width, lineWidth);
                height += lineHeight;
                mLineHeight.add(lineHeight);
                mAllViews.add(lineViews);

                lineWidth = childWidth;
                lineHeight = childHeight;
                lineViews = new ArrayList<>();
            } else {
                lineWidth += childWidth;
                lineHeight = Math.max(childHeight, lineHeight);
            }
            lineViews.add(child);

            if (i == (count - 1)) {
                width = Math.max(width, lineWidth);
                height += lineHeight;
            }
        }
        mLineHeight.add(lineHeight);
        mAllViews.add(lineViews);
        width += getPaddingLeft() + getPaddingRight();
        height += getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(
                Math.max((modeWidth == MeasureSpec.AT_MOST || modeWidth == MeasureSpec.UNSPECIFIED) ? width : sizeWidth,
                        getMinimumWidth()),
                Math.max((modeHeight == MeasureSpec.AT_MOST || modeHeight == MeasureSpec.UNSPECIFIED) ? height : sizeHeight,
                        getMinimumHeight()));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int top = getPaddingTop();//开始布局子view的 top距离
        int left = getPaddingLeft();//开始布局子view的 left距离

        int lineNum = mAllViews.size();//行数
        List<View> lineView;
        int lineHeight;
        for (int i = 0; i < lineNum; i++) {
            lineView = mAllViews.get(i);
            lineHeight = mLineHeight.get(i);

            for (int j = 0; j < lineView.size(); j++) {
                View child = lineView.get(j);
                if (child.getVisibility() == View.GONE) {
                    continue;
                }

                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) child.getLayoutParams();
                int ld = left + params.leftMargin;
                int td = top + params.topMargin;
                int rd = ld + child.getMeasuredWidth();//不需要加上 params.rightMargin,
                int bd = td + child.getMeasuredHeight();//不需要加上 params.bottomMargin, 因为在 onMeasure , 中已经加在了 lineHeight 中
                child.layout(ld, td, rd, bd);

                left += child.getMeasuredWidth() + params.leftMargin + params.rightMargin;//因为在 这里添加了;
            }

            left = getPaddingLeft();
            top += lineHeight;
        }
    }

    private int getDimensionPixelOffset(@DimenRes int id) {
        return getResources().getDimensionPixelOffset(id);
    }

    private int getColor(@ColorRes int id) {
        return ContextCompat.getColor(getContext(), id);
    }

    public RTextCheckView addCheckTextView(String text) {
        RTextCheckView textView = new RTextCheckView(getContext());
        textView.setText(text);
        textView.setGravity(Gravity.CENTER);

        int lineSize = getDimensionPixelOffset(R.dimen.base_line);
        int radius = getDimensionPixelOffset(R.dimen.base_round_little_radius);
        int offset = getDimensionPixelOffset(R.dimen.base_xxhdpi);
        textView.setPadding(offset, offset / 4, offset, offset / 4);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.rightMargin = offset / 2;
        params.topMargin = radius;
        params.bottomMargin = radius;
        textView.setLayoutParams(params);

        textView.setBackground(ResUtil.selectorChecked(
                ResUtil.createDrawable(getColor(R.color.default_base_line), Color.TRANSPARENT, lineSize, radius),
                ResUtil.createDrawable(SkinHelper.getSkin().getThemeTranColor(0x80), radius)
        ));

        addView(textView);
        return textView;
    }

    public List<String> getCheckedTextList() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof RTextCheckView) {
                if (((RTextCheckView) childAt).isChecked()) {
                    list.add(String.valueOf(((RTextCheckView) childAt).getText()));
                }
            }
        }
        return list;
    }

    public RTextCheckView addTagTextView(String text) {
        RTextCheckView textView = new RTextCheckView(getContext());
        textView.setText(text);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(getColor(R.color.base_text_color_dark));
        textView.setEnabled(false);//不允许点击

        int radius = getDimensionPixelOffset(R.dimen.base_round_little_radius);
        int offset = getDimensionPixelOffset(R.dimen.base_xxhdpi);
        textView.setPadding(offset, offset / 4, offset, offset / 4);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.rightMargin = offset / 2;
        params.topMargin = radius;
        params.bottomMargin = radius;
        textView.setLayoutParams(params);

        textView.setBackground(ResUtil.selectorChecked(
                ResUtil.createDrawable(getColor(R.color.base_chat_bg_color), radius),
                ResUtil.createDrawable(SkinHelper.getSkin().getThemeTranColor(0x80), radius)
        ));

        addView(textView);
        return textView;
    }

    public void addTextView(List<String> texts, final OnAddViewListener onAddViewListener) {
        final int offset = getDimensionPixelOffset(R.dimen.base_ldpi);

        ViewGroupExKt.resetChildCount(this, texts.size(), new Function0<View>() {
            @Override
            public View invoke() {
                RTextView textView = new RTextView(getContext());
                textView.setTextColor(getColor(R.color.base_text_color));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getDimensionPixelOffset(R.dimen.default_text_size9));
                textView.setGravity(Gravity.CENTER);

                textView.setPadding(offset, offset / 2, offset, offset / 2);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
                layoutParams.setMarginEnd(offset * 2);
                textView.setLayoutParams(layoutParams);
                return textView;
            }
        });

        for (int i = 0; i < texts.size(); i++) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
            layoutParams.setMarginEnd(offset * 2);

            TextView textView = (TextView) getChildAt(i);
            textView.setLayoutParams(layoutParams);
            textView.setText(texts.get(i));

            if (onAddViewListener != null) {
                onAddViewListener.onInitView(textView);
            }
        }
    }

    public <T> void addView(List<T> datas, final OnAddViewCallback<T> onAddViewCallback) {
        ViewGroupExKt.resetChildCount(this, datas.size(), new Function0<View>() {
            @Override
            public View invoke() {
                int layoutId = onAddViewCallback.getLayoutId();
                if (layoutId > 0) {
                    return LayoutInflater.from(getContext()).inflate(layoutId, RFlowLayout.this, false);
                }
                return onAddViewCallback.getView();
            }
        });

        for (int i = 0; i < datas.size(); i++) {
            onAddViewCallback.onInitView(getChildAt(i), datas.get(i), i);
        }
    }

    public interface OnAddViewListener {
        void onInitView(View view);
    }

    public static abstract class OnAddViewCallback<T> {
        public int getLayoutId() {
            return -1;
        }

        public View getView() {
            return null;
        }

        public void onInitView(View view, T data, int index) {

        }
    }
}
