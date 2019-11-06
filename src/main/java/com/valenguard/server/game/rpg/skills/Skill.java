package com.valenguard.server.game.rpg.skills;

import com.valenguard.server.game.rpg.ExperiencePacketInfo;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.packet.out.SkillExperiencePacketOut;
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
        sendSkillExperience();
    }

    public void sendSkillExperience() {
        if (!player.isLoggedInGameWorld()) return;
        new SkillExperiencePacketOut(player, new ExperiencePacketInfo(skillOpcode, experience)).sendPacket();
    }
}
