package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.MovingEntity;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.maps.Location;
import com.forgestorm.server.game.world.maps.MoveDirection;
import com.forgestorm.server.network.game.shared.Opcodes;

import static com.forgestorm.server.util.Log.println;

public class MovingEntityTeleportPacketOut extends AbstractServerOutPacket {

    private final MovingEntity teleportedEntity;
    private final Location teleportLocation;
    private final MoveDirection facingDirection;

    public MovingEntityTeleportPacketOut(final Player receiver, final MovingEntity teleportedEntity, final Location teleportLocation, final MoveDirection facingDirection) {
        super(Opcodes.PLAYER_TELEPORT, receiver.getClientHandler());
        this.teleportedEntity = teleportedEntity;
        this.teleportLocation = teleportLocation;
        this.facingDirection = facingDirection;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeShort(teleportedEntity.getServerEntityId());
        write.writeByte(getEntityType(teleportedEntity).getEntityTypeByte());
        write.writeString(teleportLocation.getMapName());
        write.writeShort(teleportLocation.getX());
        write.writeShort(teleportLocation.getY());
        write.writeByte(facingDirection.getDirectionByte());

        println(getClass(), "MapName: " + teleportLocation.getMapName() + "X: " + teleportLocation.getX() + ", Y: " + teleportLocation.getY() + ", Facing: " + facingDirection.toString());
    }
}