package com.valenguard.server.network.packet.out;

import com.valenguard.server.entity.Entity;
import com.valenguard.server.entity.Player;
import com.valenguard.server.network.shared.Opcodes;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class EntityDespawnPacket extends ServerOutPacket {

    private final Entity despawnTarget;

    public EntityDespawnPacket(Player receiver, Entity despawnTarget) {
        super(Opcodes.ENTITY_DESPAWN, receiver);
        this.despawnTarget = despawnTarget;
    }

    @Override
    protected void createPacket(ObjectOutputStream write) throws IOException {
        write.writeShort(despawnTarget.getServerEntityId());
    }
}
