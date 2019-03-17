package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.MovingEntity;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.maps.Location;
import com.valenguard.server.game.maps.MoveDirection;
import com.valenguard.server.network.shared.Opcodes;

import static com.valenguard.server.util.Log.println;

public class MovingEntityTeleportPacketOut extends AbstractServerOutPacket {

    private final MovingEntity teleportedEntity;
    private final Location teleportLocation;
    private final MoveDirection facingDirection;

    public MovingEntityTeleportPacketOut(Player receiver, MovingEntity teleportedEntity, Location teleportLocation, MoveDirection facingDirection) {
        super(Opcodes.PLAYER_TELEPORT, receiver);
        this.teleportedEntity = teleportedEntity;
        this.teleportLocation = teleportLocation;
        this.facingDirection = facingDirection;
    }

    @Override
    protected void createPacket(ValenguardOutputStream write) {
        write.writeShort(teleportedEntity.getServerEntityId());
        write.writeByte(getEntityType(teleportedEntity).getEntityTypeByte());
        write.writeString(teleportLocation.getMapName());
        write.writeShort(teleportLocation.getX());
        write.writeShort(teleportLocation.getY());
        write.writeByte(facingDirection.getDirectionByte());

        println(getClass(), "MapName: " + teleportLocation.getMapName() + "X: " + teleportLocation.getX() + ", Y: " + teleportLocation.getY() + ", Facing: " + facingDirection.toString());
    }
}