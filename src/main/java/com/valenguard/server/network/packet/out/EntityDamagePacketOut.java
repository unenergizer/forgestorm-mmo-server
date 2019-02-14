package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.MovingEntity;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.shared.Opcodes;

public class EntityDamagePacketOut extends ServerAbstractOutPacket {

    private final MovingEntity teleportedEntity;
    private final int health;
    private final int damageTaken;

    public EntityDamagePacketOut(Player receiver, MovingEntity teleportedEntity, int health, int damageTaken) {
        super(Opcodes.ENTITY_DAMAGE_OUT, receiver);
        this.teleportedEntity = teleportedEntity;
        this.health = health;
        this.damageTaken = damageTaken;
    }

    @Override
    protected void createPacket(ValenguardOutputStream write) {
        write.writeShort(teleportedEntity.getServerEntityId());
        write.writeInt(health);
        write.writeInt(damageTaken);
    }
}