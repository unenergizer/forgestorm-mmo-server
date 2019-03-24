package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.rpg.ExperiencePacketInfo;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.shared.Opcodes;

public class SkillExperiencePacketOut extends AbstractServerOutPacket {

    private final ExperiencePacketInfo experiencePacketInfo;

    public SkillExperiencePacketOut(Player player, ExperiencePacketInfo experiencePacketInfo) {
        super(Opcodes.EXPERIENCE, player);
        this.experiencePacketInfo = experiencePacketInfo;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeByte(experiencePacketInfo.getSkillOpcode().getSkillOpcodeByte());
        write.writeInt(experiencePacketInfo.getExperienceGained());
    }
}
