package com.angcyo.uiview.widget;

import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by angcyo on 2018-08-10.
 * Email:angcyo@126.com
 */
public class CharInputFilter implements InputFilter {

    /**
     * 默认允许所有输入
     */
    private int filterModel = 0xFF;

    /**
     * 允许中文输入
     */
    public static final int MODEL_CHINESE = 1;

    /**
     * 允许输入大小写字母
     */
    public static final int MODEL_CHAR_LETTER = 2;

    /**
     * 允许输入数字
     */
    public static final int MODEL_NUMBER = 4;

    /**
     * 允许输入Ascii码表的[33-126]的字符
     */
    public static final int MODEL_ASCII_CHAR = 8;

    /**
     * callback过滤模式
     */
    public static final int MODEL_CALLBACK = 16;

    /**
     * 身份证号码
     */
    public static final int MODEL_ID_CARD = 32;

    /**
     * 允许输入空格 ASCII 码 32
     */
    public static final int MODEL_SPACE = 64;

    /**
     * 允许非 emoji 字符输入, 即过滤emoji
     */
    public static final int MODEL_NOT_EMOJI = 128;

    /**
     * 限制输入的最大字符数, 小于0不限制
     */
    private int maxInputLength = -1;

    List<OnFilterCallback> callbacks;

    public CharInputFilter() {
    }

    public CharInputFilter(int filterModel) {
        this.filterModel = filterModel;
    }

    public CharInputFilter(int filterModel, int maxInputLength) {
        this.filterModel = filterModel;
        this.maxInputLength = maxInputLength;
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
        return c >= 0x4E00 && c <= 0x9FA5;
//        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
//        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
//                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
//                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
//                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
//                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
//                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
//            return true;
//        }
//        return false;
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

    public static boolean isAsciiSpace(char c) {
        return 32 == c;
    }


    /**
     * 将 dest 字符串中[dstart, dend] 位置对应的字符串, 替换成 source 字符串中 [start, end] 位置对应的字符串.
     */
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
            if ((filterModel & MODEL_SPACE) == MODEL_SPACE) {
                append = isAsciiSpace(c) || append;
            }
            if ((filterModel & MODEL_NOT_EMOJI) == MODEL_NOT_EMOJI) {
                append = !EmojiTools.isEmojiCharacter(c) || append;
            }
            if ((filterModel & MODEL_ID_CARD) == MODEL_ID_CARD) {
                if (length == 14 || length == 17) {
                    String oldString = dest.toString();
                    append = (!oldString.contains("x") &&
                            !oldString.contains("X") &&
                            (c == 'x' || c == 'X')
                    )
                            || append;
                }
            }

            if (callbacks != null && (filterModel & MODEL_CALLBACK) == MODEL_CALLBACK) {
                for (OnFilterCallback callback : callbacks) {
                    append = callback.onFilterAllow(source, c, i, dest, dstart, dend) || append;
                }
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

        //返回修改后, 允许输入的字符串. 返回null, 由系统处理.
        return modification;
    }

    public void addFilterCallback(OnFilterCallback callback) {
        filterModel |= MODEL_CALLBACK;
        if (callbacks == null) {
            callbacks = new ArrayList<>();
        }
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }

    public interface OnFilterCallback {
        /**
         * 是否允许输入字符c
         *
         * @param c 当前需要过滤的char
         * @return 返回true, 过滤.否则允许输入
         * @see InputFilter#filter(CharSequence, int, int, Spanned, int, int)
         */
        boolean onFilterAllow(CharSequence source,
                              char c,
                              int cIndex,
                              Spanned dest,
                              int dstart,
                              int dend);
    }

    /**
     * https://github.com/itgoyo/EmojiUtils
     */
    public static class EmojiTools {

        public static boolean containsEmoji(String str) {
            if (TextUtils.isEmpty(str)) {
                return false;
            }

            for (int i = 0; i < str.length(); i++) {


                int cp = str.codePointAt(i);


                if (isEmojiCharacter(cp)) {
                    return true;
                }
            }
            return false;
        }

        private static boolean isEmojiCharacter(int first) {

       /* 1F30D - 1F567
        1F600 - 1F636
        24C2 - 1F251
        1F680 - 1F6C0
        2702 - 27B0
        1F601 - 1F64F*/

            return !
                    ((first == 0x0) ||
                            (first == 0x9) ||
                            (first == 0xA) ||
                            (first == 0xD) ||
                            ((first >= 0x20) && (first <= 0xD7FF)) ||
                            ((first >= 0xE000) && (first <= 0xFFFD)) ||
                            ((first >= 0x10000))) ||


                    (first == 0xa9 || first == 0xae || first == 0x2122 ||
                            first == 0x3030 || (first >= 0x25b6 && first <= 0x27bf) ||
                            first == 0x2328 || (first >= 0x23e9 && first <= 0x23fa))
                    || ((first >= 0x1F000 && first <= 0x1FFFF))
                    || ((first >= 0x2702) && (first <= 0x27B0))
                    || ((first >= 0x1F601) && (first <= 0x1F64F))
                    ;
        }

        public static String filterEmoji(String str) {

            if (!containsEmoji(str)) {
                return str;
            } else {
            }
            StringBuilder buf = null;
            int len = str.length();
            for (int i = 0; i < len; i++) {
                char codePoint = str.charAt(i);
                if (!isEmojiCharacter(codePoint)) {
                    if (buf == null) {
                        buf = new StringBuilder(str.length());
                    }
                    buf.append(codePoint);
                } else {

                }
            }

            if (buf == null) {
                return "";
            } else {
                return buf.toString();
            }

        }
    }
}
