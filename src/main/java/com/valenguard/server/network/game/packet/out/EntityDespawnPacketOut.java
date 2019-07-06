package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.world.entity.Entity;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.shared.Opcodes;

import static com.valenguard.server.util.Log.println;

public class EntityDespawnPacketOut extends AbstractServerOutPacket {

    private static final boolean PRINT_DEBUG = true;
    private final Player receiver;
    private final short entityId;
    private final byte entityType;

    public EntityDespawnPacketOut(final Player receiver, final Entity despawnTarget) {
        super(Opcodes.ENTITY_DESPAWN, receiver.getClientHandler());
        this.receiver = receiver;
        entityId = despawnTarget.getServerEntityId();
        entityType = despawnTarget.getEntityType().getEntityTypeByte();
        println(getClass(), "###[ DESPAWN OUT -> " + receiver.getName() + " ]################################", false, PRINT_DEBUG);
        println(getClass(), "EntityName: " + despawnTarget.getName(), false, PRINT_DEBUG);
        println(getClass(), "MapName: " + despawnTarget.getCurrentMapLocation().getMapName(), false, PRINT_DEBUG);
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeShort(entityId);
        write.writeByte(entityType);

        println(getClass(), "EntityID: " + entityId, false, PRINT_DEBUG);
        println(getClass(), "EntityType: " + entityType, false, PRINT_DEBUG);
    }
}
