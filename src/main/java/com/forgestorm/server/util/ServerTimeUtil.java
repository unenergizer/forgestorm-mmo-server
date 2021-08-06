package com.forgestorm.server.util;

import com.forgestorm.server.game.GameConstants;

@SuppressWarnings("unused")
public class ServerTimeUtil {

    public static int getSeconds(int seconds) {
        return GameConstants.TICKS_PER_SECOND * seconds;
    }

    public static int getMinutes(int minutes) {
        return getSeconds(60) * minutes;
    }

    public static int getHours(int hours) {
        return getMinutes(60) * hours;
    }

    public static int getDays(int days) {
        return getHours(24) * days;
    }

}
