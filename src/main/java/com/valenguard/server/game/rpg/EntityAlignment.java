package com.valenguard.server.game.rpg;

public enum EntityAlignment {
    HOSTILE,
    NEUTRAL,
    FRIENDLY;

    public byte getEntityAlignmentByte() {
        return (byte) this.ordinal();
    }
}
