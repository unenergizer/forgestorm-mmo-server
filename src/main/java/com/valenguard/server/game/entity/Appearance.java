package com.valenguard.server.game.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Appearance {

    public static final int BODY = 0; // Base ID
    public static final int HEAD = 1; // Base ID
    public static final int ARMOR = 2; // Cover Base ID
    public static final int HELM = 3; // Cover Base ID

    @Getter
    private byte colorId;

    /**
     * IDs are arranged from head to toe or from top to bottom.
     */
    @Getter
    private short[] textureIds;

    public short getTextureId(int index) {
        return textureIds[index];
    }
}
