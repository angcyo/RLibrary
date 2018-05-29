package com.angcyo.uiview.net;

import com.orhanobut.hawk.Hawk;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/05/15 10:16
 * 修改人员：Robi
 * 修改时间：2018/05/15 10:16
 * 修改备注：
 * Version: 1.0.0
 */
public class Api extends RRetrofit {
    public static String API_URL = "http://api.klgwl.com/";
    public static String API_TEST_URL = "http://120.78.182.253:8181/";

    public static String OPEN_URL = "http://open.klgwl.com/";
    public static String OPEN_TEST_URL = "http://120.78.182.253:8185/";

    public static String SERVICE_URL = "http://service.klgwl.com/";
    public static String SERVICE_TEST_URL = "http://120.78.182.253:80/";

    public static String H5_URL = "http://wap.klgwl.com";
    public static String H5_TEST_URL = "http://120.78.182.253:8182";

    public static boolean isTest() {
        return Hawk.get("is_test", false);
    }

    public static void setIsTest(boolean isTest) {
        Hawk.put("is_test", isTest);
    }

    public static <T> T api(Class<T> cls) {
        return api(cls, CacheType.NO_CACHE);
    }

    public static <T> T api(Class<T> cls, CacheType cacheType) {
        return create(cls, cacheType, isTest() ? API_TEST_URL : API_URL);
    }

    public static <T> T open(Class<T> cls) {
        return open(cls, CacheType.NO_CACHE);
    }

    public static <T> T open(Class<T> cls, CacheType cacheType) {
        return create(cls, cacheType, isTest() ? OPEN_TEST_URL : OPEN_URL);
    }

    public static <T> T service(Class<T> cls) {
        return service(cls, CacheType.NO_CACHE);
    }

    public static <T> T service(Class<T> cls, CacheType cacheType) {
        return create(cls, cacheType, isTest() ? SERVICE_TEST_URL : SERVICE_URL);
    }

    public static String h5BaseUrl() {
        return isTest() ? H5_TEST_URL : H5_URL;
    }

    public static String h5Url(String url) {
        return h5BaseUrl() + url;
    }

}
