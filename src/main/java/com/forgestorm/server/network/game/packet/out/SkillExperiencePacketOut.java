package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.rpg.ExperiencePacketInfo;
import com.forgestorm.server.game.rpg.skills.SkillOpcodes;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.shared.Opcodes;

public class SkillExperiencePacketOut extends AbstractServerOutPacket {

    private final SkillOpcodes skillOpcode;
    private final int experienceGained;

    public SkillExperiencePacketOut(final Player player, final ExperiencePacketInfo experiencePacketInfo) {
        super(Opcodes.EXPERIENCE, player.getClientHandler());

        this.skillOpcode = experiencePacketInfo.getSkillOpcode();
        this.experienceGained = experiencePacketInfo.getExperienceGained();
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeByte(skillOpcode.getSkillOpcodeByte());
        write.writeInt(experienceGained);
    }
}
