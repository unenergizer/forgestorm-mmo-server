package com.valenguard.server.game.character;

import com.valenguard.server.game.world.entity.Appearance;
import com.valenguard.server.game.world.entity.Player;

public class CharacterUtil {

    public static Appearance generateAppearance(Player player, short headId, byte colorId) {
        short[] initialPlayerTextureIds = new short[6];
        initialPlayerTextureIds[Appearance.BODY] = -1; // -1 = NOT USED //TODO: REFACTOR AND REMOVE
        initialPlayerTextureIds[Appearance.HEAD] = headId;
        initialPlayerTextureIds[Appearance.HELM] = -1; // -1 = HELM not equipped
        initialPlayerTextureIds[Appearance.CHEST] = -1; // -1 = CHEST not equipped
        initialPlayerTextureIds[Appearance.PANTS] = -1; // -1 = PANTS not equipped
        initialPlayerTextureIds[Appearance.SHOES] = -1; // -1 = SHOES not equipped
        return new Appearance(player, colorId, initialPlayerTextureIds);
    }
}
