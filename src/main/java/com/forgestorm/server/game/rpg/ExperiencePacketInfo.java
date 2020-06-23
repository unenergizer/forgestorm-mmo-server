package com.forgestorm.server.game.rpg;

import com.forgestorm.server.game.rpg.skills.SkillOpcodes;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ExperiencePacketInfo {
    private SkillOpcodes skillOpcode;
    private int experienceGained;
}
