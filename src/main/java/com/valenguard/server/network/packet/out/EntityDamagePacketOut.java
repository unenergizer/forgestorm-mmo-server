package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.MovingEntity;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.shared.Opcodes;

public class EntityDamagePacketOut extends AbstractServerOutPacket {

    private final MovingEntity damagedEntity;
    private final int health;
    private final int damageTaken;

    public EntityDamagePacketOut(Player receiver, MovingEntity damagedEntity, int health, int damageTaken) {
        super(Opcodes.ENTITY_DAMAGE_OUT, receiver);
        this.damagedEntity = damagedEntity;
        this.health = health;
        this.damageTaken = damageTaken;
    }

    @Override
    protected void createPacket(ValenguardOutputStream write) {
        write.writeShort(damagedEntity.getServerEntityId());
        write.writeByte(getEntityType(damagedEntity).getEntityTypeByte());
        write.writeInt(health);
        write.writeInt(damageTaken);
    }
}