package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.world.entity.MovingEntity;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.shared.Opcodes;

public class EntityHealPacketOut extends AbstractServerOutPacket {

    private final MovingEntity healingEntity;
    private final int healthGiven;

    public EntityHealPacketOut(Player receiver, MovingEntity healingEntity, int healthGiven) {
        super(Opcodes.ENTITY_HEAL_OUT, receiver);
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