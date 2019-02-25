package com.valenguard.server.game.rpg;

import com.valenguard.server.game.rpg.skills.SkillOpcodes;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ExperiencePacketInfo {
    private SkillOpcodes skillOpcode;
    private int experienceGained;
}
