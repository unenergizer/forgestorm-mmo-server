package com.forgestorm.server.io;

import com.forgestorm.server.game.rpg.skills.SkillNodeData;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SkillNodeLoader {

    public Map<Integer, SkillNodeData> loadSkillNodeData() {
        Map<Integer, SkillNodeData> skillNodeDataMap = new HashMap<>();

        Yaml yaml = new Yaml();

        InputStream inputStream = getClass().getResourceAsStream(FilePaths.SKILL_NODES.getFilePath());

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
