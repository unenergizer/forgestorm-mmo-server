package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.EntityType;
import com.forgestorm.server.game.world.entity.MovingEntity;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.maps.Location;
import com.forgestorm.server.game.world.maps.MoveDirection;
import com.forgestorm.server.network.game.shared.Opcodes;

import static com.forgestorm.server.util.Log.println;

public class MovingEntityTeleportPacketOut extends AbstractServerOutPacket {

    private final short serverEntityId;
    private final EntityType entityType;
    private final String mapName;
    private final int x;
    private final int y;
    private final MoveDirection facingDirection;

    public MovingEntityTeleportPacketOut(final Player receiver, final MovingEntity teleportedEntity, final Location teleportLocation, final MoveDirection facingDirection) {
        super(Opcodes.PLAYER_TELEPORT, receiver.getClientHandler());

        this.serverEntityId = teleportedEntity.getServerEntityId();
        this.entityType = detectEntityType(teleportedEntity);
        this.mapName = teleportLocation.getWorldName();
        this.x = teleportLocation.getX();
        this.y = teleportLocation.getY();
        this.facingDirection = facingDirection;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeShort(serverEntityId);
        write.writeByte(entityType.getEntityTypeByte());
        write.writeString(mapName);
        write.writeInt(x);
        write.writeInt(y);
        write.writeByte(facingDirection.getDirectionByte());

        println(getClass(), "MapName: " + mapName + "X: " + x + ", Y: " + y + ", Facing: " + facingDirection);
    }
}