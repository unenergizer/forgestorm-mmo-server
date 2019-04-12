package com.valenguard.server.game;

public enum ScreenType {
    LOGIN,
    CHARACTER_SELECT,
    GAME;

    public static ScreenType getScreenType(byte screenByte) {
        for (ScreenType screen : ScreenType.values()) {
            if ((byte) screen.ordinal() == screenByte) {
                return screen;
            }
        }
        return null;
    }

    public byte getScreenTypeByte() {
        return (byte) this.ordinal();
    }
}
