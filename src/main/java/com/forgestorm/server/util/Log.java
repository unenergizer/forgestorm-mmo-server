package com.forgestorm.server.util;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.discord.DiscordManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {

    private final static String DATE_PATTERN = "dd-MM-yyyy HH:mm:ss";
    private final static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    private Log() {
    }

    public static void println(Class clazz, String message) {
        println(clazz, message, false, true);
    }

    public static void println(Class clazz, String message, boolean isError) {
        println(clazz, message, isError, true);
    }

    public static void println(Class clazz, String message, boolean isError, boolean print) {
        if (!print) return;
        DiscordManager discordManager = ServerMain.getInstance().getDiscordManager();
        String builtMessage = buildMessage(clazz, message);
        if (isError) {
            System.err.println(builtMessage);
            if (discordManager != null)
                discordManager.sendDiscordMessage("<@&266430645776023562> [ERROR LOGGED] ```" + builtMessage + "```");
        } else {
            System.out.println(builtMessage);
            if (discordManager != null) discordManager.sendDiscordMessage(builtMessage);
        }
    }

    private static String buildMessage(Class clazz, String message) {
        return TIME_FORMATTER.format(LocalDateTime.now()) + "  [" + clazz.getSimpleName() + "] " + message;
    }

    public static void println(boolean print) {
        if (!print) return;
        DiscordManager discordManager = ServerMain.getInstance().getDiscordManager();
        System.out.println();
        if (discordManager != null) discordManager.sendDiscordMessage("*** ***");
    }
}
