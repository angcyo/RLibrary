package com.angcyo.uiview.dynamicload;

import android.app.Activity;
import android.content.Intent;

import com.angcyo.uiview.base.UILayoutActivity;
import com.angcyo.uiview.utils.Reflect;
import com.angcyo.uiview.utils.T_;
import com.angcyo.uiview.view.IView;

/**
 * Created by angcyo on 2018/04/01 17:36
 */
public class ProxyStartActivity extends UILayoutActivity {

    /**
     * 需要启动那个包
     */
    public static final String START_PACKAGE_NAME = "start_package_name";

    /**
     * 启动的类名
     */
    public static final String START_CLASS_NAME = "start_class_name";

    public static void start(Activity activity, String packageName, String className) {
        Intent intent = new Intent(activity, ProxyStartActivity.class);
        intent.putExtra(START_PACKAGE_NAME, packageName);
        intent.putExtra(START_CLASS_NAME, className);
        activity.startActivity(intent);
    }

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
}
