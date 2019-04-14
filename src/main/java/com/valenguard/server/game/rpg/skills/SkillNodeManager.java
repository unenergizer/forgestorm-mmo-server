package com.valenguard.server.game.rpg.skills;

import com.valenguard.server.io.SkillNodeLoader;

import java.util.Map;

public class SkillNodeManager {

    private Map<Integer, SkillNodeData> skillNodeDataMap;

    public void start() {
        SkillNodeLoader skillNodeLoader = new SkillNodeLoader();
        skillNodeDataMap = skillNodeLoader.loadSkillNodeData();
    }

    public SkillNodeData getSkillNodeData(int skillNodeId) {
        return skillNodeDataMap.get(skillNodeId);
    }
}
