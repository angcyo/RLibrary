package com.angcyo.uiview.net;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by angcyo on 2018-02-25.
 */

public class P {

    public static Map<String, String> ofMap(String... args) {
        final Map<String, String> map = new HashMap<>();
        foreach(new OnPutValue() {
            @Override
            public void onValue(String key, String value) {
                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                    map.put(key, value);
                }
            }

            @Override
            public void onRemove(String key) {
                map.remove(key);
            }
        }, args);
        return map;
    }

    public static void foreach(OnPutValue onPutValue, String... args) {
        if (onPutValue == null || args == null) {
            return;
        }
        for (String str : args) {
            String[] split = str.split(":");
            if (split.length >= 2) {
                String first = split[0];
                onPutValue.onValue(first, str.substring(first.length() + 1));
            } else if (split.length == 1) {
                onPutValue.onRemove(split[0]);
            }
        }
    }

    interface OnPutValue {
        void onValue(String key, String value);

        void onRemove(String key);
    }

    public static class OnValue implements OnPutValue {

        @Override
        public void onValue(String key, String value) {

        }

        @Override
        public void onRemove(String key) {

        }
    }
}
