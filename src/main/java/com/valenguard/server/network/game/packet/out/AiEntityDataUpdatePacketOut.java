package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.entity.*;
import com.valenguard.server.network.game.shared.Opcodes;

public class AiEntityDataUpdatePacketOut extends AbstractServerOutPacket {

    public static final byte ALIGNMENT_INDEX = 0x01;

    private AiEntity aiEntity;
    private byte dataBits;

    public AiEntityDataUpdatePacketOut(Player packetReceiver, AiEntity aiEntity, byte dataBits) {
        super(Opcodes.AIENTITY_UPDATE_OUT, packetReceiver);
        this.aiEntity = aiEntity;
        this.dataBits = dataBits;
    }

    @Override
    protected void createPacket(ValenguardOutputStream write) {
        write.writeShort(aiEntity.getServerEntityId());
        write.writeByte(dataBits);
        if ((dataBits & ALIGNMENT_INDEX) != 0) {
            if (aiEntity.getEntityType() == EntityType.NPC) {
                write.writeByte(((NPC) aiEntity).getAlignmentByPlayer(packetReceiver).getEntityAlignmentByte());
            } else if (aiEntity.getEntityType() == EntityType.MONSTER) {
                write.writeByte(((Monster) aiEntity).getAlignment().getEntityAlignmentByte());
            }
        }
    }
}
