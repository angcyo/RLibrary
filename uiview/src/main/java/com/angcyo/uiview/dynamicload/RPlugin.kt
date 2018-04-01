package com.angcyo.uiview.dynamicload

import android.text.TextUtils
import com.angcyo.library.utils.L
import com.angcyo.uiview.RApplication
import com.angcyo.uiview.base.UIBaseView
import com.angcyo.uiview.dynamicload.internal.DLPluginManager
import com.angcyo.uiview.dynamicload.internal.DLPluginPackage
import com.angcyo.uiview.net.Func
import com.angcyo.uiview.net.Rx
import com.angcyo.uiview.view.IView
import rx.Observable

/**
 * Created by angcyo on 2018/04/01 15:37
 */
object RPlugin {

    /**从APK中, 加载指定的IView类对象*/
    fun loadIView(dexPath: String, className: String, packageName: String = ""): Observable<IView?> {
        return Rx.create(Func {
            val dlPluginPackage = DLPluginManager.getInstance(RApplication.getApp()).loadApk(dexPath)

            if (dlPluginPackage == null) {
                L.e("RPlugin: 无效的dexPath -> $dexPath")
                null
            } else if (!packageName.isNullOrEmpty() && !TextUtils.equals(packageName, dlPluginPackage.packageName)) {
                L.e("RPlugin: 不匹配的包名 $packageName -> ${dlPluginPackage.packageName}")
                null
            } else {
                val pluginClass = loadPluginClass(dlPluginPackage.classLoader, className)
                if (pluginClass == null) {
                    L.e("RPlugin: 装载插件类失败")
                    null
                } else {
                    if (UIBaseView::class.java.isAssignableFrom(pluginClass) ||
                            IView::class.java.isAssignableFrom(pluginClass)) {
                        L.e("RPlugin: 加载插件类:${pluginClass.name}")
                        val iView: IView = pluginClass.newInstance() as IView
                        iView.setPluginPackage(dlPluginPackage)
                        iView
                    } else {
                        L.e("RPlugin: 装载基类不匹配:${pluginClass.name}")
                        null
                    }
                }
            }
        })
    }

    /**返回匹配通过的插件信息*/
    fun loadPlugin(dexPath: String, className: String, packageName: String = ""): Observable<DLPluginPackage?> {
        return Rx.create(Func {
            val dlPluginPackage = DLPluginManager.getInstance(RApplication.getApp()).loadApk(dexPath)

            if (dlPluginPackage == null) {
                L.e("RPlugin: 无效的dexPath -> $dexPath")
                null
            } else if (!packageName.isNullOrEmpty() && !TextUtils.equals(packageName, dlPluginPackage.packageName)) {
                L.e("RPlugin: 不匹配的包名 $packageName -> ${dlPluginPackage.packageName}")
                null
            } else {
                val pluginClass = loadPluginClass(dlPluginPackage.classLoader, className)
                if (pluginClass == null) {
                    L.e("RPlugin: 装载插件类失败")
                    null
                } else {
                    if (UIBaseView::class.java.isAssignableFrom(pluginClass) ||
                            IView::class.java.isAssignableFrom(pluginClass)) {
                        L.e("RPlugin: 插件加载:${pluginClass.name}")
                        dlPluginPackage
                    } else {
                        L.e("RPlugin: 装载基类不匹配:${pluginClass.name}")
                        null
                    }
                }
            }
        })
    }

    fun getPluginPackage(packageName: String): DLPluginPackage? {
        return DLPluginManager.getInstance(RApplication.getApp()).getPackage(packageName)
    }

    fun loadPluginClass(classLoader: ClassLoader, className: String): Class<*>? {
        var clazz: Class<*>? = null
        try {
            clazz = Class.forName(className, true, classLoader)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        return clazz
    }
}