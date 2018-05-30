package com.angcyo.uiview.recycler.widget;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.angcyo.uiview.R;
import com.angcyo.uiview.kotlin.ViewExKt;
import com.angcyo.uiview.view.RClickListener;
import com.angcyo.uiview.widget.RTextView;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/05/11 09:37
 * 修改人员：Robi
 * 修改时间：2017/05/11 09:37
 * 修改备注：
 * Version: 1.0.0
 */
public class MenuBuilder {
    private final Context mContext;
    private List<MenuItem> mMenuList;

    public MenuBuilder(Context context) {
        mContext = context;
        mMenuList = new ArrayList<>();
    }

    public MenuBuilder addMenu(String title, int bgColor, View.OnClickListener clickListener) {
        mMenuList.add(new MenuItem(title, bgColor, clickListener)
                .setPadding(mContext.getResources().getDimensionPixelOffset(R.dimen.base_xxhdpi)));
        return this;
    }

    public MenuBuilder addMenu(MenuItem item) {
        mMenuList.add(item.setPadding(mContext.getResources().getDimensionPixelOffset(R.dimen.base_xxhdpi)));
        return this;
    }

    private RTextView createMenuItem(String title, int bgColor, View.OnClickListener clickListener) {
        RTextView menu = new RTextView(mContext);
        menu.setText(title);
        menu.setRBackgroundColor(bgColor);
        menu.setBackgroundResource(R.drawable.base_bg_selector);
        menu.setOnClickListener(clickListener);
        return menu;
    }

    public void clear() {
        mMenuList.clear();
    }

    /**
     * 此方法自动会处理, 请勿手动调用
     */
    public void build(final SwipeRecycleViewItemLayout itemLayout) {
        int targetMenuSize = mMenuList.size();
        ViewGroup menuLayout = itemLayout.getMenuView();
        int menuSize = menuLayout.getChildCount();

        if (menuSize > targetMenuSize) {
            for (int i = menuSize - 1; i > targetMenuSize - 1; i--) {
                menuLayout.removeViewAt(i);
            }
        }

        for (int i = menuLayout.getChildCount(); i < targetMenuSize; i++) {
            RTextView textView = new RTextView(mContext);
            textView.setBackgroundResource(R.drawable.base_bg_selector);
            menuLayout.addView(textView, new ViewGroup.LayoutParams(-2, -1));
        }

        for (int i = 0; i < targetMenuSize; i++) {
            View childAt = menuLayout.getChildAt(i);
            final MenuItem menuItem = mMenuList.get(i);

            childAt.setPadding(menuItem.paddLeft, 0, menuItem.paddRight, 0);

            ViewExKt.setOnRClickListener(childAt, new RClickListener() {
                @Override
                public void onRClick(@Nullable final View view) {
                    if (menuItem.autoCloseMenu) {
                        itemLayout.close();
                        itemLayout.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                menuItem.clickListener.onClick(view);
                            }
                        }, 360);
                    } else {
                        menuItem.clickListener.onClick(view);
                    }
                }
            });

            if (menuItem.bgResId != -1) {
                childAt.setBackgroundResource(menuItem.bgResId);
            }

            if (childAt instanceof TextView) {
                ((TextView) childAt).setText(menuItem.title);
                ((TextView) childAt).setTextColor(menuItem.textColor);
                ((TextView) childAt).setGravity(menuItem.gravity);
            }
            if (childAt instanceof RTextView) {
                ((RTextView) childAt).setRBackgroundColor(menuItem.bgColor);
            }
        }
    }

    public static class MenuItem {
        String title;
        int bgColor, textColor;
        int bgResId = -1;//背景资源id
        View.OnClickListener clickListener;
        int paddLeft, paddTop, paddBottom, paddRight;
        int gravity;

        /*点击之后, 自动关闭菜单, 如果为true, 会延时clickListener 的回调*/
        boolean autoCloseMenu = true;
        private Object tag;

        public MenuItem() {
        }

        public MenuItem(String title,
                        int bgColor,
                        View.OnClickListener clickListener) {
            this.title = title;
            this.bgColor = bgColor;
            this.clickListener = clickListener;
            textColor = Color.WHITE;
            gravity = Gravity.CENTER;
        }

        public Object getTag() {
            return tag;
        }

        public void setTag(Object tag) {
            this.tag = tag;
        }

        public MenuItem setGravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        public MenuItem setTextColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public MenuItem setTitle(String title) {
            this.title = title;
            return this;
        }

        public MenuItem setBgColor(int bgColor) {
            this.bgColor = bgColor;
            return this;
        }

        public MenuItem setClickListener(View.OnClickListener clickListener) {
            this.clickListener = clickListener;
            return this;
        }

        public MenuItem setPaddLeft(int paddLeft) {
            this.paddLeft = paddLeft;
            return this;
        }

        public MenuItem setPadding(int padd) {
            setPaddLeft(padd);
            setPaddRight(padd);
            setPaddTop(padd);
            setPaddBottom(padd);
            return this;
        }

        public MenuItem setAutoCloseMenu(boolean autoCloseMenu) {
            this.autoCloseMenu = autoCloseMenu;
            return this;
        }

        public MenuItem setBgResId(int bgResId) {
            this.bgResId = bgResId;
            return this;
        }

        public MenuItem setPaddTop(int paddTop) {
            this.paddTop = paddTop;
            return this;
        }

        public MenuItem setPaddBottom(int paddBottom) {
            this.paddBottom = paddBottom;
            return this;
        }

        public MenuItem setPaddRight(int paddRight) {
            this.paddRight = paddRight;
            return this;
        }
    }

}
