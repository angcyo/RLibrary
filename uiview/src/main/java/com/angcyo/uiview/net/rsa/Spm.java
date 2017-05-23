package com.angcyo.uiview.net.rsa;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * spm生成规则：
 * <p>
 * 自定义加密(用户id_类型_数据id_时间戳)
 * <p>
 * 创建人员：Robi
 * 创建时间：2017/05/23 17:58
 * 修改人员：Robi
 * 修改时间：2017/05/23 17:58
 * 修改备注：
 * Version: 1.0.0
 */
public class Spm {
    public static String create(String value) {
        StringBuilder builder = new StringBuilder();
        try {
            String key = "klgwl";
            String keyEncode = Base64Utils.encode(key.getBytes());
            String valueEncode = Base64Utils.encode(value.getBytes());

            int count = Math.min(keyEncode.length(), valueEncode.length());
            int i = 0;
            for (; i < count; i++) {
                builder.append(valueEncode.charAt(i));
                builder.append(keyEncode.charAt(i));
            }
            builder.append(valueEncode.substring(i));
        } catch (Exception e) {

        }
        return builder.toString().replaceAll("=", "-");
    }
}
