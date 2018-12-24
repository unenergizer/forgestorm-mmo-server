package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.Entity;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.shared.Opcodes;

import java.io.DataOutputStream;
import java.io.IOException;

public class EntityDespawnPacketOut extends ServerAbstractOutPacket {

    private final Entity despawnTarget;

    public EntityDespawnPacketOut(Player receiver, Entity despawnTarget) {
        super(Opcodes.ENTITY_DESPAWN, receiver);
        this.despawnTarget = despawnTarget;
    }

    @Override
    protected void createPacket(DataOutputStream write) throws IOException {
        write.writeShort(despawnTarget.getServerEntityId());
    }
}
