package com.angcyo.uiview.net.rsa;

import java.util.Calendar;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：红鸟RSA加密算法工具,可以正常使用
 * 创建人员：Robi
 * 创建时间：2016/12/12 16:49
 * 修改人员：Robi
 * 修改时间：2016/12/12 16:49
 * 修改备注：
 * Version: 1.0.0
 */
public class RSA {

//    static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC+OdspoCjCgBf/jv+Sdx3mP+JIC1i8YCi+jU6rkeBNWOTfFovA3HSk1ODZ0shWD+E0dRTMlJmFpgffFqbytsRJPsDieeNgP/0psJsejOogG6S+lSZgWAnrxRWYpno8Z5CSgBWWgXHjPGN9u22zc23TFoxjcZUjvee4AM72It/9dQIDAQAB";
//    /**
//     * 资讯API签名
//     */
//    static final String PUBLIC_KEY_INFO = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC+OdspoCjCgBf/jv+Sdx3mP+JIC1i8YCi+jU6rkeBNWOTfFovA3HSk1ODZ0shWD+E0dRTMlJmFpgffFqbytsRJPsDieeNgP/0psJsejOogG6S+lSZgWAnrxRWYpno8Z5CSgBWWgXHjPGN9u22zc23TFoxjcZUjvee4AM72It/9dQIDAQAB";

    public static final String E_PUBLIC_KEY = "LmQw7Kza/ILJ1F1/tsqAj8oLjHpPe4nDuTRdTaeZaN34uBGrocVOf2aGR3q9KKVWHpPMDJ776iy9faUstycjxZiwBuCpdZwZy56LaxTVlBsi0To1bS2gjglZ7oRx0TWO7SZFEiwR5UIYlTSzS4bI75zqOS1bm3LwHhdvpC1K1ZiRzK/8qKWhBxO81OEk7eYCeP/lFgjon0EsWuuSvfjjpctBgHq/Rf/xyLg8G5myi0nfFneVJCco9MdarH9ulPxou9bV+IMPgimTluU1A7OyQbNMAvjdhSK/KoRY0Q==";

    public static final String E_PUBLIC_KEY_INFO = "7LeYKWnAg7XZJ0h8LWRIQxLSwYeCFp8Hp5Y07uxVqowiT3IpCzrYdM9EItQy+q+R/NKZxi7BZaBpFfrqJb8OhhmwilWwOtqJzIL6pGOeElUE9y9N/S5cz9yYW44RSxB/FEabGylujRrc02hSMLyL2VRHAAQSlxrR1kPUDaG9AR6yfbaxWc7KFPI9l2qDcMKYF/AwlX5NshcsreQMlLJ/evPbOr/ZoyJ4AM0S8qA82gD8+4AMhebdSwjeHjUo3WQZACzm150CKiXksYkEZh/18ZQvvonvvEGpJBn45g==";

    public static String PUBLIC_KEY = E_PUBLIC_KEY;

    public static String PUBLIC_KEY_INFO = E_PUBLIC_KEY_INFO;

    public static String E_SALT = "";

    /**
     * 加密
     */
    public static String encode(String data) {
        return encode(data, false);
    }

    public static String encode(String data, boolean useSalt) {
        String data_ = data;
        if (useSalt) {
//            Calendar cal = Calendar.getInstance();
//            int month = cal.get(Calendar.MONTH) + 1;
//            int year = cal.get(Calendar.YEAR);
//            int day = cal.get(Calendar.DAY_OF_MONTH);
//            data_ = data + "&" + sdbmHash(month * year * day + "");
            data_ = data + "&" + E_SALT;

        }

        String encode = "";
        byte[] encodedData;
        try {
            encodedData = RSAUtils.encryptByPublicKey(data_.getBytes(), PUBLIC_KEY);
            encode = Base64Utils.encode(encodedData);
        } catch (Exception e) {
        }
        return encode.replaceAll("\\n", "");
    }

    public static String encodeInfo(String data, boolean useSalt) {
        String data_ = data;
        if (useSalt) {
//            Calendar cal = Calendar.getInstance();
//            int month = cal.get(Calendar.MONTH) + 1;
//            int year = cal.get(Calendar.YEAR);
//            int day = cal.get(Calendar.DAY_OF_MONTH);
//            data_ = data + "&" + sdbmHash(month * year * day + "");
            data_ = data + "&" + E_SALT;
        }

        String encode = "";
        byte[] encodedData;
        try {
            encodedData = RSAUtils.encryptByPublicKey(data_.getBytes(), PUBLIC_KEY_INFO);
            encode = Base64Utils.encode(encodedData);
        } catch (Exception e) {
        }
        return encode.replaceAll("\\n", "");
    }

    /**
     * 解密
     */
    public static String decode(String encode, String privateKey) {
        String target = "";
        byte[] decodedData;
        try {
            decodedData = RSAUtils.decryptByPrivateKey(Base64Utils.decode(encode), privateKey);
            target = new String(decodedData);
        } catch (Exception e) {
        }
        return target;
    }

    public static long sdbmHash(String str) {
        if (str == null || str.length() == 0) {
            return 0;
        }
        long hash = 0;
        for (int i = 0; i < str.length(); i++) {
            hash = (str.charAt(i) + (hash << 6) + (hash << 16)) - hash;
        }
        return hash & 0x7FFFFFFF;

    }

}
