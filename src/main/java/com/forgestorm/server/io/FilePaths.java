package com.forgestorm.server.io;

import lombok.AllArgsConstructor;

import java.io.File;

@AllArgsConstructor
public enum FilePaths {

    // Root
    DATABASE_SETTINGS("Database.yaml"),
    NETWORK_SETTINGS("Network.yaml"),

    // Entity
    AI_ENTITY("entity/AiEntities.yaml"),
    FACTIONS("entity/Factions.yaml"),
    SKILL_NODES("entity/SkillNodes.yaml"),

    // Item
    DROP_TABLE("item/DropTables.yaml"),
    ENTITY_SHOP("item/ShopItems.yaml"),
    ITEM_STACK("item/ItemStacks.yaml"),

    // Abilities
    COMBAT_ABILITIES("abilities/CombatAbilities.yaml"),

    // Maps
    MAPS("maps/");

    private String filePath;

    public String getFilePath() {
        boolean useLocal = false;
        if (useLocal) {
            return ResourcePathLoader.getResourcePath() + File.separator + filePath.replace("/", File.separator);
        } else {
            return "src/main/resources/data/" + filePath;
        }
    }
}
