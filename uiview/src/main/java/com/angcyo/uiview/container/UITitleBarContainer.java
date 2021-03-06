package com.angcyo.uiview.container;

import android.animation.ArgbEvaluator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.angcyo.uiview.R;
import com.angcyo.uiview.draw.RDrawNoRead;
import com.angcyo.uiview.kotlin.ViewExKt;
import com.angcyo.uiview.model.TitleBarItem;
import com.angcyo.uiview.model.TitleBarPattern;
import com.angcyo.uiview.recycler.RBaseViewHolder;
import com.angcyo.uiview.resources.ResUtil;
import com.angcyo.uiview.rsen.RGestureDetector;
import com.angcyo.uiview.skin.ISkin;
import com.angcyo.uiview.skin.SkinHelper;
import com.angcyo.uiview.widget.ImageTextView2;
import com.angcyo.uiview.widget.LoadingImageView;
import com.angcyo.uiview.widget.RImageView;
import com.angcyo.uiview.widget.RTextView;
import com.angcyo.uiview.widget.RTitleCenterLayout;

import java.util.ArrayList;

import static com.angcyo.uiview.utils.ScreenUtil.density;

/**
 * 标题栏的封装
 * Created by angcyo on 2016-11-05.
 */

public class UITitleBarContainer extends FrameLayout {

    protected RBaseViewHolder mBaseViewHolder;

    protected ILayout mILayout;
    protected ViewGroup mTitleBarLayout;
    protected LinearLayout mLeftControlLayout;
    protected ViewGroup mCenterControlLayout;
    protected LinearLayout mRightControlLayout;
    protected ImageTextView2 mBackImageView;
    protected RTextView mTitleView;
    protected View mLoadView;

    protected TitleBarPattern mTitleBarPattern;
    protected boolean isAttachedToWindow;

    protected ArrayList<View> mLeftViews = new ArrayList<>();
    protected ArrayList<View> mRightViews = new ArrayList<>();

    private int mStatusBarHeight;
    private boolean mLayoutFullscreen;
    private Drawable mBackgroundDrawable;
    /**
     * 显示底部的横线
     */
    private boolean showBottomLine = false;

    /**
     * 底部横线的高度, 1px
     */
    private int bottomLineHeight = getContext().getResources().getDimensionPixelOffset(R.dimen.base_line);
    /**
     * 底部横线的颜色
     */
    private int bottomLineColor = ContextCompat.getColor(getContext(), R.color.default_base_line);
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public UITitleBarContainer(Context context) {
        super(context);
    }

    public UITitleBarContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.UITitleBarContainer);
        if (mTitleBarPattern == null) {
            //标题文本
            mTitleBarPattern = TitleBarPattern.build(typedArray.getString(R.styleable.UITitleBarContainer_title_text));
            //标题背景颜色
            Drawable background = getBackground();
            if (background != null && background instanceof ColorDrawable) {
                mTitleBarPattern.setTitleBarBGColor(((ColorDrawable) background).getColor());
            }
            //标题大小
            mTitleBarPattern.setTitleSize(typedArray.getFloat(R.styleable.UITitleBarContainer_title_text_size, -1));
        }
        typedArray.recycle();
    }

    //----------------------------公共方法-----------------------------

    private static <T> T find(View view, int id) {
        return (T) view.findViewById(id);
    }

    /**
     * 显示未读小红点
     */
    public static void showNoRead(View view, boolean show) {
        RDrawNoRead drawNoRead = null;
        if (view instanceof RImageView) {
            drawNoRead = ((RImageView) view).getDrawNoRead();
        } else if (view instanceof RTextView) {
            drawNoRead = ((RTextView) view).getDrawNoRead();
        }

        if (drawNoRead != null) {
            drawNoRead.setShowNoRead(show);
            drawNoRead.setNoreadGravity(RDrawNoRead.CENTER);
            drawNoRead.setNoReadRadius(3 * density());
            drawNoRead.setNoReadPaddingTop(-10 * density());
            drawNoRead.setNoReadPaddingRight(12 * density());
        }
    }

    public void onAttachToLayout(ILayout container) {
        mILayout = container;
    }

    //----------------------------保护方法-----------------------------

    /**
     * 在旧的标题栏上, 应用一个新的标题栏的信息
     */
    public UITitleBarContainer appendTitleBarPattern(TitleBarPattern titleBarPattern) {
        TitleBarPattern.fix(titleBarPattern, mTitleBarPattern);
        if (isAttachedToWindow) {
            loadTitleBar();
        }
        return this;
    }

    public UITitleBarContainer showLoadView() {
        if (mLoadView != null) {
            mLoadView.setVisibility(VISIBLE);
        }
        return this;
    }

    //----------------------------私有方法-----------------------------

    public UITitleBarContainer hideLoadView() {
        if (mLoadView != null) {
            mLoadView.setVisibility(GONE);
        }
        return this;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
//        post(new Runnable() {
//            @Override
//            public void run() {
        isAttachedToWindow = true;
//                loadTitleBar();
//            }
//        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttachedToWindow = false;
//        mTitleBarPattern.clear();
//        mOnBackListener = null;
    }

    private void initTitleBar(Context context, TitleBarPattern titleBarPattern) {
        final View root;
        if (titleBarPattern.customLayoutId != -1) {
            //自定义标题栏
            root = LayoutInflater.from(context).inflate(R.layout.base_title_custom_layout, this);
            mBaseViewHolder = new RBaseViewHolder(root);
            mTitleBarLayout = find(root, R.id.base_title_bar_layout);

            LayoutInflater.from(context).inflate(titleBarPattern.customLayoutId, mTitleBarLayout);
        } else {
            if (titleBarPattern.titleGravity == Gravity.LEFT) {
                root = LayoutInflater.from(context).inflate(R.layout.base_title_layout_left, this);
            } else {
                root = LayoutInflater.from(context).inflate(R.layout.base_title_layout, this);
            }
            mBaseViewHolder = new RBaseViewHolder(root);
            mTitleBarLayout = find(root, R.id.base_title_bar_layout);
            mLeftControlLayout = find(root, R.id.base_left_control_layout);
            mCenterControlLayout = find(root, R.id.base_center_control_layout);
            mRightControlLayout = find(root, R.id.base_right_control_layout);
            mBackImageView = find(root, R.id.base_back_image_view);
            mTitleView = find(root, R.id.base_title_view);
            mLoadView = mBaseViewHolder.v(R.id.base_load_view);

            if (titleBarPattern.isShowBackImageView) {
                if (mLoadView instanceof LoadingImageView && titleBarPattern.showDarkLoading) {
                    ((LoadingImageView) mLoadView).setImageResource(R.drawable.loading_dark);
                }
            }
        }

        if (context instanceof Activity) {
            mLayoutFullscreen = ResUtil.isLayoutFullscreen((Activity) context);
            if (mLayoutFullscreen) {
                mStatusBarHeight = getResources().getDimensionPixelSize(R.dimen.status_bar_height);
//                mTitleBarLayout.setClipToPadding(false);
//                mTitleBarLayout.setClipChildren(false);
//                mTitleBarLayout.setPadding(getPaddingLeft(),
//                        getPaddingTop() + mStatusBarHeight,
//                        getPaddingRight(), getPaddingBottom());
//                ViewGroup.LayoutParams layoutParams = mTitleBarLayout.getLayoutParams();
//                layoutParams.height += mStatusBarHeight;
//                mTitleBarLayout.setLayoutParams(layoutParams);

                /*padding top 处理状态栏*/
                setPadding(0, mStatusBarHeight, 0, 0);
            }
        }

        setWillNotDraw(false);
    }

    public void onSkinChanged(ISkin skin) {

    }

    /**
     * title bar 处理开始的地方
     */
    private void loadTitleBar() {
        if (mTitleBarPattern == null) {
            if (mLeftControlLayout != null) {
                mLeftControlLayout.removeAllViews();
            }
            if (mRightControlLayout != null) {
                mRightControlLayout.removeAllViews();
            }
//            mCenterControlLayout.removeAllViews();
            setVisibility(GONE);
            return;
        }

        setVisibility(VISIBLE);
        if (getChildCount() == 0) {
            initTitleBar(getContext(), mTitleBarPattern);
        }

        int itemSize = getResources().getDimensionPixelSize(R.dimen.base_title_bar_item_size);
        int animTime = 300;

        if (mTitleBarPattern.mTitleBarBGDrawable == null) {
            setBackgroundColor(mTitleBarPattern.mTitleBarBGColor);
        } else {
            setBackground(mTitleBarPattern.mTitleBarBGDrawable);
        }

        if (mTitleBarPattern.customLayoutId != -1) {
            //自定义的标题栏

            if (mTitleBarPattern.mOnInitCustomTitleLayout != null) {
                mTitleBarPattern.mOnInitCustomTitleLayout.onInitLayout(this, mBaseViewHolder, mLayoutFullscreen, mStatusBarHeight);
            }
        } else {
            /*返回按钮*/
            if (mTitleBarPattern.isShowBackImageView) {
                mBackImageView.setVisibility(VISIBLE);
                mBackImageView.setShowText(mTitleBarPattern.backImageString);
                if (mTitleBarPattern.backImageRes != 0) {
                    mBackImageView.setImageResource(mTitleBarPattern.backImageRes);
                }
                mBackImageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mTitleBarPattern.mOnBackListener != null) {
                            mTitleBarPattern.mOnBackListener.onClick(v);
                        }
                    }
                });
//            ViewCompat.animate(mBackImageView).rotation(360)
//                    .setInterpolator(new DecelerateInterpolator())
//                    .setDuration(animTime).start();
            } else {
                mBackImageView.setVisibility(GONE);
            }
            /*标题字体大小限制*/
            mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, SkinHelper.getSkin().getMainTextSize());
            mTitleBarPattern.setTextViewSize(mTitleView);
            /*标题长度显示*/
            if (mTitleBarPattern.mTitleStringLength > 0) {
                mTitleView.setMaxLength(mTitleBarPattern.mTitleStringLength);
            }
            /*标题*/
            mTitleView.setText(mTitleBarPattern.mTitleString);
            mTitleView.setTextColor(mTitleBarPattern.mTitleTextColor);

            if (mTitleBarPattern.titleHide) {
                mTitleView.setVisibility(GONE);
            }

            //双击标题
            if (mTitleBarPattern.mOnTitleDoubleTapListener != null) {
                mTitleView.setClickable(true);
                RGestureDetector.onDoubleTap(mTitleView, mTitleBarPattern.mOnTitleDoubleTapListener);
            }

            if (!TextUtils.isEmpty(mTitleBarPattern.mTitleString) && mTitleBarPattern.titleAnim) {
//            ViewCompat.setTranslationY(mTitleView, -itemSize);
//            ViewCompat.animate(mTitleView).translationY(0)
//                    .setInterpolator(new DecelerateInterpolator())
//                    .setDuration(animTime).start();
            }

            clearViews(mLeftControlLayout, mLeftViews);
            clearViews(mRightControlLayout, mRightViews);

            /*左边控制按钮*/
            fillViews(mLeftControlLayout, mTitleBarPattern.mLeftItems, mLeftViews, -1);
            /*右边控制按钮*/
            fillViews(mRightControlLayout, mTitleBarPattern.mRightItems, mRightViews, -1);

            if (mTitleBarPattern.mOnInitTitleLayout != null) {
                mTitleBarPattern.mOnInitTitleLayout.onInitLayout((RTitleCenterLayout) mCenterControlLayout);
                mTitleBarPattern.mOnInitTitleLayout.onInitLayout(this, mLayoutFullscreen, mStatusBarHeight);
            }

            /*控制按钮的动画*/
            //animViews(mLeftViews, true);
            //animViews(mRightViews, false);
        }

    }

    public void evaluateBackgroundColor(int scrollY) {
        evaluateBackgroundColor(scrollY, null);
    }

    public void evaluateBackgroundColorSelf(int scrollY) {
        evaluateBackgroundColor(scrollY, mTitleView);
    }

    /**
     * 根据 scrollY 动态计算背景颜色, 并自动隐藏标题
     */
    public void evaluateBackgroundColor(int scrollY, View titleView) {
        float factor = 6 * scrollY * 0.1f / getMeasuredHeight();
        if (titleView != null) {
            titleView.setVisibility(factor > 0.8 ? View.VISIBLE : View.GONE);
        }
        setBackgroundColor((Integer) new ArgbEvaluator()
                .evaluate(Math.min(1, factor),
                        Color.TRANSPARENT, SkinHelper.getSkin().getThemeColor()));
    }

    /**
     * 标题文本视图
     */
    public RTextView getTitleView() {
        return mTitleView;
    }

    private void animViews(ArrayList<View> views, boolean left) {
        int itemSize = getResources().getDimensionPixelSize(R.dimen.base_title_bar_item_size);
        int animTime = 300;
        int delayTime = 100;
        final int size = views.size();
        if (left) {
            for (int j = 0; j < size; j++) {
                final View view = views.get(j);
                ViewCompat.setTranslationX(view, itemSize);
                ViewCompat.animate(view).translationX(0)
                        .setInterpolator(new DecelerateInterpolator())
                        .setDuration(animTime).start();
            }
        } else {
            for (int i = 0; i < size; i++) {
                final View view = views.get(i);
                ViewCompat.setTranslationX(view, -itemSize);
                ViewCompat.animate(view).setStartDelay(delayTime * (size - i - 1)).translationX(0)
                        .setInterpolator(new DecelerateInterpolator())
                        .setDuration(animTime).start();
            }
        }
    }

    /**
     * 清空布局
     */
    private void clearViews(ViewGroup leftControlLayout, ArrayList<View> leftViews) {
        for (View view : leftViews) {
            leftControlLayout.removeView(view);
        }
    }

    public void addLeftItemView(TitleBarItem item) {
        ArrayList<TitleBarItem> items = new ArrayList<>();
        items.add(item);
        fillViews(mLeftControlLayout, items, mLeftViews, -1);
    }

    public void addRightItemView(TitleBarItem item) {
        ArrayList<TitleBarItem> items = new ArrayList<>();
        items.add(item);
        fillViews(mRightControlLayout, items, mRightViews, -1);
    }

    public void addRightItemView(int index, TitleBarItem item) {
        ArrayList<TitleBarItem> items = new ArrayList<>();
        items.add(item);
        fillViews(mRightControlLayout, items, mRightViews, index);
    }

    /**
     * 填充左右控制按钮
     */
    private void fillViews(LinearLayout layout, ArrayList<TitleBarItem> items, ArrayList<View> views, int index) {
        if (items == null || items.isEmpty()) {
            return;
        }

        int itemSize = getResources().getDimensionPixelSize(R.dimen.base_title_bar_item_size);

        for (int i = 0; i < items.size(); i++) {
            final TitleBarItem item = items.get(i);
            View view;
            if (item.icoDrawable == null) {
                //不是图片, 就创建文本按钮
                view = createTextItem(item.text, item.textColor);
                RTextView textView = (RTextView) view;
                textView.setLeftIco(item.textLeftRes);
                textView.setRightIco(item.textRightRes);
                if (item.textSize != -1) {
                    ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_PX, item.textSize);
                }
            } else {
                //创建图片按钮
                view = createImageItem(item.icoDrawable);
                view.setMinimumWidth(itemSize);
            }

            view.setPadding(item.leftPadding, item.topPadding, item.rightPadding, item.bottomPadding);

            if (item.id != -1) {
                view.setId(item.id);
            }

            if (item.tag != null) {
                view.setTag(item.tag);
            }

            if (item.isClickable) {
                ViewExKt.setOnRClickListener(view, item.listener);
            }

            if (item.itemBgResId == -1) {
                view.setBackgroundResource(R.drawable.base_bg2_selector_v21);
            } else {
                view.setBackgroundResource(item.itemBgResId);
            }

            view.setVisibility(item.visibility);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(item.itemWidth, item.itemHeight);
            layoutParams.setMargins(item.leftMargin, item.topMargin, item.rightMargin, item.bottomMargin);
            view.setTag(R.id.tag, i);//方便之后查找这个view
            if (index >= 0) {
                int to = index + i;
                layout.addView(view, to, layoutParams);
                views.add(to, view);
            } else {
                layout.addView(view, layoutParams);
                views.add(view);
            }

            if (item.mOnItemInitListener != null) {
                item.mOnItemInitListener.onItemInit(view, item);
            }
        }

//        for (TitleBarItem item : items) {
//            View view;
//            if (item.res == -1) {
//                //不是图片, 就创建文本按钮
//                view = createTextItem(item.text, item.listener);
//            } else {
//                //创建图片按钮
//                view = createImageItem(item.res, item.listener);
//            }
//            layout.addView(view, new LinearLayout.LayoutParams(itemSize, -1));
//            views.add(view);
//        }
    }

    private RImageView createImageItem(Drawable drawable) {
        RImageView item = new RImageView(getContext());
        item.setImageDrawable(drawable);
        item.setScaleType(ImageView.ScaleType.CENTER);
        return item;
    }

    private RTextView createTextItem(String text, @ColorInt int color) {
        RTextView item = new RTextView(getContext());
        item.setText(text);
        item.setSingleLine();
        item.setMaxLines(1);
        item.setTextSize(TypedValue.COMPLEX_UNIT_PX, SkinHelper.getSkin().getSubTextSize() /*getResources().getDimensionPixelOffset(R.dimen.base_title_size)*/);
        item.setTextColor(color == -1 ? Color.WHITE : color);
        item.setGravity(Gravity.CENTER);
        return item;
    }

    public TitleBarPattern getTitleBarPattern() {
        return mTitleBarPattern;
    }

    /**
     * 标题栏的信息
     */
    public void setTitleBarPattern(TitleBarPattern titleBarPattern) {
        mTitleBarPattern = titleBarPattern;
//        if (isAttachedToWindow) {
        loadTitleBar();
//        }
    }

    public TitleBarPattern set() {
        if (mTitleBarPattern == null) {
            return TitleBarPattern.build();
        }
        post(new Runnable() {
            @Override
            public void run() {
                loadTitleBar();
            }
        });
        return mTitleBarPattern;
    }

    public ViewGroup getCenterControlLayout() {
        return mCenterControlLayout;
    }

    public LinearLayout getLeftControlLayout() {
        return mLeftControlLayout;
    }

    public LinearLayout getRightControlLayout() {
        return mRightControlLayout;
    }

    public <T extends View> T getRightView(int index) {
        return (T) getRightControlLayout().getChildAt(index);
    }

    public <T extends View> T getRightViewById(int id) {
        return (T) getRightControlLayout().findViewById(id);
    }

    public <T extends View> T getLeftViewById(int id) {
        return (T) getLeftControlLayout().findViewById(id);
    }

    /**
     * 请注意,左边有一个返回按钮
     */
    public <T extends View> T getLeftView(int index) {
        return (T) getLeftControlLayout().getChildAt(index);
    }

    /**
     * 显示右边第几个Item的View, 从0开始
     */
    public void showRightItem(int index) {
        if (mRightControlLayout != null && mRightControlLayout.getChildCount() > index) {
            visibleView(mRightControlLayout.getChildAt(index));
        }
    }

    public void showRightItemNoRead(int index, boolean show) {
        if (mRightControlLayout != null && mRightControlLayout.getChildCount() > index) {
            showNoRead(mRightControlLayout.getChildAt(index), show);
        }
    }

    public void showRightItemNoReadById(int id, boolean show) {
        showNoRead(mRightControlLayout.findViewById(id), show);
    }

    public void showRightItemNoReadByTag(String tag, boolean show) {
        showNoRead(mRightControlLayout.findViewWithTag(tag), show);
    }

    public void showRightItemById(int id) {
        visibleView(mRightControlLayout.findViewById(id));
    }

    public void showRightItemByTag(String tag) {
        visibleView(mRightControlLayout.findViewWithTag(tag));
    }

    public void showLeftItemByTag(String tag) {
        visibleView(mLeftControlLayout.findViewWithTag(tag));
    }

    public void hideRightItemById(int id) {
        goneView(mRightControlLayout.findViewById(id));
    }

    public void hideRightItem(int index) {
        if (mRightControlLayout.getChildCount() > index) {
            goneView(mRightControlLayout.getChildAt(index));
        }
    }

    public void showLeftItemById(int id) {
        visibleView(mLeftControlLayout.findViewById(id));
    }

    public void hideLeftItemById(int id) {
        goneView(mLeftControlLayout.findViewById(id));
    }

    public void hideLeftItemByTag(String tag) {
        goneView(mLeftControlLayout.findViewWithTag(tag));
    }

    public void hideRightItemByTag(String tag) {
        goneView(mRightControlLayout.findViewWithTag(tag));
    }

    /**
     * 请注意,左边有一个返回按钮
     */
    public void showLeftItem(int index) {
        if (mLeftControlLayout != null && mLeftControlLayout.getChildCount() > index) {
            visibleView(mLeftControlLayout.getChildAt(index));
        }
    }

    /**
     * 请注意,左边有一个返回按钮
     */
    public void hideLeftItem(int index) {
        if (mLeftControlLayout != null && mLeftControlLayout.getChildCount() > index) {
            goneView(mLeftControlLayout.getChildAt(index));
        }
    }

    private void goneView(View view) {
        if (view != null) {
            view.setVisibility(View.GONE);
        }
    }

    private void visibleView(View view) {
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mTitleBarPattern == null) {
            return true;
        }
        return !mTitleBarPattern.isFloating;
    }

    public float getTitleBarHeight() {
        float density = getResources().getDisplayMetrics().density;
        if (ResUtil.isLayoutFullscreen((Activity) getContext())) {
            return density * 65f;
        } else {
            return density * 40f;
        }
    }

    public boolean isTitleBarShow() {
        return getTranslationY() == 0;
    }

    public void hide(boolean anim) {
        if (getTranslationY() == 0) {
            //是显示状态
            if (anim) {
                animate().setDuration(300)
                        .translationY(-getMeasuredHeight())
                        .start();
            } else {
                ViewCompat.setTranslationY(this, -getMeasuredHeight());
            }
        }
    }

    public void show(boolean anim) {
        if (getTranslationY() == -getMeasuredHeight()) {
            //是隐藏状态
            if (anim) {
                animate().setDuration(300)
                        .translationY(0)
                        .start();
            } else {
                ViewCompat.setTranslationY(this, 0);
            }
        }
    }

    public void setRBackgroundDrawable(Drawable mBackgroundDrawable) {
        this.mBackgroundDrawable = mBackgroundDrawable;
        setWillNotDraw(mBackgroundDrawable != null);
        postInvalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
//        shadowDrawable = ResUtil.createGradientDrawable(Color.TRANSPARENT, Color.parseColor("#40000000"), 0);
//        shadowDrawable.setBounds(0, 0, w, h);
    }

    @Override
    public void draw(Canvas canvas) {
        if (mBackgroundDrawable != null) {
            mBackgroundDrawable.setBounds(canvas.getClipBounds());
            mBackgroundDrawable.draw(canvas);
        }
        super.draw(canvas);
        if (showBottomLine) {
            mPaint.setStrokeWidth(bottomLineHeight);
            mPaint.setColor(bottomLineColor);
            mPaint.setShader(null);
            canvas.drawLine(0, getMeasuredHeight(), getMeasuredWidth(), getMeasuredHeight(), mPaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void setShowBottomLine(boolean showBottomLine) {
        this.showBottomLine = showBottomLine;
    }

    public void setBottomLineHeight(int bottomLineHeight) {
        this.bottomLineHeight = bottomLineHeight;
    }

    public void setBottomLineColor(int bottomLineColor) {
        this.bottomLineColor = bottomLineColor;
    }

    public ImageTextView2 getBackImageView() {
        return mBackImageView;
    }
}
