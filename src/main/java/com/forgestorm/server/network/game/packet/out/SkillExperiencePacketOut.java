package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.rpg.ExperiencePacketInfo;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.shared.Opcodes;

public class SkillExperiencePacketOut extends AbstractServerOutPacket {

    private final ExperiencePacketInfo experiencePacketInfo;

    public SkillExperiencePacketOut(final Player player, final ExperiencePacketInfo experiencePacketInfo) {
        super(Opcodes.EXPERIENCE, player.getClientHandler());
        this.experiencePacketInfo = experiencePacketInfo;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeByte(experiencePacketInfo.getSkillOpcode().getSkillOpcodeByte());
        write.writeInt(experiencePacketInfo.getExperienceGained());
    }
}
