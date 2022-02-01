package com.forgestorm.server.game.rpg.skills;

import com.forgestorm.server.game.rpg.ExperiencePacketInfo;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.packet.out.SkillExperiencePacketOut;
import lombok.Getter;

import static com.forgestorm.server.util.Log.println;

public class Skill {

    private static final boolean PRINT_DEBUG = false;

    private final SkillOpcodes skillOpcode;
    private final Player player;

    @Getter
    private int experience = 0;

    Skill(Player player, SkillOpcodes skillOpcode) {
        this.player = player;
        this.skillOpcode = skillOpcode;
    }

    public void initExperience(int experienceTotal) {
        this.experience += experienceTotal;
    }

    public void addExperience(int expGained) {
        println(PRINT_DEBUG);
        println(getClass(), "---[ " + skillOpcode + " ]------------------------------------", false, PRINT_DEBUG);
        println(getClass(), "ExpGained: " + expGained, false, PRINT_DEBUG);
        experience += expGained;
        println(getClass(), "TotalExp: " + experience, false, PRINT_DEBUG);
        sendExperienceGained(expGained);
    }

    public void sendExperienceTotal() {
        if (!player.isLoggedInGameWorld()) return;
        println(getClass(), "TotalExp: " + experience + ", SendingTotalExp: true", false, PRINT_DEBUG);
        new SkillExperiencePacketOut(player, new ExperiencePacketInfo(skillOpcode, experience)).sendPacket();
    }

    public void sendExperienceGained(int expGained) {
        if (!player.isLoggedInGameWorld()) return;
        new SkillExperiencePacketOut(player, new ExperiencePacketInfo(skillOpcode, expGained)).sendPacket();
    }
}
