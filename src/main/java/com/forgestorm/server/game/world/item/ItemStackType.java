package com.forgestorm.server.game.world.item;

import com.forgestorm.server.game.world.entity.AppearanceType;
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
    SWORD(AppearanceType.LEFT_HAND),
    BOW(AppearanceType.LEFT_HAND),
    SHIELD(AppearanceType.RIGHT_HAND),
    ARROW,

    // Generic
    GOLD,
    POTION,
    MATERIAL,

    // Skill Items
    BOOK_SKILL;

    private AppearanceType appearanceType;

    ItemStackType() {
    }
}
