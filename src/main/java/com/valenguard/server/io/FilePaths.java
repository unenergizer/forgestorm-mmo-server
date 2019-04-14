package com.valenguard.server.io;

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

    // Maps
    MAPS("maps/");

    private String filePath;

    public String getFilePath() {
        return "src/main/resources/data/" + filePath;
    }
}
