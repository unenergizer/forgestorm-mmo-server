package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.world.entity.Entity;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.shared.Opcodes;

public class EntityDespawnPacketOut extends AbstractServerOutPacket {

    private final Entity despawnTarget;

    public EntityDespawnPacketOut(Player receiver, Entity despawnTarget) {
        super(Opcodes.ENTITY_DESPAWN, receiver);
        this.despawnTarget = despawnTarget;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeShort(despawnTarget.getServerEntityId());
        write.writeByte(despawnTarget.getEntityType().getEntityTypeByte());
    }
}
