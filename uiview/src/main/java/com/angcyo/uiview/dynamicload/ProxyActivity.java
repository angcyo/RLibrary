package com.angcyo.uiview.dynamicload;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;

import com.angcyo.uiview.dynamicload.internal.DLPluginPackage;

/**
 * Created by angcyo on 2018/04/01 21:37
 */
public class ProxyActivity extends AppCompatActivity {

    protected DLPluginPackage pluginPackage;

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
        return super.getTheme();
    }

    public DLPluginPackage getPluginPackage() {
        return pluginPackage;
    }

    public void setPluginPackage(DLPluginPackage pluginPackage) {
        this.pluginPackage = pluginPackage;
    }
}
