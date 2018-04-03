package com.angcyo.uiview.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import com.angcyo.uiview.base.UIBaseView;
import com.angcyo.uiview.base.UILayoutActivity;
import com.angcyo.uiview.container.ILayout;
import com.angcyo.uiview.container.UIParam;
import com.angcyo.uiview.dynamicload.internal.DLPluginPackage;
import com.angcyo.uiview.model.AnimParam;
import com.angcyo.uiview.model.TitleBarPattern;
import com.angcyo.uiview.model.ViewPattern;
import com.angcyo.uiview.skin.ISkin;
import com.angcyo.uiview.widget.viewpager.UIViewPager;

/**
 * Created by angcyo on 2016-11-05.
 */

public interface IView {

    /**
     * 不需要标题栏,请返回null, 请在实现的时候, 做好缓存
     */
    @Deprecated
    TitleBarPattern loadTitleBar(Context context);

    /**
     * 生命周期顺序: 1
     * 请在此方法中, 进行xml的inflate操作, 如果使用了ButterKnife, 请在loadContentView方法之后初始化view的相应操作.
     */
    View inflateContentView(Activity activity, ILayout iLayout, FrameLayout container, LayoutInflater inflater);

    /**
     * 生命周期顺序: 2
     * 当loadContentView完成之后会调用, mViewHolder在此方法中创建
     * 请使用{@link #onViewCreate(View, UIParam)}
     */
    @Deprecated
    void onViewCreate(View rootView);

    void onViewCreate(View rootView, UIParam param);//2017-3-13

    /**
     * 生命周期顺序: 3
     * 此方法会在inflateContentView之后, 紧接着执行, ButterKnife在此初始化
     * 请在此方法中, 使用mViewHolder进行视图初始化
     */
    void loadContentView(View rootView);

    /**
     * 生命周期顺序: 4
     */
    void onViewLoad();

    /**
     * 生命周期顺序: 5
     */
    @Deprecated
    void onViewShow();

    /**
     * 生命周期顺序: 5
     */
    @Deprecated
    void onViewShow(final Bundle bundle);//2016-12-15

    /**
     * 补充
     */
    void onViewShow(final Bundle bundle, Class<?> fromClz /*哪个类关闭了*/);//2018-2-7 14:22:53

    /**
     * {@link com.angcyo.uiview.container.UIParam#start_mode} 是 {@link com.angcyo.uiview.container.UIParam#SINGLE_TOP}
     * 时,会调用此方法
     */
    void onViewReShow(final Bundle bundle);//2017-1-7

    void onViewHide();

    /**
     * 对话框启动后, 底部IView执行的回调, 不会执行onViewHide
     */
    void onViewHideFromDialog();//2018-2-9

    @Deprecated
    void onViewUnload();

    void onViewUnload(UIParam uiParam /*关闭时, 传递过来的参数*/);

    /**
     * 当有些界面需要释放很多资源时, 为了界面流畅, 请在此方法中释放
     */
    @Deprecated
    void onViewUnloadDelay();//2018-1-23

    void onViewUnloadDelay(UIParam uiParam);//2018-2-28

    /**
     * 开始动画
     */
    Animation loadStartAnimation(AnimParam animParam);

    /**
     * 结束动画
     */
    Animation loadFinishAnimation(AnimParam animParam);

    /**
     * 显示动画
     */
    Animation loadShowAnimation(AnimParam animParam);

    /**
     * 结隐藏动画
     */
    Animation loadHideAnimation(AnimParam animParam);

    /**
     * 其他View开始开始, 退出的动画
     */
    Animation loadOtherExitAnimation(AnimParam animParam);

    /**
     * 其他View结束, 进入的动画
     */
    Animation loadOtherEnterAnimation(AnimParam animParam);

    Animation loadOtherHideAnimation(AnimParam animParam);

    Animation loadOtherShowAnimation(AnimParam animParam);

    /**
     * 布局动画
     */
    Animation loadLayoutAnimation(AnimParam animParam);

    /**
     * 是否是对话框, 对话框显示在对话框层
     */
    boolean isDialog();

    /**
     * 对话框背景是否变暗
     */
    boolean isDimBehind();

    /**
     * 点击变暗处,是否关闭对话框
     */
    boolean canCanceledOnOutside();

    /**
     * 窗口外是否可点击, 当此方法返回false时, 点击窗口外将不能关闭对话框
     */
    boolean canTouchOnOutside();

    /**
     * 双击是否可以关闭对话框
     */
    boolean canDoubleCancel();

    /**
     * 是否可以关闭对话框
     */
    boolean canCancel();

    /**
     * 获取对应的RootView
     */
    View getView();

    /**
     * 获取对话框变暗动画的View, 默认是对话框root layout
     */
    View getDialogDimView();//星期二 2017-1-10

    /**
     * 获取动画作用的View
     */
    View getAnimView();//星期二 2017-1-10

    /**
     * 获取对应的ILayout接口
     */
    ILayout getILayout();

    void onShowInPager(UIViewPager viewPager);//2016-11-26

    void onHideInPager(UIViewPager viewPager);//2016-11-26

    void onActivityResult(int requestCode, int resultCode, Intent data);//2016-12-13

    /**
     * 获取对话框 变暗时的颜色
     */
    int getDimColor();//2016-12-15

    /**
     * 请求是否可以退出.
     */
    boolean onBackPressed();//2016-12-16

    /**
     * 滑动返回的时候,请求是否可以结束.
     */
    boolean canSwipeBackPressed();//星期三 2017-2-22

    /**
     * 在导航上, 左边的按钮界面, 调到右边的按钮界面, 或者 右边的按钮界面, 跳到左边的按钮界面.
     * 用来决定动画是否需要反向执行
     */
    IView setIsRightJumpLeft(boolean isRightJumpLeft);//2016-12-16

    /**
     * 会在start iVew 之后, 最先执行
     */
    void onAttachedToILayout(ILayout iLayout);

    /**
     * 是否可以左边侧滑关闭
     */
    boolean canTryCaptureView();//星期二 2017-2-14

    /**
     * 当皮肤更改时回调
     */
    void onSkinChanged(ISkin skin);//星期六 2017-4-1

    /**
     * 是否需要在对话框上显示
     */
    boolean showOnDialog();//星期五 2017-4-28

    /**
     * 绑定一个其他的ILayout, 用于嵌套使用ILayout
     */
    IView bindParentILayout(ILayout iLayout);//星期二 2017-5-23

    boolean haveParentILayout();//星期二 2017-5-23

    boolean haveChildILayout();//星期六 2017-07-01

    /**
     * 返回当前IView的视图显示状态
     */
    IViewShowState getIViewShowState();//星期二 2017-5-23

    /**
     * 是否需要过渡动画, 在低设备上默认会关闭动画, 可以通过这2个方法, 强行开启动画
     * 2017-9-1 移动至UIParam中
     */
    boolean needTransitionStartAnim();//星期三 2017-8-9

    boolean needTransitionExitAnim();//星期三 2017-8-9

    void release();//星期二 2017-8-29

    /**
     * 当检测到界面不满足测量条件时, 可以通过此方法强制满足测量条件
     */
    boolean needForceMeasure();//是否需要测量

    /**
     * 点击界面其他地方, 自动隐藏键盘 (如果点击在EditText, 或者 tag不为null 的view上, 不会隐藏键盘) 星期四 2017-9-28
     */
    boolean hideSoftInputOnTouchDown();

    /**
     * 当对话框关闭时, 走这个特殊回调 星期五 2017-9-29
     */
    void onViewShowOnDialogFinish();

    /**
     * 是否支持下拉返回
     */
    boolean enableTouchBack(); //星期二 2017-10-17

    /**
     * 下拉返回时, top偏移的距离
     */
    int getOffsetScrollTop(); //星期二 2017-10-24

    /**
     * 当检测到界面不满足测量条件时, 可以通过此方法强制显示, 此方法会包含needForceMeasure. (只会在倒数2个IView时生效)
     */
    boolean needForceVisible();//是否强制可见 2017-11-29

    /**
     * 获取需要请求的屏幕方向
     */
    int getDefaultRequestedOrientation();//2017-12-8

    /**
     * 当整个界面在Ilayout中布局时, 回调
     */
    void onIViewLayout(ViewPattern viewPattern,
                       UIBaseView.LayoutState layoutState /*当IView继承UIBaseView时才有值*/,
                       IViewShowState viewShowState,
                       View rootView);//2018-1-12

    /**
     * 是否是高亮的状态栏, 如果是 状态栏字体要变成灰色
     */
    boolean isLightStatusBar();//2018-2-3

    /**
     * 是否全屏, 隐藏状态栏
     */
    boolean isFullScreen();//2018-2-8

    /**
     * 是否需要中断任务, 用来在 还没有启动任务之前, 如果任务已经开始执行了, 那么无意义
     */
    boolean isInterruptTask();//2018-3-6

    void setInterruptTask(boolean interruptTask);//2018-3-6

    void setPluginPackage(DLPluginPackage pluginPackage);//2018-4-1 插件加载的支持

    boolean isInPlugin();//是否是在插件中加载

    enum IViewShowState {
        STATE_NORMAL,
        STATE_VIEW_CREATE,
        STATE_VIEW_SHOW,
        STATE_VIEW_HIDE,
        STATE_VIEW_LOAD,
        STATE_VIEW_UNLOAD
    }

}
