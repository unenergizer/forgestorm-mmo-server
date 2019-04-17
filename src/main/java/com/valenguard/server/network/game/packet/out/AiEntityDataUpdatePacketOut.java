package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.world.entity.*;
import com.valenguard.server.network.game.shared.Opcodes;

import static com.valenguard.server.util.Log.println;

public class AiEntityDataUpdatePacketOut extends AbstractServerOutPacket {

    public static final byte ALIGNMENT_INDEX = 0x01;
    public static final byte BANK_KEEPER_INDEX = 0x02;

    private final AiEntity aiEntity;
    private final byte dataBits;

    public AiEntityDataUpdatePacketOut(final Player packetReceiver, final AiEntity aiEntity, final byte dataBits) {
        super(Opcodes.AI_ENTITY_UPDATE_OUT, packetReceiver.getClientHandler());
        this.aiEntity = aiEntity;
        this.dataBits = dataBits;
        println(getClass(), "sending AI Entity update packet!");
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
        } else if ((dataBits & BANK_KEEPER_INDEX) != 0) {
            println(getClass(), "sending bank keeper bits");
            write.writeBoolean(aiEntity.isBankKeeper());
        }
    }
}
