package com.valenguard.server.game.world.item;

import com.valenguard.server.game.world.entity.Appearance;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ItemStackType {
    // Main Body
    HELM(Appearance.HELM),
    CHEST(Appearance.CHEST),
    PANTS(Appearance.PANTS),
    SHOES(Appearance.SHOES),
    CAPE,
    GLOVES,
    BELT,

    // Rings
    RING,
    NECKLACE,

    // Weapons
    SWORD,
    BOW,
    SHIELD,
    ARROW,

    // Generic
    GOLD,
    POTION,
    MATERIAL;

    // Trade Items
    // Skill Items

    private int appearanceId;

    ItemStackType() {
    }
}
