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
public class ProxyActivity extends AppCompatActivity {

    protected DLPluginPackage pluginPackage;
    protected Resources.Theme pluginTheme;

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
