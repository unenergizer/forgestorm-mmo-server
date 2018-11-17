package com.valenguard.server.game.entity;

public enum EntityType {
    CLIENT_PLAYER,
    PLAYER,
    NPC,
    ITEM,
    MONSTER;

    public static EntityType getEntityType(byte entityTypeByte) {
        for (EntityType entityType : EntityType.values()) {
            if ((byte) entityType.ordinal() == entityTypeByte) {
                return entityType;
            }
        }
        return null;
    }

    public byte getEntityTypeByte() {
        return (byte) this.ordinal();
    }
}
