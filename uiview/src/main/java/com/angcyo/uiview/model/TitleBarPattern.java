package com.angcyo.uiview.model;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.angcyo.uiview.rsen.RGestureDetector;

import java.util.ArrayList;

/**
 * Created by angcyo on 2016-11-05.
 */

public class TitleBarPattern {

    /**
     * 标题栏背景颜色
     */
    public int mTitleBarBGColor = Color.TRANSPARENT;
    public Drawable mTitleBarBGDrawable = null;
    /**
     * 标题颜色
     */
    public int mTitleTextColor = Color.WHITE;
    /**
     * 是否显示返回按钮
     */
    public boolean isShowBackImageView;

    /**
     * 返回按钮的图片资源
     */
    @DrawableRes
    public int backImageRes = 0;
    /**
     * 返回按钮的文本显示
     */
    public String backImageString = "";
    /**
     * 标题进入的动画
     */
    public boolean titleAnim = false;

    /**
     * 隐藏标题, 不是隐藏标题Bar哦
     */
    public boolean titleHide = false;

    /**
     * 标题显示在左边, 还是中间. 目前只支持这2个位置
     */
    public int titleGravity = Gravity.CENTER;//Gravity.LEFT;

    /**
     * 标题是否显示在内容的上面, 否则内容就会显示在标题的下面
     */
    public boolean isFloating = false;

    /**
     * 当 {@link #isFloating} 为true时, 可以 使用此参数自动设置 内容的 paddingTop值
     */
    public boolean isFixContentHeight = false;

    /**
     * 处理状态栏的高度
     */
    public boolean isFixStatusHeight = true;

    /**
     * 标题栏的PaddingTop
     */
    public int titleBarPaddingTop = 0;
    /**
     * 标题
     */
    public String mTitleString;
    /**
     * 标题文本的最大长度
     */
    public int mTitleStringLength = -1;
    /**
     * 标题文本的大小
     */
    public float mTitleSize = -1;//px
    /**
     * 左边的按钮
     */
    public ArrayList<TitleBarItem> mLeftItems = new ArrayList<>();
    /**
     * 右边的按钮
     */
    public ArrayList<TitleBarItem> mRightItems = new ArrayList<>();

    public OnInitTitleLayout mOnInitTitleLayout;

    /**
     * 显示底部的横线
     */
    public boolean showTitleBarBottomLine = false;
    public boolean showBottomShadow = false;

    /**
     * 底部横线的高度, 1px
     */
    public int bottomTitleBarLineHeight = 0;

    /**
     * 底部横线的颜色
     */
    public int bottomTitleBarLineColor = 0;

    /**
     * 默认的加载进度是白色的, 可以使用dark颜色
     */
    public boolean showDarkLoading = false;

    /**
     * 双击标题的回调
     */
    public RGestureDetector.OnDoubleTapListener mOnTitleDoubleTapListener;

    /**
     * 返回按钮的事件
     */
    public View.OnClickListener mOnBackListener;

    private TitleBarPattern(String titleString) {
        mTitleString = titleString;
    }

    @Deprecated
    public static TitleBarPattern fix(TitleBarPattern from, TitleBarPattern to) {
        if (to == null) {
            to = from;
        }
        to.isShowBackImageView = from.isShowBackImageView;
        to.titleAnim = from.titleAnim;
        to.isFloating = from.isFloating;
        to.isFixContentHeight = from.isFixContentHeight;
        to.titleHide = from.titleHide;
        if (to.mTitleSize == -1) {
            to.mTitleSize = from.mTitleSize;
        }
        if (TextUtils.isEmpty(to.mTitleString)) {
            to.mTitleString = from.mTitleString;
        }
        if (to.mTitleBarBGColor == Color.TRANSPARENT) {
            to.mTitleBarBGColor = from.mTitleBarBGColor;
        }
        if (to.mLeftItems.size() == 0) {
            to.mLeftItems.addAll(from.mLeftItems);
        }
        if (to.mRightItems.size() == 0) {
            to.mRightItems.addAll(from.mRightItems);
        }
        return to;
    }

    public static TitleBarPattern build() {
        return build("");
    }

    public static TitleBarPattern build(String title) {
        return new TitleBarPattern(title);
    }

    public static TitleBarItem buildItem() {
        return new TitleBarItem();
    }

    public static TitleBarItem buildText(String text, View.OnClickListener listener) {
        return new TitleBarItem(text, listener);
    }

    public static TitleBarItem buildImage(@DrawableRes int icoRes, View.OnClickListener listener) {
        return new TitleBarItem(icoRes, listener);
    }

    public TitleBarPattern setOnTitleDoubleTapListener(RGestureDetector.OnDoubleTapListener onTitleDoubleTapListener) {
        mOnTitleDoubleTapListener = onTitleDoubleTapListener;
        return this;
    }

    public TitleBarPattern setTitleBarBGColor(@ColorInt int titleBarBGColor) {
        mTitleBarBGColor = titleBarBGColor;
        return this;
    }

    public TitleBarPattern setShowBackImageView(boolean showBackImageView) {
        isShowBackImageView = showBackImageView;
        return this;
    }

    public TitleBarPattern setTitleString(String titleString) {
        mTitleString = titleString;
        return this;
    }

    public TitleBarPattern setTitleString(Context context, @StringRes int res) {
        mTitleString = context.getResources().getString(res);
        return this;
    }

    public TitleBarPattern setTitleSize(float size /*px*/) {
        this.mTitleSize = size;
        return this;
    }

    public TitleBarPattern setTextViewSize(TextView textView) {
        if (mTitleSize != -1) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTitleSize);
        }
        return this;
    }

    public TitleBarPattern setTitleTextColor(int titleTextColor) {
        mTitleTextColor = titleTextColor;
        return this;
    }

    public TitleBarPattern setTitleStringLength(int titleStringLength) {
        mTitleStringLength = titleStringLength;
        return this;
    }

    public TitleBarPattern setTitleBarPaddingTop(int titleBarPaddingTop) {
        this.titleBarPaddingTop = titleBarPaddingTop;
        return this;
    }

    public TitleBarPattern setFixStatusHeight(boolean fixStatusHeight) {
        isFixStatusHeight = fixStatusHeight;
        return this;
    }

    public TitleBarPattern setTitleAnim(boolean titleAnim) {
        this.titleAnim = titleAnim;
        return this;
    }

    public TitleBarPattern setTitleHide(boolean titleHide) {
        this.titleHide = titleHide;
        return this;
    }

    public TitleBarPattern setFloating(boolean floating) {
        isFloating = floating;
        return this;
    }

    public TitleBarPattern setFixContentHeight(boolean fixContentHeight) {
        isFixContentHeight = fixContentHeight;
        return this;
    }

    public TitleBarPattern setLeftItems(ArrayList<TitleBarItem> leftItems) {
        mLeftItems = leftItems;
        return this;
    }

    public TitleBarPattern setRightItems(ArrayList<TitleBarItem> rightItems) {
        mRightItems = rightItems;
        return this;
    }

    public TitleBarPattern addLeftItem(TitleBarItem leftItem) {
        mLeftItems.add(leftItem);
        return this;
    }

    public TitleBarPattern addRightItem(TitleBarItem rightItem) {
        mRightItems.add(rightItem);
        return this;
    }

    public TitleBarPattern addRightItem(int index, TitleBarItem rightItem) {
        mRightItems.add(index, rightItem);
        return this;
    }

    public TitleBarPattern setTitleGravity(int titleGravity) {
        this.titleGravity = titleGravity;
        return this;
    }

    public TitleBarPattern setBackImageRes(int backImageRes) {
        this.backImageRes = backImageRes;
        return this;
    }

    public TitleBarPattern setBackImageString(String backImageString) {
        this.backImageString = backImageString;
        return this;
    }

    public TitleBarPattern setOnInitTitleLayout(OnInitTitleLayout onInitTitleLayout) {
        mOnInitTitleLayout = onInitTitleLayout;
        return this;
    }

    public TitleBarPattern setShowTitleBarBottomLine(boolean showTitleBarBottomLine) {
        this.showTitleBarBottomLine = showTitleBarBottomLine;
        return this;
    }

    public TitleBarPattern setBottomTitleBarLineHeight(int bottomTitleBarLineHeight) {
        this.bottomTitleBarLineHeight = bottomTitleBarLineHeight;
        return this;
    }

    public TitleBarPattern setBottomTitleBarLineColor(int bottomTitleBarLineColor) {
        this.bottomTitleBarLineColor = bottomTitleBarLineColor;
        return this;
    }

    public void clear() {
        mLeftItems.clear();
        mRightItems.clear();
        mOnInitTitleLayout = null;
    }

    public TitleBarPattern setOnBackListener(View.OnClickListener onBackListener) {
        mOnBackListener = onBackListener;
        return this;
    }

    public TitleBarPattern setTitleBarBGDrawable(Drawable titleBarBGDrawable) {
        mTitleBarBGDrawable = titleBarBGDrawable;
        return this;
    }

    public TitleBarPattern setShowDarkLoading(boolean showDarkLoading) {
        this.showDarkLoading = showDarkLoading;
        return this;
    }

    public TitleBarPattern setShowTitleBarBottomShadow(boolean showBottomShadow) {
        this.showBottomShadow = showBottomShadow;
        return this;
    }
}
