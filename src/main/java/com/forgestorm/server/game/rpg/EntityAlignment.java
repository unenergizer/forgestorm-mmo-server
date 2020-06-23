package com.forgestorm.server.game.rpg;

public enum EntityAlignment {
    HOSTILE,
    NEUTRAL,
    FRIENDLY;

    public static EntityAlignment getEntityAlignment(byte entityTypeByte) {
        for (EntityAlignment entityAlignment : EntityAlignment.values()) {
            if ((byte) entityAlignment.ordinal() == entityTypeByte) {
                return entityAlignment;
            }
        }
        return null;
    }

    public byte getEntityAlignmentByte() {
        return (byte) this.ordinal();
    }
}
