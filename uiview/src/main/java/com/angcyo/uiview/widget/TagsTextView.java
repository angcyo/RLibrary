package com.angcyo.uiview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.angcyo.uiview.R;
import com.angcyo.uiview.utils.RUtils;

import java.util.List;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/01/06 11:07
 * 修改人员：Robi
 * 修改时间：2017/01/06 11:07
 * 修改备注：
 * Version: 1.0.0
 */
public class TagsTextView extends RFlowLayout {

    private int mTagMargin, mTagSpaceV, mTagSpaceH;

    public TagsTextView(Context context) {
        this(context, null);
    }

    public TagsTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TagsTextView);
        String string = typedArray.getString(R.styleable.TagsTextView_r_tags);
        mTagMargin = typedArray.getDimensionPixelOffset(R.styleable.TagsTextView_r_tag_margin,
                getResources().getDimensionPixelOffset(R.dimen.base_mdpi));
        mTagSpaceV = typedArray.getDimensionPixelOffset(R.styleable.TagsTextView_r_tag_space_v,
                0);
        mTagSpaceH = typedArray.getDimensionPixelOffset(R.styleable.TagsTextView_r_tag_space_h,
                0);
        typedArray.recycle();

        setTags(string);
    }

    public void setTags(String tags) {
        final List<String> split = RUtils.split(String.valueOf(tags));
        int childCount = getChildCount();
        int size = split.size();
        if (childCount > size) {
            for (int i = 0; i < childCount - size; i++) {
                removeViewAt(getChildCount() - 1);
            }
        }

        for (int i = getChildCount(); i < size; i++) {
            addView(createTagView(), i);
        }

        for (int i = 0; i < size; i++) {
            ((RTextView) getChildAt(i)).setText(split.get(i));
        }

        setVisibility(size <= 0 ? GONE : VISIBLE);
    }

    private View createTagView() {
        RTextView textView = new RTextView(getContext());
        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.base_text_color_dark));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelOffset(R.dimen.default_text_size9));
        textView.setBackgroundResource(R.drawable.base_round_border_shape);
        int offset = getResources().getDimensionPixelOffset(R.dimen.base_mdpi);
        textView.setPadding(2 * offset, offset, 2 * offset, offset);
        return textView;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        LayoutParams params = super.generateDefaultLayoutParams();
        params.rightMargin = mTagMargin + mTagSpaceH;
        params.bottomMargin = mTagMargin + mTagSpaceV;
        params.leftMargin = mTagMargin;
        params.topMargin = mTagMargin;
        return params;
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        LayoutParams params = super.generateLayoutParams(lp);
        params.rightMargin = mTagMargin + mTagSpaceH;
        params.bottomMargin = mTagMargin + mTagSpaceV;
        params.leftMargin = mTagMargin;
        params.topMargin = mTagMargin;
        return params;
    }
}
