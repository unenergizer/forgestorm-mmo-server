package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.EntityType;
import com.valenguard.server.game.entity.MovingEntity;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.shared.Opcodes;

public class EntityDamagePacketOut extends ServerAbstractOutPacket {

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
        if (packetReceiver.equals(damagedEntity)) {
            write.writeByte(EntityType.CLIENT_PLAYER.getEntityTypeByte());
        } else {
            write.writeByte(damagedEntity.getEntityType().getEntityTypeByte());
        }
        write.writeInt(health);
        write.writeInt(damageTaken);
    }
}