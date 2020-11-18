package com.forgestorm.server.io;

import lombok.AllArgsConstructor;

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
    TILE_PROPERTIES("graphics/TileProperties.yaml"),

    // Scripts
    SCRIPTS("scripts/"),
    SCRIPT_MAPPING("scripts/script_mapping.yaml");

    private String filePath;

    public String getFilePath() {
        return "/data/" + filePath;
    }
}
