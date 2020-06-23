package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.MovingEntity;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.shared.Opcodes;

public class EntityHealPacketOut extends AbstractServerOutPacket {

    private final MovingEntity healingEntity;
    private final int healthGiven;

    public EntityHealPacketOut(final Player receiver, final MovingEntity healingEntity, final int healthGiven) {
        super(Opcodes.ENTITY_HEAL_OUT, receiver.getClientHandler());
        this.healingEntity = healingEntity;
        this.healthGiven = healthGiven;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeShort(healingEntity.getServerEntityId());
        write.writeByte(getEntityType(healingEntity).getEntityTypeByte());
        write.writeInt(healthGiven);
    }
}