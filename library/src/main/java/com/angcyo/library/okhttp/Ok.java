package com.angcyo.library.okhttp;

import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/05/10 14:00
 * 修改人员：Robi
 * 修改时间：2017/05/10 14:00
 * 修改备注：
 * Version: 1.0.0
 */
public class Ok {
    private final Map<String, ImageType> imageTypeCache = new ConcurrentHashMap<>();
    private OkHttpClient sOkHttpClient;

    private Ok() {
    }

    public static Ok instance() {
        return Holder.instance;
    }

    private void ensureClient() {
        if (sOkHttpClient == null) {
            sOkHttpClient = new OkHttpClient.Builder()
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .build();
        }
    }

    public void load(String url) {
        Call call = getCall(url);
        try {
            Response response = call.execute();
            response.body().byteStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected Call getCall(String url) {
        ensureClient();
        Request mRequest = new Request.Builder().url(url).build();
        return sOkHttpClient.newCall(mRequest);
    }

    /**
     * 根据url, 回调对应图片的类型
     */
    public void type(final String url, final OnImageTypeListener listener) {
        ImageType type = imageTypeCache.get(url);

        if (listener != null) {
            listener.onLoadStart();
        }

        if (type == null || type == ImageType.UNKNOWN) {
            Call call = getCall(url);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    imageTypeCache.put(url, ImageType.UNKNOWN);
                    if (listener != null) {
                        ThreadExecutor.instance().onMain(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) {
                                    listener.onImageType(ImageType.UNKNOWN);
                                }
                            }
                        });
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String imageType = ImageTypeUtil.getImageType(response.body().byteStream());
                    final ImageType imageType1 = ImageType.valueOf(imageType);
                    imageTypeCache.put(url, imageType1);
                    if (listener != null) {
                        ThreadExecutor.instance().onMain(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) {
                                    listener.onImageType(imageType1);
                                }
                            }
                        });
                    }
                }
            });
        } else {
            if (listener != null) {
                listener.onImageType(type);
            }
        }
    }

    public enum ImageType {
        JPEG, GIF, PNG, BMP, UNKNOWN;

        public static ImageType of(String type) {
            if (TextUtils.isEmpty(type)){
                    return UNKNOWN;
            }
            if ("JPEG".equalsIgnoreCase(type)) {
                return JPEG;
            }
            if ("GIF".equalsIgnoreCase(type)) {
                return GIF;
            }
            if ("PNG".equalsIgnoreCase(type)) {
                return PNG;
            }
            if ("BMP".equalsIgnoreCase(type)) {
                return BMP;
            }
            return UNKNOWN;
        }
    }

    public interface OnImageTypeListener {
        void onImageType(ImageType imageType);

        void onLoadStart();
    }

    private static class Holder {
        static Ok instance = new Ok();
    }

    public static class ImageTypeUtil {

        public static String getImageType(File file) {
            if (file == null) return null;
            InputStream is = null;
            try {
                is = new FileInputStream(file);
                return getImageType(is);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public static String getImageType(InputStream is) {
            if (is == null) return null;
            try {
                byte[] bytes = new byte[8];
                return is.read(bytes, 0, 8) != -1 ? getImageType(bytes) : null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        private static String getImageType(byte[] bytes) {
            if (isJPEG(bytes)) return "JPEG";
            if (isGIF(bytes)) return "GIF";
            if (isPNG(bytes)) return "PNG";
            if (isBMP(bytes)) return "BMP";
            return null;
        }

        private static boolean isJPEG(byte[] b) {
            return b.length >= 2
                    && (b[0] == (byte) 0xFF) && (b[1] == (byte) 0xD8);
        }

        private static boolean isGIF(byte[] b) {
            return b.length >= 6
                    && b[0] == 'G' && b[1] == 'I'
                    && b[2] == 'F' && b[3] == '8'
                    && (b[4] == '7' || b[4] == '9') && b[5] == 'a';
        }

        private static boolean isPNG(byte[] b) {
            return b.length >= 8
                    && (b[0] == (byte) 137 && b[1] == (byte) 80
                    && b[2] == (byte) 78 && b[3] == (byte) 71
                    && b[4] == (byte) 13 && b[5] == (byte) 10
                    && b[6] == (byte) 26 && b[7] == (byte) 10);
        }

        private static boolean isBMP(byte[] b) {
            return b.length >= 2
                    && (b[0] == 0x42) && (b[1] == 0x4d);
        }
    }

}
