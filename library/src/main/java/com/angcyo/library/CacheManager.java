package com.angcyo.library;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用程序缓存管理, 用来清理, 计算缓存大小
 * Created by angcyo on 2017/09/30 0030.
 */

public class CacheManager {

    /**
     * 缓存目录列表
     */
    List<String> cachePaths = new ArrayList<>();

    private CacheManager() {
    }

    public static CacheManager instance() {
        return Holder.instance;
    }

    private static class Holder {
        static CacheManager instance = new CacheManager();
    }

}
