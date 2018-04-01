package com.angcyo.uiview.dynamicload;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.angcyo.uiview.base.UILayoutActivity;
import com.angcyo.uiview.dynamicload.internal.DLPluginPackage;
import com.angcyo.uiview.utils.Reflect;
import com.angcyo.uiview.utils.T_;
import com.angcyo.uiview.view.IView;

/**
 * Created by angcyo on 2018/04/01 17:36
 */
public class ProxyActivity extends UILayoutActivity {

    /**
     * 需要启动那个包
     */
    public static final String START_PACKAGE_NAME = "start_package_name";

    /**
     * 启动的类名
     */
    public static final String START_CLASS_NAME = "start_class_name";

    public static void start(Activity activity, String packageName, String className) {
        Intent intent = new Intent(activity, ProxyActivity.class);
        intent.putExtra(START_PACKAGE_NAME, packageName);
        intent.putExtra(START_CLASS_NAME, className);
        activity.startActivity(intent);
    }

    DLPluginPackage pluginPackage;

    @Override
    protected void onLoadView(Intent intent) {
        String packageName = intent.getStringExtra(START_PACKAGE_NAME);
        String className = intent.getStringExtra(START_CLASS_NAME);

        pluginPackage = RPlugin.INSTANCE.getPluginPackage(packageName);
        if (pluginPackage == null) {
            T_.error("插件启动失败.");
            finishSelf();
        } else {
            Class<?> pluginClass = RPlugin.INSTANCE.loadPluginClass(pluginPackage.classLoader, className);
            if (pluginClass == null) {
                T_.error("插件类启动失败.");
                finishSelf();
            } else {
                IView iView = Reflect.newObject(pluginClass);
                iView.setPluginPackage(pluginPackage);
                startIView(iView);
            }
        }
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
        return super.getTheme();
    }
}
