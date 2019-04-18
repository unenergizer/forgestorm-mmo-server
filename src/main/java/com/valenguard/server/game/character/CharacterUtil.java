package com.valenguard.server.game.character;

import com.valenguard.server.game.world.entity.Appearance;
import com.valenguard.server.game.world.entity.Player;

public class CharacterUtil {

    public static Appearance generateAppearance(Player player, short bodyId, short headId, byte colorId) {
        short[] initialPlayerTextureIds = new short[4];
        initialPlayerTextureIds[Appearance.BODY] = bodyId;
        initialPlayerTextureIds[Appearance.HEAD] = headId;
        initialPlayerTextureIds[Appearance.ARMOR] = -1; // -1 = Armor not equipped
        initialPlayerTextureIds[Appearance.HELM] = -1; // -1 = Helm not equipped
        return new Appearance(player, colorId, initialPlayerTextureIds);
    }
}
