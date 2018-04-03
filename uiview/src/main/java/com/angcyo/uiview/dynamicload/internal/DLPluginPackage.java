/*
 * Copyright (C) 2014 singwhatiwanna(任玉刚) <singwhatiwanna@gmail.com>
 *
 * collaborator:田啸,宋思宇,Mr.Simple
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.angcyo.uiview.dynamicload.internal;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.angcyo.uiview.dynamicload.ProxyCompatActivity;

import dalvik.system.DexClassLoader;

/**
 * A plugin apk. Activities in a same apk share a same AssetManager, Resources
 * and DexClassLoader.
 *
 * @author siyu.song
 */
public class DLPluginPackage {

    public String packageName;
    public String defaultActivity;
    public DexClassLoader classLoader;
    public AssetManager assetManager;
    public Resources resources;
    public PackageInfo packageInfo;

    public String dexPath;

    public DLPluginPackage(DexClassLoader loader, Resources resources,
                           PackageInfo packageInfo, String dexPath) {
        this.packageName = packageInfo.packageName;
        this.classLoader = loader;
        this.assetManager = resources.getAssets();
        this.resources = resources;
        this.packageInfo = packageInfo;
        this.dexPath = dexPath;

        defaultActivity = parseDefaultActivityName();
    }

    private final String parseDefaultActivityName() {
        if (packageInfo.activities != null && packageInfo.activities.length > 0) {
            return packageInfo.activities[0].name;
        }
        return "";
    }

    /**
     * 接管资源处理
     */

    public View inflate(Context context, @LayoutRes int resource, @Nullable ViewGroup root) {
        return inflate(context, resource, root, root != null);
    }

    public View inflate(Context context, @LayoutRes int resource, @Nullable ViewGroup root, boolean attachToRoot) {
        if (context instanceof ProxyCompatActivity) {
            ((ProxyCompatActivity) context).setPluginPackage(this);
        }
        final XmlResourceParser parser = resources.getLayout(resource);
        try {
            return LayoutInflater.from(context).inflate(parser, root, attachToRoot);
        } finally {
            parser.close();
            if (context instanceof ProxyCompatActivity) {
                ((ProxyCompatActivity) context).setPluginPackage(null);
            }
        }
    }

    @NonNull
    public String getString(@StringRes int id) {
        return resources.getString(id);
    }

    public int getDimensionPixelOffset(@DimenRes int id) {
        return resources.getDimensionPixelOffset(id);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @ColorInt
    public int getColor(@ColorRes int id, @Nullable Resources.Theme theme) {
        return resources.getColor(id, theme);
    }

    @ColorInt
    @Deprecated
    public int getColor(@ColorRes int id) {
        return resources.getColor(id);
    }

    public Drawable getDrawable(@DrawableRes int id) {
        return resources.getDrawable(id);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Drawable getDrawable(@DrawableRes int id, @Nullable Resources.Theme theme) {
        return resources.getDrawable(id, theme);
    }
}
