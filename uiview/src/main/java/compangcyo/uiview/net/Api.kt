package com.angcyo.uiview.net

import com.orhanobut.hawk.Hawk

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/01/22 16:05
 * 修改人员：Robi
 * 修改时间：2018/01/22 16:05
 * 修改备注：
 * Version: 1.0.0
 */
class Api : RRetrofit() {
    companion object {
        var IS_TEST = false
            set(value) {
                field = value
                Hawk.put("is_test", field)
            }
            get() {
                return Hawk.get<Boolean>("is_test", false)
            }

        var API_URL = "http://api.klgwl.com/"
        var API_TEST_URL = "http://120.78.182.253:8181/"

        var OPEN_URL = "http://open.klgwl.com/"
        var OPEN_TEST_URL = "http://120.78.182.253:8185/"

        var SERVICE_URL = "http://service.klgwl.com/"
        var SERVICE_TEST_URL = "http://120.78.182.253:80/"

        var H5_URL = "http://wap.klgwl.com"
        var H5_TEST_URL = "http://120.78.182.253:8182"


        fun <T> api(cls: Class<T>): T {
            return api(cls, CacheType.NO_CACHE)
        }

        fun <T> open(cls: Class<T>): T {
            return open(cls, CacheType.NO_CACHE)
        }

        fun <T> service(cls: Class<T>): T {
            return service(cls, CacheType.NO_CACHE)
        }

        fun <T> open(cls: Class<T>, cacheType: CacheType): T {
            return create(cls, cacheType, if (IS_TEST) OPEN_TEST_URL else OPEN_URL)
        }

        fun <T> api(cls: Class<T>, cacheType: CacheType): T {
            return create(cls, cacheType, if (IS_TEST) API_TEST_URL else API_URL)
        }

        fun <T> service(cls: Class<T>, cacheType: CacheType): T {
            return create(cls, cacheType, if (IS_TEST) SERVICE_TEST_URL else SERVICE_URL)
        }

        fun h5BaseUrl(): String {
            return if (IS_TEST) {
                return H5_TEST_URL
            } else {
                return H5_URL
            }
        }

        fun h5Url(url: String): String {
            return if (IS_TEST) {
                return "$H5_TEST_URL$url"
            } else {
                return "$H5_URL$url"
            }
        }
    }
}