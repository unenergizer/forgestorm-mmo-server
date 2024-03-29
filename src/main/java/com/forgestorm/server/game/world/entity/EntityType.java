package com.forgestorm.server.game.world.entity;

public enum EntityType {
    CLIENT_PLAYER,
    PLAYER,
    NPC,
    MONSTER,
    ITEM_STACK,
    SKILL_NODE;

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
