package com.forgestorm.server.game.rpg.skills;

import com.forgestorm.server.game.ManagerStart;
import com.forgestorm.server.io.SkillNodeLoader;

import java.util.Map;

public class SkillNodeManager implements ManagerStart {

    private Map<Integer, SkillNodeData> skillNodeDataMap;

    @Override
    public void start() {
        SkillNodeLoader skillNodeLoader = new SkillNodeLoader();
        skillNodeDataMap = skillNodeLoader.loadSkillNodeData();
    }

    public SkillNodeData getSkillNodeData(int skillNodeId) {
        return skillNodeDataMap.get(skillNodeId);
    }
}
