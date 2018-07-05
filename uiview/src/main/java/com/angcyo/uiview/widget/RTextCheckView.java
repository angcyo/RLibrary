package com.angcyo.uiview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;

import com.angcyo.uiview.R;
import com.angcyo.uiview.kotlin.ViewExKt;
import com.angcyo.uiview.resources.ResUtil;
import com.angcyo.uiview.skin.SkinHelper;

/**
 * 实现了 checked 状态的 TextView
 * Created by angcyo on 2017-01-01 10:46.
 */
public class RTextCheckView extends AppCompatTextView implements View.OnClickListener, RCheckGroup.ICheckView {

    private static final int[] CHECKED_STATE_SET = {
            android.R.attr.state_checked
    };
    boolean mChecked = false;
    OnClickListener mOnClickListener;
    OnCheckedChangeListener mOnCheckedChangeListener;

    /**
     * 是否激活check功能
     */
    boolean enableCheck = true;

    /**
     * 当选中后, 是否可以通过点击事件取消选择
     */
    boolean cancelCheck = true;
    boolean useSkinStyle = false;

    public RTextCheckView(Context context) {
        this(context, null);
    }

    public RTextCheckView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RTextCheckView);
        useSkinStyle = typedArray.getBoolean(R.styleable.RTextCheckView_r_use_skin_style, getTag().toString().contains("skin"));
        cancelCheck = typedArray.getBoolean(R.styleable.RTextCheckView_r_can_cancel_check, cancelCheck);
        typedArray.recycle();

        initView();
    }

    private void initView() {
        super.setOnClickListener(this);
        setGravity(Gravity.CENTER);

        if (!isInEditMode()) {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, SkinHelper.getSkin().getMainTextSize());
        }

        if (useSkinStyle) {
            float density = ViewExKt.getDensity(this);
            if (isInEditMode()) {
                setBackground(ResUtil.selector(
                        ResUtil.createDrawable(Color.parseColor("#E0E0E0"), Color.TRANSPARENT, (int) (1 * density), 3 * density),
                        ResUtil.createDrawable(ContextCompat.getColor(getContext(), R.color.theme_color_accent), 3 * density),
                        ResUtil.createDrawable(ContextCompat.getColor(getContext(), R.color.theme_color_primary), 3 * density),
                        ResUtil.createDrawable(Color.parseColor("#E0E0E0"), 3 * density)
                ));
            } else {
                setBackground(ResUtil.selector(
                        ResUtil.createDrawable(Color.parseColor("#E0E0E0"), Color.TRANSPARENT, (int) (1 * density), 3 * density),
                        ResUtil.createDrawable(SkinHelper.getSkin().getThemeSubColor(), 3 * density),
                        ResUtil.createDrawable(SkinHelper.getSkin().getThemeSubColor(), 3 * density),
                        ResUtil.createDrawable(Color.parseColor("#E0E0E0"), 3 * density)
                ));
            }
            setTextColor(ResUtil.generateTextColor(Color.WHITE, Color.WHITE, Color.BLACK));
        } else if (getTag() == null) {
            float density = getResources().getDisplayMetrics().density;
            int paddStart = (int) (density * 10);
            int paddTop = (int) (density * 4);
            setPadding(paddStart, paddTop, paddStart, paddTop);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        setChecked(checked, true);
    }

    public void setChecked(boolean checked, boolean notifyChanged) {
        if (mChecked == checked) {
            return;
        }

        mChecked = checked;

        refreshDrawableState();

        if (mOnCheckedChangeListener != null && notifyChanged) {
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
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mOnClickListener = l;
        if (mOnClickListener == null) {
            super.setOnClickListener(null);
        }
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        mOnCheckedChangeListener = onCheckedChangeListener;
    }

    @Override
    public void onClick(View v) {
        if (enableCheck) {
            boolean checked = isChecked();
            if (checked && !cancelCheck) {

            } else {
                setChecked(!checked);
            }
        }
        if (mOnClickListener != null) {
            mOnClickListener.onClick(v);
        }
    }

    public void setEnableCheck(boolean enableCheck) {
        this.enableCheck = enableCheck;
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(RTextCheckView textCheckView, boolean isChecked);
    }
}