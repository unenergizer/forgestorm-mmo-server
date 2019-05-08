package com.valenguard.server.game.world.item;

import com.valenguard.server.game.world.entity.AppearanceType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ItemStackType {
    // Main Body
    HELM(AppearanceType.HELM_TEXTURE),
    CHEST(AppearanceType.CHEST_TEXTURE),
    PANTS(AppearanceType.PANTS_TEXTURE),
    SHOES(AppearanceType.SHOES_TEXTURE),
    CAPE,
    GLOVES(AppearanceType.GLOVES_COLOR),
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

    private AppearanceType appearanceType;

    ItemStackType() {
    }
}
