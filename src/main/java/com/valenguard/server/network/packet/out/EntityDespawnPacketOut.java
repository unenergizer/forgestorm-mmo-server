package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.Entity;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.shared.Opcodes;

public class EntityDespawnPacketOut extends ServerAbstractOutPacket {

    private final Entity despawnTarget;

    public EntityDespawnPacketOut(Player receiver, Entity despawnTarget) {
        super(Opcodes.ENTITY_DESPAWN, receiver);
        this.despawnTarget = despawnTarget;
    }

    @Override
    protected void createPacket(ValenguardOutputStream write) {
        write.writeShort(despawnTarget.getServerEntityId());
    }
}
