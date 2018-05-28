package com.angcyo.uiview.container;

import android.os.Bundle;
import android.view.View;

import com.angcyo.uiview.model.ViewPattern;
import com.angcyo.uiview.skin.ISkin;
import com.angcyo.uiview.view.IView;

import java.util.List;

/**
 * 可以添加IView, 必须实现的接口
 * Created by angcyo on 2016-11-12.
 */

public interface ILayout {

    /**
     * 开始一个IView, 返回创建的View, 执行动画
     */
    void startIView(IView iView, UIParam param);//2016-12-19

    void startIView(IView iView);

    /**
     * 移除View
     */
    void finishIView(View view, boolean needAnim);

    void finishIView(View view);

    void finishIView(IView iview, boolean needAnim);//2016-12-14

    void finishIView(IView iview, final UIParam param);//2016-12-29

    void finishIView(IView iview);//2016-12-14

    void finishIView(IView iview, boolean needAnim, boolean quiet);//2016-12-15

    void finishIView(Class<?> clz);//2017-3-15 根据类型, 关闭页面

    void finishIView(Class<?> clz, boolean keepLast /*如果clz是最后一个, 是否需要keep?*/);//2018-2-1

    /**
     * 显示一个View
     */
    void showIView(View view, boolean needAnim);

    void showIView(View view);

    void showIView(final View view, final UIParam param);//2016-12-15, 2017-7-26

    void showIView(IView iview, boolean needAnim);//2016-12-16

    void showIView(IView iview);//2016-12-16

    void showIView(final IView iview, final UIParam param);//2016-12-16, 2017-7-26

    /**
     * 替换一个View
     */
    void replaceIView(IView iView, final UIParam param);//2016-12-19

    void replaceIView(IView iView);

    /**
     * 隐藏一个View
     */
    void hideIView(View view, boolean needAnim);

    void hideIView(View view, UIParam param);

    void hideIView(IView iView, UIParam param);

    void hideIView(View view);

    /**
     * 返回Layout
     */
    View getLayout();

    /**
     * 请求返回, 最后面会调用 {@link #finishIView(IView, UIParam)},
     * 你也可以直接后面的方法, 绕过 backPress检查逻辑
     */
    boolean requestBackPressed();

    boolean requestBackPressed(final UIParam param);//2017-1-3

    /**
     * 结束所有的IView, 不会有动画执行, 最上层的IVew 也不会有 生命周期的回调
     * {@link ILayout#finishIView(IView, boolean, boolean)} 类似此方法quiet=true 的情况
     */
    void finishAll();//2016-12-16

    /**
     * 结束所有的IView,
     * 参考
     * {@link ILayout#finishAll()}
     *
     * @param keepLast true 会保留最上层的IView
     */
    void finishAll(boolean keepLast);//2016-12-16

    /**
     * 排除在外的界面, 都会finish
     *
     * @param keepList        需要保留的列表
     * @param keepLast        是否保留最后的界面
     * @param lastFinishParam 可以为null
     */
    void finishAllWithKeep(List<Class<? extends IView>> keepList, boolean keepLast, final UIParam lastFinishParam);//2017-6-23

    /**
     * 通过类,返回存在的IView对象
     */
    <IV extends IView> IV getIViewWith(Class<IV> cls);//2017-6-23

    /**
     * 强制退出
     */
    void finish();//2016-12-16

    void onSkinChanged(ISkin skin);//星期六 2017-4-1

    /**
     * 返回倒数lastCount的ViewPattern
     */
    ViewPattern getViewPatternAtLast(int lastCount);

    void onLastViewReShow(Bundle bundle);//星期二 2017-5-23

    void onLastViewShow(Bundle bundle);//星期二 2017-5-23

    void onLastViewHide();//星期二 2017-5-23

    void setChildILayout(ILayout iLayout);//星期二 2017-5-23

    ViewPattern findViewPatternByClass(Class<?> clz);//2018-03-03

    <T extends IView> T findIViewByClass(Class<T> clz);//2018-5-28
}
