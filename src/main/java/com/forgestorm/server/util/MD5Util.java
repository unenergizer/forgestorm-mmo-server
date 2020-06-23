package com.forgestorm.server.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.forgestorm.server.util.Log.println;

/**
 * Used to get Gravatar email hash for XenForo player profiles
 * URL: https://en.gravatar.com/site/implement/images/java/
 */
public class MD5Util {

    public static String hashUserEmail(String email) {
        return md5Hex(email.trim().toLowerCase());
    }

    public static String md5Hex(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return hex(md.digest(message.getBytes("CP1252")));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            println(MD5Util.class, e.toString());
        }
        return null;
    }

    public static String hex(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }
}
