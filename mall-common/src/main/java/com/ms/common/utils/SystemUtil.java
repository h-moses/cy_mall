package com.ms.common.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class SystemUtil {

    public SystemUtil() {
    }

    public static String genToken(String genTime, Long userId) {
        Random random = new Random();
        String source = genTime + userId + random.longs(100000, 1000000).toString();
        assert null != source && source.length() > 0;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(source.getBytes());
            String token = new BigInteger(1, md5.digest()).toString(16);
            if (token.length() == 31) {
                token = token + "-";
            }
            return token;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
