package com.forgestorm.server.io;

import com.forgestorm.server.game.rpg.skills.SkillNodeData;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SkillNodeLoader {

    public Map<Integer, SkillNodeData> loadSkillNodeData() {
        Map<Integer, SkillNodeData> skillNodeDataMap = new HashMap<>();

        Yaml yaml = new Yaml();

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(FilePaths.SKILL_NODES.getFilePath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Map<Integer, Map<String, Object>> root = yaml.load(inputStream);

        for (Map.Entry<Integer, Map<String, Object>> skillNodeInfo : root.entrySet()) {
            SkillNodeData skillNodeData = new SkillNodeData();
            skillNodeData.setName("");
            skillNodeData.setDropTableId((Integer) skillNodeInfo.getValue().get("dropTableId"));
            skillNodeData.setNumberOfUsages((Integer) skillNodeInfo.getValue().get("numberOfUsages"));
            skillNodeData.setExperience((Integer) skillNodeInfo.getValue().get("experience"));
            skillNodeDataMap.put(skillNodeInfo.getKey(), skillNodeData);
        }

        return skillNodeDataMap;
    }

}
