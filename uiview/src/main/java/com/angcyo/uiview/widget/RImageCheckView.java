package com.angcyo.uiview.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;

import com.angcyo.uiview.R;
import com.angcyo.uiview.skin.SkinHelper;

/**
 * 实现了 checked 状态的 ImageView
 * Created by angcyo on 2017-01-01 10:46.
 */
public class RImageCheckView extends AppCompatImageView implements View.OnClickListener, RCheckGroup.ICheckView {

    private static final int[] CHECKED_STATE_SET = {
            android.R.attr.state_checked
    };
    boolean mChecked = false;
    View.OnClickListener mOnClickListener;
    OnCheckedChangeListener mOnCheckedChangeListener;

    boolean isTintColor;

    public RImageCheckView(Context context) {
        this(context, null);
    }

    public RImageCheckView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RImageCheckView);
        mChecked = typedArray.getBoolean(R.styleable.RImageCheckView_r_checked, false);
        isTintColor = typedArray.getBoolean(R.styleable.RImageCheckView_r_tint_color, isTintColor);
        typedArray.recycle();

        setScaleType(ScaleType.CENTER_INSIDE);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        super.setOnClickListener(this);
//        setScaleType(ScaleType.CENTER_INSIDE);
//        if (getTag() != null && TextUtils.equals("checked", getTag().toString())) {
//            setChecked(true);
//        }

        if (!isInEditMode() && isTintColor) {
            ColorStateList stateList = new ColorStateList(
                    new int[][]{{android.R.attr.state_checked}, {}},
                    new int[]{SkinHelper.getSkin().getThemeSubColor(), ContextCompat.getColor(getContext(), R.color.default_base_bg_disable)});
            DrawableCompat.setTintList(getDrawable(), stateList);
        }
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        if (mChecked == checked) {
            return;
        }

        mChecked = checked;

        refreshDrawableState();

        if (mOnCheckedChangeListener != null) {
            mOnCheckedChangeListener.onCheckedChanged(this, mChecked);
        }
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            size = Math.min(width, height);
        } else if (widthMode == MeasureSpec.EXACTLY) {
            size = width;
        } else if (heightMode == MeasureSpec.EXACTLY) {
            size = height;
        } else {
            Drawable drawable = getDrawable();
            if (drawable == null) {
                size = (int) (getResources().getDisplayMetrics().density * 40);
            } else {
                size = Math.max(drawable.getIntrinsicWidth() + getPaddingLeft() + getPaddingRight(),
                        drawable.getIntrinsicHeight() + getPaddingTop() + getPaddingBottom());
            }
        }

        setMeasuredDimension(size, size);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mOnClickListener = l;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        setOnCheckedChangeListener(onCheckedChangeListener, false);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener, boolean notify) {
        mOnCheckedChangeListener = onCheckedChangeListener;
        if (notify && mOnCheckedChangeListener != null) {
            mOnCheckedChangeListener.onCheckedChanged(this, mChecked);
        }
    }

    @Override
    public void onClick(View v) {
        setChecked(!isChecked());
        if (mOnClickListener != null) {
            mOnClickListener.onClick(v);
        }
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(RImageCheckView buttonView, boolean isChecked);
    }
}
