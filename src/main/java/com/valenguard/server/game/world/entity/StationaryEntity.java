package com.valenguard.server.game.world.entity;

import com.valenguard.server.Server;
import com.valenguard.server.game.rpg.StationaryTypes;
import com.valenguard.server.game.rpg.skills.SkillNodeData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StationaryEntity extends Entity {
    private StationaryTypes stationaryType;
    private int skillNodeId;
    private boolean usedThisTick;

    public SkillNodeData getSkillNodeData() {
        return Server.getInstance().getSkillNodeManager().getSkillNodeData(skillNodeId);
    }

    public byte getBodyId() {
        return getAppearance().getMonsterBodyTexture();
    }
}
