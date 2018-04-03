package com.angcyo.uiview.dynamicload;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.angcyo.uiview.container.UILayoutImpl;
import com.angcyo.uiview.dynamicload.internal.DLPluginPackage;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/04/02 18:49
 * 修改人员：Robi
 * 修改时间：2018/04/02 18:49
 * 修改备注：
 * Version: 1.0.0
 */
public class PActivity extends Activity {
    protected DLPluginPackage pluginPackage;
    protected Resources.Theme pluginTheme;
    protected UILayoutImpl mUILayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUILayout = new UILayoutImpl(this);
        setContentView(mUILayout);
        onProxyCreate(savedInstanceState);
    }

    /**
     * 代理创建回调
     */
    protected void onProxyCreate(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public ClassLoader getClassLoader() {
        if (pluginPackage != null && pluginPackage.classLoader != null) {
            return pluginPackage.classLoader;
        }
        return super.getClassLoader();
    }

    @Override
    public AssetManager getAssets() {
        if (pluginPackage != null && pluginPackage.assetManager != null) {
            return pluginPackage.assetManager;
        }
        return super.getAssets();
    }

    @Override
    public Resources getResources() {
        if (pluginPackage != null && pluginPackage.resources != null) {
            return pluginPackage.resources;
        }
        return super.getResources();
    }

    @Override
    public Resources.Theme getTheme() {
        if (pluginTheme != null) {
            return pluginTheme;
        }
        return super.getTheme();
    }

    public DLPluginPackage getPluginPackage() {
        return pluginPackage;
    }

    public void setPluginPackage(DLPluginPackage pluginPackage) {
        this.pluginPackage = pluginPackage;
        if (pluginPackage == null) {
            pluginTheme = null;
        } else {
            if (pluginPackage.resources == null) {
                pluginTheme = null;
            } else {
                //theme用来在xml中,创建view
                pluginTheme = pluginPackage.resources.newTheme();
//                pluginTheme.setTo(super.getTheme());
                //pluginTheme.applyStyle();
            }
        }
    }
}
