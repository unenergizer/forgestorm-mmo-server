package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.EntityType;
import com.forgestorm.server.game.world.entity.MovingEntity;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.shared.Opcodes;

public class EntityHealPacketOut extends AbstractServerOutPacket {

    private final short serverEntityId;
    private final EntityType entityType;
    private final int healthGiven;

    public EntityHealPacketOut(final Player receiver, final MovingEntity healingEntity, final int healthGiven) {
        super(Opcodes.ENTITY_HEAL_OUT, receiver.getClientHandler());

        this.serverEntityId = healingEntity.getServerEntityId();
        this.entityType = detectEntityType(healingEntity);
        this.healthGiven = healthGiven;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeShort(serverEntityId);
        write.writeByte(entityType.getEntityTypeByte());
        write.writeInt(healthGiven);
    }
}