package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.rpg.ExperiencePacketInfo;
import com.forgestorm.server.game.rpg.skills.SkillOpcodes;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.shared.network.game.Opcodes;
import com.forgestorm.shared.network.game.GameOutputStream;

public class SkillExperiencePacketOutOut extends AbstractPacketOut {

    private final SkillOpcodes skillOpcode;
    private final int experienceGained;

    public SkillExperiencePacketOutOut(final Player player, final ExperiencePacketInfo experiencePacketInfo) {
        super(Opcodes.EXPERIENCE, player.getClientHandler());

        this.skillOpcode = experiencePacketInfo.getSkillOpcode();
        this.experienceGained = experiencePacketInfo.getExperienceGained();
    }

    @Override
    public void createPacket(GameOutputStream write) {
        write.writeByte(skillOpcode.getSkillOpcodeByte());
        write.writeInt(experienceGained);
    }
}
