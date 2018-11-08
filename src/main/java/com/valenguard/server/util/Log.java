package com.valenguard.server.util;

public class Log {

    public static void println(Class clazz, String message) {
        println(clazz, message, false);
    }

    public static void println(Class clazz, String message, boolean error) {
        if (error) System.err.println(buildMessage(clazz, message));
        else System.out.println(buildMessage(clazz, message));
    }

    private static String buildMessage(Class clazz, String message) {
        return "[" + clazz.getSimpleName() + "] " + message;
    }
}
