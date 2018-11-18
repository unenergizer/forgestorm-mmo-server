package com.valenguard.server.game.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Appearance {

    /**
     * IDs are arranged from head to toe or from top to bottom.
     */
    @Getter
    private short[] textureIds;

    public short getTextureId(int index) {
        return textureIds[index];
    }
}
