package com.forgestorm.server.game.world.entity;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.rpg.StationaryTypes;
import com.forgestorm.server.game.rpg.skills.SkillNodeData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StationaryEntity extends Entity {
    private StationaryTypes stationaryType;
    private int skillNodeId;
    private boolean usedThisTick;

    public SkillNodeData getSkillNodeData() {
        return ServerMain.getInstance().getSkillNodeManager().getSkillNodeData(skillNodeId);
    }

    public byte getBodyId() {
        return getAppearance().getMonsterBodyTexture();
    }
}
