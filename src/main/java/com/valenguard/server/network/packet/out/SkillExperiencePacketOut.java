package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.rpg.ExperiencePacketInfo;
import com.valenguard.server.network.shared.Opcodes;

import java.util.List;

public class SkillExperiencePacketOut extends ServerAbstractOutPacket {

    private List<ExperiencePacketInfo> experiencePacketInfos;

    public SkillExperiencePacketOut(Player player, List<ExperiencePacketInfo> experiencePacketInfos) {
        super(Opcodes.EXPERIENCE, player);
        this.experiencePacketInfos = experiencePacketInfos;
    }

    @Override
    protected void createPacket(ValenguardOutputStream write) {
        for (ExperiencePacketInfo experiencePacketInfo : experiencePacketInfos) {
            write.writeByte(experiencePacketInfo.getSkillType());
            write.writeInt(experiencePacketInfo.getExperienceGained());
        }
    }
}
