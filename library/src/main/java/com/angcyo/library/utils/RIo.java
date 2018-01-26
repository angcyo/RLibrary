package com.angcyo.library.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;
import okio.Source;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：Okio操作类 https://github.com/square/okio
 * 创建人员：Robi
 * 创建时间：2018/01/16 11:11
 * 修改人员：Robi
 * 修改时间：2018/01/16 11:11
 * 修改备注：
 * Version: 1.0.0
 */
public class RIo {

    public static long copyFile(String fromFilePath, String toFilePath) throws FileNotFoundException {
        return copyFile(new File(fromFilePath), toFilePath);
    }

    public static long copyFile(File fromFile, String toFilePath) throws FileNotFoundException {
        return copyFile(new FileInputStream(fromFile), toFilePath);
    }

    public static long copyFile(InputStream from, String toFilePath) {
        return copyFile(from, new File(toFilePath));
    }

    public static long copyFile(File fromFile, File toFile) throws FileNotFoundException {
        return copyFile(new FileInputStream(fromFile), toFile);
    }

    public static long copyFile(InputStream from, File toFile) {
        BufferedSource bufferedSource = null;
        BufferedSink bufferedSink = null;
        try {
            Source source = Okio.source(from);
            bufferedSource = Okio.buffer(source);

            if (!toFile.exists()) {
                toFile.createNewFile();
            }
            Sink sink = Okio.sink(toFile);
            bufferedSink = Okio.buffer(sink);

            long all = bufferedSource.readAll(bufferedSink);
            bufferedSink.flush();
            return all;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedSink.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                from.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                bufferedSource.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * 追加数据到文件
     */
    public static boolean appendToFile(String filePath, String data) {
        BufferedSink bufferedSink = null;
        try {
            File file = new File(filePath);
            Sink sink = Okio.appendingSink(file);
            bufferedSink = Okio.buffer(sink);
            //bufferedSink.writeAll(Okio.source(file));
            bufferedSink.writeUtf8(data);
            bufferedSink.flush();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedSink.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
