package com.angcyo.uiview.dynamicload;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.angcyo.library.utils.L;
import com.angcyo.uiview.dynamicload.internal.DLPluginPackage;
import com.angcyo.uiview.dynamicload.utils.DLConfigs;
import com.angcyo.uiview.utils.Reflect;
import com.angcyo.uiview.utils.T_;
import com.angcyo.uiview.view.IView;

/**
 * Created by angcyo on 2018/04/01 17:36
 */
public class ProxyStartActivity extends PActivity {

    /**
     * 需要启动那个包
     */
    public static final String START_PACKAGE_NAME = "start_package_name";

    /**
     * 启动的类名
     */
    public static final String START_CLASS_NAME = "start_class_name";
    /**
     * 需要启动的插件包名
     */
    String pluginPackageName;
    /**
     * 需要启动的插件类名
     */
    String pluginClassName;

    public static void start(Activity activity, String packageName, String className) {
        Intent intent = new Intent(activity, ProxyStartActivity.class);
        intent.putExtra(START_PACKAGE_NAME, packageName);
        intent.putExtra(START_CLASS_NAME, className);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onProxyCreate(@Nullable Bundle savedInstanceState) {
        super.onProxyCreate(savedInstanceState);
        onProxyCreate(getIntent());
    }

//    @Override
//    public Resources.Theme getTheme() {
//        pluginTheme = null;
//        return super.getTheme();
//    }

    public void onProxyCreate(Intent intent) {
        pluginPackageName = intent.getStringExtra(START_PACKAGE_NAME);
        pluginClassName = intent.getStringExtra(START_CLASS_NAME);

        // set the extra's class loader
        intent.setExtrasClassLoader(DLConfigs.sPluginClassloader);

        L.w("ProxyStartActivity", "mClass=" + pluginClassName + " mPackageName=" + pluginPackageName);

        DLPluginPackage pluginPackage = RPlugin.INSTANCE.getPluginPackage(pluginPackageName);
        if (pluginPackage == null) {
            T_.error("插件启动失败.");
            finish();
        } else {
            //this.pluginPackage = pluginPackage;
            setPluginPackage(pluginPackage);
            //getTheme().setTo(pluginPackage.resources.newTheme());
            Class<?> pluginClass = RPlugin.INSTANCE.loadPluginClass(pluginPackage.classLoader, pluginClassName);

            if (pluginClass == null) {
                T_.error("插件类启动失败.");
                finish();
            } else {
                IView iView = Reflect.newObject(pluginClass);
////                iView.setPluginPackage(pluginPackage);
//                setPluginPackage(pluginPackage);
                mUILayout.startIView(iView);
//                //startIView(new DynamicLoadUIView());
            }

        }
        //setTheme(R.style.BaseWhiteAppTheme);
    }

//    @Override
//    protected void onLoadView(Intent intent) {
//        if (pluginPackage != null) {
//            Class<?> pluginClass = RPlugin.INSTANCE.loadPluginClass(pluginPackage.classLoader, pluginClassName);
//            if (pluginClass == null) {
//                T_.error("插件类启动失败.");
//                finish();
//            } else {
//                IView iView = Reflect.newObject(pluginClass);
//////                iView.setPluginPackage(pluginPackage);
////                setPluginPackage(pluginPackage);
//                //startIView(iView);
////                //startIView(new DynamicLoadUIView());
//            }
//        } else {
//            Tip.tip("插件加载失败!");
//        }
//
//    }
}
