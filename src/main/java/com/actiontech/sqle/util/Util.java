package com.actiontech.sqle.util;

public class Util {
    public static String getEllipsisString(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        } else {
            return text.substring(0, maxLength - 3) + "..."; // 截断文本并添加省略号
        }
    }
}
