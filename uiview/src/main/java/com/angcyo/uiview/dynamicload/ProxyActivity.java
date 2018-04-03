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
public class ProxyActivity extends Activity {
    protected DLPluginPackage mPluginPackage;
    protected Resources.Theme mPluginTheme;
    protected UILayoutImpl mUILayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onProxyCreate(savedInstanceState);
    }

    /**
     * 代理创建回调
     */
    protected void onProxyCreate(@Nullable Bundle savedInstanceState) {
        mUILayout = new UILayoutImpl(this);
        setContentView(mUILayout);
    }

    @Override
    public ClassLoader getClassLoader() {
        if (mPluginPackage != null && mPluginPackage.classLoader != null) {
            return mPluginPackage.classLoader;
        }
        return super.getClassLoader();
    }

    @Override
    public AssetManager getAssets() {
        if (mPluginPackage != null && mPluginPackage.assetManager != null) {
            return mPluginPackage.assetManager;
        }
        return super.getAssets();
    }

    @Override
    public Resources getResources() {
        if (mPluginPackage != null && mPluginPackage.resources != null) {
            return mPluginPackage.resources;
        }
        return super.getResources();
    }

    @Override
    public Resources.Theme getTheme() {
        if (mPluginTheme != null) {
            return mPluginTheme;
        }
        return super.getTheme();
    }

    public DLPluginPackage getPluginPackage() {
        return mPluginPackage;
    }

    public void setPluginPackage(DLPluginPackage mPluginPackage) {
        this.mPluginPackage = mPluginPackage;
        if (mPluginPackage == null) {
            mPluginTheme = null;
        } else {
            if (mPluginPackage.resources == null) {
                mPluginTheme = null;
            } else {
                //theme用来在xml中,创建view
                mPluginTheme = mPluginPackage.resources.newTheme();
//                mPluginTheme.setTo(super.getTheme());
                //mPluginTheme.applyStyle();
            }
        }
    }
}
