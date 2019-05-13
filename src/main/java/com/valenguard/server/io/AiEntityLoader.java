package com.valenguard.server.io;

import com.valenguard.server.game.rpg.EntityAlignment;
import com.valenguard.server.game.world.entity.EntityType;
import lombok.Getter;
import lombok.Setter;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.valenguard.server.util.Log.println;

public class AiEntityLoader {

    private static final boolean PRINT_DEBUG = false;

    public List<AiEntityData> loadMovingEntities() {
        println(PRINT_DEBUG);
        println(getClass(), "====== START LOADING MOVING-ENTITIES ======", false, PRINT_DEBUG);

        Yaml yaml = new Yaml();

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(FilePaths.AI_ENTITY.getFilePath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Map<Integer, Map<String, Object>> root = yaml.load(inputStream);

        List<AiEntityData> aiEntityDataList = new ArrayList<>();

        for (Map.Entry<Integer, Map<String, Object>> entry : root.entrySet()) {
            Map<String, Object> entityDataNode = entry.getValue();

            // Load io from file
            int entityDataID = entry.getKey();
            String name = (String) entityDataNode.get("name");
            String type = (String) entityDataNode.get("type");
            String alignment = (String) entityDataNode.get("alignment");
            String faction = (String) entityDataNode.get("faction");
            int health = (Integer) entityDataNode.get("health");
            int damage = (Integer) entityDataNode.get("damage");
            int expDrop = (Integer) entityDataNode.get("expDrop");
            int dropTable = (Integer) entityDataNode.get("dropTable");
            double walkSpeed = (Double) entityDataNode.get("walkSpeed");
            double probabilityStill = (Double) entityDataNode.get("probabilityStill");
            double probabilityWalkStart = (Double) entityDataNode.get("probabilityWalkStart");
            Integer shopID = (Integer) entityDataNode.get("shopID");
            boolean isBankKeeper = (Boolean) entityDataNode.get("bankKeeper");

            // Create EntityData
            AiEntityData aiEntityData = new AiEntityData(entityDataID);
            aiEntityData.setName(name);
            aiEntityData.setEntityType(EntityType.valueOf(type));
            if (alignment != null) aiEntityData.setEntityAlignment(EntityAlignment.valueOf(alignment));
            aiEntityData.setFaction(faction);
            aiEntityData.setHealth(health);
            aiEntityData.setDamage(damage);
            aiEntityData.setExpDrop(expDrop);
            aiEntityData.setDropTable(dropTable);
            aiEntityData.setWalkSpeed((float) walkSpeed);
            aiEntityData.setProbabilityStill((float) probabilityStill);
            aiEntityData.setProbabilityWalkStart((float) probabilityWalkStart);
            aiEntityData.setShopID(shopID == null ? -1 : shopID);
            aiEntityData.setBankKeeper(isBankKeeper);

            // Appearance Data
            Integer monsterBodyTexture = (Integer) entityDataNode.get("monsterBodyTexture");
            Integer hairTexture = (Integer) entityDataNode.get("hairTexture");
            Integer helmTexture = (Integer) entityDataNode.get("helmTexture");
            Integer chestTexture = (Integer) entityDataNode.get("chestTexture");
            Integer pantsTexture = (Integer) entityDataNode.get("pantsTexture");
            Integer shoesTexture = (Integer) entityDataNode.get("shoesTexture");
            Integer hairColor = (Integer) entityDataNode.get("hairColor");
            Integer eyesColor = (Integer) entityDataNode.get("eyesColor");
            Integer skinColor = (Integer) entityDataNode.get("skinColor");
            Integer glovesColor = (Integer) entityDataNode.get("glovesColor");

            aiEntityData.setMonsterBodyTexture(monsterBodyTexture);
            aiEntityData.setHairTexture(hairTexture);
            aiEntityData.setHelmTexture(helmTexture);
            aiEntityData.setChestTexture(chestTexture);
            aiEntityData.setPantsTexture(pantsTexture);
            aiEntityData.setShoesTexture(shoesTexture);
            aiEntityData.setHairColor(hairColor);
            aiEntityData.setEyeColor(eyesColor);
            aiEntityData.setSkinColor(skinColor);
            aiEntityData.setGlovesColor(glovesColor);

            // Loading Finished & Creation Finished! Save me!
            aiEntityDataList.add(aiEntityData);

            // Debug print
            println(getClass(), "EntityDataID: " + aiEntityData.getEntityDataID(), false, PRINT_DEBUG);
            println(getClass(), "Name: " + aiEntityData.getName(), false, PRINT_DEBUG);
            println(getClass(), "Type: " + aiEntityData.getEntityType(), false, PRINT_DEBUG);
            println(getClass(), "Alignment: " + aiEntityData.getEntityAlignment(), false, PRINT_DEBUG);
            println(getClass(), "Faction: " + aiEntityData, false, PRINT_DEBUG);
            println(getClass(), "Health: " + aiEntityData.getHealth(), false, PRINT_DEBUG);
            println(getClass(), "Damage: " + aiEntityData.getDamage(), false, PRINT_DEBUG);
            println(getClass(), "ExpDrop: " + aiEntityData.getExpDrop(), false, PRINT_DEBUG);
            println(getClass(), "DropTable: " + aiEntityData.getDropTable(), false, PRINT_DEBUG);
            println(getClass(), "WalkSpeed: " + aiEntityData.getWalkSpeed(), false, PRINT_DEBUG);
            println(getClass(), "ProbabilityStill: " + aiEntityData.getProbabilityStill(), false, PRINT_DEBUG);
            println(getClass(), "ProbabilityWalkStart: " + aiEntityData.getProbabilityWalkStart(), false, PRINT_DEBUG);
            println(getClass(), "ShopID: " + aiEntityData.getShopID(), false, PRINT_DEBUG);
            println(PRINT_DEBUG);
            println(getClass(), "-- AppearanceData --", false, PRINT_DEBUG);
            println(getClass(), "MonsterBody: " + aiEntityData.getMonsterBodyTexture(), false, PRINT_DEBUG);
            println(getClass(), "Hair: " + aiEntityData.getHairTexture(), false, PRINT_DEBUG);
            println(getClass(), "Chest: " + aiEntityData.getChestTexture(), false, PRINT_DEBUG);
            println(getClass(), "Pants: " + aiEntityData.getPantsTexture(), false, PRINT_DEBUG);
            println(getClass(), "Shoes: " + aiEntityData.getShoesTexture(), false, PRINT_DEBUG);
            println(getClass(), "HairColor: " + aiEntityData.getHairColor(), false, PRINT_DEBUG);
            println(getClass(), "EyesColor: " + aiEntityData.getEyeColor(), false, PRINT_DEBUG);
            println(getClass(), "SkinColor: " + aiEntityData.getSkinColor(), false, PRINT_DEBUG);
            println(getClass(), "GlovesColor: " + aiEntityData.getGlovesColor(), false, PRINT_DEBUG);
            println(PRINT_DEBUG);
            println(PRINT_DEBUG);
        }

        println(getClass(), "====== END LOADING MOVING-ENTITIES ======", false, PRINT_DEBUG);
        println(PRINT_DEBUG);
        return aiEntityDataList;
    }

    @Setter
    @Getter
    public class AiEntityData {

        private final int entityDataID;

        private String name;
        private EntityType entityType;
        private EntityAlignment entityAlignment;
        private String faction;
        private int health;
        private int damage;
        private int expDrop;
        private int dropTable;
        private float walkSpeed;
        private float probabilityStill;
        private float probabilityWalkStart;
        private Integer shopID;
        private boolean isBankKeeper;

        // Appearance Data
        private Integer monsterBodyTexture;
        private Integer hairTexture;
        private Integer helmTexture;
        private Integer chestTexture;
        private Integer pantsTexture;
        private Integer shoesTexture;
        private Integer hairColor;
        private Integer eyeColor;
        private Integer skinColor;
        private Integer glovesColor;

        AiEntityData(final int entityDataID) {
            this.entityDataID = entityDataID;
        }
    }
}
