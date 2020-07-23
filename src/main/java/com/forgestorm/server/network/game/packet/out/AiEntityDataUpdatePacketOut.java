package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.*;
import com.forgestorm.server.network.game.shared.Opcodes;

public class AiEntityDataUpdatePacketOut extends AbstractServerOutPacket {

    public static final byte ALIGNMENT_INDEX = 0x01;
    public static final byte BANK_KEEPER_INDEX = 0x02;

    private final short serverEntityId;
    private final boolean isBankKeeper;
    private final byte dataBits;
    private byte entityAlignmentByte;

    public AiEntityDataUpdatePacketOut(final Player packetReceiver, final AiEntity aiEntity, final byte dataBits) {
        super(Opcodes.AI_ENTITY_UPDATE_OUT, packetReceiver.getClientHandler());

        this.serverEntityId = aiEntity.getServerEntityId();
        this.isBankKeeper = aiEntity.isBankKeeper();
        this.dataBits = dataBits;

        EntityType entityType = aiEntity.getEntityType();
        if (entityType == EntityType.NPC) {
            this.entityAlignmentByte = ((NPC) aiEntity).getAlignmentByPlayer(packetReceiver).getEntityAlignmentByte();
        } else if (entityType == EntityType.MONSTER) {
            this.entityAlignmentByte = ((Monster) aiEntity).getAlignment().getEntityAlignmentByte();
        }
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeShort(serverEntityId);
        write.writeByte(dataBits);
        if ((dataBits & ALIGNMENT_INDEX) != 0) {
            write.writeByte(entityAlignmentByte);
        } else if ((dataBits & BANK_KEEPER_INDEX) != 0) {
            write.writeBoolean(isBankKeeper);
        }
    }
}
