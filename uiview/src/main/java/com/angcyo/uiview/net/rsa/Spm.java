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


    public static void main(String[] args) {

        String encode = create("1234567");
        System.out.println("encode : " + encode);

        String decode = decode(encode);
        System.out.println("decode : " + decode);


        String encode2 = create("123");
        System.out.println("encode : " + encode2);

        String decode2 = decode(encode2);
        System.out.println("decode : " + decode2);
    }

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
            e.printStackTrace();
        }
        return builder.toString().replaceAll("=", "-").replaceAll("\\n", "");
    }

    public static String decode(String result) {
        StringBuilder builder = new StringBuilder();
        try {
            String key = "klgwl";
            String keyEncode = Base64Utils.encode(key.getBytes()).replaceAll("\\n", "");
            String str = result.replaceAll("-", "=");

            int count = 2 * keyEncode.length();

            if (str.length() > count) {
                String lastStr = str.substring(count);
                for (int i = 0; i < count; i += 2) {
                    builder.append(str.charAt(i));
                }
                builder.append(lastStr);
            } else {
                for (int i = 0; i < str.length(); i += 2) {
                    builder.append(str.charAt(i));
                }
            }
            return new String(Base64Utils.decode(builder.toString()));

//            byte[] decode = Base64Utils.decode(result);
//            String value = new String(decode);
//            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
