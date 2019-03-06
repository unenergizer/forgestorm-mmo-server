package com.valenguard.server.game.data;

import com.valenguard.server.game.entity.AiEntityData;
import com.valenguard.server.game.entity.EntityType;
import com.valenguard.server.game.rpg.EntityAlignment;
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

    private static final boolean PRINT_DEBUG = true;
    private static final String FILE_PATH = "src/main/resources/data/entity/AiEntities.yaml";

    public List<AiEntityData> loadMovingEntities() {
        println(PRINT_DEBUG);
        println(getClass(), "====== START LOADING MOVING-ENTITIES ======", false, PRINT_DEBUG);

        Yaml yaml = new Yaml();

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(FILE_PATH));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Map<Integer, Map<String, Object>> root = yaml.load(inputStream);

        List<AiEntityData> aiEntityDataList = new ArrayList<>();

        for (Map.Entry<Integer, Map<String, Object>> entry : root.entrySet()) {
            Map<String, Object> entityDataNode = entry.getValue();

            // Load data from file
            int entityDataID = entry.getKey();
            String name = (String) entityDataNode.get("name");
            String type = (String) entityDataNode.get("type");
            String alignment = (String) entityDataNode.get("alignment");
            Integer colorID = (Integer) entityDataNode.get("colorID");
            int atlasBodyID = (Integer) entityDataNode.get("atlasBodyID");
            Integer atlasHeadID = (Integer) entityDataNode.get("atlasHeadID");
            int health = (Integer) entityDataNode.get("health");
            int damage = (Integer) entityDataNode.get("damage");
            int expDrop = (Integer) entityDataNode.get("expDrop");
            int dropTable = (Integer) entityDataNode.get("dropTable");
            double walkSpeed = (Double) entityDataNode.get("walkSpeed");
            double probabilityStill = (Double) entityDataNode.get("probabilityStill");
            double probabilityWalkStart = (Double) entityDataNode.get("probabilityWalkStart");
            Integer shopID = (Integer) entityDataNode.get("shopID");

            // Create EntityData
            AiEntityData aiEntityData = new AiEntityData(entityDataID);
            aiEntityData.setName(name);
            aiEntityData.setEntityType(EntityType.valueOf(type));
            aiEntityData.setEntityAlignment(EntityAlignment.valueOf(alignment));
            aiEntityData.setColorID(colorID);
            aiEntityData.setAtlasBodyID((short) atlasBodyID);
            aiEntityData.setAtlasHeadID(atlasHeadID);
            aiEntityData.setHealth(health);
            aiEntityData.setDamage(damage);
            aiEntityData.setExpDrop(expDrop);
            aiEntityData.setDropTable(dropTable);
            aiEntityData.setWalkSpeed((float) walkSpeed);
            aiEntityData.setProbabilityStill((float) probabilityStill);
            aiEntityData.setProbabilityWalkStart((float) probabilityWalkStart);
            aiEntityData.setShopID(shopID);

            // Loading Finished & Creation Finished! Save me!
            aiEntityDataList.add(aiEntityData);

            // Debug print
            println(getClass(), "EntityDataID: " + entityDataID, false, PRINT_DEBUG);
            println(getClass(), "Name: " + name, false, PRINT_DEBUG);
            println(getClass(), "Type: " + type, false, PRINT_DEBUG);
            println(getClass(), "Alignment: " + alignment, false, PRINT_DEBUG);
            println(getClass(), "ColorID: " + colorID, false, PRINT_DEBUG);
            println(getClass(), "AtlasBodyID: " + atlasBodyID, false, PRINT_DEBUG);
            println(getClass(), "AtlasHeadID: " + atlasHeadID, false, PRINT_DEBUG);
            println(getClass(), "Health: " + health, false, PRINT_DEBUG);
            println(getClass(), "Damage: " + damage, false, PRINT_DEBUG);
            println(getClass(), "ExpDrop: " + expDrop, false, PRINT_DEBUG);
            println(getClass(), "DropTable: " + dropTable, false, PRINT_DEBUG);
            println(getClass(), "WalkSpeed: " + walkSpeed, false, PRINT_DEBUG);
            println(getClass(), "ProbabilityStill: " + probabilityStill, false, PRINT_DEBUG);
            println(getClass(), "ProbabilityWalkStart: " + probabilityWalkStart, false, PRINT_DEBUG);
            println(getClass(), "ShopID: " + shopID, false, PRINT_DEBUG);
            println(PRINT_DEBUG);
        }

        println(getClass(), "====== END LOADING MOVING-ENTITIES ======", false, PRINT_DEBUG);
        println(PRINT_DEBUG);
        return aiEntityDataList;
    }
}
