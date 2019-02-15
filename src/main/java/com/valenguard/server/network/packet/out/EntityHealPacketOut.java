package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.MovingEntity;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.shared.Opcodes;

public class EntityHealPacketOut extends ServerAbstractOutPacket {

    private final MovingEntity teleportedEntity;
    private final int healthGiven;

    public EntityHealPacketOut(Player receiver, MovingEntity teleportedEntity, int healthGiven) {
        super(Opcodes.ENTITY_HEAL_OUT, receiver);
        this.teleportedEntity = teleportedEntity;
        this.healthGiven = healthGiven;
    }

    @Override
    protected void createPacket(ValenguardOutputStream write) {
        write.writeShort(teleportedEntity.getServerEntityId());
        write.writeInt(healthGiven);
    }
}