package com.forgestorm.server.game.world.maps.building;

@SuppressWarnings("unused")
public enum LayerDefinition {

    ROOF,
    WALL,
    WALL_DECORATION,
    GROUND,
    GROUND_DECORATION;

    public static LayerDefinition getLayerDefinition(byte entityTypeByte) {
        for (LayerDefinition entityType : LayerDefinition.values()) {
            if ((byte) entityType.ordinal() == entityTypeByte) {
                return entityType;
            }
        }
        return null;
    }

    public byte getLayerDefinitionByte() {
        return (byte) this.ordinal();
    }
}
