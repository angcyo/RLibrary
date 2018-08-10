package com.angcyo.uiview.widget;

import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

/**
 * Created by angcyo on 2018-08-10.
 * Email:angcyo@126.com
 */
public class CharInputFilter implements InputFilter {

    //默认允许所有输入
    private int filterModel = 0xFF;

    //允许中文输入
    public static final int MODEL_CHINESE = 1;

    //允许输入大小写字母
    public static final int MODEL_CHAR_LETTER = 2;

    //允许输入数字
    public static final int MODEL_NUMBER = 4;

    //允许输入Ascii码表的[33-126]的字符
    public static final int MODEL_ASCII_CHAR = 8;

    //限制输入的最大字符数, 小于0不限制
    private int maxInputLength = -1;


    public CharInputFilter() {
    }

    public CharInputFilter(int filterModel) {
        this.filterModel = filterModel;
    }

    public void setFilterModel(int filterModel) {
        this.filterModel = filterModel;
    }

    public void setMaxInputLength(int maxInputLength) {
        this.maxInputLength = maxInputLength;
    }

    /**
     * 是否是中文
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    /**
     * 是否是大小写字母
     */
    public static boolean isCharLetter(char c) {
        // Allow [a-zA-Z]
        if ('a' <= c && c <= 'z')
            return true;
        if ('A' <= c && c <= 'Z')
            return true;
        return false;
    }

    public static boolean isNumber(char c) {
        return ('0' <= c && c <= '9');
    }

    public static boolean isAsciiChar(char c) {
        return (33 <= c && c <= 126);
    }

    /**
     * 将 dest 字符串中[dstart, dend] 位置对应的字符串, 替换成 source 字符串中 [start, end] 位置对应的字符串.
     *
     * */
    @Override
    public CharSequence filter(CharSequence source, //本次需要更新的字符串, (可以理解为输入法输入的字符,比如:我是文本)
                               int start, //取 source 字符串的开始位置,通常是0
                               int end,//取 source 字符串的结束位置,通常是source.length()
                               Spanned dest, //原始字符串
                               int dstart, //原始字符串开始的位置,
                               int dend //原始字符串结束的位置, 这种情况会在你已经选中了很多个字符, 然后用输入法输入字符的情况下.
    ) {
        //此次操作后, 原来的字符数量
        int length = dest.length() - (dend - dstart);
        if (maxInputLength > 0) {
            if (length == maxInputLength) {
                return "";
            }
        }

        SpannableStringBuilder modification = new SpannableStringBuilder();

        for (int i = start; i < end; i++) {
            char c = source.charAt(i);

            boolean append = false;

            if ((filterModel & MODEL_CHINESE) == MODEL_CHINESE) {
                append = isChinese(c) || append;
            }
            if ((filterModel & MODEL_CHAR_LETTER) == MODEL_CHAR_LETTER) {
                append = isCharLetter(c) || append;
            }
            if ((filterModel & MODEL_NUMBER) == MODEL_NUMBER) {
                append = isNumber(c) || append;
            }
            if ((filterModel & MODEL_ASCII_CHAR) == MODEL_ASCII_CHAR) {
                append = isAsciiChar(c) || append;
            }

            if (append) {
                modification.append(c);
            }
        }

        if (maxInputLength > 0) {

            int newLength = length + modification.length();
            if (newLength > maxInputLength) {
                //越界
                modification.delete(maxInputLength - length, modification.length());
            }
        }

        return modification;//返回修改后, 允许输入的字符串. 返回null, 由系统处理.
    }
}
