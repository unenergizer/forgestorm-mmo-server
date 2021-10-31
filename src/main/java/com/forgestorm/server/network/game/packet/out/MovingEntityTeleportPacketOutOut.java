package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.EntityType;
import com.forgestorm.server.game.world.entity.MovingEntity;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.maps.Location;
import com.forgestorm.shared.game.world.maps.MoveDirection;
import com.forgestorm.shared.network.game.Opcodes;
import com.forgestorm.shared.network.game.GameOutputStream;

import static com.forgestorm.server.util.Log.println;

public class MovingEntityTeleportPacketOutOut extends AbstractPacketOut {

    private static final boolean PRINT_DEBUG = false;

    private final short serverEntityId;
    private final EntityType entityType;
    private final String mapName;
    private final int x;
    private final int y;
    private final short z;
    private final MoveDirection facingDirection;

    public MovingEntityTeleportPacketOutOut(final Player receiver, final MovingEntity teleportedEntity, final Location teleportLocation, final MoveDirection facingDirection) {
        super(Opcodes.PLAYER_TELEPORT, receiver.getClientHandler());

        this.serverEntityId = teleportedEntity.getServerEntityId();
        this.entityType = detectEntityType(teleportedEntity);
        this.mapName = teleportLocation.getWorldName();
        this.x = teleportLocation.getX();
        this.y = teleportLocation.getY();
        this.z = teleportLocation.getZ();
        this.facingDirection = facingDirection;
    }

    @Override
    public void createPacket(GameOutputStream write) {
        write.writeShort(serverEntityId);
        write.writeByte(entityType.getEntityTypeByte());
        write.writeString(mapName);
        write.writeInt(x);
        write.writeInt(y);
        write.writeShort(z);
        write.writeByte(facingDirection.getDirectionByte());

        println(getClass(), "MapName: " + mapName + "X: " + x + ", Y: " + y + ", Z: " + z + ", Facing: " + facingDirection, false, PRINT_DEBUG
        );
    }
}