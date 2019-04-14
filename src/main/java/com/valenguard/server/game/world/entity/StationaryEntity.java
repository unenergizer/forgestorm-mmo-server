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

    public SkillNodeData getSkillNodeData() {
        return Server.getInstance().getSkillNodeManager().getSkillNodeData(skillNodeId);
    }

    public int getBodyId() {
        return getAppearance().getTextureId(Appearance.BODY);
    }
}
