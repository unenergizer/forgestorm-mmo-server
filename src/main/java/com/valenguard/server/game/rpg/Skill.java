package com.valenguard.server.game.rpg;

import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.packet.out.SkillExperiencePacketOut;
import lombok.Getter;

public class Skill {

    private final SkillOpcodes skillOpcode;

    private final Player player;

    @Getter
    private int experience = 0;

    Skill(Player player, SkillOpcodes skillOpcode) {
        this.player = player;
        this.skillOpcode = skillOpcode;
    }

    public void addExperience(int experience) {
        this.experience += experience;
        new SkillExperiencePacketOut(player, new ExperiencePacketInfo(skillOpcode, experience)).sendPacket();
    }
}
