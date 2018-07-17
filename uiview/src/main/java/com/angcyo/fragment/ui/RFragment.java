package com.angcyo.fragment.ui;

import android.animation.Animator;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/07/16 17:51
 * 修改人员：Robi
 * 修改时间：2018/07/16 17:51
 * 修改备注：
 * Version: 1.0.0
 */
public class RFragment extends Fragment {

    /*保持生命周期的顺序调用*/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(getFragmentLayoutId(), container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        /* transit = 0
        * enter  = true
        * nextAnim = 0
        */
        return super.onCreateAnimator(transit, enter, nextAnim);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        /* transit = 0
         * enter  = false
         * nextAnim = 0
         */
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    /*------------------------自定义 可重载的方法---------------------------*/

    protected int getFragmentLayoutId() {
        return -1;
    }
}
