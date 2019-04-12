package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.world.entity.*;
import com.valenguard.server.network.game.shared.Opcodes;

public class AiEntityDataUpdatePacketOut extends AbstractServerOutPacket {

    public static final byte ALIGNMENT_INDEX = 0x01;

    private final AiEntity aiEntity;
    private final byte dataBits;

    public AiEntityDataUpdatePacketOut(final Player packetReceiver, final AiEntity aiEntity, final byte dataBits) {
        super(Opcodes.AI_ENTITY_UPDATE_OUT, packetReceiver.getClientHandler());
        this.aiEntity = aiEntity;
        this.dataBits = dataBits;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
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
