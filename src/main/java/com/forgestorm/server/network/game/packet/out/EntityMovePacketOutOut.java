package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.EntityType;
import com.forgestorm.server.game.world.entity.MovingEntity;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.maps.Location;
import com.forgestorm.shared.game.world.maps.MoveDirection;
import com.forgestorm.shared.network.game.Opcodes;
import com.forgestorm.shared.network.game.GameOutputStream;

import static com.forgestorm.server.util.Log.println;
import static com.google.common.base.Preconditions.checkArgument;

public class EntityMovePacketOutOut extends AbstractPacketOut {

    private static final boolean PRINT_DEBUG = false;

    private final String entityName;
    private final short serverEntityId;
    private final EntityType entityType;
    private final MoveDirection moveDirection;
    private final int x;
    private final int y;
    private final short z;

    public EntityMovePacketOutOut(final Player packetReceiver, final MovingEntity movingEntity, final Location attemptLocation) {
        super(Opcodes.ENTITY_MOVE_UPDATE, packetReceiver.getClientHandler());

        this.entityName = movingEntity.getName();
        this.serverEntityId = movingEntity.getServerEntityId();
        this.entityType = detectEntityType(movingEntity);
        this.moveDirection = movingEntity.getFacingDirection();
        this.x = attemptLocation.getX();
        this.y = attemptLocation.getY();
        this.z = attemptLocation.getZ();
    }

    @Override
    public void createPacket(GameOutputStream write) {
        checkArgument(moveDirection != MoveDirection.NONE, "Server tried to send a NONE type face direction!");

        write.writeShort(serverEntityId);
        write.writeByte(entityType.getEntityTypeByte());
        write.writeInt(x);
        write.writeInt(y);
        write.writeShort(z);

        println(getClass(), "EntityName: " + entityName + ", ServerID: " + serverEntityId + ", X: " + x + ", Y: " + y + ", Z: " + z, false, PRINT_DEBUG);
    }
}
