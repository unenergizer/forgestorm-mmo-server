package com.forgestorm.server.util;

public class MathUtil {

    public static int applyArmor(int amountAttackedBy, int armor) {
        double percentage = (100.0D - (Math.log(armor + 1) * 10)) / 100.0D;
        return (int) (amountAttackedBy * percentage);
    }

}
