package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.EntityType;
import com.forgestorm.server.game.world.entity.MovingEntity;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.shared.network.game.Opcodes;
import com.forgestorm.shared.network.game.GameOutputStream;

public class EntityDamagePacketOut extends AbstractPacketOut {

    private final short serverEntityId;
    private final EntityType entityType;
    private final int health;
    private final int damageTaken;

    public EntityDamagePacketOut(final Player receiver, final MovingEntity damagedEntity, final int health, final int damageTaken) {
        super(Opcodes.ENTITY_DAMAGE_OUT, receiver.getClientHandler());

        this.serverEntityId = damagedEntity.getServerEntityId();
        this.entityType = detectEntityType(damagedEntity);
        this.health = health;
        this.damageTaken = damageTaken;
    }

    @Override
    public void createPacket(GameOutputStream write) {
        write.writeShort(serverEntityId);
        write.writeByte(entityType.getEntityTypeByte());
        write.writeInt(health);
        write.writeInt(damageTaken);
    }
}