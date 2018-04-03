package com.angcyo.uiview.dynamicload;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.angcyo.uiview.dynamicload.internal.DLPluginPackage;

/**
 * Created by angcyo on 2018/04/01 21:37
 */
public class ProxyCompatActivity extends AppCompatActivity {
    protected DLPluginPackage mPluginPackage;
    protected Resources.Theme mPluginTheme;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onProxyCreate(savedInstanceState);
    }

    /**
     * 代理创建回调
     */
    protected void onProxyCreate(@Nullable Bundle savedInstanceState) {
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

    public void setPluginPackage(DLPluginPackage pluginPackage) {
        this.mPluginPackage = pluginPackage;
        if (pluginPackage == null) {
            mPluginTheme = null;
        } else {
            if (pluginPackage.resources == null) {
                mPluginTheme = null;
            } else {
                //theme用来在xml中,创建view
                mPluginTheme = pluginPackage.resources.newTheme();
//                mPluginTheme.setTo(super.getTheme());
                //mPluginTheme.applyStyle();
            }
        }
    }
}
