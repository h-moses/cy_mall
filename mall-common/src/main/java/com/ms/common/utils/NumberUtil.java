package com.ms.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberUtil {

    public static boolean isPhone(String phone) {
        Pattern compile = Pattern.compile("^((13[0-9])|(14[5,7])|(15[^4, \\D])|(17[0-8])|(18[0-9]))\\d{8}$");
        Matcher matcher = compile.matcher(phone);
        return matcher.matches();
    }
}
